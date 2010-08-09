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

import javax.xml.transform.TransformerConfigurationException;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.xml.sax.SAXException;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;

/**
 * Exports a graph into a GraphML file. It uses the {@link org.jgrapht.ext.GraphMLExporter}.
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/GraphML">
 * http://en.wikipedia.org/wiki/GraphML</a>.
 * </p>
 * 
 * @see org.jgrapht.ext.GraphMLExporter#export(java.io.Writer, Graph)
 * @author Trevor Harmon
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: GraphMLTransformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public class GraphMLTransformer extends ToFileTransformer {
	private static final Logger LOGGER = Logger.getLogger(GraphMLTransformer.class);

	@Override
	public String transform(Graph<StateVertix, Eventable> g) {
		StringWriter writer = new StringWriter();
		GraphMLExporter<StateVertix, Eventable> realExporter =
		        new GraphMLExporter<StateVertix, Eventable>(
		                new IntegerNameProvider<StateVertix>(), null,
		                new IntegerEdgeNameProvider<Eventable>(), this.getEdgeLabelProvider());
		try {
			realExporter.export(writer, g);
		} catch (TransformerConfigurationException e) {
			LOGGER.error("XML exeption while exporting", e);
		} catch (SAXException e) {
			LOGGER.error("SAX XML exeption while exporting", e);
		}
		return writer.toString();
	}

	@Override
	public String getExtension() {
		return "graphml";
	}
}