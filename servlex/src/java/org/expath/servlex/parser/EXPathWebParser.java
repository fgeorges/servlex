/****************************************************************************/
/*  File:       EXPathWebParser.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.functions.regex.JDK15RegexTranslator;
import net.sf.saxon.functions.regex.RegexSyntaxException;
import net.sf.saxon.functions.regex.RegularExpression;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Packages;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.saxon.SaxonRepository;
import org.expath.servlex.model.Application;
import org.expath.servlex.components.Component;
import org.expath.servlex.model.Resource;
import org.expath.servlex.model.Servlet;
import org.expath.servlex.components.XProcPipeline;
import org.expath.servlex.components.XProcStep;
import org.expath.servlex.components.XQueryFunction;
import org.expath.servlex.components.XQueryModule;
import org.expath.servlex.components.XSLTFunction;
import org.expath.servlex.components.XSLTTemplate;
import org.expath.servlex.components.XSLTTransform;
import org.expath.servlex.model.*;

/**
 * Facade class for this package, to parse EXPath Webapp descriptors.
 *
 * @author Florent Georges
 * @date   2010-02-09
 */
public class EXPathWebParser
{
    public EXPathWebParser(SaxonRepository repo)
            throws ParseException
    {
        myRepo = repo;
    }

    /**
     * Parse all webapp descriptors in the repository.
     *
     * TODO: For now, only get the "latest" version of a package.  See comments
     * of {@link Repository#resolve(String,URISpace)} about that (versionning
     * scheme is not always SemVer -- or could we impose it for webapps?)
     */
    public Set<Application> parseDescriptors()
            throws ParseException
    {
        // the result
        Set<Application> apps = new HashSet<Application>();
        // iterate on every sub-directories of the repo (i.e. on each package)
        for ( Packages pp : myRepo.getUnderlyingRepo().listPackages() ) {
            Package pkg = pp.latest();
            try {
                // the web descriptor location
                StreamSource descriptor = pkg.getResolver().resolveResource("expath-web.xml");
                // ignore non-webapps (i.e. plain library packages)
                if ( descriptor == null ) {
                    continue;
                }
                // parse the descriptor and add it to the app list
                apps.add(parseDescriptorFile(descriptor.getInputStream(), pkg));
            }
            catch ( Storage.NotExistException ex ) {
                String msg = "Package does not have any web descriptor, must be a library, ignore it: ";
                LOG.debug(msg + pkg.getName());
            }
            catch ( PackageException ex ) {
                throw new ParseException("Error accessing the web descriptor of " + pkg.getName(), ex);
            }
        }
        // return the application maps
        return apps;
    }

    /**
     * Parse the webapp descriptor of a given package, if any.
     * 
     * Return null if the package is not a webapp.
     */
    public Application loadPackage(Package pkg)
            throws ParseException
    {
        try {
            StreamSource descriptor = pkg.getResolver().resolveResource("expath-web.xml");
            return parseDescriptorFile(descriptor.getInputStream(), pkg);
        }
        catch ( Storage.NotExistException ex ) {
                String msg = "Package does not have any web descriptor, must be a library, ignore it: ";
                LOG.debug(msg + pkg.getName());
                return null;
        }
        catch ( PackageException ex ) {
            throw new ParseException("Error accessing the web descriptor of " + pkg.getName(), ex);
        }
    }

