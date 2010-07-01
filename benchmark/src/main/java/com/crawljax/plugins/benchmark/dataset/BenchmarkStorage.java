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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;
import com.crawljax.plugins.benchmark.configuration.BenchmarkConfiguration;
import com.javamex.classmexer.MemoryUtil;
import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

/**
 * This class is used to store all the recored / measured values during benchmarking.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: BenchmarkStorage.java 5953 2009-12-03 14:21:31Z stefan $
 */
public final class BenchmarkStorage {

	private static final Logger LOGGER = Logger.getLogger(BenchmarkStorage.class.getName());

	/**
	 * The main instance used inside this class, init with null to determine the current state.
	 */
	private static BenchmarkStorage localInstance = null;

	/* Internal */
	private int counter = 0;

	private int lastEdges = 0;
	private long lastMemory = 0;
	private int lastStates = 0;
	private long lastTime = 0;

	private int maxDepth = 0;

	private long startTime;
	/**
	 * Store the absolute total.
	 */
	private BenchmarkRecord total;

	/**
	 * This ArrayList stores a single record which applies to the denoted state.
	 */
	private final BenchmarkDataset records;

	/**
	 * This ArrayList stores a single record which applies to the accumulative state of the crawl
	 * process.
	 */
	private final BenchmarkDataset sumRecords;

	private final Hashtable<String, Integer> statesSeen;
	private final Hashtable<String, Integer> edgesSeen;

	private final BenchmarkConfiguration config;

	private StateVertix lastState;

	/**
	 * Retrieve the global unique storage used in benchmarking.
	 * 
	 * @param config
	 *            the object where to retrieve the output config from
	 * @return the instance of the globally used BenchmarkStorage
	 */
	public static synchronized BenchmarkStorage instance(BenchmarkConfiguration config) {
		if (localInstance == null) {
			localInstance = new BenchmarkStorage(config);
			try {
				if (config.useDataFiles()) {
					localInstance.doLoad();
				}
			} catch (FileNotFoundException e) {
				// This is normal when the plugin is run for the first-time
				LOGGER.warn("Datafile(s) not found creating a new-one");
			} catch (IOException e) {
				LOGGER.error("Recieved IOException when opening Datafiles(s)", e);
			} catch (CrawljaxException e) {
				LOGGER.error("CrawljaxException Recieved....", e);
			}
		}
		return localInstance;
	}

	/**
	 * Destroy the BenchmarkStorge instance. A new call to instace will create a new instance and in
	 * doing so calling the constructor again and "restarting" from scratch.
	 */
	public static synchronized void destroy() {
		if (localInstance != null) {
			try {
				if (localInstance.config.useDataFiles()) {
					localInstance.doSave();
				}
			} catch (IOException e) {
				LOGGER.error("Recieved IOException during saving...", e);
			} catch (CrawljaxException e) {
				LOGGER.error("Recieved Crawljax exception during saving...", e);
			} finally {
				localInstance = null;
			}
		}
	}

	/**
	 * Private constructor to prevent external instancing.
	 */
	private BenchmarkStorage(BenchmarkConfiguration config) {
		this.config = config;
		records = new BenchmarkDataset();
		sumRecords = new BenchmarkDataset();
		total = new BenchmarkRecord();

		statesSeen = new Hashtable<String, Integer>();
		edgesSeen = new Hashtable<String, Integer>();
	}

	/**
	 * @return the records
	 */
	public BenchmarkDataset getRecords() {
		return records;
	}

	/**
	 * @return the sumRecords
	 */
	public BenchmarkDataset getSumRecords() {
		return sumRecords;
	}

	/**
	 * @return the total
	 */
	public BenchmarkRecord getTotal() {
		return total;
	}

