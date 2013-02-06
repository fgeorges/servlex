/****************************************************************************/
/*  File:       TreeBuilderHelper.java                                      */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package org.expath.servlex.tools;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;

/**
 * Simple helper to ease the use of Saxon's Builder API.
 *
 * @author Florent Georges
 * @date   2010-11-26
 */
public class TreeBuilderHelper
{
    public TreeBuilderHelper(Processor proc, String ns_uri, String ns_prefix)
            throws XPathException
    {
        Configuration conf = proc.getUnderlyingConfiguration();
        myNsUri = ns_uri;
        myNsPrefix = ns_prefix;
        myDocBuilder = proc.newDocumentBuilder();
        myBuilder = myDocBuilder.getTreeModel().makeBuilder(conf.makePipelineConfiguration());
        myBuilder.open();
        myBuilder.startDocument(0);
    }

    public void startElem(String local)
            throws XPathException
    {
        NodeName name = new FingerprintedQName(myNsPrefix, myNsUri, local);
        myBuilder.startElement(name, Untyped.getInstance(), 0, 0);
    }

    public void attribute(String local, String value)
            throws XPathException
    {
        if ( value != null ) {
            NodeName name = new NoNamespaceName(local);
            myBuilder.attribute(name, BuiltInAtomicType.UNTYPED_ATOMIC, value, 0, 0);
        }
    }

    public void startContent()
            throws XPathException
    {
        myBuilder.startContent();
    }

    public void characters(String value)
            throws XPathException
    {
        myBuilder.characters(value, 0, 0);
    }

    public void endElem()
            throws XPathException
    {
        myBuilder.endElement();
    }

    public void textElem(String local, String value)
            throws XPathException
    {
        startElem(local);
        startContent();
        characters(value);
        endElem();
    }

    public XdmNode getRoot()
            throws XPathException
    {
        myBuilder.endDocument();
        myBuilder.close();
        return myDocBuilder.wrap(myBuilder.getCurrentRoot());
    }

    private String myNsUri;
    private String myNsPrefix;
    private DocumentBuilder myDocBuilder;
    private Builder myBuilder;
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