    /**
     * Parse one webapp descriptor file.
     */
    private Application parseDescriptorFile(InputStream descriptor, Package pkg)
            throws ParseException
    {
        LOG.info("Parse webapp descriptor for app " + pkg.getName());

        StreamParser parser = new StreamParser(descriptor, DESC_NS);

        // position the parser on the root 'web-app' element
        parser.ensureNextElement("webapp", true);

        // the values used to build the application object
        ParsingContext ctxt = new ParsingContext();
        // TODO: Check the value returned for 'abbrev'.
        String abbrev = parser.getAttribute("abbrev");
        ctxt.setAbbrev(abbrev);
        LOG.info("  webapp abbrev:" + abbrev);

        try {
            for ( ; /* ever */; ) {
                parser.nextTag();
                // consume any </group>
                while ( XMLStreamConstants.END_ELEMENT == parser.getEventType() && parser.getLocalName().equals("group") ) {
                    ctxt.popGroup();
                    parser.nextTag();
                }
                if ( XMLStreamConstants.START_ELEMENT != parser.getEventType() ) {
                    // TODO: Check consistency...!
                    break;
                }
                parser.ensureNamespace(true);
                String elem = parser.getLocalName();
                if ( elem.equals("title") ) {
                    String title = parser.getElementText();
                    ctxt.setTitle(title);
                }
                else if ( elem.equals("application") ) {
                    ParsingApp a = handleApplication(parser);
                    ctxt.setApplication(a);
                }
                else if ( elem.equals("error") ) {
                    ErrorHandler h = handleError(parser);
                    ctxt.addWrapper(h);
                }
                else if ( elem.equals("group") ) {
                    ParsingGroup g = handleGroup(parser, ctxt);
                    ctxt.pushGroup(g);
                }
                else if ( elem.equals("filter") ) {
                    Filter f = handleFilter(parser);
                    ctxt.addWrapper(f);
                }
                else if ( elem.equals("chain") ) {
                    ParsingChain c = handleChain(parser);
                    ctxt.addChain(c);
                }
                else if ( elem.equals("servlet") ) {
                    ParsingServlet s = handleServlet(parser, ctxt);
                    ctxt.addServlet(s);
                }
                else if ( elem.equals("resource") ) {
                    Resource rsrc = handleResource(parser);
                    ctxt.addResource(rsrc);
                }
                else {
                    String msg = "Unkown element in the descriptor for webapp ";
                    parser.parseError(msg + pkg.getName() + ": " + elem);
                }
            }
            // TODO: Check we consumed everything...
        }
        catch ( XMLStreamException ex ) {
            parser.parseError("Error parsing the webapp descriptor", ex);
        }

        // build the application object
        return createApplication(pkg, ctxt);
    }

    private Application createApplication(Package pkg, ParsingContext ctxt)
            throws ParseException
    {
        // create actual chains and push them into wrappers
        createChains(ctxt);
        // get values and build the app object
        String      abbrev = ctxt.getAbbrev();
        String      title  = ctxt.getTitle();
        Application app    = new Application(abbrev, title, pkg);
        // build the servlets
        for ( ParsingServlet s : ctxt.getServlets() ) {
            String    name    = s.getName();
            Component implem  = s.getImplem();
            String    pattern = s.getPattern();
            String    java_re = translateXPathToJavaRegex(pattern);
            Pattern   regex   = Pattern.compile(java_re);
            String[]  groups  = s.getMatchGroups();
            Servlet   servlet = new Servlet(name, implem, regex, groups);
            Wrapper   wrapper = s.makeWrapper(ctxt);
            servlet.setWrapper(wrapper);
            app.addHandler(servlet);
        }
        // add the resources
        for ( Resource rsrc : ctxt.getResources() ) {
            app.addHandler(rsrc);
        }
        return app;
    }

    /**
     * Create the chain objects and push them into wrappers.
     */
    private void createChains(ParsingContext ctxt)
            throws ParseException
    {
        for ( ParsingChain c : ctxt.getChains() ) {
            QName         name  = c.getName();
            List<Wrapper> list  = c.makeFilters(ctxt);
            Wrapper[]     array = list.toArray(new Wrapper[]{});
            Chain         chain = new Chain(name, array);
            ctxt.addWrapper(chain);
        }
    }

    /**
     * Handle an element 'application' in the webapp descriptor.
     */
    private ParsingApp handleApplication(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        ParsingApp app = new ParsingApp();
        String[] names = handleFiltersAttr(parser);
        for ( String name : names ) {
            QName f = parser.parseLiteralQName(name);
            app.addFilter(f);
        }
        parser.nextTag();
        return app;
    }

