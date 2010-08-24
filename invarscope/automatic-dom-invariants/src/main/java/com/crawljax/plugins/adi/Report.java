/*
    Automatic DOM Invariants is a plugin for Crawljax that can be used to
    derive DOM invariants automatically and use them for regressions
    testing.
    Copyright (C) 2010  crawljax.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.crawljax.plugins.adi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.util.Helper;
import com.crawljax.util.PrettyHTML;
import com.crawljax.util.XPathHelper;

/**
 * Class used by DomInvariantTester to report errors.
 * 
 * @author Frank Groeneveld
 */
public class Report {

	private Document controlDom;
	private Document testDom;
	private List<String[]> failures = new ArrayList<String[]>();

	public static final String[] COLORS =
	        { "#ff0000", "#00ff00", "#0000ff", "#ffff00", "#00ffff", "#ff00ff", "#c0c0c0",
	                "#ff6600", "#99ff00", "#663300", "#336600", "#6633cc", "#ff99ff", "#ff0066", };

	/**
	 * Loads contents of a resource, works also in JARs.
	 * 
	 * @param filename
	 *            The name of the resource.
	 * @return The text inside the file.
	 */
	private String loadTextFromResource(String filename) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
		StringBuilder sb = new StringBuilder();
		String line;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * Save the report as filename.
	 * 
	 * @param filename
	 *            The file to save the report in.
	 */
	public void saveAs(String filename) {

		File stateFile = new File(filename);
		try {
			FileWriter writer = new FileWriter(stateFile, false);

			writer.write(loadTextFromResource("head.html"));
			writer.write(addFailuresToDom(controlDom));
			writer.write(loadTextFromResource("middle.html"));
			writer.write(addFailuresToDom(testDom));
			writer.write(loadTextFromResource("tail.html"));
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* reset failures */
		failures = new ArrayList<String[]>();
	}

	private String addFailuresToDom(Document dom) {

		/* TOOD: this modifies the DOM, it should create a copy */

		for (int i = 0; i < failures.size(); i++) {
			String[] failure = failures.get(i);
			if (failure[1] != null) {
				dom = addMarker("" + i, dom, failure[1]);
			}
			dom = addMarker("" + i, dom, failure[0]);
		}
		String formattedDom = Helper.getDocumentToString(dom);
		formattedDom = PrettyHTML.prettyHTML(formattedDom, "  ");
		formattedDom = StringEscapeUtils.escapeHtml(formattedDom);
		formattedDom = replaceMarkers(formattedDom);

		return formattedDom;
	}

	/**
	 * Add a failure.
	 * 
	 * @param xPathExpression
	 *            First expression.
	 * @param secondXPathExpression
	 *            Second expression.
	 * @param message
	 *            Failure message shown in the report.
	 */
	public void addFailure(String xPathExpression, String secondXPathExpression, String message) {
		failures.add(new String[] { xPathExpression, secondXPathExpression, message });
		System.err.println(message);
	}

	/**
	 * Taken from ErrorReport.
	 */
	private Document addMarker(String id, Document doc, String xpath) {
		try {

			String prefixMarker = "###BEGINMARKER" + id + "###";
			String suffixMarker = "###ENDMARKER###";

			NodeList nodeList = XPathHelper.evaluateXpathExpression(doc, xpath);

			if (nodeList.getLength() == 0 || nodeList.item(0) == null) {
				return doc;
			}

			Node element = nodeList.item(0);

			if (element.getNodeType() == Node.ELEMENT_NODE) {
				Node beginNode = doc.createTextNode(prefixMarker);
				Node endNode = doc.createTextNode(suffixMarker);

				element.getParentNode().insertBefore(beginNode, element);
				if (element.getNextSibling() == null) {
					element.getParentNode().appendChild(endNode);
				} else {
					element.getParentNode().insertBefore(endNode, element.getNextSibling());
				}
			} else if (element.getNodeType() == Node.TEXT_NODE
			        && element.getTextContent() != null) {
				element.setTextContent(prefixMarker + element.getTextContent() + suffixMarker);
			} else if (element.getNodeType() == Node.ATTRIBUTE_NODE) {
				element.setNodeValue(prefixMarker + element.getTextContent() + suffixMarker);
			}

			return doc;
		} catch (Exception e) {
			return doc;
		}
	}

	/**
	 * Taken from ErrorReport.
	 */
	private String replaceMarkers(String dom) {
		for (int i = 0; i < failures.size(); i++) {
			String[] failure = failures.get(i);
			String regexSearch = "\\s*" + "###BEGINMARKER" + i + "###";
			String replace =
			        "<div class=\"difference\" title=\""
			                + StringEscapeUtils.escapeHtml(failure[2])
			                + "\" style=\"background: " + COLORS[i % COLORS.length] + ";\">";
			dom = dom.replaceAll(regexSearch, replace);
		}

		dom = dom.replaceAll("(\\s)*###ENDMARKER###", "</div>");

		return dom;
	}

	/**
	 * Set control DOM to use.
	 * 
	 * @param document
	 *            The control DOM.
	 */
	public void setControlDom(Document document) {
		controlDom = document;
	}

	/**
	 * Set test DOM to use.
	 * 
	 * @param document
	 *            The test DOM.
	 */
	public void setTestDom(Document document) {
		testDom = document;
	}

}
