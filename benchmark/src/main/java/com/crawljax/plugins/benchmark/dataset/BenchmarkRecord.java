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

import java.io.PrintStream;

import org.apache.log4j.Logger;

import com.crawljax.plugins.benchmark.dataset.BenchmarkDataset.PlotType;

/**
 * This class holds all the properties calculated per InCrawl run.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: BenchmarkRecord.java 5953 2009-12-03 14:21:31Z stefan $
 */
public class BenchmarkRecord implements Cloneable {

	private static final Logger LOGGER = Logger.getLogger(BenchmarkRecord.class);

	private int countNr = 0;
	private String stateId = "";
	private long memorySize = 0;
	private long runtime = 0;
	private int edges = 0;
	private int states = 0;
	private int revisitedStates = 0;
	private int revisitedEdges = 0;
	private int browsers = 1;
	private int threads = 1;
	private int waitTimeAfterEvent;
	private int waitTimeAfterReloadUrl;
	private int processors;
	private double load;
	private double cpuPctsUsage;

	private int sumTimes = 1;

	/**
	 * Depth of the taken path. The depth of the path from the index state to the state denoted by
	 * this record.
	 */
	private int depth = 0;

	/**
	 * The total number of data fields this record holds. used in tryReadLine
	 */
	private static final int FIELDS = 13;

	private static final int FIELD_COUNT = 0;
	private static final int FIELD_STATEID = 1;
	private static final int FIELD_STATES = 2;
	private static final int FIELD_EDGES = 3;
	private static final int FIELD_RUNTIME = 4;
	private static final int FIELD_MEMORYSIZE = 5;

	private static final int FIELD_REVISITED_STATES = 6;
	private static final int FIELD_REVISITED_EDGES = 7;
	private static final int FIELD_DEPTH = 8;
	private static final int FIELD_BROWSERS = 9;
	private static final int FIELD_THREADS = 10;
	private static final int FIELD_WAITTIMEAFTEREVENT = 11;
	private static final int FIELD_WAITTIMEAFTERRELOADURL = 12;
	private static final int FIELD_PROCESSORS = 13;
	private static final int FIELD_LOAD = 14;
	private static final int FIELD_CPUPCTSUSAGE = 15;

	/**
	 * Create a new BenchmarkRecord based on the given fixed dataset.
	 * 
	 * @param fixedData
	 *            the fix data to begin with.
	 */
	public BenchmarkRecord(FixedBenchmarkData fixedData) {
		this.setBrowsers(fixedData.getNumberOfBrowsers());
		this.setThreads(fixedData.getNumberOfThreads());
		this.setWaitTimeAfterEvent(fixedData.getWaitTimeAfterEvent());
		this.setWaitTimeAfterReloadUrl(fixedData.getWaitTimeAfterReloadUrl());
		this.setProcessors(fixedData.getNumberOfProcessors());
	}

	/**
	 * Create a new BenchmarkRecord without a fixed dataset.
	 */
	public BenchmarkRecord() {
	}

	/* Getters & Setters */

	/**
	 * @return the countNr
	 */
	public final int getCountNr() {
		return countNr;
	}

	/**
	 * @param countnr
	 *            the countNr to set
	 */
	public final void setCountNr(final int countnr) {
		this.countNr = countnr;
	}

	/**
	 * @return the stateId
	 */
	public final String getStateId() {
		return stateId;
	}

	/**
	 * @param newStateId
	 *            the stateId to set
	 */
	public final void setStateId(final String newStateId) {
		this.stateId = newStateId;
	}

	/**
	 * @return the memorySize
	 */
	public final long getMemorySize() {
		return memorySize;
	}

	/**
	 * @param newMemorySize
	 *            the memorySize to set
	 */
	public final void setMemorySize(final long newMemorySize) {
		this.memorySize = newMemorySize;
	}

	/**
	 * @return the runtime
	 */
	public final long getRuntime() {
		return runtime;
	}

	/**
	 * @param newRuntime
	 *            the runtime to set
	 */
	public final void setRuntime(final long newRuntime) {
		this.runtime = newRuntime;
	}

	/**
	 * @return the depth
	 */
	public final int getDepth() {
		return depth;
	}

	/**
	 * @param newDepth
	 *            the depth to set
	 */
	public final void setDepth(final int newDepth) {
		this.depth = newDepth;
	}

	/**
	 * @return the edges
	 */
	public final int getEdges() {
		return edges;
	}

	/**
	 * @param newEdges
	 *            the edges to set
	 */
	public final void setEdges(final int newEdges) {
		this.edges = newEdges;
	}

	/**
	 * @return the revisitedStates
	 */
	public final int getRevisitedStates() {
		return revisitedStates;
	}

	/**
	 * @param newRevisitedStates
	 *            the revisitedStates to set
	 */
	public final void setRevisitedStates(final int newRevisitedStates) {
		this.revisitedStates = newRevisitedStates;
	}