    /**
     * Handle an element 'error' in the webapp descriptor.
     */
    private ErrorHandler handleError(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("error", true);
        String name = parser.getAttribute("name");
        String catc = parser.getAttribute("catch");
        LOG.debug("expath-web parser: error: " + name + " catching " + catc);
        QName   qname = parser.parseLiteralQName(name);
        boolean every = false;
        QName   code  = null;
        String  ns    = null;
        String  local = null;
        if ( catc.equals("*") ) {
            every = true;
        }
        else if ( catc.startsWith("*:") ) {
            local = catc.substring(2);
        }
        else if ( catc.endsWith(":*") ) {
            String prefix = catc.substring(0, catc.length() - 2);
            ns = parser.resolvePrefix(prefix);
        }
        else {
            code  = parser.parseLiteralQName(catc);
            ns    = code.getNamespaceURI();
            local = code.getLocalPart();
        }
        parser.nextTag();
        parser.ensureNamespace(true);
        Component implem = handleComponent(parser);
        parser.nextTag();
        if ( every ) {
            return new ErrorHandler(qname, implem);
        }
        else {
            return new ErrorHandler(qname, implem, code, ns, local);
        }
    }

    /**
     * Handle an element 'resource' in the webapp descriptor.
     *
     * Like other handleXXX() functions, the current event must be 'end tag'
     * at the end of the function (the end tag 'resource' corresponding to the
     * open tag 'resource' when the function is called).
     */
    private Resource handleResource(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("resource", true);
        String pattern = parser.getAttribute("pattern");
        String rewrite = parser.getAttribute("rewrite");
        String type    = parser.getAttribute("media-type");
        parser.nextTag();
        parser.ensureEndTag(true);
        String java_regex = translateXPathToJavaRegex(pattern);
        return new Resource(Pattern.compile(java_regex), java_regex, rewrite, type);
    }

    /**
     * Translate an XPath regex to a native Java SE 1.5 regex.
     */
    private String translateXPathToJavaRegex(String pattern)
            throws ParseException
    {
        try {
            int options = RegularExpression.XML11 | RegularExpression.XPATH20;
            List<RegexSyntaxException> warnings = new ArrayList<RegexSyntaxException>();
            String res = JDK15RegexTranslator.translate(pattern, options, 0, warnings);
            for ( RegexSyntaxException w : warnings ) {
                LOG.warn("expath-web.xml parser: Warning in regex: '" + w + "'");
            }
            return res;
        }
        catch ( RegexSyntaxException ex ) {
            throw new ParseException("The pattern is not a valid XPath regex", ex);
        }
    }

    /**
     * Handle an element 'filter' in the webapp descriptor.
     *
     * TODO: The exact rule here is: filter contains an optional in, and an
     * optional out, and must have at least one of them.  The parsing algorithm
     * must be rework to guarantee exactly that and handle all possible errors.
     */
    private Filter handleFilter(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("filter", true);
        String name = parser.getAttribute("name");
        parser.nextTag();
        parser.ensureNamespace(true);
        String elem = parser.getLocalName();
        Component in  = null;
        Component out = null;
        if ( elem.equals("in") ) {
            parser.nextTag(); // component start tag
            in = handleComponent(parser);
            parser.nextTag(); // </in>
            parser.nextTag(); // <out> or </filter>
            elem = parser.getLocalName();
        }
        if ( elem.equals("out") ) {
            parser.nextTag(); // component start tag
            out = handleComponent(parser);
            parser.nextTag(); // </out>
            parser.nextTag(); // </filter>
        }
        QName qname = parser.parseLiteralQName(name);
        return new Filter(qname, in, out);
    }

    /**
     * Handle an element 'chain' in the webapp descriptor.
     * 
     * TODO: Rework the loop algorithm, to ensure we have 1 or more filter
     * references in chain...
     */
    private ParsingChain handleChain(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("chain", true);
        String name_s = parser.getAttribute("name");
        QName name = parser.parseLiteralQName(name_s);
        ParsingChain chain = new ParsingChain(name);
        parser.nextTag();
        while ( parser.getLocalName().equals("filter") ) {
            parser.ensureElement("filter", true);
            String ref = parser.getAttribute("ref");
            QName qname = parser.parseLiteralQName(ref);
            chain.addFilter(qname);
            parser.nextTag(); // </filter>
            parser.nextTag(); // <filter> or </chain>
        }
        // TODO: Ensure we are on </chain>
        return chain;
    }

