/*
 * Copyright (c) 2019 Heng Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yuanheng.jgvt.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.export.DotFileChooser;
import org.yuanheng.jgvt.gui.graph.GVTGraph;
import org.yuanheng.jgvt.gui.graph.GVTGraphComponent;
import org.yuanheng.jgvt.gui.graph.GVTGraphFactory;
import org.yuanheng.jgvt.gui.graph.GVTVertex;
import org.yuanheng.jgvt.relation.RelationNode;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraphSelectionModel;

/**
 * This class handle all GUI related property settings.
 *
 * @author Heng Yuan
 */
public class GUI
{
	private final static String TITLE = "Java Git Version Tree";
	private static int TOOLTIP_DELAY = 100;

	private final Controller m_controller;
	private final JFrame m_frame;
	private JMenuBar m_menuBar;
	private JToolBar m_toolBar;
	private final StatusBar m_statusBar;
	private GVTGraph m_graph;
	private GVTGraphComponent m_graphComp;
	private PropertyPane m_propertyPane;
	private JSplitPane m_splitPane;
	private boolean m_splitPaneSetup;
	private JFileChooser m_fileChooser;
	private DotFileChooser m_dotFileChooser;

	private Action m_exitAction = new AbstractAction ("Exit")
	{
		private static final long serialVersionUID = 5063205298749002856L;

		{
			this.putValue (Action.MNEMONIC_KEY, (int)'x');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			System.exit (0);
		}
	};