	/**
	 * @return the revisitedEdges
	 */
	public final int getRevisitedEdges() {
		return revisitedEdges;
	}

	/**
	 * @param newRevisitedEdges
	 *            the revisitedEdges to set
	 */
	public final void setRevisitedEdges(final int newRevisitedEdges) {
		this.revisitedEdges = newRevisitedEdges;
	}

	/**
	 * @return the states
	 */
	public final int getStates() {
		return states;
	}

	/**
	 * @param newStates
	 *            the states to set
	 */
	public final void setStates(final int newStates) {
		this.states = newStates;
	}

	/**
	 * @return the threads
	 */
	public final int getThreads() {
		return threads;
	}

	/**
	 * @param threads
	 *            the threads to set
	 */
	public final void setThreads(int threads) {
		this.threads = threads;
	}

	/**
	 * @return the browsers
	 */
	public final int getBrowsers() {
		return browsers;
	}

	/**
	 * @param browsers
	 *            the browsers to set
	 */
	public final void setBrowsers(int browsers) {
		this.browsers = browsers;
	}

	/**
	 * Sum the record given with the current record.
	 * 
	 * @param record
	 *            the record to add.
	 */
	public final void sumRecord(BenchmarkRecord record) {
		if (record == this || record.equals(this)) {
			LOGGER.warn("Summing two records which are the same");
		}
		sumTimes++;
		if (countNr < record.countNr) {
			countNr = record.countNr;
			stateId = record.stateId;
		}

		memorySize += record.memorySize;
		runtime += record.runtime;
		edges += record.edges;
		states += record.states;
		revisitedStates += record.revisitedStates;
		revisitedEdges += record.revisitedEdges;
		browsers += record.browsers;
		threads += record.threads;
		depth += record.depth;
		waitTimeAfterEvent += record.waitTimeAfterEvent;
		waitTimeAfterReloadUrl += record.waitTimeAfterReloadUrl;
		processors = record.processors;
		load = record.load;
		cpuPctsUsage = record.cpuPctsUsage;
	}

	/**
	 * Make a new clone (deep) of this record.
	 * 
	 * @return a new BenchmakrRecord which is copied from this
	 */
	@Override
	public final BenchmarkRecord clone() {
		BenchmarkRecord record = new BenchmarkRecord();
		record.load(this.toString());
		record.setSumTimes(this.getSumTimes());
		return record;
	}

	/**
	 * Write the output to a PrintStream.
	 * 
	 * @param output
	 *            the PrintStream to write to
	 */
	public final void save(final PrintStream output) {
		output.println(this); // Print this object; by dynamic binding the
		// toString function will be used
		output.flush(); // We are done, flush the printstream
	}

	/**
	 * Return the string representation of a record. The output will be:
	 * 
	 * @return "countNr stateId states edges runtime memorySize revisitedStates revisitedEdges depth
	 *         browsers threads"
	 */
	@Override
	public String toString() {
		return this.countNr + "\t" + this.stateId + "\t" + this.states + "\t" + this.edges + "\t"
		        + this.runtime + "\t" + this.memorySize + "\t" + this.revisitedStates + "\t"
		        + this.revisitedEdges + "\t" + this.depth + "\t" + this.browsers + "\t"
		        + this.threads + "\t" + this.waitTimeAfterEvent + "\t"
		        + this.waitTimeAfterReloadUrl + "\t" + this.processors + "\t" + this.load + "\t"
		        + this.cpuPctsUsage;
	}

	/**
	 * Try to read a Record from a given line.
	 * 
	 * @param line
	 *            the line holding a possible record
	 * @return true if line has been read correctly
	 */
	public final boolean load(String line) {
		if (line.trim().equals("")) {
			// Line is empty so return false
			return false;
		}
		String[] fields = line.split("\t");
		if (fields.length != BenchmarkRecord.FIELDS) {
			// The line does not contain the required number of fields
			return false;
		}
		this.setCountNr(Integer.valueOf(fields[BenchmarkRecord.FIELD_COUNT]));
		this.setStateId(fields[BenchmarkRecord.FIELD_STATEID]);
		this.setStates(Integer.valueOf(fields[BenchmarkRecord.FIELD_STATES]));
		this.setEdges(Integer.valueOf(fields[BenchmarkRecord.FIELD_EDGES]));
		this.setRuntime(Long.valueOf(fields[BenchmarkRecord.FIELD_RUNTIME]));
		this.setMemorySize(Long.valueOf(fields[BenchmarkRecord.FIELD_MEMORYSIZE]));
		this.setRevisitedStates(Integer.valueOf(fields[BenchmarkRecord.FIELD_REVISITED_STATES]));
		this.setRevisitedEdges(Integer.valueOf(fields[BenchmarkRecord.FIELD_REVISITED_EDGES]));
		this.setDepth(Integer.valueOf(fields[BenchmarkRecord.FIELD_DEPTH]));
		this.setBrowsers(Integer.valueOf(fields[BenchmarkRecord.FIELD_BROWSERS]));
		this.setThreads(Integer.valueOf(fields[BenchmarkRecord.FIELD_THREADS]));
		this.setWaitTimeAfterEvent(Integer
		        .valueOf(fields[BenchmarkRecord.FIELD_WAITTIMEAFTEREVENT]));
		this.setWaitTimeAfterReloadUrl(Integer
		        .valueOf(fields[BenchmarkRecord.FIELD_WAITTIMEAFTERRELOADURL]));
		this.setProcessors(Integer.valueOf(fields[BenchmarkRecord.FIELD_PROCESSORS]));
		this.setLoad(Double.valueOf(fields[BenchmarkRecord.FIELD_LOAD]));
		this.setCpuPctsUsage(Double.valueOf(fields[BenchmarkRecord.FIELD_CPUPCTSUSAGE]));

		return true;
	}

