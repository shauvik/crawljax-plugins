// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.plugins.crossbrowser.core;

import com.crawljax.core.Crawler;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;

import java.util.List;

/**
 * A Crawler which overrides the {@link #run()} function to only backtrack (init) and afterwards
 * shutdown.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class CrossBrowserCrawler extends Crawler {

	/**
	 * Create a new CrossBrowserCrawler to follow a returnPath.
	 *
	 * @param mother
	 *            the controller to run the Crawler on.
	 * @param returnPath
	 *            the path that needs to be followed.
	 */
	public CrossBrowserCrawler(CrawljaxController mother, List<Eventable> returnPath) {
		super(mother, new CrawlPath(returnPath));
	}

	@Override
	public void run() {
		// Get Browser, Go to index & backtrack
		try {
			init();
		} catch (InterruptedException e) {
			// The Crawler was interrupted during initialization stopping the crawling immediately.
			return;
		}

		// Release the browser to be used by other Crawlers
		shutdown();
	}
}
