/****************************************************************************/
/*  File:       XdmDestinationLogProxy.java                                 */
/*  Author:     F. Georges - H2O Consulting                                 */
/*  Date:       2015-12-05                                                  */
/*  Tags:                                                                   */
/*      Copyright (c) 2015 Florent Georges (see end of file.)               */
/* ------------------------------------------------------------------------ */


package net.servlex.saxon.tools;

import java.net.URI;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import org.expath.servlex.tools.Log;

/**
 * Proxy debugging class for {@link XdmDestination}.
 * 
 * @author Florent Georges
 */
public class XdmDestinationLogProxy
        extends XdmDestination
{
    public XdmDestinationLogProxy(XdmDestination proxied) {
        myProxied = proxied;
    }

    @Override
    public void reset() {
        LOG.error("reset");
        myProxied.reset();
    }

    @Override
    public XdmNode getXdmNode() {
        LOG.error("getXdmNode");
        return myProxied.getXdmNode();
    }

    @Override
    public void close() throws SaxonApiException {
        LOG.error("close");
        myProxied.close();
    }

    @Override
    public Receiver getReceiver(Configuration config) throws SaxonApiException {
        LOG.error("getReceiver: " + config);
        Receiver r = myProxied.getReceiver(config);
        return new ReceiverLogProxy(r);
    }

    @Override
    public TreeModel getTreeModel() {
        LOG.error("getTreeModel");
        return myProxied.getTreeModel();
    }

    @Override
    public void setTreeModel(TreeModel model) {
        LOG.error("setTreeModel: " + model);
        myProxied.setTreeModel(model);
    }

    @Override
    public URI getBaseURI() {
        LOG.error("getBaseURI");
        return myProxied.getBaseURI();
    }

    @Override
    public void setBaseURI(URI base) {
        LOG.error("setBaseURI: " + base);
        myProxied.setBaseURI(base);
    }

    /** The logger. */
    private static final Log LOG = new Log(ReceiverLogProxy.class);

    /** The proxied object. */
    private final XdmDestination myProxied;
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
