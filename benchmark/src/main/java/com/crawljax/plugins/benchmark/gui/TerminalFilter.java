/*
 * Copyright (C) 2010 crawljax.com. This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.crawljax.plugins.benchmark.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * This class extends the FileFilter class and can be used to let a user filter out certain files in
 * a fileselectionbox. This class is used to be able to let the use select which Terminal (output
 * type) must be used.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: TerminalFilter.java 5953 2009-12-03 14:21:31Z stefan $
 */
public class TerminalFilter extends FileFilter {

	private final String extension;
	private final String description;

	/**
	 * Init a new filter for a given extension and description.
	 * 
	 * @param ext
	 *            the extension of the files to filter
	 * @param desc
	 *            the description of the file to filter
	 */
	public TerminalFilter(final String ext, final String desc) {
		this.extension = ext;
		this.description = desc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean accept(final File f) {
		if (f.isDirectory()) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getDescription() {
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getExtension() {
		return this.extension;
	}

	/**
	 * Get the extension of a file.
	 * 
	 * @param f
	 *            the file for which the extension needs to be retrieved
	 * @return the extension of the file or null if non
	 */
	public static String getExtensionOfFile(final File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}
