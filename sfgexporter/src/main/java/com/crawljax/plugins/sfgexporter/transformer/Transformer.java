/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.transformer;

import org.jgrapht.Graph;
import org.jgrapht.ext.EdgeNameProvider;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;

/**
 * This interface denotes a Transformer. It contains three methods the transform method transforms a
 * given Graph into a String. The export method writes the transfromed result of the transfrom
 * method to a file in the output directory. the getExtension method is used to derive the extension
 * this Transformer converts into.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: Transformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public interface Transformer {

	/**
	 * Convert the Graph to the String contentens of the new format.
	 * 
	 * @param g
	 *            the graph which needs to be transformed
	 * @return the String representation of the transformed graph
	 */
	String transform(Graph<StateVertix, Eventable> g);

	/**
	 * Export the given Graph in the format handeled by this Transformer in the given outputFolder.
	 * 
	 * @param outputFolder
	 *            the folder where to store the transformed graph
	 * @param g
	 *            the graph which needs to be transformed
	 */
	void export(String outputFolder, Graph<StateVertix, Eventable> g);

	/**
	 * Return the extension for the Transformed file.
	 * 
	 * @return the extension for the Transformed file
	 */
	String getExtension();

	/**
	 * Set the edge name provider to use. If null is specified default wil be used.
	 * 
	 * @param edgeNameProvider
	 *            the edge name provider
	 */
	void setEdgeLabelProvider(EdgeNameProvider<Eventable> edgeNameProvider);

	/**
	 * Return the Edge name provider to use, null may also be returned if default must be used.
	 * 
	 * @return the label name provider to use
	 */
	EdgeNameProvider<Eventable> getEdgeLabelProvider();
}
