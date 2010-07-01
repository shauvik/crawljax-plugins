/*
 * Copyright (C) 2010 crawljax.com. This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.crawljax.plugins.benchmark.configuration;

import com.panayotis.io.FileUtils;

/**
 * This class represents and operates as the Runtime configuration for the Benchmark plugin.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public class Configuration implements BenchmarkConfiguration {

	private String outputFolder;
	private String gnuPlotLocation;
	private static final String DEFAULT_RECORDS_FILE = "benchmark.records";
	private static final String DEFAULT_SUMRECORDS_FILE = "benchmark.sumrecords";
	private static final String DEFAULT_TOTAL_FILE = "benchmark.total";
	private String totalFile;
	private String recordsFile;
	private String sumRecordsFile;
	private boolean useDataFiles = false;

	/**
	 * Return the location of gnuplot or specified by the config or by the General "look in path"
	 * system.
	 * 
	 * @see com.crawljax.plugins.benchmark.configuration.BenchmarkConfiguration#getGnuPlotLocation()
	 * @return the location of gnuplot or specified by the config or by the General "look in path"
	 *         system
	 */
	@Override
	public String getGnuPlotLocation() {
		if (gnuPlotLocation == null) {
			return FileUtils.findPathExec("gnuplot");
		}
		return gnuPlotLocation;
	}

	@Override
	public String getOutputFolder() {
		if (outputFolder == null || outputFolder == "") {
			outputFolder = "output/benchmark";
		}
		return outputFolder;
	}

	@Override
	public void setOutputFolder(String absolutePath) {
		outputFolder = absolutePath;
	}

	/**
	 * Set the location of the gnuplot executable, note not denote the directory but the exact
	 * location of the executable including the executable itself. for example /home/stefan/gnplt
	 * 
	 * @param gnuPlotLocation
	 *            the location of the gnuplot executable
	 */
	public final void setGnuPlotLocation(String gnuPlotLocation) {
		this.gnuPlotLocation = gnuPlotLocation;
	}

	@Override
	public String getRecordsFile() {
		if (recordsFile == null) {
			return this.getOutputFolder() + "/" + Configuration.DEFAULT_RECORDS_FILE;
		}
		return recordsFile;
	}

	@Override
	public String getSumRecordsFile() {
		if (sumRecordsFile == null) {
			return this.getOutputFolder() + "/" + Configuration.DEFAULT_SUMRECORDS_FILE;
		}
		return sumRecordsFile;
	}

	/**
	 * Set the location of the records file.
	 * 
	 * @param recordsFile
	 *            the recordsFile to set
	 */
	public final void setRecordsFile(String recordsFile) {
		this.recordsFile = recordsFile;
	}

	/**
	 * Set the location of the records file.
	 * 
	 * @param sumRecordsFile
	 *            the sumRecordsFile to set
	 */
	public final void setSumRecordsFile(String sumRecordsFile) {
		this.sumRecordsFile = sumRecordsFile;
	}

	@Override
	public String getTotalFile() {
		if (totalFile == null) {
			return this.getOutputFolder() + "/" + Configuration.DEFAULT_TOTAL_FILE;
		}
		return totalFile;
	}

	/**
	 * @param totalFile
	 *            the totalFile to set
	 */
	public final void setTotalFile(String totalFile) {
		this.totalFile = totalFile;
	}

	/**
	 * data files are used?
	 * 
	 * @return true if the data files are used
	 */
	public final boolean useDataFiles() {
		return this.useDataFiles;
	}

	/**
	 * data files must be used?
	 * 
	 * @param useDataFiles
	 *            true if the data files must be used
	 */
	public final void setUseDataFiles(boolean useDataFiles) {
		this.useDataFiles = useDataFiles;
	}

}