	/**
	 * Notify the BenchmarkStorage of a new state which has been visited. This method is designed
	 * for the real work and applicable to class from anyware.
	 * 
	 * @param sfg
	 *            the state flow graph holding the flow of states as known now
	 * @param currentState
	 *            the current state
	 * @param indexState
	 *            the start (index) state
	 * @param data
	 *            the object to read the data from
	 */
	public synchronized void stateVisited(final StateFlowGraph sfg,
	        final StateVertix currentState, final StateVertix indexState, FixedBenchmarkData data) {
		// Get The Data now as fast as possible
		long currentTime = System.currentTimeMillis();

		double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

		long currentMemory = 0;
		try {
			currentMemory = MemoryUtil.deepMemoryUsageOf(sfg, VisibilityFilter.ALL);
		} catch (IllegalStateException e) {
			currentMemory = 0;
			LOGGER.warn("MemoryUtil (ClassMexer) is not added to cmd line,"
			        + " add it by adding -javaagent:lib/classmexer-0.03.jar");
		}

		// Get the final part of the local variable
		int currentEdges = sfg.getAllEdges().size();
		int currentStates = sfg.getAllStates().size();
		int currentDepth = 0;
		int thisStateSeen = 0;
		int thisEdgesSeen = 0;
		String stateId = "";

		if (currentState != null && indexState != null) {
			currentDepth = this.calculateDepth(sfg, currentState, indexState);
			thisEdgesSeen = this.calculateSeenEdges(sfg, currentState, indexState);
		}

		if (currentState != null) {
			thisStateSeen =
			        this.updateOrInitHashtabeValue(this.statesSeen, currentState.getName());
			stateId = currentState.getName();
		}

		int currentSeenEdges = this.getSumOfHashtableElements(this.edgesSeen);
		int currentSeenStates = this.getSumOfHashtableElements(this.statesSeen);

		this.maxDepth = Math.max(currentDepth, maxDepth);

		// Increase the counter
		this.counter++;

		// Delta Record (denoting one single state)
		BenchmarkRecord r = new BenchmarkRecord(data);
		r.setCountNr(counter);
		r.setStateId(stateId);
		r.setMemorySize(currentMemory - lastMemory);
		r.setRuntime(currentTime - lastTime);
		r.setEdges(currentEdges - lastEdges);
		r.setStates(currentStates - lastStates);
		r.setRevisitedStates(thisStateSeen);
		r.setRevisitedEdges(thisEdgesSeen);
		r.setRevisitedEdges(0);
		r.setDepth(currentDepth);
		r.setLoad(load);
		r.setCpuPctsUsage((load * 100) / r.getProcessors());
		records.put(r.getCountNr(), r);

		// Cumulative Record
		BenchmarkRecord r2 = new BenchmarkRecord(data);
		r2.setCountNr(counter);
		r2.setStateId(stateId);
		r2.setMemorySize(currentMemory);
		r2.setRuntime(currentTime - this.startTime);
		r2.setEdges(currentEdges);
		r2.setStates(currentStates);
		r2.setRevisitedStates(currentSeenStates);
		r2.setRevisitedEdges(currentSeenEdges);
		r2.setDepth(currentDepth); // currentDepth of
		// maxDepth??
		r2.setLoad(load);
		r2.setCpuPctsUsage((load * 100) / r.getProcessors());

		// Take care of the total and the running total
		sumRecords.put(r2.getCountNr(), r2);

		this.total = r2.clone();

		this.total.setDepth(maxDepth);

		this.lastStates = currentStates;
		this.lastEdges = currentEdges;

		this.lastState = currentState;

		// set the lastMemory
		this.lastMemory = currentMemory;
		// At final reset the lastTime to get a measurement as close as
		// possible
		this.lastTime = System.currentTimeMillis();
	}

	/**
	 * Calculate the distance between the root state (initial state) and the current state (state
	 * which is last added).
	 * 
	 * @param sfg
	 *            the state flow graph holding the flow of states
	 * @param current
	 *            the current state
	 * @param index
	 *            the index(start) state
	 * @return the depth of the path last added to the state machine
	 */
	private int calculateDepth(final StateFlowGraph sfg, final StateVertix current,
	        final StateVertix index) {
		List<Eventable> p = sfg.getShortestPath(index, current); // Store
		// locally
		if (p != null) {
			return p.size();
		} else {
			return 0;
		}
	}

	/**
	 * Calculate the number of time the edges between the previous state and the current state has
	 * already been seen.
	 * 
	 * @param sfg
	 *            the state flow graph holding the flow of states
	 * @param current
	 *            the current state
	 * @param index
	 *            the index(start) state
	 * @return the number of times the edges between the previous state and the current state has
	 *         already been seen
	 */
	private int calculateSeenEdges(final StateFlowGraph sfg, final StateVertix current,
	        final StateVertix index) {
		int thisEdgesSeen = 0;
		Set<Eventable> eList = sfg.getIncomingClickable(current);
		if (eList != null) {
			for (Eventable eventable : eList) {
				try {
					if (eventable.getSourceStateVertix().equals(this.lastState)
					        && eventable.getTargetStateVertix().equals(current)) {
						// From is the old (previous) state and destination is the
						// current state
						// So this edge is new??
						thisEdgesSeen +=
						        this.updateOrInitHashtabeValue(this.edgesSeen, String
						                .valueOf(eventable.getId()));
					}
				} catch (CrawljaxException e) {
					LOGGER.error("Catched CrawljaxException...", e);
				}
			}
		}
		return thisEdgesSeen;
	}

