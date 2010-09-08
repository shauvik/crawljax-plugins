// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.plugins.crossbrowser.statecompare;

/**
 * Data container combining String and the xPath location of the parent of that sting.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class TextNode {

	private final String text;
	private final String xPath;

	/**
	 * Create a new TextNode given its text and its xPath.
	 *
	 * @param text
	 *            the text contents found.
	 * @param xPath
	 *            the location where this text is found.
	 */
	public TextNode(String text, String xPath) {
		this.text = text;
		this.xPath = xPath;
	}

	@Override
	public String toString() {
		return this.text;
	}

	/**
	 * @return the xPath
	 */
	public String getxPath() {
		return xPath;
	}

	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
