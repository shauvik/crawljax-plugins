/**
 * Copyright [2010] [Stefan Lenselink, Ali Mesbah] Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */
package com.crawljax.plugins.sfgexporter.transformer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.ext.EdgeNameProvider;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertix;

/**
 * This abstract class implements the export method, it calles the not implemented transform
 * function. It also implements a field (with get/setters) to specify the filename. If no filename
 * is specified "graph" is used as filename.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: ToFileTransformer.java 6576 2010-01-12 15:38:57Z stefan $
 */
public abstract class ToFileTransformer implements Transformer {

	private static final Logger LOGGER = Logger.getLogger(ToFileTransformer.class);
	private String filename;
	private EdgeNameProvider<Eventable> edgeNameProvider;

	@Override
	public void export(String outputFolder, Graph<StateVertix, Eventable> g) {
		File parentDir = new File(outputFolder);
		if (parentDir.isFile()) {
			LOGGER.error("The directory specified as outputFolder (" + outputFolder
			        + ") is a file");
			throw new RuntimeException("Denoted outputFolder " + outputFolder + " is a File!");
		} else if (!parentDir.exists()) {
			if (!parentDir.mkdirs()) {
				LOGGER.error("The directory specified as outputFolder (" + outputFolder
				        + ") cannot be created");
				throw new RuntimeException("Denoted outputFolder " + outputFolder
				        + " can not be created");
			}
		}

		File outputFile =
		        new File(outputFolder + File.separator + this.getFilename() + "."
		                + this.getExtension());
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			PrintWriter pw = new PrintWriter(fos);

			pw.println(this.transform(g));
			pw.flush();
			pw.close();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("File not found", e);
		} catch (IOException e) {
			LOGGER.error("IO Exception", e);
		}
	}

	/**
	 * Getter for the filename of the Transformed result.
	 * 
	 * @return the filename used to write the result to
	 */
	public final String getFilename() {
		if (filename == null) {
			return "graph";
		}
		return filename;
	}

	/**
	 * Setter for the filename of the Transformed result.
	 * 
	 * @param filename
	 *            the filename to be used to write the result to
	 */
	public final void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public void setEdgeLabelProvider(EdgeNameProvider<Eventable> edgeNameProvider) {
		this.edgeNameProvider = edgeNameProvider;
	}

	@Override
	public EdgeNameProvider<Eventable> getEdgeLabelProvider() {
		return edgeNameProvider;
	}

}
