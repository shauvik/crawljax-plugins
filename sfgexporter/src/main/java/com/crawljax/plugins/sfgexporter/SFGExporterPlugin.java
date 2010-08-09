/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.plugins.sfgexporter.transformer.Transformer;

/**
 * This class collects the data used while benchmarking.
 * 
 * @version $Id: SFGExporterPlugin.java 7009 2010-04-19 12:51:55Z stefan $
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public class SFGExporterPlugin implements PostCrawlingPlugin, GeneratesOutput {

	private String outputFolder;
	private final ArrayList<Transformer> list;
	private static final Logger LOGGER = Logger.getLogger(SFGExporterPlugin.class);

	/**
	 * The default constructor for the SFGExporterPlugin.
	 * 
	 * @param transformers
	 *            the list of Transformers to run
	 */
	public SFGExporterPlugin(ArrayList<Transformer> transformers) {
		this.list = transformers;
	}

	@Override
	public void postCrawling(CrawlSession session) {
		LOGGER.info("Starting the Export(s)");
		for (Transformer transformer : this.list) {
			LOGGER.info("Starting transformer " + transformer.getClass().getName());
			transformer.export(this.getOutputFolder(), session.getStateFlowGraph().getSfg());
			LOGGER.info("Transformer " + transformer.getClass().getName()
			        + " is done transforming");
		}
	}

	@Override
	public String getOutputFolder() {
		if (outputFolder == null) {
			return "output";
		}
		return outputFolder;
	}

	@Override
	public void setOutputFolder(String absolutePath) {
		outputFolder = absolutePath;
	}

}
