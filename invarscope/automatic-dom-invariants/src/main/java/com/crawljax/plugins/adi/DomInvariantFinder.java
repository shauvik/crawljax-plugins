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

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.util.Helper;
import com.crawljax.util.PrettyHTML;
import com.crawljax.util.XPathHelper;

/**
 * This plugin is used to capture all DOMs encountered when crawling a web application. The output
 * can be used to find invariants over these DOMs.
 * 
 * @author Frank Groeneveld
 */
public class DomInvariantFinder implements OnNewStatePlugin, GeneratesOutput {

	private String outputFolder = "";

	/*
	 * LinkedHashSet, because we want unique contents, but also insertion-order (not possible with
	 * HashSet or TreeSet)
	 */
	private LinkedHashSet<String> invariant;

	private static String getAttributeExpression(Node node) {
		StringBuffer buffer = new StringBuffer();

		NamedNodeMap attribs = node.getAttributes();
		if (attribs.getLength() > 0) {
			buffer.append("[");
			for (int j = 0; j < attribs.getLength(); j++) {
				Node attrib = attribs.item(j);

				if (attrib == null) {
					continue;
				}

				if (j != 0) {
					buffer.append(" and ");
				}

				buffer.append("@" + attrib.getNodeName() + "=\"");
				/* TODO: verify that &quot; works */
				buffer.append(attrib.getNodeValue().replaceAll("\"", "&quot;").replace("/",
				        "&47;")
				        + "\"");
			}
			buffer.append("]");
		}

		return buffer.toString();
	}

	/**
	 * Reverse Engineers an XPath Expression of a given Node in the DOM. This method is more
	 * specific than getXpathExpression because it also adds the attributes of the nodes to the
	 * expression.
	 * 
	 * @param node
	 *            the given node.
	 * @param includeAttributes
	 *            Whether or not attributes should be added to the xpath expression.
	 * @param includeNumbers
	 *            Whether or not numbers should be added to the xpath expression.
	 * @return string xpath expression (e.g.,
	 *         "/html[1]/body[1][@class="content"]/div[3][@class="sidebar"]").
	 */
	public static String getDetailedXPathExpression(Node node, boolean includeAttributes,
	        boolean includeNumbers) {

		Node parent = node.getParentNode();

		if ((parent == null) || parent.getNodeName().contains("#document")) {
			String result = "\t//" + node.getNodeName();
			if (includeNumbers) {
				result += "[1]";
			}
			if (includeAttributes) {
				result += getAttributeExpression(node);
			}
			return result;
		}

		StringBuffer buffer = new StringBuffer();

		if (parent != node) {
			/* done recursing down to the parents */
			buffer.append("//");
		}

		buffer.append(node.getNodeName());

		List<Node> mySiblings = XPathHelper.getSiblings(parent, node);

		if (includeNumbers) {
			/* walk all sibling to find out which number (order) we are */
			for (int i = 0; i < mySiblings.size(); i++) {
				Node el = mySiblings.get(i);

				if (el.equals(node)) {
					buffer.append("[");
					buffer.append(Integer.toString(i + 1));
					buffer.append("]");
				}
			}
		}

		if (includeAttributes) {
			buffer.append(getAttributeExpression(node));
		}

		while (node.getParentNode() != null) {
			buffer.insert(0, "\t");
			node = node.getParentNode();
		}
		return buffer.toString();
	}

	/**
	 * Default constructor.
	 */
	public DomInvariantFinder() {

		invariant = new LinkedHashSet<String>();
	}

	private void recurse(NodeList nodes) throws IOException {
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				Element e = (Element) nodes.item(i);

				invariant.add(getDetailedXPathExpression(e, true, false));

				recurse(e.getChildNodes());
			}
		}
	}

	private LinkedHashSet<String> test(Document dom, Set<String> invariant) {
		String[] invariants = invariant.toArray(new String[] {});
		InvariantElement tree = InvariantElement.parseInvariants(invariants);

		try {
			return tree.checkAndRemoveFailures(dom);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return new LinkedHashSet<String>();
	}

	private String setToString(Set<String> list) {
		StringBuffer buffer = new StringBuffer();

		for (String s : list) {
			buffer.append(s);
			buffer.append("\n");
		}

		return buffer.toString();
	}

	@Override
	public void onNewState(CrawlSession session) {
		try {

			String hash = getCrawlPathHash(session.getExactEventPath());

			Document dom = session.getCurrentState().getDocument();

			File file = new File(outputFolder + "inv-" + hash + ".txt");
			if (file.exists()) {
				/* existing invariants */
				String content = Helper.getContent(file);
				invariant = new LinkedHashSet<String>(Arrays.asList(content.split("\n")));
			} else {
				/* new */
				invariant = new LinkedHashSet<String>();
				recurse(dom.getChildNodes());
			}

			invariant = test(dom, invariant);

			Helper.writeToFile(getOutputFolder() + "inv-" + hash + ".txt",
			        setToString(invariant), false);

			Helper.writeToFile(getOutputFolder() + "dom-" + hash + ".txt", PrettyHTML.prettyHTML(
			        Helper.getDocumentToString(dom), "  "), false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calculate a hash of the clickpath.
	 * 
	 * @param list
	 *            The clickpath.
	 * @return The hash.
	 */
	public static String getCrawlPathHash(List<Eventable> list) {
		String crawlPath = "";
		String result = "";

		if (list != null) {
			for (Eventable e : list) {
				crawlPath += e.toString();
			}
		}

		MessageDigest md;

		try {
			md = MessageDigest.getInstance("SHA");

			md.update(crawlPath.getBytes());
			byte[] digest = md.digest();

			for (byte b : digest) {
				result += String.format("%02x", b & 0xff);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public String getOutputFolder() {
		return outputFolder;
	}

	@Override
	public void setOutputFolder(String absolutePath) {
		outputFolder = Helper.addFolderSlashIfNeeded(absolutePath);
		try {
			Helper.directoryCheck(outputFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
