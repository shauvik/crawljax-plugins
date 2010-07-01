/*
 * Copyright (C) 2010 crawljax.com. This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.crawljax.plugins.benchmark.dataset;

import java.lang.management.ManagementFactory;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawlSpecificationReader;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.configuration.ThreadConfigurationReader;

/**
 * The fixed, non changed data for a Crawl run.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: FixedBenchmarkData.java 7351 2010-07-01 15:09:52Z stefan $
 */
public class FixedBenchmarkData {
	private final int waitTimeAfterEvent;
	private final int waitTimeAfterReloadUrl;
	private final int numberOfBrowsers;
	private final int numberOfThreads;
	private final int numberOfProcessors;

	private static FixedBenchmarkData instance = null;

	/**
	 * @param waitTimeAfterEvent
	 *            the time in ms to wait after an event has fired
	 * @param waitTimeAfterReloadUrl
	 *            the time in ms to wait after a reload has been done
	 * @param numberOfBrowsers
	 *            the number of browser used.
	 * @param numberOfThreads
	 *            the number of threads used.
	 */
	public FixedBenchmarkData(int waitTimeAfterEvent, int waitTimeAfterReloadUrl,
	        int numberOfBrowsers, int numberOfThreads) {
		this.waitTimeAfterEvent = waitTimeAfterEvent;
		this.waitTimeAfterReloadUrl = waitTimeAfterReloadUrl;
		this.numberOfBrowsers = numberOfBrowsers;
		this.numberOfThreads = numberOfThreads;
		this.numberOfProcessors =
		        ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	}

	/**
	 * @return the waitTimeAfterEvent
	 */
	public final int getWaitTimeAfterEvent() {
		return waitTimeAfterEvent;
	}

	/**
	 * @return the waitTimeAfterReloadUrl
	 */
	public final int getWaitTimeAfterReloadUrl() {
		return waitTimeAfterReloadUrl;
	}

	/**
	 * @return the numberOfBrowsers
	 */
	public final int getNumberOfBrowsers() {
		return numberOfBrowsers;
	}

	/**
	 * @return the numberOfThreads
	 */
	public final int getNumberOfThreads() {
		return numberOfThreads;
	}

	/**
	 * @return the numberOfProcessors
	 */
	public final int getNumberOfProcessors() {
		return numberOfProcessors;
	}

	/**
	 * Static function to build the fixed data from the CrawlSession.
	 * 
	 * @param session
	 *            the session variable to build the data from.
	 * @return the new FixedBenchmarkData.
	 */
	public static FixedBenchmarkData buildFixedBenchmarkDataFromSession(CrawlSession session) {
		if (instance != null) {
			return instance;
		}
		CrawljaxConfigurationReader reader = session.getCrawljaxConfiguration();
		CrawlSpecificationReader crawlSpecReader = reader.getCrawlSpecificationReader();
		ThreadConfigurationReader threadReader = reader.getThreadConfigurationReader();
		instance =
		        new FixedBenchmarkData(crawlSpecReader.getWaitAfterEvent(), crawlSpecReader
		                .getWaitAfterReloadUrl(), threadReader.getNumberBrowsers(), threadReader
		                .getNumberThreads());
		return instance;
	}
}