    /**
     * Handle an element 'servlet' in the webapp descriptor.
     *
     * Like other handleXXX() functions, the current event must be 'end tag'
     * at the end of the function (the end tag 'servlet' corresponding to the
     * open tag 'servlet' when the function is called).
     *
     * @param wrap_refs A map of references from servlets to filters, error
     *          handlers or chains. It is built during parsing, then resolved
     *          at the end, because a servlet can make reference to a filter
     *          before it is declared.
     * 
     * @param group_filters The in-scope filters referenced by parent and/or
     *          ancestor groups.
     */
    private ParsingServlet handleServlet(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("servlet", true);
        String name = parser.getAttribute("name");
        LOG.debug("expath-web parser: servlet: " + name);
        ParsingGroup   group   = ctxt.getCurrentGroup();
        ParsingServlet servlet = new ParsingServlet(name, group);
        String[]       filters = handleFiltersAttr(parser);
        for ( String filter : filters ) {
            QName f = parser.parseLiteralQName(filter);
            servlet.addFilter(f);
        }
        parser.nextTag();
        parser.ensureNamespace(true);
        Component implem = handleComponent(parser);
        servlet.setImplem(implem);
        // FIXME: TODO: There can be several URL element ! (to bind a servlet
        // to several URL patterns)
        // go to the next element: 'url'
        parser.nextTag();
        parser.ensureElement("url", true);
        String pattern = parser.getAttribute("pattern");
        servlet.setPattern(pattern);
        int last_group = 0;
        while ( XMLStreamConstants.START_ELEMENT == parser.nextTag() ) {
            parser.ensureElement("match", true);
            String re_group = parser.getAttribute("group");
            int num = Integer.parseInt(re_group);
            while ( last_group + 1 < num ) {
                servlet.addMatchGroup(null);
                last_group++;
            }
            servlet.addMatchGroup(parser.getAttribute("name"));
            last_group = num;
            // the end tag
            parser.nextTag();
        }
        // if there are no 'match' element, then we have just closed the 'url'
        // element, if there was 'match' elements, then the last 'nextTag()'
        // consumed the 'match' close tag
        if ( new QName(DESC_NS, "match").equals(parser.getName()) ) {
            // go to the 'url' end tag (check this is the case?)
            parser.nextTag();
        }
        parser.ensureEndTag(true);
        while ( XMLStreamConstants.START_ELEMENT == parser.nextTag() ) {
            parser.ensureElement("param", true);
            // FIXME: ignore for now
            parser.debug_skipElement();
        }
        return servlet;
    }