	private Action m_exportDotAction = new AbstractAction ("Export Dot Graph")
	{
		private static final long serialVersionUID = -2457064892479677548L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			JFileChooser chooser = getExportDotFileChooser ();
			if (chooser.showSaveDialog (m_frame) == JFileChooser.APPROVE_OPTION)
			{
				File file = chooser.getSelectedFile ();
				m_dotFileChooser.updateOptions ();
				try
				{
					m_controller.exportDot (file);
				}
				catch (Exception ex)
				{
					ex.printStackTrace ();
				}
			}
		}
	};

	private Action m_searchAction = new AbstractAction ("Search")
	{
		private static final long serialVersionUID = 5495164556562597316L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'s');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
		}
	};

	private Action m_aboutAction = new AbstractAction ("About")
	{
		private static final long serialVersionUID = 7089364664624793507L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'a');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			AboutDialog dialog = new AboutDialog (m_frame);
			dialog.setLocationRelativeTo (m_frame);
			dialog.setVisible (true);
		}
	};

	private mxIEventListener m_selectNodeListener = new mxIEventListener ()
	{
		@Override
		public void invoke (Object sender, mxEventObject evt)
		{
			mxGraphSelectionModel sm = (mxGraphSelectionModel)sender;
			mxCell cell = (mxCell) sm.getCell ();
			if (cell != null && cell.isVertex ())
			{
				GVTVertex v = (GVTVertex) cell.getValue ();
				RelationNode node = m_graph.getTree ().getNode (v);
				m_controller.select (node, false);
			}
		}
	};

	public GUI (Controller controller)
	{
		m_controller = controller;
		try
		{
			UIManager.setLookAndFeel (new Plastic3DLookAndFeel ());
			UIDefaults defaults = UIManager.getDefaults ();
			BorderUIResource emptyBorder = new BorderUIResource(BorderFactory.createEmptyBorder());
			defaults.put("SplitPaneDivider.border", emptyBorder);
			defaults.put("SplitPane.border", emptyBorder);
		}
		catch (Exception ex)
		{
		}

		m_frame = new JFrame ();
		m_frame.setTitle (TITLE);
		m_frame.setSize (1024, 768);

		setupActions ();
		createMenuBar ();
		m_frame.getRootPane ().setJMenuBar (m_menuBar);

		Container contentPane = m_frame.getContentPane ();
		contentPane.setLayout (new BorderLayout ());

		createToolBar ();
		contentPane.add (m_toolBar, BorderLayout.NORTH);

		m_statusBar = new StatusBar ();

		createGraphComp ();
		createPropertyPane ();

		contentPane.add (m_statusBar, BorderLayout.SOUTH);

		m_splitPane = new JSplitPane (JSplitPane.VERTICAL_SPLIT, m_graphComp, m_propertyPane);
		m_splitPane.setResizeWeight (1);
		contentPane.add (m_splitPane, BorderLayout.CENTER);
		contentPane.addComponentListener (new ComponentAdapter ()
		{
			@Override
			public void componentResized (ComponentEvent e)
			{
				if (!m_splitPaneSetup)
				{
					m_splitPaneSetup = true;
					m_splitPane.setDividerLocation (0.7);
				}
			}
		});

		ToolTipManager.sharedInstance ().setInitialDelay (TOOLTIP_DELAY);

		controller.setGUI (this);
	}

	private void setupActions ()
	{
		Icons icons = new Icons ();

		m_searchAction.putValue (Action.SMALL_ICON, icons.SEARCH);
		m_aboutAction.putValue (Action.SMALL_ICON, icons.ABOUT);
	}

	private void createMenuBar ()
	{
		m_menuBar = new JMenuBar ();

		JMenu menu;

		menu = new JMenu ("File");
		menu.setMnemonic ('F');
		menu.add (new JMenuItem (m_exportDotAction));
		menu.addSeparator ();
		menu.add (new JMenuItem (m_exitAction));
		m_menuBar.add (menu);

		menu = new JMenu ("Help");
		menu.setMnemonic ('H');
		menu.add (new JMenuItem (m_aboutAction));
		m_menuBar.add (menu);
	}

	private void createToolBar ()
	{
		m_toolBar = new JToolBar ();
		m_toolBar.setFloatable (false);
		m_toolBar.add (new ToolBarButton (m_searchAction));
		m_toolBar.add (new ToolBarButton (m_aboutAction));
	}

	private void createGraphComp ()
	{
		m_graph = new GVTGraph ();
		m_graph.setStylesheet (GVTGraphFactory.GRAPH_STYLE);
		// a bit odd to use UNDO, but that's how JGraphX's selection works.
		m_graph.getSelectionModel ().addListener (mxEvent.UNDO, m_selectNodeListener);

		m_graphComp = new GVTGraphComponent (m_graph);
	}

	private void createPropertyPane ()
	{
		m_propertyPane = new PropertyPane (m_controller);
	}

	public void setVisible (boolean visible)
	{
		m_frame.setLocationRelativeTo (null);
		m_frame.setVisible (visible);
	}

	/**
	 * This is a simple utility to wait for the JFrame to be closed.
	 *
	 * @throws Exception
	 *             in case of error.
	 */
	public void waitForClose () throws Exception
	{
		final Object lock = new Object ();

		// start a thread that detects if the JFrame is closed.
		Thread detectThread = new Thread ()
		{
			public void run ()
			{
				synchronized (lock)
				{
					while (m_frame.isVisible ())
					{
						try
						{
							lock.wait ();
						}
						catch (Exception ex)
						{
						}
					}
				}
			}
		};
		detectThread.start ();

		// notify detect thread if the frame is closed.
		m_frame.addWindowListener (new WindowAdapter ()
		{
			public void windowClosing (WindowEvent e)
			{
				synchronized (lock)
				{
					m_frame.setVisible (false);
					lock.notify ();
				}
			}
		});

		detectThread.join ();
	}

	public StatusBar getStatusbar ()
	{
		return m_statusBar;
	}

	public void setRoot (String repo)
	{
		m_statusBar.setRoot (repo);
	}

	public void setBranch (String branch)
	{
		m_statusBar.setBranch (branch);
	}

	public void setFile (String file)
	{
		m_statusBar.setFile (file);
	}

	public GVTGraph getGraph ()
	{
		return m_graph;
	}

	public void select (RelationNode node, boolean center)
	{
		if (center)
		{
			m_graphComp.center (node);
		}
		m_propertyPane.select (node);
	}

	private JFileChooser getFileChooser ()
	{
		if (m_fileChooser == null)
		{
			m_fileChooser = new JFileChooser (m_controller.getPrefs ().getDefaultDirectory ());
		}
		return m_fileChooser;
	}

	private JFileChooser getExportDotFileChooser ()
	{
		if (m_dotFileChooser == null)
		{
			m_dotFileChooser = new DotFileChooser (m_controller.getDotFileOptions ());
		}
		JFileChooser chooser = getFileChooser ();
		m_dotFileChooser.updateJFileChooser (chooser);
		return chooser;
	}
}