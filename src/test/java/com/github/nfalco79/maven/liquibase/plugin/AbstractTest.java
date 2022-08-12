/*
 * Copyright 2022 Nikolas Falco
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.nfalco79.maven.liquibase.plugin;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class AbstractTest {

    @Rule
    public TemporaryFolder fileRule = new TemporaryFolder();

    protected static Element createDocument() throws ParserConfigurationException {
        Document document = DocumentHelper.createDocument();
        Element child = document.addElement("databaseChangeLog");
        Namespace ns = new Namespace("nfalco79", "http://www.liquibase.org/xml/ns/dbchangelog-ext/nfalco79");
        child.add(ns);
        child = child.addElement("changeSet");
        return child;
    }

    protected static Element addChild(Node parent, String childName, Map<String, String> childAttributes) {
        Element child = ((Element) parent).addElement(childName);
        for (String attKey : childAttributes.keySet()) {
            child.addAttribute(attKey, childAttributes.get(attKey));
        }
        return child;
    }

}
