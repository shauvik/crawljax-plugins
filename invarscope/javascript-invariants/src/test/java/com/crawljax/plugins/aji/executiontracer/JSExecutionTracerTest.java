package com.crawljax.plugins.aji.executiontracer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.browser.WebDriverFirefox;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StateVertix;
import com.crawljax.util.Helper;

/**
 * Test class for JavaScript execution tracer (Crawljax plugin).
 * 
 * @author Frank Groeneveld <frankgroeneveld@gmail.com>
 */
public class JSExecutionTracerTest {

	private static WebDriverFirefox browser;
	private static JSExecutionTracer plugin;

	/**
	 * Open browser before tests are started.
	 */
	@BeforeClass
	public static void oneTimeSetUp() {
		plugin = new JSExecutionTracer("daikon.assertions");
		plugin.setOutputFolder("jsexecutiontracertest-output/automaticjsassertions");
		/* FIXME: this doesn't work with current crawljx trunk */
		//browser = new WebDriverFirefox(null, 0, 0);

		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setOutputFolder("jsexecutiontracertest-output");
		CrawlSpecification crawler = new CrawlSpecification("http://pluginte.st/");
		// crawler.clickDefaultElements();

		crawljaxConfiguration.setCrawlSpecification(crawler);

		/* get old files and remove them if they exist */
		List<String> files = plugin.allTraceFiles();
		if (!files.isEmpty()) {
			cleanUpDirectoriesAndFiles();
		}
	}

	/**
	 * Close browser after the tests.
	 */
	@AfterClass
	public static void oneTimeTearDown() {
		try {
			browser.close();
		} catch (Exception e) {
			/* ignore if already closed */
			return;
		}

		/* remove old files */
		cleanUpDirectoriesAndFiles();
	}

	private static void cleanUpDirectoriesAndFiles() {
		/* find all trace files in the trace directory */
		File dir = new File("automaticjsassertions/" + JSExecutionTracer.EXECUTIONTRACEDIRECTORY);
		String[] files = dir.list();
		for (String file : files) {
			File f =
			        new File(Helper.addFolderSlashIfNeeded("automaticjsassertions/"
			                + JSExecutionTracer.EXECUTIONTRACEDIRECTORY)
			                + file);
			f.delete();
		}
		File f = new File("automaticjsassertions/" + "daikon.inv.gz");
		if (f.exists()) {
			f.delete();
		}
		f = new File("automaticjsassertions/" + "daikon.assertions");
		if (f.exists()) {
			f.delete();
		}
		if (dir.exists()) {
			dir.delete();
		}
		dir = new File("automaticjsassertions");
		if (dir.exists()) {
			dir.delete();
		}
	}

	/**
	 * Test the directory checks.
	 */
	@Test
	public void prePreStateAndPost() {
		plugin = new JSExecutionTracer("daikon.assertions");
		plugin.setOutputFolder("automaticjsassertions");
		plugin.preCrawling(browser);
		File directory = new File("automaticjsassertions");

		if (!directory.exists()) {
			fail("Didn't create output directory " + directory.getAbsolutePath());
		}

		directory =
		        new File("automaticjsassertions/" + JSExecutionTracer.EXECUTIONTRACEDIRECTORY);

		if (!directory.exists()) {
			fail("Didn't create output directory " + directory.getAbsolutePath());
		}

		/* run this from here, because order is important */
		preStateCrawling();
	}

	/**
	 * Test execution trace reader.
	 */
	public void preStateCrawling() {
		/* create a fake crawlsession */
		/* FIXME: this doesn't work with current crawljx trunk */
		//CrawlSession session = new CrawlSession(browser);

		/* add a state name (used to make a filename) */
		//session.setCurrentState(new StateVertix("index", "<!-- empty -->"));

		/* add a log entry in the browsers JavaScript runtime */
		String vars =
		        "['a', 'undefined', 'nonsensical'], ['b', 'string', 'string'], "
		                + "['c', 'number', 20], ['d', 'boolean', 'true']";
		String code =
		        AstInstrumenter.JSINSTRUMENTLOGNAME + " = eval([['"
		                + "http://pluginte.st.script0.func', '" + ProgramPoint.ENTERPOSTFIX
		                + "', [" + vars + "]], " + "['http://pluginte.st.script0.func', '"
		                + ProgramPoint.EXITPOSTFIX + "0', [" + vars + "]]]);";

		try {
			browser.executeJavaScript(code);
		} catch (Exception e) {
			fail("Failed setting up JavaScript for test.");
		}

		//plugin.preStateCrawling(session, new ArrayList<CandidateElement>());

		List<String> files = plugin.allTraceFiles();
		if (files.isEmpty()) {
			fail("No execution trace was saved to disk or allTraceFiles function is broken");
		}

		/*
		 * jUnit doesn't have any ordering when calling test functions, so we call the postcrawling
		 * function here.
		 */

		postCrawling();
	}

	/**
	 * Test post crawling function.
	 */
	public void postCrawling() {
		/* create a fake crawlsession */
		/* FIXME: this doesn't work with current crawljx trunk */
		//CrawlSession session = new CrawlSession(browser);

		/* add a state name (used to make a filename) */
		//session.setCurrentState(new StateVertix("index", "<!-- empty -->"));

		//plugin.postCrawling(session);

		File file = new File("automaticjsassertions/daikon.inv.gz");

		assertTrue("Daikon serialized invariant file(" + file.getAbsolutePath()
		        + ") not created.", file.exists());

		file = new File("automaticjsassertions/" + "daikon.assertions");

		assertTrue("Daikon output was not saved to file.", file.exists());

		String results = Helper.getContent(file);

		assertFalse("Daikon output was not saved correctly", "".equals(results));
	}
}
