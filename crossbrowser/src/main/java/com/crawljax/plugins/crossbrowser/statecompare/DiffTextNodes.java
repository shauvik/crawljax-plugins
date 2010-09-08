// Copyright 2010 Google Inc. All Rights Reserved.
package com.crawljax.plugins.crossbrowser.statecompare;

import com.google.common.collect.Lists;

import com.crawljax.plugins.errorreport.Highlight;

import java.util.Collections;
import java.util.List;

/**
 * A data class representing two ordered list of Nodes. The original list contains all the TextNodes
 * changed from the original dom, while the current list represent the TextNodes changed in the
 * current dom.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class DiffTextNodes {
	private final List<TextNode> original = Lists.newArrayList();
	private final List<TextNode> current = Lists.newArrayList();

	/**
	 * @return the list of changed TextNodes in current.
	 */
	public List<TextNode> getCurrent() {
		return current;
	}

	/**
	 * @return the list of changed TextNodes in original.
	 */
	public List<TextNode> getOriginal() {
		return original;
	}

	/**
	 * Add a TextNode as a node that has changed on the original dom.
	 *
	 * @param textNode
	 *            the node to add as difference on original.
	 */
	void addOriginal(TextNode textNode) {
		this.original.add(textNode);
	}

	/**
	 * Add a TextNode as a node that has changed on the new/current dom.
	 *
	 * @param textNode
	 *            the node to add as difference on current.
	 */
	void addCurrent(TextNode textNode) {
		this.current.add(textNode);
	}

	/**
	 * Transform a list of TextNodes into a single concatenated String.
	 *
	 * @param nodes
	 *            the list to be flattened to string
	 * @return the string containing nodes(0) + nodes(1) + ...
	 */
	public static String makeLine(List<TextNode> nodes) {
		String line = "";
		for (TextNode n : nodes) {
			line += n;
		}
		return line;
	}

	/**
	 * Build a {@link Highlight} from the given Diff.
	 *
	 * @return the highlight used in the report.
	 */
	public Highlight buildHighLight() {
		String description = "";
		String xpathCurrentDom = "";
		String xpathOriginalDom = "";

		for (TextNode n : this.getOriginal()) {
			description += n.toString();
		}
		xpathOriginalDom = findCommonXPath(this.getOriginal());

		description += " <=> ";

		for (TextNode n : this.getCurrent()) {
			description += n.toString();
		}
		xpathCurrentDom = findCommonXPath(this.getCurrent());

		if (xpathCurrentDom.equals("") && !xpathOriginalDom.equals("")) {
			xpathCurrentDom = xpathOriginalDom;
		}
		return new Highlight(description, xpathCurrentDom, xpathOriginalDom);
	}

	/**
	 * Given a List of TextNodes find the xPath they have in common. Not this could result in
	 * '//HTML[1]' if unlucky.
	 *
	 * @param nodes
	 *            the nodes to examine the xPaths of.
	 * @return the commonly shared xPath.
	 */
	private String findCommonXPath(List<TextNode> nodes) {
		if (nodes.size() == 0) {
			return "";
		}
		if (nodes.size() == 1) {
			return nodes.get(0).getxPath();
		}
		List<String> pathList = Lists.newArrayList();
		for (TextNode n : nodes) {
			pathList.add(n.getxPath());
		}
		Collections.sort(pathList);
		char[] xp = pathList.get(0).toCharArray();
		String cmp = "";
		boolean stop = false;
		for (char next : xp) {
			for (String s : pathList) {
				if (!s.startsWith(cmp + next)) {
					stop = true;
					break;
				}
			}
			if (stop) {
				break;
			} else {
				cmp += next;
			}
		}
		if (cmp.length() > 0 && cmp.charAt(cmp.length() - 1) != ']') {
			cmp = cmp.substring(0, cmp.lastIndexOf(']') + 1);
		}
		return cmp;
	}

	/**
	 * Does this DiffTextNodes object contain any value-able information?
	 *
	 * @return true if one of the delegate lists contains a element or more.
	 */
	public boolean hasData() {
		return getCurrent().size() != 0 || getOriginal().size() != 0;
	}
}
