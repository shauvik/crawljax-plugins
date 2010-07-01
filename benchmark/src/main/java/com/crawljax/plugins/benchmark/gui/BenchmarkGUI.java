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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.crawljax.plugins.benchmark.configuration.BenchmarkConfiguration;
import com.crawljax.plugins.benchmark.dataset.BenchmarkStorage;
import com.crawljax.plugins.benchmark.dataset.BenchmarkDataset.PlotType;
import com.panayotis.gnuplot.GNUPlotException;

/**
 * The Main GUI of the Benchmark Plugin.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: BenchmarkGUI.java 5953 2009-12-03 14:21:31Z stefan $
 */
public final class BenchmarkGUI extends JFrame {

	/**
	 * Generated serialVersionUID.
	 */
	private static final long serialVersionUID = -2412859185365548449L;

	/**
	 * The instance of the window.
	 */
	private static BenchmarkGUI instance;

	/**
	 * The main panel on which the plots are shown and added.
	 */
	private final JPanel graphPanel;

	/**
	 * Default width of the GUI.
	 */
	private static final int WIDTH = 1280;

	/**
	 * Default height of the GUI.
	 */
	private static final int HEIGHT = 1224;

	private final JComboBox xBox;
	private final JComboBox yBox;
	private final JTextField titleField;
	private final JTextField labelField;

	private final JCheckBox cumulativeCheck;
	private final AtomicBoolean isUpdating = new AtomicBoolean(false);

	/**
	 * Init a new Window (JFrame) which displays all the graphs added to it. Becarefull exit of the
	 * window ends the crawl-session
	 * 
	 * @param config
	 */
	private BenchmarkGUI(final BenchmarkConfiguration config) {
		super("Crawljax Benchmark GUI");

		final BenchmarkStorage store = BenchmarkStorage.instance(config);

		this.getContentPane().setLayout(new BorderLayout());

		/* Graph Panel */
		this.graphPanel = new JPanel();
		this.graphPanel.setLayout(new GridLayout(2, 2));

		/* Button Panel */
		JPanel buttonPanel = new JPanel();
		// buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setLayout(new GridLayout(5, 2));

		// X-Axis
		buttonPanel.add(new JLabel("x-Axis:"));
		xBox = new JComboBox(PlotType.values());
		buttonPanel.add(xBox);

		// Y-Axis
		buttonPanel.add(new JLabel("y-Axis:"));
		yBox = new JComboBox(PlotType.values());
		buttonPanel.add(yBox);

		// Title
		buttonPanel.add(new JLabel("Title:"));
		titleField = new JTextField();
		buttonPanel.add(titleField);

		// Label
		buttonPanel.add(new JLabel("Label:"));
		labelField = new JTextField();
		buttonPanel.add(labelField);

		// Checkbox
		cumulativeCheck = new JCheckBox("Cumulative data", true);

		// Add button
		JButton b = new JButton("ADD");
		try {
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PlotType xAxis = (PlotType) xBox.getSelectedItem();
					PlotType yAxis = (PlotType) yBox.getSelectedItem();
					// TODO implement the tight settings
					BenchmarkPlot plot =
					        new BenchmarkPlot(titleField.getText(), labelField.getText(),
					                cumulativeCheck.getSelectedObjects() == null ? store
					                        .getRecords() : store.getSumRecords(), xAxis, yAxis,
					                config, false, false);

					// Add the save / delete mouse listener
					plot.addMouseListener(buildMouseListener());

					graphPanel.add(plot);
					graphPanel.updateUI(); // make the new graph visible
				}
			});
			buttonPanel.add(b);
			buttonPanel.add(cumulativeCheck);

		} catch (GNUPlotException e) {
			JOptionPane.showMessageDialog(null, "GnuPlot location " + config.getGnuPlotLocation()
			        + "\n not set correct:\n" + e.getMessage());
		}

		JPanel shrinkPanel = new JPanel();
		shrinkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		shrinkPanel.add(buttonPanel);

		this.getContentPane().add(new JScrollPane(this.graphPanel), BorderLayout.CENTER);
		this.getContentPane().add(shrinkPanel, BorderLayout.SOUTH);
		this.setSize(BenchmarkGUI.WIDTH, BenchmarkGUI.HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

	}

	/**
	 * create a new instance of the BenchmarkGUI class.
	 * 
	 * @param config
	 *            the configuration to use when creating a new instance
	 * @return a new Window or the already running window
	 */
	public static synchronized BenchmarkGUI instance(BenchmarkConfiguration config) {
		if (instance == null) {
			instance = new BenchmarkGUI(config);
		}
		return instance;
	}

	/**
	 * init the possible plots in a Vector.
	 * 
	 * @return a Vector filled with possible graphs
	 */
	private BenchmarkPopup buildMouseListener() {
		// Create the popup menu.
		JPopupMenu popup = new JPopupMenu();

		// Add listener to the text area so the popup menu can come up.
		BenchmarkPopup popupListener = new BenchmarkPopup(popup);

		JMenuItem menuItem = new JMenuItem("Save");
		menuItem.addActionListener(popupListener);
		popup.add(menuItem);

		menuItem = new JMenuItem("Delete");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				/*
				 * this is not placed in separate function because to many work
				 */
				JMenuItem i = (JMenuItem) e.getSource();
				JPopupMenu jp = (JPopupMenu) i.getParent();
				graphPanel.remove(jp.getInvoker());
				graphPanel.updateUI();
			}
		});

		popup.add(menuItem);

		return popupListener;
	}

	@Override
	public void repaint() {
		if (!isUpdating.compareAndSet(false, true)) {
			return;
		}
		super.repaint();
		isUpdating.set(false);
	}
}
