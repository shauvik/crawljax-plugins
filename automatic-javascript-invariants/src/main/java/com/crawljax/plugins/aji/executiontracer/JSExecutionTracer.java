/*
    Automatic JavaScript Invariants is a plugin for Crawljax that can be
    used to derive JavaScript invariants automatically and use them for
    regressions testing.
    Copyright (C) 2010  crawljax.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.crawljax.plugins.aji.executiontracer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.util.Helper;

import daikon.Daikon;

/**
 * Crawljax Plugin that reads an instrumentation array from the webbrowser and saves the contents in
 * a Daikon trace file.
 * 
 * @author Frank Groeneveld
 * @version $Id: JSExecutionTracer.java 6162 2009-12-16 13:56:21Z frank $
 */
public class JSExecutionTracer
        implements PreStateCrawlingPlugin, PostCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput {

	private static final int ONE_SEC = 1000;

	private static String outputFolder;
	private static String assertionFilename;

	private static JSONArray points = new JSONArray();

	private static final Logger LOGGER = Logger.getLogger(JSExecutionTracer.class.getName());

	public static final String EXECUTIONTRACEDIRECTORY = "executiontrace/";

	/**
	 * @param filename
	 *            How to name the file that will contain the assertions after execution.
	 */
	public JSExecutionTracer(String filename) {
		assertionFilename = filename;
	}

	/**
	 * Initialize the plugin and create folders if needed.
	 * 
	 * @param browser
	 *            The browser.
	 */
	@Override
	public void preCrawling(EmbeddedBrowser browser) {
		try {
			Helper.directoryCheck(getOutputFolder());
			Helper.directoryCheck(getOutputFolder() + EXECUTIONTRACEDIRECTORY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the JavaScript instrumentation array from the webbrowser and writes its contents in
	 * Daikon format to a file.
	 * 
	 * @param session
	 *            The crawling session.
	 * @param candidateElements
	 *            The candidate clickable elements.
	 */
	@Override
	public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {

		String filename = getOutputFolder() + EXECUTIONTRACEDIRECTORY + "jsexecutiontrace-";
		filename += session.getCurrentState().getName();

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		filename += dateFormat.format(date) + ".dtrace";

		try {

			LOGGER.info("Reading execution trace");

			LOGGER.info("Parsing JavaScript execution trace");

			/* FIXME: Frank, hack to send last buffer items and wait for them to arrive */
			session.getBrowser().executeJavaScript("sendReally();");
			Thread.sleep(ONE_SEC);

			Trace trace = Trace.parse(points);

			PrintWriter file = new PrintWriter(filename);
			file.write(trace.getDeclaration());
			file.write('\n');
			file.write(trace.getData(points));
			file.close();

			LOGGER.info("Saved execution trace as " + filename);

			points = new JSONArray();
		} catch (CrawljaxException we) {
			we.printStackTrace();
			LOGGER.error("Unable to get instrumentation log from the browser");
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a list with all trace files in the executiontracedirectory.
	 * 
	 * @return The list.
	 */
	public List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(getOutputFolder() + EXECUTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".dtrace")) {
				result.add(getOutputFolder() + EXECUTIONTRACEDIRECTORY + file);
			}
		}

		return result;
	}

	@Override
	public void postCrawling(CrawlSession session) {
		try {
			PrintStream output = new PrintStream(getOutputFolder() + getAssertionFilename());

			/* save the current System.out for later usage */
			PrintStream oldOut = System.out;
			/* redirect it to the file */
			System.setOut(output);

			/* don't print all the useless stuff */
			Daikon.dkconfig_quiet = true;
			Daikon.noversion_output = true;

			List<String> arguments = allTraceFiles();

			/*
			 * TODO: Frank, fix this hack (it is done because of Daikon calling cleanup before init)
			 */
			arguments.add("-o");
			arguments.add(getOutputFolder() + "daikon.inv.gz");
			arguments.add("--format");
			arguments.add("javascript");
			arguments.add("--config_option");
			arguments.add("daikon.FileIO.unmatched_procedure_entries_quiet=true");
			arguments.add("--config_option");
			arguments.add("daikon.FileIO.ignore_missing_enter=true");

			/* start daikon */
			Daikon.mainHelper(arguments.toArray(new String[0]));

			/* Restore the old system.out */
			System.setOut(oldOut);

			/* close the output file */
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return Name of the assertion file.
	 */
	public String getAssertionFilename() {
		return assertionFilename;
	}

	@Override
	public String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	@Override
	public void setOutputFolder(String absolutePath) {
		outputFolder = absolutePath;
	}

	/**
	 * Dirty way to save program points from the proxy request threads. TODO: Frank, find cleaner
	 * way.
	 * 
	 * @param string
	 *            The JSON-text to save.
	 */
	public static void addPoint(String string) {
		JSONArray buffer = null;
		try {
			buffer = new JSONArray(string);
			for (int i = 0; i < buffer.length(); i++) {
				points.put(buffer.get(i));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