    /**
     * Handle an element 'group' in the webapp descriptor.
     */
    private ParsingGroup handleGroup(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
    {
        ParsingGroup parent = ctxt.getCurrentGroup();
        ParsingGroup group  = new ParsingGroup(parent);
        String[] names = handleFiltersAttr(parser);
        for ( String name : names ) {
            QName f = parser.parseLiteralQName(name);
            group.addFilter(f);
        }
        return group;
    }

    private String[] handleFiltersAttr(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        String filters = parser.getAttribute("filters");
        if ( filters == null ) {
            return new String[]{};
        }
        String[] names = filters.split("\\s");
        if ( names.length == 0 ) {
            parser.parseError("Filter attribtue is empty");
        }
        return names;
    }

    /**
     * Handle a component element (either 'xquery', 'xslt' or 'xproc').
     */
    private Component handleComponent(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        String elem = parser.getLocalName();
        if ( elem.equals("xquery") ) {
            return handleXQuery(parser);
        }
        else if ( elem.equals("xslt") ) {
            return handleXSLT(parser);
        }
        else if ( elem.equals("xproc") ) {
            return handleXProc(parser);
        }
        else {
            parser.parseError("Unkown component type: " + elem);
        }
        return null; // cannot happen, to make javac happy
    }

    /**
     * Handle a element 'xquery' in the webapp descriptor.
     *
     * Like other handleXXX() functions, the current event must be 'end tag'
     * at the end of the function (the end tag 'xquery' corresponding to the
     * open tag 'xquery' when the function is called).
     */
    private Component handleXQuery(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("xquery", true);
        String uri      = parser.getAttribute("uri");
        String function = parser.getAttribute("function");
        String file     = parser.getAttribute("file");
        if ( file != null ) {
            parser.parseError("xquery's @file deprecated: " + file);
        }
        if ( function != null && uri != null ) {
            String msg = "xquery's @function and @uri cannot be both set: ";
            parser.parseError(msg + function + " / " + uri);
        }
        Component result = null;
        if ( function != null ) {
            // get the function name
            QName f = parser.parseLiteralQName(function);
            // create the implem
            String ns    = f.getNamespaceURI();
            String local = f.getLocalPart();
            result = new XQueryFunction(ns, local);
        }
        else if ( uri != null ) {
            // create the implem
            result = new XQueryModule(myRepo, uri);
        }
        else {
            parser.parseError("@function and @uri both null on xquery component");
        }
        // go to the end element event
        parser.nextTag();
        parser.ensureEndTag(true);
        // return the implem
        return result;
    }

    /**
     * Handle a element 'xslt' in the webapp descriptor.
     *
     * Like other handleXXX() functions, the current event must be 'end tag'
     * at the end of the function (the end tag 'xslt' corresponding to the
     * open tag 'xslt' when the function is called).
     */
    private Component handleXSLT(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("xslt", true);
        String uri      = parser.getAttribute("uri");
        String function = parser.getAttribute("function");
        String template = parser.getAttribute("template");
        String file     = parser.getAttribute("file");
        if ( file != null ) {
            parser.parseError("xslt's @file deprecated: " + file);
        }
        if ( function != null && template != null ) {
            String msg = "xslt's @function and @template cannot be both set: ";
            parser.parseError(msg + function + " / " + uri);
        }
        Component result = null;
        if ( function != null || template != null ) {
            // get the component name
            String component = function == null ? template : function;
            QName c = parser.parseLiteralQName(component);
            // return the implem
            String ns    = c.getNamespaceURI();
            String local = c.getLocalPart();
            result = function == null
                    ? new XSLTTemplate(uri, ns, local)
                    : new XSLTFunction(uri, ns, local);
        }
        else if ( uri != null ) {
            // return the implem
            result = new XSLTTransform(uri);
        }
        else {
            parser.parseError("@function and @uri both null on xslt component");
        }
        // go to the end element event
        parser.nextTag();
        parser.ensureEndTag(true);
        // return the implem
        return result;
    }

    /**
     * Handle a element 'xproc' in the webapp descriptor.
     *
     * Like other handleXXX() functions, the current event must be 'end tag'
     * at the end of the function (the end tag 'xproc' corresponding to the
     * open tag 'xproc' when the function is called).
     */
    private Component handleXProc(StreamParser parser)
            throws ParseException
                 , XMLStreamException
    {
        parser.ensureElement("xproc", true);
        String uri  = parser.getAttribute("uri");
        String step = parser.getAttribute("step");
        String file = parser.getAttribute("file");
        if ( file != null ) {
            parser.parseError("xproc's @file deprecated: " + file);
        }
        Component result = null;
        if ( uri == null ) {
            parser.parseError("@uri null on xproc component");
        }
        else if ( step != null ) {
            // get the component name
            QName c = parser.parseLiteralQName(step);
            // return the implem
            String ns    = c.getNamespaceURI();
            String local = c.getLocalPart();
            result = new XProcStep(uri, ns, local);
        }
        else {
            // return the implem
            result = new XProcPipeline(uri);
        }
        // go to the end element event
        parser.nextTag();
        parser.ensureEndTag(true);
        // return the implem
        return result;
    }

    /** The webapp descriptor namespace. */
    private static final String DESC_NS = "http://expath.org/ns/webapp/descriptor";
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(EXPathWebParser.class);

    /** The webapp repository. */
    private final SaxonRepository myRepo;
}


/* ------------------------------------------------------------------------ */
/*  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS COMMENT.               */
/*                                                                          */
/*  The contents of this file are subject to the Mozilla Public License     */
/*  Version 1.0 (the "License"); you may not use this file except in        */
/*  compliance with the License. You may obtain a copy of the License at    */
/*  http://www.mozilla.org/MPL/.                                            */
/*                                                                          */
/*  Software distributed under the License is distributed on an "AS IS"     */
/*  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See    */
/*  the License for the specific language governing rights and limitations  */
/*  under the License.                                                      */
/*                                                                          */
/*  The Original Code is: all this file.                                    */
/*                                                                          */
/*  The Initial Developer of the Original Code is Florent Georges.          */
/*                                                                          */
/*  Contributor(s): none.                                                   */
/* ------------------------------------------------------------------------ */
