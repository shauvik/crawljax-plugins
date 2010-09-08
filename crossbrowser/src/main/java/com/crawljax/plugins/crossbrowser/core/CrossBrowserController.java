// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.plugins.crossbrowser.core;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;

/**
 * An extended version of {@link CrawljaxController} enabling CrossBrowserCrawling. Use
 * {@link #doCrossBrowserCrawl(StateFlowGraph, Collection, StateVertix)} as the main function to
 * start the crawling. Caution: do NOT invoke {@link CrawljaxController#run()} because that will
 * take off a 'normal' Crawl.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class CrossBrowserController extends CrawljaxController {

  private static final Logger LOGGER = Logger.getLogger(CrossBrowserController.class);

	/**
	 * Build a new CrossBrowserController.
	 *
	 * @param config
	 *            the configuration to load into the parent {@link CrawljaxController}.
	 * @throws ConfigurationException
	 *             when {@link CrawljaxController} throws an exception. TODO this is bad it exposes
	 *             the {@link ConfigurationException} to the outside world and making it part of our
	 *             api.
	 */
  public CrossBrowserController(CrawljaxConfiguration config) throws ConfigurationException {
    super(config);
  }

  /**
	 * initialize the current used controller. 1. build the index state, 2. create a new
	 * CrawlSession from the new index state and the golden standard, 3. run revisitedStatesPlugins
	 * on the new found index.
	 *
	 * @param golderStandard
	 *            the SFG containing all the original states & edges.
	 * @param goldenIndexState
	 *            the golden/original index state.
	 */
  private void initializeController(StateFlowGraph golderStandard, StateVertix goldenIndexState) {
    EmbeddedBrowser browser = null;
    try {
      browser = getBrowserPool().requestBrowser();
    } catch (InterruptedException e) {
      LOGGER.error("The request for a browser was interuped", e);
    }

    /**
     * Go to the initial URL
     */
    try {
      browser.goToUrl(this.getConfigurationReader().getCrawlSpecificationReader().getSiteUrl());
      doBrowserWait(browser);
    } catch (CrawljaxException e) {
      LOGGER.fatal("Failed to load the site: " + e.getMessage(), e);
    }

    /**
     * Build the index state
     */
    StateVertix indexState = null;
    try {
      indexState = new StateVertix(
          browser.getCurrentUrl(), "index", browser.getDom(), getStrippedDom(browser));
    } catch (CrawljaxException e) {
      LOGGER.error("Can not build the index state due to a CrawljaxException", e);
    }

    /**
     * Build the CrawlSession
     */
    CrawlSession session = new CrawlSession(
        getBrowserPool(), golderStandard, indexState, getStartCrawl(), getConfigurationReader());
    setSession(session);
    
    // Because of new Index must be checked run the onRevisitedStatesPlugins
    CrawljaxPluginsUtil.runOnRevisitStatePlugins(session, goldenIndexState);

    // Release the browser
    this.getBrowserPool().freeBrowser(browser);
  }

	/**
	 * Perform a crossBrowser crawl. First the goldenStandard is loaded into the current controller,
	 * the session is created, browser etc. loaded. Than the index state is index state is compared
	 * after that all the eventPaths are loaded into {@link CrossBrowserCrawler} and the crawlers
	 * are added to the queue. If the 'cross'-browser supports multi-threaded multiple threads will
	 * be used.
	 *
	 * @param goldenStandard
	 *            the SFG containing all the original states & edges.
	 * @param paths
	 *            the paths to examine cross-browser.
	 * @param goldenIndexState
	 *            the golden/original index state.
	 */
  public void doCrossBrowserCrawl(StateFlowGraph goldenStandard, Collection<List<Eventable>> paths,
      StateVertix goldenIndexState) {

    // Load the golden standard to compare against
    initializeController(goldenStandard, goldenIndexState);

    // Feed all the CrawlPaths into a CrossBrowserCrawler and add the crawler to
    // the queue
    for (List<Eventable> path : paths) {
      this.addWorkToQueue(new CrossBrowserCrawler(this, path));
    }

    try {
      this.waitForTermination();
    } catch (InterruptedException e) {
      LOGGER.error("Interupted during waitting for all the crawlers to finish", e);
    } finally {
      getBrowserPool().shutdown();
    }
  }

}
