/*
 * Copyright (C) 2010 crawljax.com. This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Plugin to perform a benchmark on Crawljax. Notice: you need to add -javaagent:lib/classmexer.jar
 * to your commandline to get Memory calculation working InCrawlPlugins: The
 * BenchmarkCollectorPlugin only collects the data The BenchmarkGUIPlugin updates the GUI
 * PostCrawlPlugins: The BenchmarkProcessPlugin does the post processing (saving average data)
 * 
 * @version $Id: package-info.java 5979 2009-12-07 10:40:02Z stefan $
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
package com.crawljax.plugins.benchmark;