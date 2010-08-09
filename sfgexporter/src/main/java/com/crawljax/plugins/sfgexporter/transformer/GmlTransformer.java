/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.transformer;

import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.GmlExporter;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;

/**
 * Exports a graph into a GML file (Graph Modelling Language).{@link org.jgrapht.ext.GmlExporter}.
 * <p>
 * For a description of the format see <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www.infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
 * </p>
 * <p>
 * The objects associated with vertices and edges are exported as labels using their toString()
 * implementation. See the {@link #setPrintLabels(Integer)} method. The default behavior is to
 * export no label information.
 * </p>
 * 
 * @see org.jgrapht.ext.GmlExporter#export(java.io.Writer, DirectedGraph)
 * @see org.jgrapht.ext.GmlExporter#export(java.io.Writer, UndirectedGraph)
 * @author Dimitrios Michail
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: GmlTransformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public class GmlTransformer extends ToFileTransformer {
	private static final Logger LOGGER = Logger.getLogger(GmlTransformer.class);

	@Override
	public String transform(Graph<StateVertix, Eventable> g) {
		StringWriter writer = new StringWriter();
		LOGGER.debug("GmlExported does not support custom label providers, not using them");
		GmlExporter<StateVertix, Eventable> realExporter =
		        new GmlExporter<StateVertix, Eventable>();
		if (g instanceof DirectedGraph<?, ?>) {
			realExporter.export(writer, (DirectedGraph<StateVertix, Eventable>) g);
		} else if (g instanceof UndirectedGraph<?, ?>) {
			realExporter.export(writer, (UndirectedGraph<StateVertix, Eventable>) g);
		} else {
			LOGGER.error("The given graph was not a Directed or Undirected Graph; can't export");
			throw new RuntimeException(
			        "The given graph was not a Directed or Undirected Graph; can't export");
		}
		return writer.toString();
	}

	@Override
	public String getExtension() {
		return "gml";
	}
}