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
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.BorderUIResource;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.export.*;
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
	private CommitPane m_propertyPane;
	private JSplitPane m_splitPane;
	private boolean m_splitPaneSetup;
	private JFileChooser m_exportFileChooser;
	private RememberButton m_rememberButton;

	private String m_branch;
	private String m_file;
	private String m_repo;

	private Action m_exitAction = new AbstractAction ("Exit")
	{
		private static final long serialVersionUID = 5063205298749002856L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'x');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			System.exit (0);
		}
	};

	private Action m_exportAction = new AbstractAction ("Export")
	{
		private static final long serialVersionUID = -7051858706222087355L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			JFileChooser chooser = getExportFileChooser ();
			if (chooser.showSaveDialog (m_frame) == JFileChooser.APPROVE_OPTION)
			{
				ExportFileFilter filter = (ExportFileFilter) chooser.getFileFilter ();
				String ext = filter.getExtension ();
				File file = chooser.getSelectedFile ();
				if (!file.getName ().endsWith (ext))
				{
					file = new File (file.getPath () + ext);
				}

				try
				{
					filter.save (m_controller, file);
				}
				catch (Exception ex)
				{
					ex.printStackTrace ();
					String msg = ex.getMessage ();
					JOptionPane.showMessageDialog (m_frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	};

	private ActionListener m_searchListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			try
			{
				SearchDialog dialog = new SearchDialog (m_frame, "Commits", m_controller);
				dialog.setLocationRelativeTo (m_frame);
				dialog.setVisible (true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (m_frame, "Unable to get the commit list.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_searchAction = new AbstractAction ("Search")
	{
		private static final long serialVersionUID = 5495164556562597316L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Search");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchListener.actionPerformed (e);
		}
	};

	private Action m_searchAction2 = new AbstractAction ("Search")
	{
		private static final long serialVersionUID = 3415698093054735622L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'s');
			putValue (Action.SHORT_DESCRIPTION, "Search");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchListener.actionPerformed (e);
		}
	};

	private Action m_rememberAction = new AbstractAction ("Remember selected")
	{
		private static final long serialVersionUID = -1447129537275798612L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'r');
			putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_R, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (!m_controller.remember ())
			{
				JOptionPane.showMessageDialog (m_frame, "No selected commit.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_clearRememberAction = new AbstractAction ("Clear remembered")
	{
		private static final long serialVersionUID = -1447129537275798612L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'m');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_controller.clearRemember ();
		}
	};

	private Action m_locateRememberAction = new AbstractAction ("Locate remembered")
	{
		private static final long serialVersionUID = -1447129537275798612L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'l');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (!m_controller.locateRemember ())
			{
				JOptionPane.showMessageDialog (m_frame, "No remembered commit.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_compareRememberAction = new AbstractAction ("Compare selected to remembered")
	{
		private static final long serialVersionUID = -1447129537275798612L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'c');
			putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_C, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (!m_controller.hasRememberNode ())
			{
				JOptionPane.showMessageDialog (m_frame, "No remembered commit.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!m_controller.hasSelectedNode ())
			{
				JOptionPane.showMessageDialog (m_frame, "No selected commit.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!m_controller.compareToRemember ())
			{
				JOptionPane.showMessageDialog (m_frame, "Unable to compare.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_listBranchAction = new AbstractAction ("List branches")
	{
		private static final long serialVersionUID = 2439092079745308557L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'b');
			putValue (Action.SHORT_DESCRIPTION, "List branches");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			try
			{
				ListDialog dialog = new ListDialog (m_frame, "Branches", m_controller, m_controller.getBranchList ());
				dialog.setLocationRelativeTo (m_frame);
				dialog.setVisible (true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (m_frame, "Unable to get branch list.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_listTagAction = new AbstractAction ("List tags")
	{
		private static final long serialVersionUID = 2439092079745308557L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'b');
			putValue (Action.SHORT_DESCRIPTION, "List branches");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			try
			{
				ListDialog dialog = new ListDialog (m_frame, "Tags", m_controller, m_controller.getTagList ());
				dialog.setLocationRelativeTo (m_frame);
				dialog.setVisible (true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (m_frame, "Unable to get branch list.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_aboutAction = new AbstractAction ("About")
	{
		private static final long serialVersionUID = 7089364664624793507L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'a');
			putValue (Action.SHORT_DESCRIPTION, "About");
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

		// disable status bar for now
//		contentPane.add (m_statusBar, BorderLayout.SOUTH);

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
		Icons icons = Icons.getInstance ();

		m_searchAction.putValue (Action.SMALL_ICON, icons.SEARCH);
		m_searchAction2.putValue (Action.SMALL_ICON, icons.SEARCH_SMALL);
		m_aboutAction.putValue (Action.SMALL_ICON, icons.ABOUT);
	}

	private void createMenuBar ()
	{
		m_menuBar = new JMenuBar ();

		JMenu menu;

		menu = new JMenu ("File");
		menu.setMnemonic ('F');
		menu.add (new JMenuItem (m_exportAction));
		menu.addSeparator ();
		menu.add (new JMenuItem (m_exitAction));
		m_menuBar.add (menu);

		menu = new JMenu ("Compare");
		menu.setMnemonic ('C');
		menu.add (new JMenuItem (m_rememberAction));
		menu.add (new JMenuItem (m_clearRememberAction));
		menu.add (new JMenuItem (m_locateRememberAction));
		menu.add (new JMenuItem (m_compareRememberAction));
		m_menuBar.add (menu);

		menu = new JMenu ("Repo");
		menu.setMnemonic ('R');
		menu.add (new JMenuItem (m_searchAction2));
		menu.add (new JMenuItem (m_listBranchAction));
		menu.add (new JMenuItem (m_listTagAction));
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
		m_toolBar.addSeparator ();
		m_rememberButton = new RememberButton (m_controller);
		m_toolBar.add (m_rememberButton);
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
		m_propertyPane = new CommitPane (m_controller);
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
		m_repo = repo;
		m_statusBar.setRoot (repo);
		m_frame.setTitle (computeTitle ());
	}

	public void setBranch (String branch)
	{
		m_branch = branch;
		m_statusBar.setBranch (branch);
		m_frame.setTitle (computeTitle ());
	}

	public void setFile (String file)
	{
		m_file = file;
		m_statusBar.setFile (file);
		m_frame.setTitle (computeTitle ());
	}

	public GVTGraphComponent getGraphComponent ()
	{
		return m_graphComp;
	}

	public GVTGraph getGraph ()
	{
		return m_graph;
	}

	public void setRemembered (RelationNode node)
	{
		m_rememberButton.setNode (node);
	}

	public void select (RelationNode node, boolean center)
	{
		if (center)
		{
			m_graphComp.center (node);
		}
		m_propertyPane.select (node);
	}

	private JFileChooser getExportFileChooser ()
	{
		if (m_exportFileChooser == null)
		{
			JFileChooser chooser = new JFileChooser (m_controller.getPrefs ().getDefaultDirectory ());
			chooser.setAcceptAllFileFilterUsed (false);

			chooser.addChoosableFileFilter (new DotFileFilter ());
			chooser.addChoosableFileFilter (new SvgFileFilter ());

			chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
			ExportChooserAccessoryUI accessaryUI = new ExportChooserAccessoryUI (chooser);
			for (FileFilter f : chooser.getChoosableFileFilters ())
			{
				ExportFileFilter filter = (ExportFileFilter) f;
				ExportFileFilterUI ui = filter.getUI ();
				if (ui != null)
				{
					accessaryUI.addUI (filter.getExtension (), (JComponent)ui);
				}
			}
			chooser.setAccessory (accessaryUI);
			ExportFileFilter filter = (ExportFileFilter) chooser.getFileFilter ();
			accessaryUI.switchTo (filter.getExtension ());
			m_exportFileChooser = chooser;
		}
		m_exportFileChooser.setSelectedFiles (new File[] { new File ("") });	// clear the previous selected files, if any.
		return m_exportFileChooser;
	}

	private String computeTitle ()
	{
		if (m_repo == null)
			return TITLE;
		String title = "jgvt: " + m_repo;
		if (m_branch != null)
		{
			title += " [ " + m_branch + " ]";
		}
		if (m_file != null && m_file.length () > 0)
		{
			title += " - " + m_file;
		}
		return title;
	}

	public void showDiffWindow (RelationNode n1, RelationNode n2)
	{
		DiffWindow dialog = new DiffWindow (m_controller, n1, n2);
		dialog.setLocationRelativeTo (m_frame);
		dialog.setVisible (true);
	}
}
