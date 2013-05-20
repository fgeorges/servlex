/****************************************************************************/
/*  File:       EXPathWebParser.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Packages;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.repo.Storage.PackageResolver;
import org.expath.servlex.ServerConfig;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.model.Application;
import org.expath.servlex.components.Component;
import org.expath.servlex.model.Resource;
import org.expath.servlex.model.Servlet;
import org.expath.servlex.model.*;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.tools.RegexHelper;

/**
 * Facade class for this package, to parse EXPath Webapp descriptors.
 *
 * @author Florent Georges
 * @date   2010-02-09
 */
public class EXPathWebParser
{
    public EXPathWebParser(ServerConfig config)
    {
        myConfig = config;
    }

    /**
     * Parse all webapp descriptors in the repository.
     *
     * TODO: For now, only get the "latest" version of a package.  See comments
     * of {@link Repository#resolve(String,URISpace)} about that (versionning
     * scheme is not always SemVer -- or could we impose it for webapps?)
     */
    public Set<Application> parseDescriptors(Collection<Packages> packages)
            throws ParseException
                 , TechnicalException
    {
        // the result
        Set<Application> apps = new HashSet<Application>();
        // iterate on every sub-directories of the repo (i.e. on each package)
        for ( Packages pp : packages ) {
            Package pkg = pp.latest();
            Application app = loadPackage(pkg);
            if ( app != null ) {
                apps.add(app);
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
                 , TechnicalException
    {
        InputStream descriptor = getDescriptor(pkg, DESC_FILENAME);
        if ( descriptor == null ) {
            LOG.debug("Package does not have any web descriptor, must be a library, ignore it: " + pkg.getName());
            return null;
        }
        return parseDescriptorFile(descriptor, pkg);
    }

    /**
     * Return a descriptor from the package, like {@code expath-web.xml} or {@code servlex.xml}.
     */
    private InputStream getDescriptor(Package pkg, String name)
            throws ParseException
    {
        try {
            PackageResolver resolver = pkg.getResolver();
            StreamSource    source   = resolver.resolveResource(name);
            return source.getInputStream();
        }
        catch ( Storage.NotExistException ex ) {
                String msg = "No descriptor '" + name + "' in " + pkg.getName();
                LOG.debug(msg + " (" + ex + ")");
                return null;
        }
        catch ( PackageException ex ) {
            throw new ParseException("Error accessing the descriptor '" + name + "' of " + pkg.getName(), ex);
        }
    }

    /**
     * Initiate the parsing context for one application.
     * 
     * Look into {@code servlex.xml}.  If it declares a specific {@link Processors}
     * implementation, this implementation is instantiated, and set on the context.
     * If not the instance of the default implementation passed as parameter is
     * used instead.
     * 
     * TODO: Plug schema validation of servlex.xml.
     */
    private ParsingContext initContext(Package pkg)
            throws ParseException
    {
        // the parsing context, with the default processors
        ParsingContext ctxt = new ParsingContext();
        ctxt.setProcessors(myConfig.getDefaultProcessors());

        // try servlex.xml
        InputStream extensions = getDescriptor(pkg, SERVLEX_FILENAME);
        if ( extensions == null ) {
            LOG.debug("Package does not have Servlex extension descriptor: " + pkg.getName());
            return ctxt;
        }

        // parser on servlex.xml, and position on the root 'webapp' element
        StreamParser parser = new StreamParser(extensions, SERVLEX_NS);
        parser.ensureNextElement("webapp", true);

        try {
            for ( ; /* ever */; ) {
                parser.nextTag();
                if ( XMLStreamConstants.START_ELEMENT != parser.getEventType() ) {
                    // TODO: Check consistency...!
                    break;
                }
                parser.ensureNamespace(true);
                String elem = parser.getLocalName();
                if ( elem.equals("processors") ) {
                    String clazz = parser.getAttribute("class");
                    try {
                        Processors procs = myConfig.getProcessors(clazz);
                        ctxt.setProcessors(procs);
                    }
                    catch ( TechnicalException ex ) {
                        // this is a non-fatal error (and we can have several such elements)
                        LOG.warn("Error instantiating Processors implementation: " + clazz, ex);
                    }
                }
                else {
                    String msg = "Unkown element in the servlex extensions for webapp ";
                    parser.parseError(msg + pkg.getName() + ": " + elem);
                }
            }
            // TODO: Check we consumed everything...
        }
        catch ( XMLStreamException ex ) {
            parser.parseError("Error parsing the servlex extension descriptor", ex);
        }

        return ctxt;
    }

    /**
     * Parse one webapp descriptor file.
     * 
     * TODO: Plug schema validation of expath-web.xml.
     */
    private Application parseDescriptorFile(InputStream descriptor, Package pkg)
            throws ParseException
                 , TechnicalException
    {
        LOG.info("Parse webapp descriptor for app " + pkg.getName());

        StreamParser parser = new StreamParser(descriptor, DESC_NS);

        // position the parser on the root 'webapp' element
        parser.ensureNextElement("webapp", true);

        // the values used to build the application object
        ParsingContext ctxt = initContext(pkg);
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
                    ErrorHandler h = handleError(parser, ctxt);
                    ctxt.addWrapper(h);
                }
                else if ( elem.equals("group") ) {
                    ParsingGroup g = handleGroup(parser, ctxt);
                    ctxt.pushGroup(g);
                }
                else if ( elem.equals("filter") ) {
                    Filter f = handleFilter(parser, ctxt);
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
        Processors  procs  = ctxt.getProcessors();
        Application app    = new Application(abbrev, title, pkg, procs);
        // build the servlets
        for ( ParsingServlet s : ctxt.getServlets() ) {
            try {
                String    name    = s.getName();
                Component implem  = s.getImplem();
                String    pattern = s.getPattern();
                String    java_re = RegexHelper.xpathToJava(pattern, LOG);
                Pattern   regex   = Pattern.compile(java_re);
                String[]  groups  = s.getMatchGroups();
                Servlet   servlet = new Servlet(name, implem, regex, groups);
                Wrapper   wrapper = s.makeWrapper(ctxt);
                servlet.setWrapper(wrapper);
                app.addHandler(servlet);
            }
            catch ( TechnicalException ex ) {
                throw new ParseException("The pattern is not a valid XPath regex", ex);
            }
        }
        // TODO: FIXME: If I am right, all the servlets are added first, then
        // all the resources.  But they should preserve the order from the 
        // descriptor, because of the regex precedence...
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
    private ErrorHandler handleError(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
                 , TechnicalException
    {
        parser.ensureStartTag("error", true);
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
        Component implem = handleComponent(parser, ctxt);
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
        parser.ensureStartTag("resource", true);
        String pattern = parser.getAttribute("pattern");
        String rewrite = parser.getAttribute("rewrite");
        String type    = parser.getAttribute("media-type");
        parser.nextTag();
        parser.ensureEndTag(true);
        try {
            String java_regex = RegexHelper.xpathToJava(pattern, LOG);
            return new Resource(Pattern.compile(java_regex), java_regex, rewrite, type);
        }
        catch ( TechnicalException ex ) {
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
    private Filter handleFilter(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
                 , TechnicalException
    {
        parser.ensureStartTag("filter", true);
        String name = parser.getAttribute("name");
        parser.nextTag();
        parser.ensureNamespace(true);
        String elem = parser.getLocalName();
        Component in  = null;
        Component out = null;
        if ( elem.equals("in") ) {
            parser.nextTag(); // component start tag
            in = handleComponent(parser, ctxt);
            parser.nextTag(); // </in>
            parser.nextTag(); // <out> or </filter>
            elem = parser.getLocalName();
        }
        if ( elem.equals("out") ) {
            parser.nextTag(); // component start tag
            out = handleComponent(parser, ctxt);
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
        parser.ensureStartTag("chain", true);
        String name_s = parser.getAttribute("name");
        QName name = parser.parseLiteralQName(name_s);
        ParsingChain chain = new ParsingChain(name);
        parser.nextTag();
        while ( parser.getLocalName().equals("filter") ) {
            parser.ensureStartTag("filter", true);
            String ref = parser.getAttribute("ref");
            QName qname = parser.parseLiteralQName(ref);
            chain.addFilter(qname);
            parser.nextTag(); // </filter>
            parser.nextTag(); // <filter> or </chain>
        }
        // ensuring end tag only (without the name) should be enough...
        parser.ensureEndTag("chain", true);
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
                 , TechnicalException
    {
        parser.ensureStartTag("servlet", true);
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
        Component implem = handleComponent(parser, ctxt);
        servlet.setImplem(implem);
        // FIXME: TODO: There can be several URL element ! (to bind a servlet
        // to several URL patterns)
        // go to the next element: 'url'
        parser.nextTag();
        parser.ensureStartTag("url", true);
        String pattern = parser.getAttribute("pattern");
        servlet.setPattern(pattern);
        int last_group = 0;
        while ( XMLStreamConstants.START_ELEMENT == parser.nextTag() ) {
            parser.ensureStartTag("match", true);
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
            parser.ensureStartTag("param", true);
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
    private Component handleComponent(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
                 , TechnicalException
    {
        String elem = parser.getLocalName();
        if ( elem.equals("xquery") ) {
            return handleXQuery(parser, ctxt);
        }
        else if ( elem.equals("xslt") ) {
            return handleXSLT(parser, ctxt);
        }
        else if ( elem.equals("xproc") ) {
            return handleXProc(parser, ctxt);
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
    private Component handleXQuery(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
                 , TechnicalException
    {
        parser.ensureStartTag("xquery", true);
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
            result = ctxt.getProcessors().getXQuery().makeFunction(ns, local);
        }
        else if ( uri != null ) {
            // create the implem
            result = ctxt.getProcessors().getXQuery().makeQuery(uri);
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
    private Component handleXSLT(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
                 , TechnicalException
    {
        parser.ensureStartTag("xslt", true);
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
            if ( function == null ) {
                result = ctxt.getProcessors().getXSLT().makeTemplate(uri, ns, local);
            }
            else {
                result = ctxt.getProcessors().getXSLT().makeFunction(uri, ns, local);
            }
        }
        else if ( uri != null ) {
            // return the implem
            result = ctxt.getProcessors().getXSLT().makeTransform(uri);
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
    private Component handleXProc(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , XMLStreamException
                 , TechnicalException
    {
        parser.ensureStartTag("xproc", true);
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
            result = ctxt.getProcessors().getXProc().makeStep(uri, ns, local);
        }
        else {
            // return the implem
            result = ctxt.getProcessors().getXProc().makePipeline(uri);
        }
        // go to the end element event
        parser.nextTag();
        parser.ensureEndTag(true);
        // return the implem
        return result;
    }

    /** The webapp descriptor namespace. */
    private static final String DESC_NS = "http://expath.org/ns/webapp/descriptor";
    /** The servlex extension file name. */
    private static final String SERVLEX_FILENAME = "servlex.xml";
    /** The servlex extension namespace. */
    private static final String SERVLEX_NS = "http://servlex.net/";
    /** The webapp descriptor file name. */
    private static final String DESC_FILENAME = "expath-web.xml";
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(EXPathWebParser.class);

    /** The config object. */
    private ServerConfig myConfig;
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
