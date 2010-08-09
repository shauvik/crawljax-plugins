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
import org.jgrapht.ext.MatrixExporter;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;

/**
 * This Transformer transforms a Graph to the Matrix format used in MathLab. It uses the
 * {@link org.jgrapht.ext.MatrixExporter}. Exports a graph to a plain text matrix format, which can
 * be processed by matrix manipulation software, such as <a href="http://rs.cipr.uib.no/mtj/">
 * MTJ</a> or <a href="http://www.mathworks.com/products/matlab/">MATLAB</a>.
 * 
 * @see org.jgrapht.ext.MatrixExporter#exportAdjacencyMatrix(java.io.Writer, DirectedGraph)
 * @see org.jgrapht.ext.MatrixExporter#exportAdjacencyMatrix(java.io.Writer, UndirectedGraph)
 * @author Charles Fry
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: MatrixTransformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public class MatrixTransformer extends ToFileTransformer {
	private static final Logger LOGGER = Logger.getLogger(MatrixTransformer.class);

	@Override
	public String transform(Graph<StateVertix, Eventable> g) {
		StringWriter writer = new StringWriter();
		LOGGER.debug("MatrixExport does not support custom label providers, not using them");
		MatrixExporter<StateVertix, Eventable> realExporter =
		        new MatrixExporter<StateVertix, Eventable>();

		if (g instanceof DirectedGraph<?, ?>) {
			realExporter.exportAdjacencyMatrix(writer, (DirectedGraph<StateVertix, Eventable>) g);
		} else if (g instanceof UndirectedGraph<?, ?>) {
			realExporter.exportAdjacencyMatrix(writer,
			        (UndirectedGraph<StateVertix, Eventable>) g);
		} else {
			LOGGER.error("The given graph was not a Directed or Undirected Graph; can't export");
			throw new RuntimeException(
			        "The given graph was not a Directed or Undirected Graph; can't export");
		}
		return writer.toString();
	}

	@Override
	public String getExtension() {
		return "matrix";
	}
}