	/**
	 * @return the sumTimes
	 */
	public final int getSumTimes() {
		return sumTimes;
	}

	/**
	 * @param sumTimes
	 *            the sumTimes to set
	 */
	public final void setSumTimes(int sumTimes) {
		this.sumTimes = sumTimes;
	}

	/**
	 * TODO make better.
	 */
	public final void averageOffRecord() {
		memorySize = memorySize / sumTimes;
		runtime = runtime / sumTimes;
		edges = edges / sumTimes;
		states = states / sumTimes;
		revisitedStates = revisitedStates / sumTimes;
		revisitedEdges = revisitedEdges / sumTimes;
		browsers = browsers / sumTimes;
		threads = threads / sumTimes;
		depth = depth / sumTimes;
		waitTimeAfterEvent = waitTimeAfterEvent / sumTimes;
		waitTimeAfterReloadUrl = waitTimeAfterReloadUrl / sumTimes;
		sumTimes = 1;
	}

	/**
	 * Retrieve the data for a given plotType.
	 * 
	 * @param type
	 *            the type to retrieve the data for.
	 * @return the value for the data for the given plotType.
	 */
	public long getValueForPlotType(PlotType type) {
		switch (type) {
			case RUNTIME:
				return this.getRuntime() / this.getSumTimes();
			case MEMORY:
				return this.getMemorySize() / this.getSumTimes();
			case EDGES:
				return this.getEdges() / this.getSumTimes();
			case STATES:
				return this.getStates() / this.getSumTimes();
			case REVISITED_EDGES:
				return this.getRevisitedEdges() / this.getSumTimes();
			case REVISITED_STATES:
				return this.getRevisitedStates() / this.getSumTimes();
			case DEPTH:
				return this.getDepth() / this.getSumTimes();
			case CPU_USAGE:
				return (long) (this.getCpuPctsUsage() / this.getSumTimes());
			default:
				LOGGER.warn("PlotType " + type + " not defined in the getValueForPlotType!");
				break;
		}
		return 0;
	}

	/**
	 * @param waitTimeAfterEvent
	 *            the waitTimeAfterEvent to set
	 */
	public void setWaitTimeAfterEvent(int waitTimeAfterEvent) {
		this.waitTimeAfterEvent = waitTimeAfterEvent;
	}

	/**
	 * @return the waitTimeAfterEvent
	 */
	public int getWaitTimeAfterEvent() {
		return waitTimeAfterEvent;
	}

	/**
	 * @param waitTimeAfterReloadUrl
	 *            the waitTimeAfterReloadUrl to set
	 */
	public void setWaitTimeAfterReloadUrl(int waitTimeAfterReloadUrl) {
		this.waitTimeAfterReloadUrl = waitTimeAfterReloadUrl;
	}

	/**
	 * @return the waitTimeAfterReloadUrl
	 */
	public int getWaitTimeAfterReloadUrl() {
		return waitTimeAfterReloadUrl;
	}

	/**
	 * @return the processors
	 */
	public final int getProcessors() {
		return processors;
	}

	/**
	 * @param processors
	 *            the processors to set
	 */
	public final void setProcessors(int processors) {
		this.processors = processors;
	}

	/**
	 * @return the load
	 */
	public final double getLoad() {
		return load;
	}

	/**
	 * @param load
	 *            the load to set
	 */
	public final void setLoad(double load) {
		this.load = load;
	}

	/**
	 * @return the cpuPctsUsage
	 */
	public final double getCpuPctsUsage() {
		return cpuPctsUsage;
	}

	/**
	 * @param cpuPctsUsage
	 *            the cpuPctsUsage to set
	 */
	public final void setCpuPctsUsage(double cpuPctsUsage) {
		this.cpuPctsUsage = cpuPctsUsage;
	}

}
