// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.plugins.crossbrowser.statecompare;

import com.google.common.collect.Lists;

import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This class is able to load a List of {@link TextNode} from a String.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public final class TextNodeLoader {

    private static final int SPACE_INT_VAL = 32;
    private static final int NBSP_INT_VAL = 160;

	/**
	 * Utility class; empty constructor.
	 */
	private TextNodeLoader() {

	}

	/**
	 * Parse a string to a list of {@link TextNode}.
	 *
	 * @param dom
	 *            the dom to parse
	 * @return the list of bare-text.
	 */
	public static List<TextNode> stripDom(String dom) {
		List<TextNode> result = Lists.newArrayList();

		Document doc;
		try {
			doc = Helper.getDocument(dom);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			Object resultObject;
			try {
				XPathExpression expr = xpath.compile("//*[@id='content']");
				resultObject = expr.evaluate(doc, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				return result;
			}
			if (resultObject instanceof NodeList) {
				NodeList nodes = (NodeList) resultObject;
				if (nodes.getLength() > 0) {
					for (int i = 0; i < nodes.getLength(); i++) {
						result.addAll(goDeeper(nodes.item(i)));
					}
					return result;
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static Collection<? extends TextNode> goDeeper(Node n) {
		if (n.getNodeType() == Node.TEXT_NODE) {
			assert (!n.hasChildNodes());
			String s = n.getNodeValue();
			// TODO This is real hacky to replace char 160 (&nbsp;) with 32 (' ')
			s =
			        s.trim().replace((char) NBSP_INT_VAL, (char) SPACE_INT_VAL).trim().replaceAll(
			                "\t", "").replace('?', ' ').replaceAll(" ", "");
			if (s == null || s.equals("\n") || s.equals("")) {
				return Lists.newArrayList();
			}
			return Lists.newArrayList(
			        new TextNode(s, XPathHelper.getXPathExpression(n.getParentNode())));
		} else {
			NodeList nl = n.getChildNodes();
			List<TextNode> result = Lists.newArrayList();
			for (int i = 0; i < nl.getLength(); i++) {
				result.addAll(goDeeper(nl.item(i)));
			}
			return result;
		}
	}
}
