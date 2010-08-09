/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.transformer;

import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.ext.VisioExporter;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;

/**
 * This Transformer Exports a graph to a csv format that can be imported into MS Visio. It uses the
 * {@link org.jgrapht.ext.VisioExporter}
 * <p>
 * <b>Tip:</b> By default, the exported graph doesn't show link directions. To show link directions:
 * <br>
 * <ol>
 * <li>Select All (Ctrl-A)</li>
 * <li>Right Click the selected items</li>
 * <li>Format/Line...</li>
 * <li>Line ends: End: (choose an arrow)</li>
 * </ol>
 * </p>
 * 
 * @see org.jgrapht.ext.VisioExporter#export(java.io.OutputStream, Graph)
 * @author Avner Linder
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: VisioTransformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public class VisioTransformer extends ToFileTransformer {

	private static final Logger LOGGER = Logger.getLogger(VisioTransformer.class);

	@Override
	public String transform(Graph<StateVertix, Eventable> g) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		LOGGER.debug("VisioExporter does not support custom label providers, not using them");
		VisioExporter<StateVertix, Eventable> realExporter =
		        new VisioExporter<StateVertix, Eventable>();
		realExporter.export(out, g);
		return out.toString();
	}

	@Override
	public String getExtension() {
		return "csv";
	}
}