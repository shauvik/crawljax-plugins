// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.plugins.crossbrowser.statecompare;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A List implementation based on a {@link ForwardingList} holding {@link DiffTextNodes}. Capable to
 * add elements if needed and building a list given two lists of TextNodes to compare.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class DiffTextNodesList extends ForwardingList<DiffTextNodes> {
	private final List<DiffTextNodes> delegate = Lists.newArrayList();
	@Override
	protected List<DiffTextNodes> delegate() {
		return delegate;
	}

	/**
	 * add a given {@link DiffTextNodes} element to this list if it {@link DiffTextNodes#hasData()}
	 * and return a (new) empty DiffTextNodes.
	 *
	 * @param element
	 *            the element to add to this list.
	 * @return a empty {@link DiffTextNodes}
	 */
	private DiffTextNodes addIfFilledAndCreateNew(DiffTextNodes element) {
		if (element.hasData()) {
			this.add(element);
			return new DiffTextNodes();
		}
		// No Data so return the same instance, saves mem...
		return element;
	}

	/**
	 * Find the differences between two doms, given the list of TextNodes. It use the
	 * {@link diff_match_patch} diffing library.
	 *
	 * @param originalNodes
	 *            the list of TextNodes on the originalDom.
	 * @param currentNodes
	 *            the list of TextNodes on the currentDom.
	 * @return a list of Differences.
	 */
	public static DiffTextNodesList findDifferences(
	        List<TextNode> originalNodes, List<TextNode> currentNodes) {
		diff_match_patch dmp = new diff_match_patch();
		LinkedList<Diff> diffs = dmp.diff_main(
		        DiffTextNodes.makeLine(originalNodes), DiffTextNodes.makeLine(currentNodes));

		DiffTextNodesList result = new DiffTextNodesList();

		boolean equal = true;
		for (Diff diff : diffs) {
			if (diff.operation != diff_match_patch.Operation.EQUAL) {
				equal = false;
				break;
			}
		}

		if (equal) {
			// If all equal return stop the processing
			return result;
		}

		// The iterators are used to keep track between the two lists
		Iterator<TextNode> orgIt = originalNodes.iterator();
		Iterator<TextNode> newIt = currentNodes.iterator();

		// The both nodes are pointers to the current node under process
		TextNode orgTN = null;
		TextNode newTN = null;

		// Catch the corner cases...
		if (orgIt.hasNext()) {
			orgTN = orgIt.next();
		}
		if (newIt.hasNext()) {
			newTN = newIt.next();
		}

		DiffTextNodes diffTmp = new DiffTextNodes();

		String deleteRest = "";
		String insertRest = "";

		for (Diff diff : diffs) {
			String diffText = diff.text;
			if (diff.operation == Operation.EQUAL) {

				// Store the previous results and start a new one.

				diffTmp = result.addIfFilledAndCreateNew(diffTmp);

				// While both nodes are equal and the diff-text start with the text
				while (orgTN.equals(newTN) && diffText.startsWith(orgTN.toString())) {
					// Ok we passed a portion of the diff-text, stip it of
					diffText = diffText.substring(orgTN.toString().length());

					if (!orgIt.hasNext() || !newIt.hasNext()) {
						// break because one of the nodes can not process futher
						break;
					}
					orgTN = orgIt.next();
					newTN = newIt.next();
				}
				if (!diffText.equals("")) {
					// There was some diff-text left, both nodes are affected...
					diffTmp.addCurrent(newTN);
					diffTmp.addOriginal(orgTN);
					diffTmp = result.addIfFilledAndCreateNew(diffTmp);

					if (orgTN.toString().equals(diffText)) {
						// org matches the text left to diff
						if (newIt.hasNext()) {
							// process if possible
							newTN = newIt.next();
						}
						if (orgIt.hasNext()) {
							// process if possible
							orgTN = orgIt.next();
						}
						// Store the part of the diff-text not matched
						insertRest = diffText;
					}
					if (newTN.toString().equals(diffText)) {
						// new matches the text left to diff
						if (newIt.hasNext()) {
							// process if possible
							newTN = newIt.next();
						}
						if (orgIt.hasNext()) {
							// process if possible
							orgTN = orgIt.next();
						}
						// Store the part of the diff-text not matched
						deleteRest = diffText;
					}
				}
			} else if (diff.operation == Operation.INSERT) {
				diffText = insertRest.concat(diff.text);
				// While the diff-text starts with the node text
				while (diffText.startsWith(newTN.toString())) {
					// Record the part that is added
					diffTmp.addCurrent(newTN);
					diffText = diffText.substring(newTN.toString().length());
					if (!newIt.hasNext()) {
						// Stop because no more elements
						break;
					}
					newTN = newIt.next();
				}
			} else if (diff.operation == Operation.DELETE) {
				diffText = deleteRest.concat(diff.text);
				// While the diff-text starts with the node text
				while (diffText.startsWith(orgTN.toString())) {
					// Record the part that was deleted
					diffTmp.addOriginal(orgTN);
					diffText = diffText.substring(orgTN.toString().length());
					if (!orgIt.hasNext()) {
						// Stop because no more elements
						break;
					}
					orgTN = orgIt.next();
				}
			}
		}

		// Did we managed to store everything?
		if (diffTmp.getCurrent().size() != 0 || diffTmp.getOriginal().size() != 0) {
			result.add(diffTmp);
		}

		return result;
	}
}
