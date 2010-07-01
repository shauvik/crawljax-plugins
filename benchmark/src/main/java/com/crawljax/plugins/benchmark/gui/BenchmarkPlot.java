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

import java.awt.Graphics;
import java.util.Hashtable;

import com.crawljax.plugins.benchmark.configuration.BenchmarkConfiguration;
import com.crawljax.plugins.benchmark.dataset.BenchmarkDataset;
import com.crawljax.plugins.benchmark.dataset.BenchmarkDataset.PlotType;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.JavaPlot.Key;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Smooth;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.swing.JPlot;

/**
 * This class extends the JPlot (from the JavaPlot package) class and rewrites its paint function.
 * Also it initialises all titles and other stuff. Replotting is done by the general Java Swing
 * repaint methods
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: BenchmarkPlot.java 5953 2009-12-03 14:21:31Z stefan $
 */
public class BenchmarkPlot extends JPlot {

	private final Hashtable<Integer, String> plotCache = new Hashtable<Integer, String>();

	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = -5908661565181673511L;

	/**
	 * Here comes the data from.
	 */
	private final BenchmarkDataset dataset;

	/**
	 * The title of the the plot.
	 */
	private final String plotTitle;

	/**
	 * The configuration object where to read the config from.
	 */
	private final BenchmarkConfiguration config;

	private final PlotType xAxis;

	private final PlotType yAxis;

	/**
	 * Init a new Plot Pannel with the given data.
	 * 
	 * @param title
	 *            the title of the Plot
	 * @param xLabel
	 *            the label on the xAxses
	 * @param yLabel
	 *            the label on the yAxses
	 * @param data
	 *            the dataset containter
	 * @param type
	 *            the type of Plot (Runtime, Memory, etc)
	 * @param config
	 *            the configuration object where the config can be retrieved from
	 * @param tightY
	 * @param tightX
	 */
	public BenchmarkPlot(final String title, final String label, final BenchmarkDataset data,
	        final PlotType xAxis, final PlotType yAxis, BenchmarkConfiguration config,
	        boolean tightY, boolean tightX) {
		super();
		synchronized (data.getDatasetLock()) {
			data.setCurrentPlot(this);

			this.config = config;
			this.dataset = data;
			this.plotTitle = title;
			this.xAxis = xAxis;
			this.yAxis = yAxis;

			/**
			 * Process the Styling == make it lines styled
			 */
			PlotStyle ps = new PlotStyle();
			ps.setStyle(Style.LINES);

			DataSetPlot dp = new DataSetPlot(this.dataset);
			dp.setTitle(label);
			dp.setPlotStyle(ps);

			// TODO config smooth
			dp.setSmooth(Smooth.SBEZIER);

			JavaPlot jp = new JavaPlot(this.config.getGnuPlotLocation());
			jp.setTitle(title);
			jp.getAxis("x").setLabel(xAxis.getLabel());
			jp.getAxis("y").setLabel(yAxis.getLabel());
			if (tightY) {
				double minY = dataset.getMinY();
				jp.getAxis("y").setBoundaries(minY > 0 ? minY - 1 : minY, dataset.getMaxY() + 1);
			}
			if (tightX) {
				double minX = dataset.getMinX();
				jp.getAxis("x").setBoundaries(minX > 0 ? minX - 1 : minX, dataset.getMaxX() + 1);
			}

			// Legend location
			jp.setKey(Key.TOP_LEFT);
			jp.addPlot(dp);

			this.setJavaPlot(jp);
			this.repaint();
		}
	}

	/**
	 * Do the paint. Calling the super method for all the JavaPlot magik but we first do a set of
	 * the plot type we want (this will be changed all the time) and doing a plot. The plot will
	 * only be done when there is atleast two entries otherwise JavaPlot crashes.
	 * 
	 * @param g
	 *            Graphics container to paint on
	 */
	@Override
	public final void paint(final Graphics g) {

		if (this.dataset.size() > 1) {
			synchronized (this.dataset.getDatasetLock()) {

				// Setup the correct type
				this.dataset.setCurrentPlot(this);

				this.plot();
				super.paint(g);
			}
		}
	}

	/**
	 * create a String repsentation of this Plot.
	 * 
	 * @return the Title of the plot
	 */
	@Override
	public final String toString() {
		return this.plotTitle;
	}

	public String getCachedPoint(int point) {
		return plotCache.get(point);
	}

	public void storeCachedPoint(int point, String cache) {
		plotCache.put(point, cache);
	}

	/**
	 * @return the xAxis
	 */
	public final PlotType getXAxis() {
		return xAxis;
	}

	/**
	 * @return the yAxis
	 */
	public final PlotType getYAxis() {
		return yAxis;
	}
}
