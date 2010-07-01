/*
 * Copyright (C) 2010 crawljax.com. This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.crawljax.plugins.benchmark;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnRevisitStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.StateVertix;
import com.crawljax.plugins.benchmark.configuration.BenchmarkConfiguration;
import com.crawljax.plugins.benchmark.gui.BenchmarkGUI;

/**
 * Displays the main GUI to generate the figures / graphs and updates the graphs.
 * 
 * @version $Id: BenchmarkGUIPlugin.java 5979 2009-12-07 10:40:02Z stefan $
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public class BenchmarkGUIPlugin
        implements OnNewStatePlugin, OnRevisitStatePlugin, PostCrawlingPlugin {

	/**
	 * This is bad, no way about it...
	 */
	private final BenchmarkConfiguration config;

	/**
	 * The default constructor for the GUI.
	 * 
	 * @param config
	 *            the config object to use to read the config from
	 */
	public BenchmarkGUIPlugin(BenchmarkConfiguration config) {
		this.config = config;
	}

	/**
	 * Start and / or update the GUI on new State. {@inheritDoc}
	 */
	@Override
	public void onNewState(CrawlSession session) {
		BenchmarkGUI.instance(config).repaint();
	}

	/**
	 * Start and / or update the GUI on revisitedState. {@inheritDoc}
	 */
	@Override
	public void onRevisitState(CrawlSession session, StateVertix currentState) {
		BenchmarkGUI.instance(config).repaint();
	}

	/**
	 * Start and / or update the GUI on finish. {@inheritDoc}
	 */
	@Override
	public void postCrawling(CrawlSession session) {
		BenchmarkGUI.instance(config).repaint();
	}
}
