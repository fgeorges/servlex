/****************************************************************************/
/*  File:       EXPathWebParser.java                                        */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-02-09                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.Source;
import org.apache.log4j.Logger;
import org.expath.pkg.repo.Package;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Storage;
import org.expath.pkg.repo.Storage.PackageResolver;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.components.Component;
import org.expath.servlex.model.AddressHandler;
import org.expath.servlex.model.Application;
import org.expath.servlex.model.ConfigParam;
import org.expath.servlex.processors.Processors;
import org.expath.servlex.tools.ProcessorsMap;

/**
 * Facade class for this package, to parse EXPath Webapp descriptors.
 *
 * @author Florent Georges
 */
public class EXPathWebParser
{
    public EXPathWebParser(ProcessorsMap procs)
    {
        myProcs = procs;
    }

    /**
     * Parse the webapp descriptor of a given package, if any.
     * 
     * @return th application corresponding to a package, if any, or null if the
     * package is not a webapp.
     * 
     * @param pkg The package to parse as a webapp.
     * 
     * @throws ParseException in case of any parsing error (for both
     * {@code expath-web.xml} and {@code servlex.xml}).
     * 
     * @throws TechnicalException in case of any other technical error.
     */
    public Application loadPackage(Package pkg)
            throws ParseException
                 , TechnicalException
    {
        Source descriptor = getDescriptor(pkg, DESC_FILENAME);
        if ( descriptor == null ) {
            LOG.debug("Package does not have any web descriptor, must be a library, ignore it: " + pkg.getName());
            return null;
        }
        Application app;
        try {
            app = parseDescriptorFile(descriptor, pkg, DESC_NS);
        }
        catch ( ParseException ex ) {
            // TODO: For compatibility reason, try to parse using the legacy namespace
            descriptor = getDescriptor(pkg, DESC_FILENAME);
            LOG.error("Parsing the web descriptor for " + pkg.getName() + " failed, trying the legacy namespace.");
            app = parseDescriptorFile(descriptor, pkg, LEGACY_DESC_NS);
        }
        app.logApplication();
        return app;
    }

