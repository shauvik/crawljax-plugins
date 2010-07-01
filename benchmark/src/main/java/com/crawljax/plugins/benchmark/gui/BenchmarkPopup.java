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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.panayotis.gnuplot.terminal.FileTerminal;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;

/**
 * This class handles the Actions taken on BenchmarkPlots with the mouse and the resulting popup
 * menu.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: BenchmarkPopup.java 5953 2009-12-03 14:21:31Z stefan $
 */
public class BenchmarkPopup extends MouseAdapter implements ActionListener {

	/**
	 * The popup menu to show and/or the handle.
	 */
	private final JPopupMenu popup;

	/**
	 * Init a new Popup Handeling class for a given popupMenu.
	 * 
	 * @param popupMenu
	 *            the menu where this class should handle requests for
	 */
	BenchmarkPopup(final JPopupMenu popupMenu) {
		popup = popupMenu;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void mousePressed(final MouseEvent e) {
		maybeShowPopup(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void mouseReleased(final MouseEvent e) {
		maybeShowPopup(e);
	}

	/**
	 * Based on the e.isPopupTrigger() result the popup is shown.
	 * 
	 * @param e
	 *            the MouseEvent fired
	 */
	private void maybeShowPopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void actionPerformed(final ActionEvent e) {
		JMenuItem i = (JMenuItem) e.getSource();
		JPopupMenu jp = (JPopupMenu) i.getParent();
		BenchmarkPlot bp = (BenchmarkPlot) jp.getInvoker();
		GNUPlotTerminal oldTerminal = bp.getJavaPlot().getTerminal();
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new TerminalFilter("png",
		        "PNG images using libgd and TrueType fonts"));
		fc.addChoosableFileFilter(new TerminalFilter("aifm", "Adobe Illustrator 3.0 Format"));
		fc.addChoosableFileFilter(new TerminalFilter("bitgraph", "BBN Bitgraph Terminal"));
		fc.addChoosableFileFilter(new TerminalFilter("cgm", "Computer Graphics Metafile"));
		fc.addChoosableFileFilter(new TerminalFilter("corel", "EPS format for CorelDRAW"));
		fc.addChoosableFileFilter(new TerminalFilter("dumb",
		        "ascii art for anything that prints text"));
		fc.addChoosableFileFilter(new TerminalFilter("dxf",
		        "dxf-file for AutoCad (default size 120x80)"));
		fc.addChoosableFileFilter(new TerminalFilter("eepic",
		        "EEPIC -- extended LaTeX picture environment"));
		fc.addChoosableFileFilter(new TerminalFilter("emf", "Enhanced Metafile format"));
		fc.addChoosableFileFilter(new TerminalFilter("emtex",
		        "LaTeX picture environment with emTeX specials"));
		fc.addChoosableFileFilter(new TerminalFilter("epslatex",
		        "LaTeX picture environment using graphicx package"));
		fc.addChoosableFileFilter(new TerminalFilter("fig",
		        "FIG graphics language for XFIG graphics editor"));
		fc.addChoosableFileFilter(new TerminalFilter("gif",
		        "GIF images using libgd and TrueType fonts"));
		fc.addChoosableFileFilter(new TerminalFilter("gpic",
		        "GPIC -- Produce graphs in groff using the gpic preprocessor"));
		fc.addChoosableFileFilter(new TerminalFilter("jpeg",
		        "JPEG images using libgd and TrueType fonts"));
		fc.addChoosableFileFilter(new TerminalFilter("latex", "LaTeX picture environment"));
		fc.addChoosableFileFilter(new TerminalFilter("pbm",
		        "Portable bitmap [small medium large] [monochrome gray color]"));
		fc.addChoosableFileFilter(new TerminalFilter("postscript",
		        "PostScript graphics, including EPSF embedded files (*.eps)"));
		fc.addChoosableFileFilter(new TerminalFilter("pslatex",
		        "LaTeX picture environment with PostScript \\specials"));
		fc.addChoosableFileFilter(new TerminalFilter("pstex",
		        "plain TeX with PostScript \\specials"));
		fc.addChoosableFileFilter(new TerminalFilter("pstricks",
		        "LaTeX picture environment with PSTricks macros"));
		fc.addChoosableFileFilter(new TerminalFilter("regis", "REGIS graphics language"));
		fc.addChoosableFileFilter(new TerminalFilter("selanar", "Selanar"));
		fc.addChoosableFileFilter(new TerminalFilter("svg",
		        "W3C Scalable Vector Graphics driverv"));
		fc.addChoosableFileFilter(new TerminalFilter("texdraw", "LaTeX texdraw environment"));
		fc.addChoosableFileFilter(new TerminalFilter("tgif",
		        "TGIF X11 [mode] [x,y] [dashed] [\"font\" [fontsize]]"));
		fc.addChoosableFileFilter(new TerminalFilter("tkcanvas",
		        "Tk/Tcl canvas widget [perltk] [interactive]"));
		fc.addChoosableFileFilter(new TerminalFilter("tpic",
		        "TPIC -- LaTeX picture environment with tpic \\specials"));
		if (fc.showSaveDialog(bp) == JFileChooser.APPROVE_OPTION) {
			/* Retrieve the file to get stored */
			File f = fc.getSelectedFile();

			/* prepare the filename.ext */
			String fullPath = f.getAbsolutePath();
			String ext = TerminalFilter.getExtensionOfFile(f);
			TerminalFilter ff = (TerminalFilter) fc.getFileFilter();
			if (ext == null || !ext.equals(ff.getExtension())) {
				fullPath += "." + ff.getExtension();
			}

			/* Setup a new terminal and plot */
			FileTerminal ft = new FileTerminal(ff.getExtension(), fullPath);
			bp.getJavaPlot().setTerminal(ft);
			bp.getJavaPlot().plot();

			/* Restore to the old terminal and repaint */
			bp.getJavaPlot().setTerminal(oldTerminal);
			bp.repaint();
		}
	}
}
