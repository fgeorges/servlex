/****************************************************************************/
/*  File:       SaxonTreeBuilder.java                                       */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2010-11-26                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2010 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon;

import net.servlex.saxon.model.SaxonDocument;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.expr.parser.Location;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;
import org.expath.servlex.TechnicalException;
import org.expath.servlex.processors.Document;
import org.expath.servlex.processors.TreeBuilder;

/**
 * Tree builder implementation for Saxon.
 *
 * @author Florent Georges
 */
class SaxonTreeBuilder
        implements TreeBuilder
{
    public SaxonTreeBuilder(Processor proc, String ns_uri, String ns_prefix)
            throws TechnicalException
    {
        Configuration conf = proc.getUnderlyingConfiguration();
        myNsUri = ns_uri;
        myNsPrefix = ns_prefix;
        myDocBuilder = proc.newDocumentBuilder();
        myBuilder = myDocBuilder.getTreeModel().makeBuilder(conf.makePipelineConfiguration());
        myBuilder.open();
        try {
            myBuilder.startDocument(0);
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error starting document", ex);
        }
    }

    @Override
    public void startElem(String local)
            throws TechnicalException
    {
        NodeName name = new FingerprintedQName(myNsPrefix, myNsUri, local);
        try {
            myBuilder.startElement(name, Untyped.getInstance(), ourLocation, 0);
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error starting element '" + local + "'", ex);
        }
    }

    @Override
    public void attribute(String local, String value)
            throws TechnicalException
    {
        if ( value != null ) {
            NodeName name = new NoNamespaceName(local);
            try {
                myBuilder.attribute(name, BuiltInAtomicType.UNTYPED_ATOMIC, value, ourLocation, 0);
            }
            catch ( XPathException ex ) {
                throw new TechnicalException("Error building attribute '" + local + "'", ex);
            }
        }
    }

    @Override
    public void startContent()
            throws TechnicalException
    {
        try {
            myBuilder.startContent();
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error starting content", ex);
        }
    }

    @Override
    public void characters(String value)
            throws TechnicalException
    {
        try {
            myBuilder.characters(value, ourLocation, 0);
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error building characters", ex);
        }
    }

    @Override
    public void endElem()
            throws TechnicalException
    {
        try {
            myBuilder.endElement();
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error ending element", ex);
        }
    }

    @Override
    public void textElem(String local, String value)
            throws TechnicalException
    {
        startElem(local);
        startContent();
        characters(value);
        endElem();
    }

    @Override
    public Document getRoot()
            throws TechnicalException
    {
        try {
            myBuilder.endDocument();
            myBuilder.close();
        }
        catch ( XPathException ex ) {
            throw new TechnicalException("Error ending document", ex);
        }
        NodeInfo node = myBuilder.getCurrentRoot();
        XdmNode doc = myDocBuilder.wrap(node);
        return new SaxonDocument(doc);
    }

    private String myNsUri;
    private String myNsPrefix;
    private DocumentBuilder myDocBuilder;
    private Builder myBuilder;
    private static final Location ourLocation = new MyLocation();

    private static class MyLocation
            implements Location
    {
        @Override
        public String getSystemId() {
            return null;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public int getColumnNumber() {
            return -1;
        }

        @Override
        public Location saveLocation() {
            return this;
        }
    }
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
