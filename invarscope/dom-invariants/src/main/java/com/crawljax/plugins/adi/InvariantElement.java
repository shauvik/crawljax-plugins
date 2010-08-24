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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.util.XPathHelper;

/**
 * Tree structure node to store XPath expressions of elements and their children.
 * 
 * @author Frank Groeneveld
 */
public class InvariantElement {
	private String query;
	private List<InvariantElement> children = new ArrayList<InvariantElement>();
	private InvariantElement parent;

	private static final double FUZZY_MATCH_TRESHOLD = 0.85;

	/**
	 * Default root constructor.
	 * 
	 * @param q
	 *            The XPath query.
	 */
	public InvariantElement(String q) {
		query = q;
	}

	/**
	 * Default node constructor for intermediate nodes (with parent).
	 * 
	 * @param q
	 *            The XPath query.
	 * @param p
	 *            The parent InvariantElement.
	 */
	public InvariantElement(String q, InvariantElement p) {
		query = q;
		parent = p;
	}

	/**
	 * @return The XPath expression (query).
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Add a child to this node.
	 * 
	 * @param child
	 *            The child.
	 * @return The child that was added.
	 */
	public InvariantElement addChild(InvariantElement child) {
		children.add(child);
		return child;
	}

	/**
	 * @return A list of children.
	 */
	public List<InvariantElement> getChildren() {
		return children;
	}

	/**
	 * @return The parent of this node.
	 */
	public InvariantElement getParent() {
		return parent;
	}

	/**
	 * Check whether this tree structure can be found in the DOM.
	 * 
	 * @param dom
	 *            The DOM structure.
	 * @param previousNodes
	 *            The nodes that should occur before this one in the DOM.
	 * @param report
	 *            The report to add failures to.
	 * @throws XPathExpressionException
	 *             When an error occurs.
	 */
	public void check(Document dom, List<List<Node>> previousNodes, Report report)
	        throws XPathExpressionException {

		List<Node> result = findIn(dom, true);

		if (result.size() == 0) {
			report.addFailure(query, null, "No matching element found: " + query);
		} else {
			for (List<Node> elements : previousNodes) {
				boolean correct = false;
				for (Node previousNode : elements) {
					for (Node currentNode : result) {
						short ret = currentNode.compareDocumentPosition(previousNode);

						/* when the current node is in the correct order */
						if (ret != Node.DOCUMENT_POSITION_FOLLOWING
						        && ret != Node.DOCUMENT_POSITION_CONTAINED_BY) {
							correct = true;
						}
					}
				}
				if (!correct) {
					report.addFailure(query, null, "Element found on the wrong location: "
					        + query);
				}
			}

			previousNodes.add(result);
		}

		/* check children now */
		for (InvariantElement i : children) {
			i.check(dom, previousNodes, report);
		}
	}

	/**
	 * Check whether this tree structure can be found in the DOM. Any failing elements are removed.
	 * 
	 * @param dom
	 *            The DOM structure.
	 * @return The new set.
	 * @throws XPathExpressionException
	 *             When an error occurs.
	 */
	public LinkedHashSet<String> checkAndRemoveFailures(Document dom)
	        throws XPathExpressionException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();

		if (findIn(dom, true).size() != 0) {
			result.add(query);

			/* check children now */
			for (InvariantElement i : children) {
				result.addAll(i.checkAndRemoveFailures(dom));
			}
		}

