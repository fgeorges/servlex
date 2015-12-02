/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.expath.servlex.processors.saxon.tools;

import java.net.URI;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import org.expath.servlex.tools.Log;

/**
 *
 * @author fgeorges
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
