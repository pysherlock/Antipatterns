/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cocoon.stax.sample.src;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.stax.AbstractStAXTransformer;
import org.apache.cocoon.stax.navigation.FindStartElementNavigator;
import org.apache.cocoon.stax.navigation.InSubtreeNavigator;
import org.apache.cocoon.stax.navigation.Navigator;

/**
 * Rewrite the links of a Daisy publisher document. It shows the easier state management of StAX
 * components, by having an implicit state through functions, which perform further pulls.
 */
public class DaisyLinkRewriteTransformer extends AbstractStAXTransformer {

    private static final String DEFAULT_DATA_URL = "data/{id}/{filename}";
    private static final String DEFAULT_IMG_URL = "image/{id}/{filename}";
    private static final String LINK_INFO_EL = "linkInfo";
    private static final String LINK_PART_INFO_EL = "linkPartInfo";
    private static final String PUBLISHER_NS = "http://outerx.org/daisy/1.0#publisher";

    private Navigator anchorNavigator = new FindStartElementNavigator("a");
    private String currentPath;
    private String dataUrl = DEFAULT_DATA_URL;

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private Navigator imageNavigator = new FindStartElementNavigator("img");

    private String imageUrl = DEFAULT_IMG_URL;

    public DaisyLinkRewriteTransformer(String currentPath) {
        this.currentPath = currentPath;
    }

    public DaisyLinkRewriteTransformer(String currentPath, String imageUrl, String dataUrl) {
        this(currentPath);

        this.imageUrl = imageUrl;
        this.dataUrl = dataUrl;
    }

    /**
     * Returns the attribute value of a StartElement or an empty String if there is no such
     * attribute.
     * 
     * @param element the StartElement which has the attribute
     * @param attributeName the name of the attribute.
     * @return the value of the attribute or an empty string if there is no such attribute
     */
    protected String getAttributeValue(StartElement element, QName attributeName) {
        Attribute attribute = element.getAttributeByName(attributeName);
        if (attribute == null) {
            return "";
        }
        
        return attribute.getValue();
    }

    protected String getAttributeValue(StartElement element, String attributeName) {
        return this.getAttributeValue(element, new QName(attributeName));
    }

    protected String getLinkForAttachment(LinkInfo linkInfo) {
        return this.relativizePath() + this.replaceVariables(this.dataUrl, linkInfo);
    }

    protected String getLinkForDocument(LinkInfo linkInfo) {
        return this.relativizePath() + "part/WebpagePartGerman/" + linkInfo.getNavigationPath();
    }

    protected String getLinkForImage(LinkInfo linkInfo) {
        return this.relativizePath() + this.replaceVariables(this.imageUrl, linkInfo);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.stax.AbstractStAXTransformer#produceEvents()
     */
    @Override
    protected void produceEvents() throws XMLStreamException {
        while (this.getParent().hasNext()) {
            XMLEvent event = this.getParent().nextEvent();

            if (this.anchorNavigator.fulfillsCriteria(event) || this.imageNavigator.fulfillsCriteria(event)) {
                ArrayList<XMLEvent> innerContent = new ArrayList<XMLEvent>();
                LinkInfo linkInfo = this.collectLinkInfo(innerContent);
                if (linkInfo != null) {
                    linkInfo.setNavigationPath(this.getAttributeValue(event.asStartElement(), PUBLISHER_NS,
                            "navigationPath"));
                    this.rewriteAttributesAndEmitEvent(event.asStartElement(), linkInfo);

                    if (innerContent.size() != 0) {
                        this.addAllEventsToQueue(innerContent);
                    }
                } else {
                    this.addEventToQueue(event);

                    if (innerContent.size() != 0) {
                        this.addAllEventsToQueue(innerContent);
                    }
                }
            } else {
                this.addEventToQueue(event);
                return;
            }
        }
    }

    protected String relativizePath() {
        StringBuilder sb = new StringBuilder();

        int matches = this.countSlashes(this.currentPath);
        for (int i = 0; i < matches; i++) {
            sb.append("../");
        }

        return sb.toString();
    }

    /**
     * Replaces placeholder in the expression with the values of the linkInfo object.
     * 
     * @param expression the expression with placeholders
     * @param linkInfo the linkinfo object with the corresponding values.
     * @return the expression with the correct values.
     */
    protected String replaceVariables(String expression, LinkInfo linkInfo) {
        String result = expression;
        
        result = result.replace("{id}", linkInfo.getDocumentId());
        result = result.replace("{filename}", linkInfo.getFileName());

        return result;
    }

    /**
     * Parses the LinkInfo specific tags and assembles a linkInfo object.
     * 
     * @param events list for caching non link info specific events.
     * @return a linkInfo object
     * @throws XMLStreamException
     */
    private LinkInfo collectLinkInfo(List<XMLEvent> events) throws XMLStreamException {
        Navigator linkInfoNavigator = new InSubtreeNavigator(LINK_INFO_EL);
        Navigator linkInfoPartNavigator = new FindStartElementNavigator(LINK_PART_INFO_EL);
        LinkInfo linkInfo = null;

        while (this.getParent().hasNext()) {
            XMLEvent event = this.getParent().peek();

            if (linkInfoNavigator.fulfillsCriteria(event)) {
                event = this.getParent().nextEvent();
                if (linkInfoPartNavigator.fulfillsCriteria(event)) {
                    if (linkInfo == null) {
                        throw new ProcessingException(new NullPointerException(
                                "The LinkInfo object mustn't be null here."));
                    }

                    String fileName = this.getAttributeValue(event.asStartElement(), "fileName");
                    if (!"".equals(fileName)) {
                        linkInfo.setFileName(fileName);
                    }

                } else if (event.isStartElement()) {
                    linkInfo = new LinkInfo();

                    StartElement linkInfoEvent = event.asStartElement();
                    linkInfo.setHref(this.getAttributeValue(linkInfoEvent, "href"));
                    linkInfo.setSource(this.getAttributeValue(linkInfoEvent, "src"));
                    String documentType = this.getAttributeValue(linkInfoEvent, "documentType");

                    if (documentType.equals("Attachment")) {
                        linkInfo.setLinkInfoType(LinkInfoType.ATTACHEMENT);
                    } else if (documentType.equals("Image")) {
                        linkInfo.setLinkInfoType(LinkInfoType.IMAGE);
                    }
                }
            } else if (event.isCharacters()) {
                events.add(this.getParent().nextEvent());
            } else {
                return linkInfo;
            }
        }
        return linkInfo;
    }

    private int countSlashes(String path) {
        int count = 0;

        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                count++;
            }
        }

