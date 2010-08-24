package com.crawljax.plugins.aji.assertionchecker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.browser.WebDriverFirefox;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StateVertix;
import com.crawljax.plugins.errorreport.ErrorReport;
import com.crawljax.plugins.errorreport.ReportErrorList;

/**
 * JavaScript assertion results plugin tester.
 * 
 * @author Frank Groeneveld
 */
public final class JSAssertionResultsTest {

	private static WebDriverFirefox browser;
	private static JSAssertionResults plugin;
	private static ErrorReport reporter;
	private static Class<? extends ErrorReport> accessibleReporter;

	/**
	 * Open browser before tests are started.
	 */
	@BeforeClass
	public static void oneTimeSetUp() {
		reporter = new ErrorReport("Invariants", "jsexecutiontracertest-output");
		accessibleReporter = reporter.getClass();
		plugin = new JSAssertionResults(reporter);
		browser = new WebDriverFirefox(null, 0, 0);

		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setOutputFolder("jsexecutiontracertest-output");
		CrawlSpecification crawler = new CrawlSpecification("http://pluginte.st/");
		// crawler.clickDefaultElements();

		crawljaxConfiguration.setCrawlSpecification(crawler);

		plugin.preCrawling(browser);
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
	}

	/**
	 * Test the prestatecrawling method.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void preStateCrawling() {
		/* create a fake crawlsession */
		CrawlSession session = new CrawlSession(browser);

		/* add a state name (used to make a filename) */
		session.setCurrentState(new StateVertix("index", "<!-- empty -->"));

		/* add a fake failure to the browser */
		String code =
		        AstAssertionInserter.JSASSERTIONLOGNAME
		                + " = new Array(new Array('typeof(window) == \"undefined\"',"
		                + " 'assertionresultstest', '12'));";

		/* run the code in the browser */
		try {
			browser.executeJavaScript(code);
		} catch (Exception e) {
			fail("Error when setting up fake test results.");
		}

		/* run the actual method */
		plugin.preStateCrawling(session, new ArrayList<CandidateElement>());

		Map<String, ReportErrorList> list = null;

		try {
			Field f = accessibleReporter.getDeclaredField("reportErrors");
			f.setAccessible(true);

			list = (Map<String, ReportErrorList>) f.get(reporter);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to get private field reportErrors");
		}
		assertFalse("Failures were not detected correctly", list.isEmpty());
	}
}