	/**
	 * calculate and return the sum of all the values in a has table.
	 * 
	 * @param table
	 *            the Hashtable containing the the count of the duplicate seen states
	 * @return the sum of all the values in the hashtable;
	 */
	private int getSumOfHashtableElements(final Hashtable<String, Integer> table) {
		Enumeration<Integer> intList = table.elements();
		int returnInteger = 0;
		while (intList.hasMoreElements()) {
			returnInteger += intList.nextElement();
		}
		return returnInteger;
	}

	/**
	 * update the counting table for a given state or edge identifier and return the number of times
	 * a duplicate has been found.
	 * 
	 * @param table
	 *            the Hashtable containing the the count of the duplicate seen states
	 * @param entry
	 *            the state identifier
	 * @return the number of times the given state has been seen duplicated
	 */
	private int updateOrInitHashtabeValue(final Hashtable<String, Integer> table,
	        final String entry) {
		Object tmp = table.get(entry);
		int count = 0;
		if (tmp != null) {
			count = (Integer) tmp;
			count++;
			statesSeen.put(entry, count);
		} else {
			statesSeen.put(entry, new Integer(0));
		}
		return count;
	}

	private PrintStream openFilePrinter(String location) throws IOException, CrawljaxException {
		File f = new File(location);
		File parentDir = f.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		if (!f.createNewFile()) {
			if (f.delete()) {
				if (!f.createNewFile()) {
					// We can recreate file :(
					LOGGER.warn("Can not create file " + f.getName());
					throw new CrawljaxException("Can not create file " + f.getName());
				}
			} else {
				// We can not delete the file :(
				LOGGER.warn("Can not delete file " + f.getName());
				throw new CrawljaxException("Can not delete file " + f.getName());
			}
		}
		if (!f.canWrite()) {
			// we can not write to the given file
			LOGGER.warn("Can not write to file " + f.getName());
			throw new CrawljaxException("Can not write to file " + f.getName());
		}

		return new PrintStream(f);
	}

	private BufferedReader openFileReader(String location) throws CrawljaxException,
	        FileNotFoundException {
		File f = new File(location);
		if (!f.canRead()) {
			// File does not exists so returning
			// Or File can not be read
			throw new CrawljaxException("File does not exists Or File can not be read "
			        + f.getName());
		}
		return new BufferedReader(new FileReader(f));
	}

	/**
	 * Save all the datasets.
	 * 
	 * @throws CrawljaxException
	 * @throws IOException
	 */
	private void doSave() throws IOException, CrawljaxException {
		PrintStream rStream = openFilePrinter(config.getRecordsFile());
		this.records.save(rStream);
		rStream.flush();
		rStream.close();

		PrintStream srStream = openFilePrinter(config.getSumRecordsFile());
		this.sumRecords.save(srStream);
		srStream.flush();
		srStream.close();
	}

	private void doLoad() throws IOException, CrawljaxException {
		BufferedReader rReader = openFileReader(config.getRecordsFile());
		this.records.load(rReader);
		rReader.close();

		BufferedReader srReader = openFileReader(config.getSumRecordsFile());
		this.sumRecords.load(srReader);
		srReader.close();

		this.total = this.sumRecords.getLastLoaded();
	}

	/**
	 * Inform the Storage of the stateVisited.
	 * 
	 * @param session
	 *            the CrawlSession to retrieve the data from.
	 */
	public void stateVisited(CrawlSession session) {
		stateVisited(session.getStateFlowGraph(), session.getCurrentState(), session
		        .getInitialState(), FixedBenchmarkData
		        .buildFixedBenchmarkDataFromSession(session));
	}

	/**
	 * Inform the Storage that the Crawling has finished.
	 * 
	 * @param session
	 *            the session to get the data from.
	 */
	public void finishedCrawling(CrawlSession session) {
		this.stateVisited(session.getStateFlowGraph(), null, null, FixedBenchmarkData
		        .buildFixedBenchmarkDataFromSession(session));
	}

	/**
	 * Tell the Storage that the crawling has started.
	 */
	public void startCrawling() {
		lastTime = System.currentTimeMillis();
		startTime = lastTime;
	}
}
