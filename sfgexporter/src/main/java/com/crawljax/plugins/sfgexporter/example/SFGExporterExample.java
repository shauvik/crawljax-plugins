/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.example;

import java.util.ArrayList;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.plugins.sfgexporter.SFGExporterPlugin;
import com.crawljax.plugins.sfgexporter.transformer.DOTTransformer;
import com.crawljax.plugins.sfgexporter.transformer.Transformer;
import com.crawljax.plugins.sfgexporter.transformer.dot.DOTConfig;
import com.crawljax.plugins.sfgexporter.transformer.dot.DOTConvertedTransformer;
import com.crawljax.plugins.sfgexporter.transformer.dot.EventableNameProvider;

/**
 * SFGExporterExample class.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public final class SFGExporterExample {

	private static final String URL = "http://crawljax.com";
	private static final int MAXSTATES = 15;

	/**
	 * Default empty constructor.
	 */
	private SFGExporterExample() {
	}

	/**
	 * Start the example.
	 * 
	 * @param args
	 *            the arguments specified on the command line.
	 */
	public static void main(String[] args) {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification crawler;
		if (args.length > 0 && args[0] != null) {
			crawler = new CrawlSpecification(args[0]);
		} else {
			crawler = new CrawlSpecification(URL);
		}
		crawler.clickDefaultElements();
		crawler.setMaximumStates(MAXSTATES);
		config.setCrawlSpecification(crawler);

		ArrayList<Transformer> list = new ArrayList<Transformer>();

		// DOTTransformer with EdgeLabelProvider
		DOTTransformer dotTransformer = new DOTTransformer();
		dotTransformer.setEdgeLabelProvider(new EventableNameProvider());
		list.add(dotTransformer);

		// DOTConvertedTransformer using dot output to convert it by dot to png
		DOTConfig dconfig = new DOTConfig();
		dconfig.setOutputFormat(DOTConfig.OutputFormat.png);
		DOTConvertedTransformer t = new DOTConvertedTransformer(dconfig);
		list.add(t);

		SFGExporterPlugin exp = new SFGExporterPlugin(list);
		exp.setOutputFolder("output/");
		config.addPlugin(exp);

		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
