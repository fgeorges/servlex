/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.expath.servlex.processors.saxon.tools;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import org.expath.servlex.tools.Log;

/**
 *
 * @author fgeorges
 */
public class ReceiverLogProxy
        implements Receiver
{
    public ReceiverLogProxy(Receiver proxied) {
        myProxied = proxied;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pc) {
        LOG.error("setPipelineConfiguration");
        myProxied.setPipelineConfiguration(pc);
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        LOG.error("getPipelineConfiguration");
        return myProxied.getPipelineConfiguration();
    }

    @Override
    public void setSystemId(String id) {
        LOG.error("setSystemId: " + id);
        myProxied.setSystemId(id);
    }

    @Override
    public void open() throws XPathException {
        LOG.error("open");
        myProxied.open();
    }

    @Override
    public void startDocument(int i) throws XPathException {
        LOG.error("startDocument: " + i);
        myProxied.startDocument(i);
    }

    @Override
    public void endDocument() throws XPathException {
        LOG.error("endDocument");
        myProxied.endDocument();
    }

    @Override
    public void setUnparsedEntity(String name, String sys, String pub) throws XPathException {
        LOG.error("setUnparsedEntity: " + name + ", " + sys + ", " + pub);
        myProxied.setUnparsedEntity(name, sys, pub);
    }

    @Override
    public void startElement(NodeName name, SchemaType type, int loc, int props) throws XPathException {
        LOG.error("startElement: " + name + ", " + type + ", " + loc + ", " + props);
        myProxied.startElement(name, type, loc, props);
    }

    @Override
    public void namespace(NamespaceBinding binding, int props) throws XPathException {
        LOG.error("namespace: " + binding + ", " + props);
        myProxied.namespace(binding, props);
    }

    @Override
    public void attribute(NodeName name, SimpleType type, CharSequence value, int loc, int props) throws XPathException {
        LOG.error("attribute: " + name + ", " + type + ", " + value + ", " + loc + ", " + props);
        myProxied.attribute(name, type, value, loc, props);
    }

    @Override
    public void startContent() throws XPathException {
        LOG.error("startContent");
        myProxied.startContent();
    }

    @Override
    public void endElement() throws XPathException {
        LOG.error("endElement");
        myProxied.endElement();
    }

    @Override
    public void characters(CharSequence chars, int loc, int props) throws XPathException {
        LOG.error("characters: " + chars + ", " + loc + ", " + props);
        myProxied.characters(chars, loc, props);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, int loc, int props) throws XPathException {
        LOG.error("processingInstruction: " + name + ", " + data + ", " + loc + ", " + props);
        myProxied.processingInstruction(name, data, loc, props);
    }

    @Override
    public void comment(CharSequence content, int loc, int props) throws XPathException {
        LOG.error("comment: " + content + ", " + loc + ", " + props);
        myProxied.comment(content, loc, props);
    }

    @Override
    public void close() throws XPathException {
        LOG.error("close");
        myProxied.close();
    }

    @Override
    public boolean usesTypeAnnotations() {
        LOG.error("usesTypeAnnotations");
        return myProxied.usesTypeAnnotations();
    }

    @Override
    public String getSystemId() {
        LOG.error("getSystemId");
        return myProxied.getSystemId();
    }

    /** The logger. */
    private static final Log LOG = new Log(ReceiverLogProxy.class);

    /** The proxied object. */
    private final Receiver myProxied;
}
