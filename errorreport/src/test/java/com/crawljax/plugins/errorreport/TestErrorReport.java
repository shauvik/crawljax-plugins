package com.crawljax.plugins.errorreport;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverFirefox;
import com.crawljax.condition.UrlCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.StateVertix;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;

import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple test class to demonstrate how to use ErrorReport.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class TestErrorReport {

	private static final String URL = "http://crawljax.com";
	private static final int MAX_EVENTABLES = 3;

	/**
	 * @throws Exception
	 *             when something fails
	 */
	@Test
	public void testErrorReport() throws Exception {

		// setting everything up
		//TODO switch when in 2.0-SNAPSHOT
		//WebDriverBackedEmbeddedBrowser browser = WebDriverBackedEmbeddedBrowser
		//		.withDriver(new FirefoxDriver());
		EmbeddedBrowser browser = new WebDriverFirefox(new ArrayList(), 0, 0);
		browser.goToUrl(URL);
		String html = browser.getDom();
		String strippedHtml = html.replace("Accessibility", "");
		StateVertix originalState = new StateVertix("test", html);

		String differentHtml = html.replace("Plugins", "Change");
		differentHtml = differentHtml.replace("NeoEase", "<b>Foo</b>NeoEase");
		StateVertix differentState = new StateVertix("State5", differentHtml);

		Document dom = Helper.getDocument(html);

		Node aLink = Helper.getElementByXpath(dom, "//A[contains(text(), 'Documentation')]");
		NodeList links = XPathHelper.evaluateXpathExpression(dom, "//A");
		List<Eventable> pathToFailure = new ArrayList<Eventable>();
		for (int i = 0; i < MAX_EVENTABLES; i++) {
			pathToFailure.add(new Eventable(links.item(i), EventType.click));
		}

		// create a new report
		ErrorReport report = new ErrorReport("Test");

		// expression which is evaluated every time a failure is added
		report.setJavascriptExpressions("document.title");

		// adding different types of failures
		report.addInvariantViolation(new Invariant("No error messages",
				new UrlCondition("foo")), browser);
		report.addInvariantViolation(new Invariant(
		        "List should always have more than one item", new UrlCondition("foo")), browser);
		report.addEventFailure(new Eventable(aLink, EventType.click),
				originalState, browser);

		report.addStateFailure(strippedHtml, differentState, pathToFailure,
				browser);

		report
		        .addFailure(new ReportError("Timeouts", "Timeout state3").includeOriginalState(
		                originalState).dontIncludeScreenshots(), browser);

		// generate the report
		report.generate();
		browser.close();
	}
}
