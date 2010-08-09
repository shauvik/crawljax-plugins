/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.transformer.dot;

import org.jgrapht.ext.EdgeNameProvider;

import com.crawljax.core.state.Eventable;

/**
 * This class is designed to find a better name for the Edges used in Crawljax.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: EventableNameProvider.java 6576 2010-01-12 15:38:57Z stefan $
 */
public class EventableNameProvider implements EdgeNameProvider<Eventable> {

	@Override
	public String getEdgeName(Eventable edge) {
		if (edge.getElement() != null && !edge.getElement().getText().equals("")) {
			return edge.getElement().getText();
		}
		return edge.toString();
	}

}
