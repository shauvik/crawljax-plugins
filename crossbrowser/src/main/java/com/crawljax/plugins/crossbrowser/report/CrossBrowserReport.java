// Copyright 2010 Google Inc. All Rights Reserved.
package com.crawljax.plugins.crossbrowser.report;

import com.google.common.collect.Lists;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;
import com.crawljax.plugins.crossbrowser.statecompare.DiffTextNodes;
import com.crawljax.plugins.crossbrowser.statecompare.DiffTextNodesList;
import com.crawljax.plugins.crossbrowser.statecompare.TextNode;
import com.crawljax.plugins.errorreport.ErrorReport;
import com.crawljax.plugins.errorreport.Highlight;
import com.crawljax.plugins.errorreport.ReportError;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * This is a Wrapper around the {@link ErrorReport} class to support the needs for
 * CrossBrowserTesting.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class CrossBrowserReport {
	private static final Logger LOGGER = Logger.getLogger(CrossBrowserReport.class);
	
	private final ErrorReport baseReport;

	/**
	 * Create a new CrossBrowserReport based on a current ErrorReport.
	 *
	 * @param report
	 *            the base of this report.
	 */
	public CrossBrowserReport(ErrorReport report) {
		this.baseReport = report;
	}

	/**
	 * Add a Failure when a difference between states is found.
	 *
	 * @param currentDom
	 *            the dom string representing the browser under test.
	 * @param originalState
	 *            the StateVertex retrieved by the record browser.
	 * @param pathToFailure
	 *            the pathTaken that leads to this failure.
	 * @param browser
	 *            the browser instance of the Browser under test.
	 * @param originalList
	 *            the List of textNodes containing the text representation of the original browser.
	 * @param newList
	 *            the List of textNodes containing the text representation of the browser under
	 *            test.
	 */
	public void addStateFailure(String currentDom, StateVertix originalState,
	        List<Eventable> pathToFailure, EmbeddedBrowser browser, List<TextNode> originalList,
	        List<TextNode> newList) {

		DiffTextNodesList diff = DiffTextNodesList.findDifferences(originalList, newList);

		List<Highlight> highlights = Lists.newArrayList();
		for (DiffTextNodes d : diff) {
			highlights.add(d.buildHighLight());
		}

		baseReport.addFailure(
		        new ReportError("State Differences",
		                originalState.getName() + " (" + diff.size() + ")").withPathToFailure(
		                pathToFailure).withHighlights(highlights).includeOriginalState(
		                originalState).useDomInSteadOfBrowserDom(currentDom), browser);
	}

	/**
	 * Add a failure when a certain path can not be replayed by the current browser.
	 *
	 * @param eventable
	 *            the eventable causing the eventFailure.
	 * @param pathToFailure
	 *            the path which leads to this failure.
	 * @param browser
	 *            the current browser under test.
	 * @throws CrawljaxException
	 *             when the source state from the eventable can not be found.
	 */
	public void addEventFailure(
	        Eventable eventable, List<Eventable> pathToFailure, EmbeddedBrowser browser)
	        throws CrawljaxException {
		baseReport.addEventFailure(
		        eventable, eventable.getSourceStateVertix(), pathToFailure, browser);
	}

	/**
	 * Pass the generate request to the ErrorReport, see {@link ErrorReport#generate()}.
	 */
	public void genereate() {
		try {
			baseReport.generate();
		} catch (IOException e) {
			//Not re-throwing as Runtime as other plugins might succeed.
			LOGGER.error("Could not generate ErrorReport as the base report throwed exception: "
			        + e.getMessage(), e);
		}
	}
}
