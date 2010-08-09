/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.transformer;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;

/**
 * Exports a graph into a DOT file.
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/DOT_language">
 * http://en.wikipedia.org/wiki/DOT_language</a>.
 * </p>
 * This code is direct copy of the {@link org.jgrapht.ext.DOTExporter} but from the 0.8.1 version.
 * 
 * @author Trevor Harmon
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: DOTTransformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public class DOTTransformer extends ToFileTransformer {

	private VertexNameProvider<StateVertix> vertexIDProvider;
	private VertexNameProvider<StateVertix> vertexLabelProvider;
	private EdgeNameProvider<Eventable> edgeLabelProvider;

	/**
	 * Constructs a new DOTTransformer object with an integer name provider for the vertex IDs and
	 * null providers for the vertex and edge labels.
	 */
	public DOTTransformer() {
		this(new IntegerNameProvider<StateVertix>(), null, null);
	}

	/**
	 * Constructs a new DOTTransformer object with the given ID and label providers.
	 * 
	 * @param vertexIDProvider
	 *            for generating vertex IDs. Must not be null.
	 * @param vertexLabelProvider
	 *            for generating vertex labels. If null, vertex labels will not be written to the
	 *            file.
	 * @param edgeLabelProvider
	 *            for generating edge labels. If null, edge labels will not be written to the file.
	 */
	public DOTTransformer(VertexNameProvider<StateVertix> vertexIDProvider,
	        VertexNameProvider<StateVertix> vertexLabelProvider,
	        EdgeNameProvider<Eventable> edgeLabelProvider) {
		this.vertexIDProvider = vertexIDProvider;
		this.vertexLabelProvider = vertexLabelProvider;
		this.edgeLabelProvider = edgeLabelProvider;
	}

	/**
	 * Exports a graph into a plain text file in DOT format.
	 * 
	 * @param g
	 *            the graph to be exported
	 * @return the String representation of the Graph
	 */
	public String transform(Graph<StateVertix, Eventable> g) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		String indent = "  ";
		String connector;

		if (g instanceof DirectedGraph<?, ?>) {
			out.println("digraph G {");
			connector = " -> ";
		} else {
			out.println("graph G {");
			connector = " -- ";
		}

		for (StateVertix v : g.vertexSet()) {
			out.print(indent + getVertexID(v));

			if (vertexLabelProvider != null) {
				out.print(" [label = \"" + vertexLabelProvider.getVertexName(v) + "\"]");
			}

			out.println(";");
		}

		for (Eventable e : g.edgeSet()) {
			String source = getVertexID(g.getEdgeSource(e));
			String target = getVertexID(g.getEdgeTarget(e));

			out.print(indent + source + connector + target);

			if (edgeLabelProvider != null) {
				out.print(" [label = \"" + edgeLabelProvider.getEdgeName(e) + "\"]");
			}

			out.println(";");
		}

		out.println("}");

		out.flush();
		return writer.toString();
	}

	/**
	 * Return a valid vertex ID (with respect to the .dot language definition as described in
	 * http://www.graphviz.org/doc/info/lang.html Quoted from above mentioned source: An ID is valid
	 * if it meets one of the following criteria:
	 * <ul>
	 * <li>any string of alphabetic characters, underscores or digits, not beginning with a digit;
	 * <li>a number [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? );
	 * <li>any double-quoted string ("...") possibly containing escaped quotes (\");
	 * <li>an HTML string (<...>).
	 * </ul>
	 * 
	 * @throws RuntimeException
	 *             if the given <code>vertexIDProvider</code> didn't generate a valid vertex ID.
	 */
	private String getVertexID(StateVertix v) {
		// TODO jvs 28-Jun-2008: possible optimizations here are
		// (a) only validate once per vertex
		// (b) compile regex patterns

		// use the associated id provider for an ID of the given vertex
		String idCandidate = vertexIDProvider.getVertexName(v);

		// now test that this is a valid ID
		boolean isAlphaDig = idCandidate.matches("[a-zA-Z]+([\\w_]*)?");
		boolean isDoubleQuoted = idCandidate.matches("\".*\"");
		boolean isDotNumber = idCandidate.matches("[-]?([.][0-9]+|[0-9]+([.][0-9]*)?)");
		boolean isHTML = idCandidate.matches("<.*>");

		if (isAlphaDig || isDotNumber || isDoubleQuoted || isHTML) {
			return idCandidate;
		}

		throw new RuntimeException("Generated id '" + idCandidate + "'for vertex '" + v
		        + "' is not valid with respect to the .dot language");
	}

	@Override
	public String getExtension() {
		return "dot";
	}

	/**
	 * @param vertexIDProvider
	 *            the vertexIDProvider to set
	 */
	public final void setVertexIDProvider(VertexNameProvider<StateVertix> vertexIDProvider) {
		this.vertexIDProvider = vertexIDProvider;
	}

	/**
	 * @param vertexLabelProvider
	 *            the vertexLabelProvider to set
	 */
	public final void setVertexLabelProvider(VertexNameProvider<StateVertix> vertexLabelProvider) {
		this.vertexLabelProvider = vertexLabelProvider;
	}

	/**
	 * @param edgeLabelProvider
	 *            the edgeLabelProvider to set
	 */
	@Override
	public final void setEdgeLabelProvider(EdgeNameProvider<Eventable> edgeLabelProvider) {
		this.edgeLabelProvider = edgeLabelProvider;
	}

	@Override
	public EdgeNameProvider<Eventable> getEdgeLabelProvider() {
		return edgeLabelProvider;
	}
}