    /**
     * Return a descriptor from the package, like {@code expath-web.xml} or {@code servlex.xml}.
     */
    private Source getDescriptor(Package pkg, String name)
            throws ParseException
    {
        try {
            PackageResolver resolver = pkg.getResolver();
            return resolver.resolveResource(name);
        }
        catch ( Storage.NotExistException ex ) {
                String msg = "No web descriptor '" + name + "' in " + pkg.getName();
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
        ctxt.setProcessors(myProcs.getDefault());

        // set the base URI
        try {
            URI base = pkg.getResolver().getContentDirBaseURI();
            ctxt.setBase(base);
        }
        catch ( PackageException ex ) {
            // do nothing, will be an error only if we need the base URI and it is null
            LOG.debug("Impossible to get the content base URI for: " + pkg.getName(), ex);
        }

        // try servlex.xml
        Source extensions = getDescriptor(pkg, SERVLEX_FILENAME);
        if ( extensions == null ) {
            LOG.info("Package does not have Servlex extension descriptor: " + pkg.getName());
            return ctxt;
        }

        LOG.info("Parse Servlex extension descriptor for app " + pkg.getName());

        // parser on servlex.xml, and position on the root 'webapp' element
        StreamParser parser = new StreamParser(extensions, SERVLEX_NS);
        parser.ensureNextElement("webapp");

        for ( ; /* ever */; ) {
            parser.nextTag();
            if ( XMLStreamConstants.START_ELEMENT != parser.getEventType() ) {
                // TODO: Check consistency...!
                break;
            }
            parser.ensureNamespace();
            String elem = parser.getLocalName();
            if ( elem.equals("processors") ) {
                String clazz = parser.getAttribute("class");
                try {
                    Processors procs = myProcs.getProcessors(clazz);
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

        // must be on </webapp>
        parser.ensureEndTag("webapp");
        // TODO: Check we consumed everything...

        return ctxt;
    }

    /**
     * Parse one webapp descriptor file.
     * 
     * TODO: Plug schema validation of expath-web.xml.
     * 
     * Note: it is private, but is package-level to be unit-testable.
     * 
     * @param ns The namespace of the web descriptor.
     */
    Application parseDescriptorFile(Source descriptor, Package pkg, String ns)
            throws ParseException
                 , TechnicalException
    {
        LOG.info("Parse webapp descriptor for app " + pkg.getName());

        StreamParser parser = new StreamParser(descriptor, ns);

        // position the parser on the root 'webapp' element
        parser.ensureNextElement("webapp");
        validateSpecNumber(parser);

        // the values used to build the application object
        ParsingContext ctxt = initContext(pkg);
        // TODO: Check the value returned for 'abbrev'.
        String abbrev = parser.getAttribute("abbrev");
        ctxt.setAbbrev(abbrev);
        LOG.info("  webapp abbrev:" + abbrev);

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
            parser.ensureNamespace();
            String elem = parser.getLocalName();
            switch ( elem ) {
                case "title": {
                    String title = parser.getElementText();
                    ctxt.setTitle(title);
                    break;
                }
                case "config-param": {
                    ParsingConfigParam c = handleConfigParam(parser);
                    ctxt.addConfigParam(c);
                    break;
                }
                case "application": {
                    ParsingApp a = handleApplication(parser);
                    ctxt.setApplication(a);
                    break;
                }
                case "error": {
                    ParsingError e = handleError(parser, ctxt);
                    ctxt.addWrapper(e);
                    break;
                }
                case "group": {
                    ParsingGroup g = handleGroup(parser, ctxt);
                    ctxt.pushGroup(g);
                    break;
                }
                case "filter": {
                    ParsingFilter f = handleFilter(parser, ctxt);
                    ctxt.addWrapper(f);
                    break;
                }
                case "chain": {
                    ParsingChain c = handleChain(parser);
                    ctxt.addWrapper(c);
                    break;
                }
                case "servlet": {
                    ParsingServlet s = handleServlet(parser, ctxt);
                    ctxt.addHandler(s);
                    break;
                }
                case "resource": {
                    ParsingResource rsrc = handleResource(parser, ctxt);
                    ctxt.addHandler(rsrc);
                    break;
                }
                default: {
                    String msg = "Unkown element in the descriptor for webapp ";
                    parser.parseError(msg + pkg.getName() + ": " + elem);
                    break;
                }
            }
        }

        // must be on </webapp>
        parser.ensureEndTag("webapp");
        // TODO: Check we consumed everything...

        // build the application object
        return createApplication(pkg, ctxt);
    }

    /**
     * Validate that /webapp/@spec is the correct number.
     * 
     * @throws ParseException If @spec is not exactly "1.0".
     * 
     * TODO: For now, for compatibility reason, it accepts webapp descriptors
     * without the @spec attribute.  That is deprecated and will be removed
     * once the Webapp Module is published as version 1.0.
     */
    private void validateSpecNumber(StreamParser parser)
            throws ParseException
    {
        String spec = parser.getAttribute("spec");
        if ( spec == null ) {
            LOG.error("  webapp/@spec is missing! (ignored for now, for compatibility reason)");
        }
        else if ( ! spec.equals("1.0") ) {
            throw new ParseException("webapp/@spec is not exactly 1.0, it is: '" + spec + "'");
        }
    }

    private Application createApplication(Package pkg, ParsingContext ctxt)
            throws ParseException
    {
        for ( ParsingWrapper w : ctxt.getWrappers() ) {
            w.makeIt(ctxt);
        }
        // get values and build the app object
        String      abbrev = ctxt.getAbbrev();
        String      title  = ctxt.getTitle();
        Processors  procs  = ctxt.getProcessors();
        Application app    = new Application(abbrev, title, pkg, procs);
        // build the servlets
        for ( ParsingHandler h : ctxt.getHandlers()) {
            AddressHandler handler = h.makeAddressHandler(ctxt, LOG);
            app.addHandler(handler);
        }
        // add config params
        for ( ParsingConfigParam c : ctxt.getConfigParams()) {
            ConfigParam config = c.makeConfigParam(ctxt);
            app.addConfigParam(config);
        }
        return app;
    }

    /**
     * Handle an element 'config-param' in the webapp descriptor.
     */
    private ParsingConfigParam handleConfigParam(StreamParser parser)
            throws ParseException
    {
        parser.ensureStartTag("config-param");
        String id = parser.getAttribute("id");
        if ( id == null ) {
            parser.parseError("/webapp/config-param/@id is null");
        }
        LOG.debug("expath-web parser: config param: " + id);
        ParsingConfigParam cfg = new ParsingConfigParam(id);
        parser.nextTag();
        // consume <name> and <desc> if any
        handleDescription(parser, cfg);
        // now, current event must be either <value> or <uri>
        if ( parser.isStartTag("value") ) {
            String value = parser.getElementText();
            cfg.setValue(value);
            parser.nextTag();
        }
        else if ( parser.isStartTag("uri") ) {
            String uri = parser.getElementText();
            cfg.setURI(uri);
            parser.nextTag();
        }
        else {
            parser.parseError("Expecting element 'value' or 'uri'");
        }
        // the end of the element
        parser.ensureEndTag("config-param");
        return cfg;
    }

    /**
     * Handle an element with optional {@code <name>} and {@code <desc>}.
     * 
     * TODO: Plug it into other elements as well, like for instance servlet and
     * filter elements.
     * 
     * Precondition: the parser must be on {@code <name>} if there is one, or on
     * {@code <desc>} if there is one and no name, or on the next tag event if
     * there is neither of them. Postcondition: the parser is on the next tag
     * event (the next sibling element start or the parent end).
     */
    private void handleDescription(StreamParser parser, ParsingDescribed described)
            throws ParseException
    {
        if ( parser.isStartTag("name") ) {
            String name = parser.getElementText();
            described.setName(name);
            parser.nextTag();
        }
        if ( parser.isStartTag("desc") ) {
            String desc = parser.getElementText();
            described.setDescription(desc);
            parser.nextTag();
        }
    }

    /**
     * Handle an element 'application' in the webapp descriptor.
     */
    private ParsingApp handleApplication(StreamParser parser)
            throws ParseException
    {
        ParsingApp app = new ParsingApp();
        handleFiltersAttr(parser, app);
        parser.nextTag();
        return app;
    }

    /**
     * Handle an element 'error' in the webapp descriptor.
     */
    private ParsingError handleError(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , TechnicalException
    {
        parser.ensureStartTag("error");
        String name = parser.getAttribute("name");
        String catc = parser.getAttribute("catch");
        LOG.debug("expath-web parser: error: " + name + " catching " + catc);
        ParsingError error = new ParsingError(name);
        handleFiltersAttr(parser, error);
        if ( catc.equals("*") ) {
            // nothing
        }
        else if ( catc.startsWith("*:") ) {
            error.setLocal(catc.substring(2));
        }
        else if ( catc.endsWith(":*") ) {
            String prefix = catc.substring(0, catc.length() - 2);
            error.setNs(parser.resolvePrefix(prefix));
        }
        else {
            QName code = parser.parseLiteralQName(catc);
            error.setCode(code);
            error.setNs(code.getNamespaceURI());
            error.setLocal(code.getLocalPart());
        }
        parser.nextTag();
        parser.ensureNamespace();
        Component implem = handleComponent(parser, ctxt);
        error.setImplem(implem);
        parser.nextTag();
        return error;
    }

    /**
     * Handle common attributes on elements 'resource' or 'servlet'.
     *
     * Unlike other handleXXX() functions, this one does not consume any new
     * events in the parsing event stream.  It only looks at attributes.
     */
    private void handleAdressHandler(ParsingHandler handler, StreamParser parser, ParsingContext ctxt)
            throws ParseException
    {
        // the current group
        ParsingGroup group = ctxt.getCurrentGroup();
        handler.setGroup(group);
        // the filters
        handleFiltersAttr(parser, handler);
    }

    /**
     * Handle an element 'resource' in the webapp descriptor.
     *
     * Like other handleXXX() functions, the current event must be 'end tag'
     * at the end of the function (the end tag 'resource' corresponding to the
     * open tag 'resource' when the function is called).
     */
    private ParsingResource handleResource(StreamParser parser, ParsingContext ctxt)
            throws ParseException
    {
        parser.ensureStartTag("resource");
        ParsingResource rsrc = new ParsingResource();
        handleAdressHandler(rsrc, parser, ctxt);
        // the pattern
        String pattern = parser.getAttribute("pattern");
        rsrc.setPattern(pattern);
        // the rewrite rule
        String rewrite = parser.getAttribute("rewrite");
        rsrc.setRewrite(rewrite);
        // the media type
        String type = parser.getAttribute("media-type");
        rsrc.setMediaType(type);
        parser.nextTag();
        parser.ensureEndTag();
        return rsrc;
    }

    /**
     * Handle an element 'filter' in the webapp descriptor.
     *
     * TODO: The exact rule here is: filter contains an optional in, and an
     * optional out, and must have at least one of them.  The parsing algorithm
     * must be rework to guarantee exactly that and handle all possible errors.
     */
    private ParsingFilter handleFilter(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , TechnicalException
    {
        parser.ensureStartTag("filter");
        String name = parser.getAttribute("name");
        parser.nextTag();
        parser.ensureNamespace();
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
        return new ParsingFilter(name, in, out);
    }

    /**
     * Handle an element 'chain' in the webapp descriptor.
     * 
     * TODO: Rework the loop algorithm, to ensure we have 1 or more filter
     * references in chain...
     */
    private ParsingChain handleChain(StreamParser parser)
            throws ParseException
    {
        parser.ensureStartTag("chain");
        String name = parser.getAttribute("name");
        ParsingChain chain = new ParsingChain(name);
        parser.nextTag();
        while ( parser.getLocalName().equals("filter") ) {
            parser.ensureStartTag("filter");
            String ref = parser.getAttribute("ref");
            chain.addFilter(ref);
            parser.nextTag(); // </filter>
            parser.nextTag(); // <filter> or </chain>
        }
        // ensuring end tag only (without the name) should be enough...
        parser.ensureEndTag("chain");
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
                 , TechnicalException
    {
        parser.ensureStartTag("servlet");
        String name = parser.getAttribute("name");
        LOG.debug("expath-web parser: servlet: " + name);
        ParsingServlet servlet = new ParsingServlet(name);
        handleAdressHandler(servlet, parser, ctxt);
        parser.nextTag();
        parser.ensureNamespace();
        Component implem = handleComponent(parser, ctxt);
        servlet.setImplem(implem);
        // FIXME: TODO: There can be several URL element ! (to bind a servlet
        // to several URL patterns)
        // go to the next element: 'url'
        parser.nextTag();
        parser.ensureStartTag("url");
        String pattern = parser.getAttribute("pattern");
        servlet.setPattern(pattern);
        int last_group = 0;
        while ( XMLStreamConstants.START_ELEMENT == parser.nextTag() ) {
            parser.ensureStartTag("match");
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
        parser.ensureEndTag();
        while ( XMLStreamConstants.START_ELEMENT == parser.nextTag() ) {
            parser.ensureStartTag("param");
            // FIXME: ignore for now
            LOG.error("FIXME: Element PARAM ignored in expath-web.xml...");
            parser.debug_skipElement();
        }
        return servlet;
    }

    /**
     * Handle an element 'group' in the webapp descriptor.
     */
    private ParsingGroup handleGroup(StreamParser parser, ParsingContext ctxt)
            throws ParseException
    {
        ParsingGroup parent = ctxt.getCurrentGroup();
        ParsingGroup group  = new ParsingGroup(parent);
        handleFiltersAttr(parser, group);
        return group;
    }

    private void handleFiltersAttr(StreamParser parser, ParsingFiltered filtered)
            throws ParseException
    {
        String filters = parser.getAttribute("filters");
        if ( filters == null ) {
            return;
        }
        String[] names = filters.split("\\s");
        if ( names.length == 0 ) {
            parser.parseError("Filter attribtue is empty");
        }
        for ( String name : names ) {
            filtered.addFilter(name);
        }
    }

    /**
     * Handle a component element (either 'xquery', 'xslt' or 'xproc').
     */
    private Component handleComponent(StreamParser parser, ParsingContext ctxt)
            throws ParseException
                 , TechnicalException
    {
        String elem = parser.getLocalName();
        switch (elem) {
            case "xquery":
                return handleXQuery(parser, ctxt);
            case "xslt":
                return handleXSLT(parser, ctxt);
            case "xproc":
                return handleXProc(parser, ctxt);
            default:
                parser.parseError("Unkown component type: " + elem);
                return null; // cannot happen, to make javac happy
        }
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
                 , TechnicalException
    {
        parser.ensureStartTag("xquery");
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
        parser.ensureEndTag();
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
                 , TechnicalException
    {
        parser.ensureStartTag("xslt");
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
        parser.ensureEndTag();
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
                 , TechnicalException
    {
        parser.ensureStartTag("xproc");
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
        parser.ensureEndTag();
        // return the implem
        return result;
    }

    /** The webapp descriptor namespace. */
    static final String DESC_NS = "http://expath.org/ns/webapp";
    /** The legacy webapp descriptor namespace (in some drafts before Webapp 1.0). */
    @Deprecated
    static final String LEGACY_DESC_NS = "http://expath.org/ns/webapp/descriptor";
    /** The servlex extension file name. */
    private static final String SERVLEX_FILENAME = "servlex.xml";
    /** The servlex extension namespace. */
    private static final String SERVLEX_NS = "http://servlex.net/";
    /** The webapp descriptor file name. */
    private static final String DESC_FILENAME = "expath-web.xml";
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(EXPathWebParser.class);

    /** The map of Processors objects. */
    private final ProcessorsMap myProcs;
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
