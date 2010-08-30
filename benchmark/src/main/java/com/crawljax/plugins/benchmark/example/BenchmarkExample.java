/*
 * Copyright (C) 2010 crawljax.com. This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.crawljax.plugins.benchmark.example;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.plugins.benchmark.BenchmarkCollectorPlugin;
import com.crawljax.plugins.benchmark.BenchmarkGUIPlugin;
import com.crawljax.plugins.benchmark.configuration.Configuration;

/**
 * A example runner.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public final class BenchmarkExample {

	private BenchmarkExample() {
	}

	private static final String URL = "http://crawljax.com";
	private static final int MAXIMUMSTATES = 5;

	/**
	 * @param args
	 *            the arguments given on the commandline
	 */
	public static void main(String[] args) {

		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification crawler;
		if (args.length > 0 && args[0] != null) {
			crawler = new CrawlSpecification(args[0]);
		} else {
			crawler = new CrawlSpecification(URL);
			crawler.setWaitTimeAfterEvent(500);
			crawler.setWaitTimeAfterReloadUrl(500);
		}
		crawler.setMaximumStates(BenchmarkExample.MAXIMUMSTATES);
		config.setCrawlSpecification(crawler);

		Configuration benchmakrConfig = new Configuration();
		benchmakrConfig.setUseDataFiles(true);
		config.addPlugin(new BenchmarkGUIPlugin(benchmakrConfig));
		config.addPlugin(new BenchmarkCollectorPlugin(benchmakrConfig));

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
