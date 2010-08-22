/*
    Automatic DOM Invariants is a plugin for Crawljax that can be used to
    derive DOM invariants automatically and use them for regressions
    testing.
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
package com.crawljax.plugins.adi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.util.Helper;

/**
 * Test xpath dom invariants from a file. This class also does some fuzzy matching if there is not
 * exact match to find element that are almost the same (for ex. id="content-container" and
 * id="contentContainer").
 * 
 * @author Frank Groeneveld
 */
public class DomInvariantTester implements OnNewStatePlugin, GeneratesOutput {

	private String outputFolder = "";
	private String inputFolder = "";
	/* the list of XPath expressions */
	private InvariantElement root;

	private Report report = new Report();

	/**
	 * @param inputFolder
	 *            Folder to read the XPath DOM invariants from.
	 */
	public DomInvariantTester(String inputFolder) {
		this.inputFolder = Helper.addFolderSlashIfNeeded(inputFolder);

		// String content = Helper.getContent(new File(inputFolder + "invariants.txt"));
		// String[] invariants = content.split("\n");
		//
		// root = InvariantElement.parseInvariants(invariants);
		//
		// try {
		// report.setControlDom(Helper.getDocument(Helper.getContent(new File(inputFolder
		// + "originaldom.txt"))));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public void onNewState(CrawlSession session) {
		try {

			String hash = DomInvariantFinder.getCrawlPathHash(session.getExactEventPath());

			File file = new File(outputFolder + "inv-" + hash + ".txt");
			if (file.exists()) {
				/* existing invariants */
				String content = Helper.getContent(file);

				String[] invariants = content.split("\n");
				root = InvariantElement.parseInvariants(invariants);

				report.setControlDom(Helper.getDocument(Helper.getContent(new File(inputFolder
				        + "dom-" + hash + ".txt"))));
			} else {
				/* TODO: load the global invariants here */
			}

			Document dom = session.getCurrentState().getDocument();

			report.setTestDom(dom);

			root.check(dom, new ArrayList<List<Node>>(), report);

			report.saveAs(getOutputFolder() + session.getCurrentState().getName() + ".html");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getOutputFolder() {
		return outputFolder;
	}

	@Override
	public void setOutputFolder(String absolutePath) {
		outputFolder = Helper.addFolderSlashIfNeeded(absolutePath);
	}
}
