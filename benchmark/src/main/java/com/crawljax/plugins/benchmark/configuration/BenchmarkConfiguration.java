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

import com.crawljax.core.plugin.GeneratesOutput;

/**
 * This Interface denotes the required config fields that every configuration object needs to
 * implement.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public interface BenchmarkConfiguration extends GeneratesOutput {

	/**
	 * Return the location of the gnuplot executable, note; not return the directory but the
	 * executable it self. For example: /home/stefan/bin/gnplt
	 * 
	 * @return the location of the gnuplot executable
	 */
	String getGnuPlotLocation();

	/**
	 * Return the file where to store the Records.
	 * 
	 * @return the records file
	 */
	String getRecordsFile();

	/**
	 * Return the file where to store the Sum Records.
	 * 
	 * @return the sum records file
	 */
	String getSumRecordsFile();

	/**
	 * Return the file where to store the total of all the Crawling.
	 * 
	 * @return the total file location.
	 */
	String getTotalFile();

	/**
	 * Are data files in use?
	 * 
	 * @return true if the datafiles are in use.
	 */
	boolean useDataFiles();
}
