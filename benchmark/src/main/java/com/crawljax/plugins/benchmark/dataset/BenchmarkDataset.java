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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.crawljax.plugins.benchmark.gui.BenchmarkPlot;
import com.panayotis.gnuplot.dataset.DataSet;

/**
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: BenchmarkDataset.java 5953 2009-12-03 14:21:31Z stefan $
 */
public class BenchmarkDataset extends Hashtable<Integer, BenchmarkRecord>
        implements DataSet, Comparator<BenchmarkRecord> {

	private static final Logger LOGGER = Logger.getLogger(BenchmarkDataset.class);

	/**
	 * Generated versionUID.
	 */
	private static final long serialVersionUID = 5916138674598289841L;

	private BenchmarkRecord lastLoaded;

	private ArrayList<BenchmarkRecord> recordList = new ArrayList<BenchmarkRecord>();

	private final Object datasetLock = new Object();

	private BenchmarkPlot currentPlot;

	/**
	 * @return the datasetLock
	 */
	public final Object getDatasetLock() {
		return datasetLock;
	}

	/**
	 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
	 */
	public enum PlotType {
		RUNTIME("Runtime (ms)"), MEMORY("Memory (b)"), STATES("States (#)"), EDGES("Edges (#)"),
		REVISITED_STATES("Revisited states (#)"), REVISITED_EDGES("Revisited edges(#)"), DEPTH(
		        "Depth (#)"), CPU_USAGE("CPU usage (%)");
		private final String label;

		/**
		 * Create a new PlotType.
		 * 
		 * @param label
		 *            the label for this plot
		 */
		private PlotType(String label) {
			this.label = label;
		}

		/**
		 * Retrieve the label for this plot.
		 * 
		 * @return the name of the label
		 */
		public final String getLabel() {
			return this.label;
		}

		@Override
		public String toString() {
			return this.label;
		}
	}

	/**
	 * Default constructor, setting the plotType to Runtime.
	 */
	public BenchmarkDataset() {
		/* Install some defaults */
	}

	/**
	 * Retrieve how many dimensions this dataset refers to. Typically, for every point, this method
	 * informs JavaPlot how many "columns" of data this point has. Make sure that every point has at
	 * least as many dimensions as what is reported here .
	 * 
	 * @return the number of dimensions always 1
	 */
	@Override
	public final int getDimensions() {
		return 1;
	}

	private ArrayList<BenchmarkRecord> sortedList() {
		Enumeration<BenchmarkRecord> set = this.elements();
		recordList = new ArrayList<BenchmarkRecord>();

		while (set.hasMoreElements()) {
			recordList.add(set.nextElement());
		}
		Collections.sort(recordList, this);
		return recordList;
	}

	@Override
	public synchronized int size() {
		ArrayList<BenchmarkRecord> lst = sortedList();
		return (int) lst.get(lst.size() - 1).getValueForPlotType(currentPlot.getXAxis());
	}

	/**
	 * Retrieve data information from a point. To retrieve information for each point, a continious
	 * call to this method will be executed, keeping the item number constant and increasing the
	 * dimension.
	 * 
	 * @param point
	 *            The point number
	 * @param dimension
	 *            The point dimension (or "column") to request data from
	 * @return the point data for this dimension
	 */
	@Override
	public final String getPointValue(final int point, final int dimension) {
		if (point == 0) {
			return "";
		}

		if (currentPlot == null) {
			LOGGER.warn("Holy smokes; how is this possible there is no currentPlot!");
		}

		String cache = currentPlot.getCachedPoint(point);
		if (cache == null) {
			// Not known
			BenchmarkRecord last = null;
			ArrayList<BenchmarkRecord> rs = this.sortedList();
			for (BenchmarkRecord record : rs) {
				if (record.getValueForPlotType(currentPlot.getXAxis()) < point) {
					last = record;
				} else {
					long prevPoint = 0;
					long prevValue = 0;
					if (last != null) {
						prevPoint = last.getValueForPlotType(currentPlot.getXAxis());
						prevValue = last.getValueForPlotType(currentPlot.getYAxis());
					}
					long nextValue = record.getValueForPlotType(currentPlot.getYAxis());
					long nextPoint = record.getValueForPlotType(currentPlot.getXAxis());

					cache =
					        ""
					                + ((((nextValue - prevValue) / (nextPoint - prevPoint)) * (point - prevPoint)) + prevValue);
					if (last != null
					        && point >= record.getValueForPlotType(currentPlot.getXAxis())) {
						System.out.println("BriBraBreak");
						break;
					}
				}
			}
			if (cache == null) {
				return "";
			} else {
				currentPlot.storeCachedPoint(point, cache);
				return cache;
			}
		} else {
			return cache;
		}
	}

	/**
	 * Custom put a record depending on the action which needs to be taken. {@inheritDoc}
	 */
	@Override
	public synchronized BenchmarkRecord put(Integer key, BenchmarkRecord value) {
		BenchmarkRecord old = this.get(key);
		// is the old record already known
		if (old == null) {
			// This is easy store it
			return super.put(key, value);
		}

		old.sumRecord(value);

		return super.put(key, old);
	}

	/**
	 * Tries to load the data set from a given reader.
	 * 
	 * @param br
	 *            The reader to load the data from
	 * @throws IOException
	 *             when readLine fails on the given bufferedReader
	 */
	public void load(BufferedReader br) throws IOException {
		lastLoaded = null;
		String line = br.readLine();
		if (line == null) {
			// First line is already null so no data!
			return;
		}
		int avg = Integer.valueOf(line);
		while ((line = br.readLine()) != null) {
			// instance BenchmarkRecord
			BenchmarkRecord r = new BenchmarkRecord();
			r.setSumTimes(avg);
			if (r.load(line)) { // load line
				this.put(r.getCountNr(), r); // add to our
				if (lastLoaded == null || lastLoaded.getCountNr() < r.getCountNr()) {
					lastLoaded = r;
				}
			}
		}
	}

	/**
	 * Write this collection to stream.
	 * 
	 * @param ps
	 *            the PrintStream to write to
	 */
	public void save(PrintStream ps) {
		Enumeration<BenchmarkRecord> en = this.elements();

		boolean first = true;
		while (en.hasMoreElements()) {
			BenchmarkRecord r = en.nextElement();
			if (first) {
				ps.println(r.getSumTimes());
				first = false;
			}
			r.save(ps);
		}
	}

	/**
	 * @return the lastLoaded
	 */
	public final BenchmarkRecord getLastLoaded() {
		return lastLoaded;
	}

	@Override
	public int compare(BenchmarkRecord o1, BenchmarkRecord o2) {
		return (int) (o1.getValueForPlotType(currentPlot.getXAxis()) - o2
		        .getValueForPlotType(currentPlot.getXAxis()));
	}

	/**
	 * Retrieve the lower bound value for the y-axis.
	 * 
	 * @return the min Y value
	 */
	public double getMinY() {
		ArrayList<BenchmarkRecord> list = sortedList();
		long value = 0;
		for (BenchmarkRecord benchmarkRecord : list) {
			value = benchmarkRecord.getValueForPlotType(currentPlot.getYAxis());
			if (value > 0) {
				break;
			}
		}
		return value;
	}

	/**
	 * Retrieve the upper-bound y-value for the plot.
	 * 
	 * @return the upper y value
	 */
	public double getMaxY() {
		return sortedList().get(sortedList().size() - 1).getValueForPlotType(
		        currentPlot.getYAxis());
	}

	/**
	 * Retrieve the lower bound X value.
	 * 
	 * @return the lower bound x value
	 */
	public double getMinX() {
		ArrayList<BenchmarkRecord> list = sortedList();
		long value = 0;
		for (BenchmarkRecord benchmarkRecord : list) {
			value = benchmarkRecord.getValueForPlotType(currentPlot.getYAxis());
			if (value > 0) {
				value = benchmarkRecord.getValueForPlotType(currentPlot.getXAxis());
				break;
			}
		}
		return value;
	}

	/**
	 * Get Max X value.
	 * 
	 * @see #size()
	 * @return the size basically.
	 */
	public double getMaxX() {
		System.out.println("MAX X " + size());
		return size();
	}

	/**
	 * @return the currentPlot
	 */
	public final BenchmarkPlot getCurrentPlot() {
		return currentPlot;
	}

	/**
	 * @param currentPlot
	 *            the currentPlot to set
	 */
	public final void setCurrentPlot(BenchmarkPlot currentPlot) {
		this.currentPlot = currentPlot;
	}

}
