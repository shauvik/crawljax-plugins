/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.transformer.dot;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;
import com.crawljax.plugins.sfgexporter.transformer.DOTTransformer;

/**
 * This Transformer is used to do conversions based on the exported dot file. It use dot from the
 * graphvis package to do conversions. All conversions and other options are specified in
 * {@link DOTConfig}. For more information on graphvis see <a href="http://www.graphviz.org/">the
 * Graphvis website</a>
 * 
 * @see DOTConfig
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: DOTConvertedTransformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public class DOTConvertedTransformer extends DOTTransformer {
	private static final Logger LOGGER = Logger.getLogger(DOTConvertedTransformer.class);
	private final DOTConfig config;

	/**
	 * Create the Transformer for the given DOTConfig.
	 * 
	 * @param config
	 *            the object where to read the config from
	 */
	public DOTConvertedTransformer(DOTConfig config) {
		this.config = config;
	}

	/**
	 * Default constructor using all the default settings set in DOTConfig.
	 */
	public DOTConvertedTransformer() {
		this(new DOTConfig());
	}

	@Override
	public void export(String outputFolder, Graph<StateVertix, Eventable> g) {
		// First call the super export that will create the used dot file
		super.export(outputFolder, g);

		String file =
		        outputFolder + File.separator + this.getFilename() + "." + this.getExtension();
		String cmdLine = config.buildCommandline(file);
		try {
			LOGGER.debug("Going to run: " + cmdLine);
			Runtime.getRuntime().exec(cmdLine).waitFor();
		} catch (InterruptedException e) {
			LOGGER.warn("The execution of " + cmdLine + " is interupted", e);
		} catch (IOException e) {
			LOGGER.error("There was an error executing " + cmdLine, e);
		}
	}
}