        return count;
    }

    /**
     * Creates an attribute from the linkInfo and the localname.
     * 
     * @param localName the name of the tag
     * @param linkInfo the linkInfo, from which the target link is constructed
     * @return the assembled attribute
     */
    private Attribute createAttributeForLinkInfo(String localName, LinkInfo linkInfo) {
        String value = null;

        if (linkInfo.getLinkInfoType() == LinkInfoType.IMAGE) {
            value = this.getLinkForImage(linkInfo);
        } else

        if (linkInfo.getLinkInfoType() == LinkInfoType.ATTACHEMENT) {
            value = this.getLinkForAttachment(linkInfo);
        } else {
            value = this.getLinkForDocument(linkInfo);
        }

        return this.eventFactory.createAttribute(new QName(localName), value);
    }

    private String getAttributeValue(StartElement element, String namespace, String attributeName) {
        return this.getAttributeValue(element, new QName(namespace, attributeName));
    }

    /**
     * Rewrites the attribute of the start element with the information of the linkInfo and emits
     * it.
     * 
     * @param event the start element.
     * @param linkInfo the linkInfo.
     */
    private void rewriteAttributesAndEmitEvent(StartElement event, LinkInfo linkInfo) {
        Iterator<?> oldAttributes = event.getAttributes();
        Set<Attribute> attributes = new HashSet<Attribute>();
        while (oldAttributes.hasNext()) {
            Attribute attribute = (Attribute) oldAttributes.next();
            // remove the linkinfo attributes
            if (PUBLISHER_NS.equals(attribute.getName().getNamespaceURI())) {
                continue;
            }

            String localName = attribute.getName().getLocalPart();
            if (localName.equals("href") || localName.equals("src")) {
                attributes.add(this.createAttributeForLinkInfo(localName, linkInfo));
            } else {
                attributes.add(attribute);
            }

        }
        this.addEventToQueue(this.eventFactory.createStartElement(event.getName(), attributes.iterator(), event
                .getNamespaces()));
    }

    protected static class LinkInfo {

        private String fileName;
        private String href;
        private LinkInfoType linkInfoType = LinkInfoType.MISC;
        private String navigationPath;
        private String source;

        public String getDocumentId() {
            if (this.href != null && this.href.startsWith("daisy:")) {
                return this.href.substring("daisy:".length());
            }

            if (this.source != null && this.source.startsWith("daisy:")) {
                return this.source.substring("daisy:".length());
            }

            return null;
        }

        public String getFileName() {
            return this.fileName;
        }

        public String getHref() {
            return this.href;
        }

        public LinkInfoType getLinkInfoType() {
            return this.linkInfoType;
        }

        public String getNavigationPath() {
            return this.navigationPath;
        }

        public String getSource() {
            return this.source;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public void setLinkInfoType(LinkInfoType linkInfoType) {
            this.linkInfoType = linkInfoType;
        }

        public void setNavigationPath(String navigationPath) {
            if (navigationPath != null && navigationPath.startsWith("/")) {
                this.navigationPath = navigationPath.substring(1);
            } else {
                this.navigationPath = navigationPath;
            }
        }

        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public String toString() {
            return "LinkInfo[documentId=" + this.getDocumentId() + ", navigationPath=" + this.navigationPath
                    + ", source=" + this.source + ", href=" + this.href + ", fileName=" + this.fileName + "]";
        }
    }

    protected static enum LinkInfoType {
        ATTACHEMENT, IMAGE, MISC
    }
}
