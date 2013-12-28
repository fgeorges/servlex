/****************************************************************************/
/*  File:       WebappsParser.java                                          */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2013-09-01                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2013 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.Source;
import org.expath.pkg.repo.PackageException;
import org.expath.pkg.repo.Repository;
import org.expath.pkg.repo.Storage;
import org.expath.servlex.TechnicalException;

/**
 * Facade class for this package, to parse the webapps.xml configuration file.
 *
 * @author Florent Georges
 * @date   2013-09-01
 */
public class WebappsParser
{
    public WebappsParser(Repository repo)
            throws TechnicalException
    {
        // webapps.xml source
        mySource = getWebappsXml(repo);
    }

    /**
     * Return true if the parameter is a syntactically valid context root.
     */
    public static boolean isContextRootValid(String ctxt_root)
    {
        return ROOT_PATTERN.matcher(ctxt_root).matches();
    }

    public List<WebappDecl> parse()
            throws TechnicalException
    {
        List<WebappDecl> result;
        // the streaming parser
        StreamParser parser = new StreamParser(mySource, EXPATH_WEB_NS);
        // the root element
        parser.ensureNextElement("webapps");
        try {
            // parse it!
            result = handletWebapps(parser);
        }
        catch ( ParseException ex ) {
            throw new TechnicalException("Error parsing " + WEBAPPS_FILE, ex);
        }
        // must be on </webapps>
        parser.ensureEndTag("webapps");
        // TODO: ensure end document, terminate parsing, etc...
        return result;
    }

    private Source getWebappsXml(Repository repo)
            throws TechnicalException
    {
        Storage storage = repo.getStorage();
        Storage.PackageResolver resolver = null;
        // get the repo's resolver
        try {
            resolver = storage.makePackageResolver(WEB_PRIVATE_DIR, null);
        }
        catch ( PackageException ex ) {
            throw new TechnicalException("Error creating the resolver in " + WEB_PRIVATE_DIR, ex);
        }
        // actually resolve webapps.xml
        try {
            return resolver.resolveResource(WEBAPPS_FILE);
        }
        catch ( PackageException ex ) {
            throw new TechnicalException("Error resolving " + WEBAPPS_PATH, ex);
        }
        catch ( Storage.NotExistException ex ) {
            String msg = "The repository is not a web-enabled repository, "
                    + "it does not contain " + WEBAPPS_PATH;
            throw new TechnicalException(msg, ex);
        }
    }

    private List<WebappDecl> handletWebapps(StreamParser parser)
            throws ParseException
    {
        // the result map
        List<WebappDecl> result = new ArrayList<>();

        // the "webapp" elements
        for ( ; /* ever */; ) {
            parser.nextTag();
            if ( XMLStreamConstants.START_ELEMENT != parser.getEventType() ) {
                // TODO: Check consistency...!
                break;
            }
            parser.ensureNamespace();
            String elem = parser.getLocalName();
            if ( elem.equals("webapp") ) {
                handleWebapp(parser, result);
            }
            else {
                parser.parseError("Unkown element in " + WEBAPPS_PATH + ": " + elem);
            }
        }

        return result;
    }

    private void handleWebapp(StreamParser parser, List<WebappDecl> decls)
            throws ParseException
    {
        // <webapp root="myapp" enabled="false">
        //    <package name="http://example.org/my/webapp"/>
        // </webapp>
        parser.ensureStartTag("webapp");

        // @root
        String ctxt_root = parser.getAttribute("root");
        if ( ctxt_root == null ) {
            parser.parseError("No @root on /webapps/webapp in " + WEBAPPS_PATH);
        }
        if ( ! isContextRootValid(ctxt_root) ) {
            parser.parseError("/webapps/webapp/@root in " + WEBAPPS_PATH + " is not valid: " + ctxt_root + ": " + ROOT_RE);
        }

        // @enabled
        String enabled = parser.getAttribute("enabled");
        if ( enabled != null ) {
            switch ( enabled.trim() ) {
                case "true":
                case "1":
                    // webapp enabled, nothing to do;
                    break;
                case "false":
                case "0":
                    // webapp disabled, not supported yet
                    parser.parseError("/webapps/webapp/@enabled is " + enabled +
                            " for " + ctxt_root + ", disabling not supported yet");
                    break;
                default:
                    // invalid value
                    parser.parseError("Invalid value for /webapps/webapp/@enabled for "
                            + ctxt_root + ": " + enabled);
            }
        }

        // package/@name
        parser.ensureNextElement("package");
        String name_str = parser.getAttribute("name");
        if ( name_str == null ) {
            parser.parseError("No @name on /webapps/webapp/package in " + WEBAPPS_PATH);
        }
        URI name = null;
        try {
            name = new URI(name_str);
        }
        catch ( URISyntaxException ex ) {
            parser.parseError("/webapps/webapp/package/@name in " + WEBAPPS_PATH + " is not a valid URI: " + name_str, ex);
        }

        // create the webapp declaration object
        WebappDecl decl = new WebappDecl(name, ctxt_root);
        decls.add(decl);
        parser.nextTag(); // </package>
        parser.ensureEndTag("package");

        // add the config parameters
        parser.nextTag(); // <config-param> or </webapp>
        while ( parser.getLocalName().equals("config-param") ) {
            String cfg_name  = parser.getAttribute("name");
            if ( cfg_name == null ) {
                parser.parseError("/webapps/webapp/config-param with no name, in root: " + ctxt_root);
            }
            String cfg_value = parser.getAttribute("value");
            if ( cfg_value == null ) {
                parser.parseError("/webapps/webapp/config-param with no value, in root: "
                        + ctxt_root + ", name: " + cfg_name);
            }
            decl.setConfigParam(cfg_name, cfg_value);
            parser.nextTag(); // </config-param>
            parser.nextTag(); // <config-param> or </webapp>
        }

        parser.ensureEndTag("webapp");
    }

    private static final String  WEB_PRIVATE_DIR = ".expath-web";
    private static final String  WEBAPPS_FILE    = "webapps.xml";
    private static final String  WEBAPPS_PATH    = WEB_PRIVATE_DIR + "/" + WEBAPPS_FILE;
    private static final String  EXPATH_WEB_NS   = "http://expath.org/ns/webapp";
    private static final String  ROOT_RE         = "^[-a-zA-Z0-9]+$";
    private static final Pattern ROOT_PATTERN    = Pattern.compile(ROOT_RE);

    private final Source mySource;
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
