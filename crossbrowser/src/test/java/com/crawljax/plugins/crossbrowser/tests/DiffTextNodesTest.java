// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.plugins.crossbrowser.tests;

import com.google.common.collect.Lists;

import com.crawljax.plugins.crossbrowser.statecompare.DiffTextNodes;
import com.crawljax.plugins.crossbrowser.statecompare.DiffTextNodesList;
import com.crawljax.plugins.crossbrowser.statecompare.TextNode;

import junit.framework.Assert;

import org.junit.Test;

import java.util.List;

/**
 * This class tests the DiffTextNodesList and especially the findDifferences call.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class DiffTextNodesTest {

	/**
	 * Feed two empty lists; retult empty list.
	 */
	@Test
	public void twoEmptyLists() {
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(
		        Lists.<TextNode> newArrayList(), Lists.<TextNode> newArrayList());
		Assert.assertNotNull(nodes);
		Assert.assertTrue(nodes.isEmpty());
	}

	/**
	 * Feed 1 item and a empty list; retult not empty list but that item.
	 */
	@Test
	public void newNodesEmptyList() {
		List<TextNode> tnLsit = Lists.newArrayList(new TextNode("abc", "/some/xpath"));
		List<DiffTextNodes> nodes =
		        DiffTextNodesList.findDifferences(tnLsit, Lists.<TextNode> newArrayList());
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertTrue(nodes.get(0).getCurrent().isEmpty());
		Assert.assertNotNull(nodes.get(0).getOriginal());
		Assert.assertEquals(nodes.get(0).getOriginal().get(0), tnLsit.get(0));
	}

	/**
	 * same as {@link #newNodesEmptyList()} but reversed.
	 */
	@Test
	public void originalNodesEmptyList() {
		List<TextNode> tnLsit = Lists.newArrayList(new TextNode("abc", "/some/xpath"));
		List<DiffTextNodes> nodes =
		        DiffTextNodesList.findDifferences(Lists.<TextNode> newArrayList(), tnLsit);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertNotNull(nodes.get(0).getCurrent());
		Assert.assertTrue(nodes.get(0).getOriginal().isEmpty());
		Assert.assertEquals(nodes.get(0).getCurrent().get(0), tnLsit.get(0));
	}

	/**
	 * What happens with identical objects; Nothing.
	 */
	@Test
	public void noDifferenceSameXpath() {
		List<TextNode> tnLsit = Lists.newArrayList(new TextNode("abc", "/some/xpath"));
		List<TextNode> tnLsit2 = Lists.newArrayList(new TextNode("abc", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertTrue(nodes.isEmpty());
	}

	/**
	 * Text identical xapth differ; no result.
	 */
	@Test
	public void noDifferenceDifferentXpath() {
		List<TextNode> tnLsit = Lists.newArrayList(new TextNode("abc", "/some/xpath"));
		List<TextNode> tnLsit2 = Lists.newArrayList(new TextNode("abc", "/some2/xpath2"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertTrue(nodes.isEmpty());
	}

	/**
	 * there is a total difference; 1 list with 1 item.
	 */
	@Test
	public void singleDifferenceOneNode() {
		List<TextNode> tnLsit = Lists.newArrayList(new TextNode("abc", "/some/xpath"));
		List<TextNode> tnLsit2 = Lists.newArrayList(new TextNode("def", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertEquals(nodes.get(0).getCurrent().get(0), tnLsit2.get(0));
		Assert.assertEquals(nodes.get(0).getOriginal().get(0), tnLsit.get(0));
	}
	
	/**
	 * Find the extra node.
	 */
	@Test
	public void extraNodeInOriginal() {
		List<TextNode> tnLsit =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<TextNode> tnLsit2 = Lists.newArrayList(
		        new TextNode("abc", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertTrue(nodes.get(0).getCurrent().isEmpty());
		Assert.assertEquals(nodes.get(0).getOriginal().get(0).toString(), "ghj");
	}

	/**
	 * same as {@link #extraNodeInOriginal()} reversed.
	 */
	@Test
	public void extraNodeInCurrent() {
		List<TextNode> tnLsit = Lists.newArrayList(
		        new TextNode("abc", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<TextNode> tnLsit2 =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertTrue(nodes.get(0).getOriginal().isEmpty());
		Assert.assertEquals(nodes.get(0).getCurrent().get(0).toString(), "ghj");
	}

	/**
	 * Find the two extra nodes.
	 */
	@Test
	public void twoExtraNodeInCurrent() {
		List<TextNode> tnLsit = Lists.newArrayList(
		        new TextNode("abc", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<TextNode> tnLsit2 =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("rte", "/some/xpath"),
		                new TextNode("def", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertTrue(nodes.get(0).getOriginal().isEmpty());
		Assert.assertEquals(nodes.get(0).getCurrent().get(0).toString(), "ghj");
		Assert.assertEquals(nodes.get(0).getCurrent().get(1).toString(), "rte");
	}

	/**
	 * same as {@link #twoExtraNodeInCurrent()}.
	 */
	@Test
	public void twoExtraNodeInOriginal() {
		List<TextNode> tnLsit =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("rte", "/some/xpath"),
		                new TextNode("def", "/some/xpath"));
		List<TextNode> tnLsit2 = Lists.newArrayList(
		        new TextNode("abc", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertTrue(nodes.get(0).getCurrent().isEmpty());
		Assert.assertEquals(nodes.get(0).getOriginal().get(0).toString(), "ghj");
		Assert.assertEquals(nodes.get(0).getOriginal().get(1).toString(), "rte");
	}

	/**
	 * Try a typical diff example.
	 */
	@Test
	public void typicalDiff() {
		List<TextNode> tnLsit =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("opl", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<TextNode> tnLsit2 =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("def", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertEquals(nodes.get(0).getCurrent().get(0).toString(), "ghj");
		Assert.assertEquals(nodes.get(0).getOriginal().get(0).toString(), "opl");
	}

	/**
	 * Typical diff over multiple nodes.
	 */
	@Test
	public void typicalDiffMultiNode() {
		List<TextNode> tnLsit =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("opl", "/some/xpath"), new TextNode("def", "/some/xpath"),
		                new TextNode("yyy", "/some/xpath"));
		List<TextNode> tnLsit2 =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("def", "/some/xpath"),
		                new TextNode("zzz", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 2);
		Assert.assertEquals(nodes.get(0).getCurrent().get(0).toString(), "ghj");
		Assert.assertEquals(nodes.get(0).getOriginal().get(0).toString(), "opl");
		Assert.assertEquals(nodes.get(1).getCurrent().get(0).toString(), "zzz");
		Assert.assertEquals(nodes.get(1).getOriginal().get(0).toString(), "yyy");
	}

	/**
	 * Typical diff one node contains more text.
	 */
	@Test
	public void typicalDiffMultiTextLength() {
		List<TextNode> tnLsit = Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		        new TextNode("ghj1234", "/some/xpath"), new TextNode("zzz", "/some/xpath"));
		List<TextNode> tnLsit2 =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("zzz", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 1);
		Assert.assertEquals(nodes.get(0).getCurrent().get(0).toString(), "ghj");
		Assert.assertEquals(nodes.get(0).getOriginal().get(0).toString(), "ghj1234");
	}

	/**
	 * What happened when same text overflows nodes?
	 */
	@Test
	public void typicalDiffMultiTextLengthSpanningTwo() {
		List<TextNode> tnLsit =
		        Lists.newArrayList(new TextNode("abc", "/some/xpath"),
		                new TextNode("dgh", "/some/xpath"), new TextNode("jzzz", "/some/xpath"));
		List<TextNode> tnLsit2 =
		        Lists.newArrayList(new TextNode("abd", "/some/xpath"),
		                new TextNode("ghj", "/some/xpath"), new TextNode("zzz", "/some/xpath"));
		List<DiffTextNodes> nodes = DiffTextNodesList.findDifferences(tnLsit, tnLsit2);
		Assert.assertNotNull(nodes);
		Assert.assertFalse(nodes.isEmpty());
		Assert.assertEquals(nodes.size(), 2);
		Assert.assertEquals(nodes.get(0).getCurrent().get(0).toString(), "abd");
		Assert.assertEquals(nodes.get(0).getOriginal().get(0).toString(), "abc");

		// Assert.assertEquals(nodes.get(1).getCurrent().toString(), "ghj");
		Assert.assertEquals(nodes.get(1).getOriginal().get(0).toString(), "abc");

		// Assert.assertEquals(nodes.get(2).getCurrent().toString(), "zzz");
		// Assert.assertEquals(nodes.get(2).getOriginal().toString(), "jzzz");
	}

}