		return result;
	}

	/**
	 * Helper to convert NodeList to List<Node>.
	 * 
	 * @param nodes
	 *            Nodes to add to the list.
	 * @return List with the nodes.
	 */
	private List<Node> nodeListToList(NodeList nodes) {
		ArrayList<Node> list = new ArrayList<Node>();
		for (int i = 0; i < nodes.getLength(); i++) {
			list.add(nodes.item(i));
		}
		return list;
	}

	/**
	 * Helper to convert Node to List<Node>.
	 * 
	 * @param node
	 *            The node to add to the list.
	 * @return List with the node.
	 */
	private List<Node> nodeToList(Node node) {
		ArrayList<Node> list = new ArrayList<Node>();
		list.add(node);
		return list;
	}

	private List<Node> findIn(Document dom, boolean checkChildren)
	        throws XPathExpressionException {
		List<Node> result;

		NodeList nodes = XPathHelper.evaluateXpathExpression(dom, query);

		if (nodes.getLength() > 0) {
			result = nodeListToList(nodes);
		} else {

			result = fuzzyFind(dom, query);
		}

		/* we found the element, now check the children */

		if (checkChildren) {
			if (children.size() > 0) {
				for (InvariantElement child : children) {
					List<Node> foundChildren = child.findIn(dom, false);

					if (foundChildren.size() == 0) {
						/* not even a child could be found, fail hard */
						return new ArrayList<Node>();

					} else {
						/* check the parents of all these children */

						/* remove attributes from xpath */
						String lookingFor = query.replaceAll("\\[.*\\]", "");
						/* remove prepend stuff from xpath */
						lookingFor = lookingFor.replaceAll(".*//", "");

						for (Node n : foundChildren) {

							if (n.getParentNode().getNodeName().equals(lookingFor)) {
								/* parent is also same type as we're looking for */
								result.add(n.getParentNode());
								/*
								 * we don't stop here, because there could be another parent that is
								 * actually our element
								 */
							}
						}
					}
				}

			}
		}
		return result;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * To string function that includes indentation.
	 * 
	 * @param indent
	 *            Number of indentations before this element.
	 * @return String.
	 */
	public String toString(int indent) {
		String result = query.replace("\t", "") + "\n";

		for (int i = 0; i < indent; i++) {
			result = "\t" + result;
		}

		for (InvariantElement child : children) {
			result += child.toString(indent + 1);
		}
		return result;
	}

	/**
	 * Try to fuzzily match an XPath expression and a DOM.
	 * 
	 * @param dom
	 *            The DOM document.
	 * @param thisXPath
	 *            The XPath expression
	 * @return The nodes that match.
	 */
	private List<Node> fuzzyFind(Document dom, String thisXPath) {
		try {
			String xPathWithoutAttributes = thisXPath.replaceAll("\\[.*\\]", "");

			NodeList nodes = XPathHelper.evaluateXpathExpression(dom, xPathWithoutAttributes);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String otherXPath =
				        DomInvariantFinder.getDetailedXPathExpression(node, true, false);

				/* get the attributes from the string */
				Map<String, String> thisAttributes = attributesFromXPath(thisXPath.toLowerCase());
				Map<String, String> otherAttributes =
				        attributesFromXPath(otherXPath.toLowerCase());

				float match = 0;
				for (String key : thisAttributes.keySet()) {
					String value = thisAttributes.get(key);

					String otherValue = otherAttributes.get(key);

					/* key should be equal, but for class/id we make an exception */
					if (otherValue == null) {
						if (key.equals("id")) {
							otherValue = otherAttributes.get("class");
						} else if (key.equals("class")) {
							otherValue = otherAttributes.get("id");
						}
					}

					if (otherValue != null) {
						match++;

						if (otherValue == value) {
							match++;
						} else if (!value.equals("") && !otherValue.equals("")) {

							match +=
							        sorensenIndex(orderedMatchingChars(value, otherValue), value,
							                otherValue);
						}
					}
				}

				if (match >= thisAttributes.size() * 2 * FUZZY_MATCH_TRESHOLD) {
					// System.err.println("Fuzzy match between " + thisXPath + " and " +
					// otherXPath);
					return nodeToList(node);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<Node>();
	}

	/**
	 * Calculates the Sørensen index. } else { return result;
	 * 
	 * @param match
	 *            Total number of matching characters.
	 * @param first
	 *            First (original) string.
	 * @param second
	 *            Second (test) string.
	 * @return Sørensen index.
	 */
	private float sorensenIndex(int match, String first, String second) {
		return (match * 2) / (float) (first.length() + second.length());
	}

	/**
	 * Builds a list of attributes from an xpath expression. These attributes are from the last
	 * element in the xPath.
	 * 
	 * @param xPath
	 *            The expression to get the attributes from.
	 * @return List of key value attributes.
	 */
	private Map<String, String> attributesFromXPath(String xPath) {
		HashMap<String, String> result = new HashMap<String, String>();

		if (!xPath.contains("[")) {
			return result;
		}

		/* TODO: Do not hardcode this to be the last element */
		xPath = xPath.substring(xPath.lastIndexOf('[') + 1, xPath.lastIndexOf(']'));

		/* FIXME: This is dangerous when the contents of an attribute contain this word as well */
		String[] attributes = xPath.split(" and ");

		for (String attrib : attributes) {
			/* start at 1 to skip @ */
			String key = attrib.substring(1, attrib.indexOf('='));

			/* FIXME: part of the previous fixme */
			if (attrib.lastIndexOf('"') != -1) {
				String value = attrib.substring(attrib.indexOf('"') + 1, attrib.lastIndexOf('"'));

				result.put(key, value);

			}
		}

		return result;
	}

	/**
	 * Count number of matching characters in same sequence. For example: "abc" and "a1b2" have 2
	 * ("ab") but "abc" and "b1a2" have 1 ("a"). After implementing this I found out it has some
	 * similarities with Levensthein distance, but you can't say they're the same.
	 * 
	 * @param first
	 *            First string.
	 * @param second
	 *            Second string.
	 * @return Number of characters
	 */
	private int orderedMatchingChars(String first, String second) {
		int total = 0;
		int nextNewJ = 0;

		for (int i = 0; i < first.length(); i++) {
			for (int j = nextNewJ; j < second.length(); j++) {

				/* count if we have a match and in the next iteration start at j + 1 (and i + 1) */
				if (first.charAt(i) == second.charAt(j)) {
					total++;

					nextNewJ = j + 1;
					break;
				}
			}
		}

		return total;
	}

	private static int countTabs(String s) {
		int result = 0;
		while (s.indexOf('\t') != -1) {
			s = s.substring(s.indexOf('\t') + 1);
			result++;
		}
		return result;
	}

	/**
	 * Parse the invariants from a string into a tree structure.
	 * 
	 * @param invariants
	 *            The invariants as string array.
	 * @return The root of the invariant tree.
	 */
	public static InvariantElement parseInvariants(String[] invariants) {
		InvariantElement root = null;
		InvariantElement previous = null;
		int curIndent = 1;

		for (String invariant : invariants) {

			/* first node? */
			if (root == null) {
				root = new InvariantElement(invariant);
				previous = root;
			} else {
				/* if this invariant has more indentation */

				if (curIndent < countTabs(invariant)) {
					/*
					 * TODO handle elements without parents (because of multiple indent diffs with
					 * previous
					 */
					previous = previous.addChild(new InvariantElement(invariant, previous));

				} else if (curIndent == countTabs(invariant)) {
					previous =
					        previous.getParent().addChild(
					                new InvariantElement(invariant, previous.getParent()));
				} else {

					/* this invariant has less indentation */
					int indentDiff = curIndent - countTabs(invariant);

					/* go up the number of indents we differ */
					for (int i = 0; i < indentDiff; i++) {
						previous = previous.getParent();
					}

					previous = previous.addChild(new InvariantElement(invariant, previous));

				}

			}
			curIndent = countTabs(invariant);
		}

		return root;
	}
}
