/*
 * Copyright (C) 2010 crawljax.com. This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.crawljax.plugins.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnRevisitStatePlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.StateVertix;
import com.crawljax.plugins.benchmark.configuration.BenchmarkConfiguration;
import com.crawljax.plugins.benchmark.dataset.BenchmarkRecord;
import com.crawljax.plugins.benchmark.dataset.BenchmarkStorage;
import com.crawljax.plugins.benchmark.dataset.FixedBenchmarkData;

/**
 * This class collects the data used while benchmarking.
 * 
 * @version $Id: BenchmarkCollectorPlugin.java 5979 2009-12-07 10:40:02Z stefan $
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public class BenchmarkCollectorPlugin
        implements OnNewStatePlugin, OnRevisitStatePlugin, PostCrawlingPlugin, OnUrlLoadPlugin,
        PreCrawlingPlugin, GeneratesOutput {

	private final BenchmarkConfiguration config;
	private CrawlSession session;

	/**
	 * The default constructor for the BenchmarkCollectorPlugin.
	 * 
	 * @param config
	 *            the config object to use to read the config from
	 */
	public BenchmarkCollectorPlugin(BenchmarkConfiguration config) {
		this.config = config;
	}

	@Override
	public String getOutputFolder() {
		/**
		 * place the request in the config class
		 */
		return config.getOutputFolder();
	}

	@Override
	public void setOutputFolder(String absolutePath) {
		/**
		 * place the request in the config class
		 */
		config.setOutputFolder(absolutePath);
	}

	/**
	 * Append the last total record to a master file after averaging the record.
	 * 
	 * @param file
	 *            the file to write to
	 * @throws IOException
	 *             when the master file can not be written
	 */
	public void appendToMaster(String file) throws IOException {
		// Get Storage instance
		BenchmarkStorage storage = BenchmarkStorage.instance(config);

		// Create fileWriter
		FileWriter fileWriter = new FileWriter(new File(file), true);

		// Get the last total Record
		BenchmarkRecord totalRecord = storage.getTotal();

		// Average the record
		totalRecord.averageOffRecord();

		// Write & close the masterFile
		fileWriter.write(totalRecord.toString() + "\n");
		fileWriter.flush();
		fileWriter.close();

		// Destroy the Datastorage
		BenchmarkStorage.destroy();

		// Remove the records File created by instance
		File f3 = new File(config.getRecordsFile());
		if (f3.exists()) {
			f3.delete();
		}

		// Remove the sumRecords file created by instance
		File f4 = new File(config.getSumRecordsFile());
		if (f4.exists()) {
			f4.delete();
		}
	}

	/* Crawljax Extension points */

	@Override
	public void onUrlLoad(EmbeddedBrowser browser) {
		if (session != null) {
			BenchmarkStorage.instance(config).stateVisited(session.getStateFlowGraph(),
			        session.getInitialState(), session.getInitialState(),
			        FixedBenchmarkData.buildFixedBenchmarkDataFromSession(session));
		}
	}

	@Override
	public void preCrawling(EmbeddedBrowser browser) {
		// Tell the Storage we start Crawling now!
		BenchmarkStorage.instance(config).startCrawling();
	}

	/**
	 * Store a new State. {@inheritDoc}
	 */
	@Override
	public void onNewState(CrawlSession session) {
		this.session = session;
		BenchmarkStorage.instance(config).stateVisited(session);
	}

	/**
	 * Store a revisited state. {@inheritDoc}
	 */
	@Override
	public void onRevisitState(CrawlSession session, StateVertix currentState) {
		BenchmarkStorage.instance(config).stateVisited(session.getStateFlowGraph(), currentState,
		        session.getInitialState(),
		        FixedBenchmarkData.buildFixedBenchmarkDataFromSession(session));
	}

	/**
	 * Validate / Run all the Post Processing Jobs for the Benchmark plugin. {@inheritDoc}
	 */
	@Override
	public void postCrawling(CrawlSession session) {
		BenchmarkStorage store = BenchmarkStorage.instance(config);
		store.finishedCrawling(session);
		BenchmarkStorage.destroy();
	}
}
