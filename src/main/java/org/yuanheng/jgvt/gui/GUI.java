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
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.BorderUIResource;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.Debug;
import org.yuanheng.jgvt.Main;
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
	private JDialog m_searchDialog;
	private JDialog m_preferenceDialog;
	private JDialog m_editListDialog;
	private JDialog m_listBranchDialog;
	private JDialog m_listTagDialog;
	private JDialog m_branchLogDialog;

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

	private ActionListener m_exportListener = new ActionListener ()
	{
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
					Debug.printStackTrace (ex);
					String msg = ex.getMessage ();
					JOptionPane.showMessageDialog (m_frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	};

	private Action m_preferenceAction = new AbstractAction ("Preferences")
	{
		private static final long serialVersionUID = -1756402293339927940L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (m_preferenceDialog == null)
			{
				m_preferenceDialog = new PreferenceDialog (m_controller, m_frame);
			}
			m_preferenceDialog.setLocationRelativeTo (m_frame);
			m_preferenceDialog.setVisible (true);
		}
	};

	private Action m_exportAction = new AbstractAction ()
	{
		private static final long serialVersionUID = -3891179358209369117L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Export");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_exportListener.actionPerformed (e);
		}
	};

	private Action m_exportAction2 = new AbstractAction ("Export")
	{
		private static final long serialVersionUID = -7051858706222087355L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'e');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_exportListener.actionPerformed (e);
		}
	};

	private Action m_refreshAction = new AbstractAction ("Refresh")
	{
		private static final long serialVersionUID = 3415698093054735622L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'r');
			putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_F5, 0));
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_controller.refresh ();
		}
	};

	private Action m_editListAction = new AbstractAction ("Show edit list")
	{
		private static final long serialVersionUID = -2363655691891537720L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'s');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (m_editListDialog == null)
			{
				m_editListDialog = new EditListDialog (m_frame, m_controller);
				m_editListDialog.setLocationRelativeTo (m_frame);
			}
			m_editListDialog.setVisible (true);
		}
	};

	private Action m_branchLogAction = new AbstractAction ("Show branch discovery log")
	{
		private static final long serialVersionUID = 2439092079745308557L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (m_branchLogDialog == null)
			{
				m_branchLogDialog = new BranchLogDialog (m_frame, m_controller);
				m_branchLogDialog.setLocationRelativeTo (m_frame);
			}
			m_branchLogDialog.setVisible (true);
		}
	};

	private Action m_rememberAction = new AbstractAction ("Remember selected")
	{
		private static final long serialVersionUID = 3415698093054735622L;

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
		private static final long serialVersionUID = -2363655691891537720L;

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
		private static final long serialVersionUID = 2439092079745308557L;

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

	private ActionListener m_searchListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			try
			{
				if (m_searchDialog == null)
				{
					m_searchDialog = new SearchDialog (m_frame, "Commits", m_controller);
				}
				m_searchDialog.setLocationRelativeTo (m_frame);
				m_searchDialog.setVisible (true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (m_frame, "Unable to get the commit list.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_searchCommitAction = new AbstractAction ()
	{
		private static final long serialVersionUID = 5495164556562597316L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Search commits");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchListener.actionPerformed (e);
		}
	};

	private Action m_searchCommitAction2 = new AbstractAction ("Search commits")
	{
		private static final long serialVersionUID = -3123479739031644852L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'c');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchListener.actionPerformed (e);
		}
	};

	private ActionListener m_searchBranchListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			try
			{
				if (m_listBranchDialog == null)
				{
					m_listBranchDialog = new ListDialog (m_frame, "Branches", m_controller, m_controller.getBranchList ());
				}
				m_listBranchDialog.setLocationRelativeTo (m_frame);
				m_listBranchDialog.setVisible (true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (m_frame, "Unable to get branch list.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_searchBranchAction = new AbstractAction ()
	{
		private static final long serialVersionUID = -3081431635785233383L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Search branches");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchBranchListener.actionPerformed (e);
		}
	};

	private Action m_searchBranchAction2 = new AbstractAction ("Search branches")
	{
		private static final long serialVersionUID = -1510278584367430303L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'b');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchBranchListener.actionPerformed (e);
		}
	};

	private ActionListener m_searchTagListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			try
			{
				if (m_listTagDialog == null)
				{
					m_listTagDialog = new ListDialog (m_frame, "Tags", m_controller, m_controller.getTagList ());
				}
				m_listTagDialog.setLocationRelativeTo (m_frame);
				m_listTagDialog.setVisible (true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (m_frame, "Unable to get branch list.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	};

	private Action m_searchTagAction = new AbstractAction ()
	{
		private static final long serialVersionUID = -5545585336599216782L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Search tags");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchTagListener.actionPerformed (e);
		}
	};

	private Action m_searchTagAction2 = new AbstractAction ("Search tags")
	{
		private static final long serialVersionUID = -5229584358642625498L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'t');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_searchTagListener.actionPerformed (e);
		}
	};

	private ActionListener m_zoomInListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_graphComp.zoomIn ();
		}
	};

	private Action m_zoomInAction = new AbstractAction ()
	{
		private static final long serialVersionUID = -7620237457388167152L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Zoom in");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_zoomInListener.actionPerformed (e);
		}
	};

	private Action m_zoomInAction2 = new AbstractAction ("Zoom in")
	{
		private static final long serialVersionUID = -3438718584546464839L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'i');
			putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_PLUS, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_zoomInListener.actionPerformed (e);
		}
	};

	private ActionListener m_zoomOutListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_graphComp.zoomOut ();
		}
	};

	private Action m_zoomOutAction = new AbstractAction ()
	{
		private static final long serialVersionUID = 1245038749655657265L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Zoom out");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_zoomOutListener.actionPerformed (e);
		}
	};

	private Action m_zoomOutAction2 = new AbstractAction ("Zoom out")
	{
		private static final long serialVersionUID = -482961184150515444L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'o');
			putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_zoomOutListener.actionPerformed (e);
		}
	};

	private ActionListener m_zoomResetListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_graphComp.zoomTo (1.0, true);
		}
	};

	private Action m_zoomResetAction = new AbstractAction ()
	{
		private static final long serialVersionUID = -5649817480346963149L;

		{
			putValue (Action.SHORT_DESCRIPTION, "Zoom reset");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_zoomResetListener.actionPerformed (e);
		}
	};

	private Action m_zoomResetAction2 = new AbstractAction ("Zoom reset")
	{
		private static final long serialVersionUID = 8070169564849661643L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'r');
			putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_0, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_zoomResetListener.actionPerformed (e);
		}
	};

	private ActionListener m_aboutListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			AboutDialog dialog = new AboutDialog (m_frame);
			dialog.setLocationRelativeTo (m_frame);
			dialog.setVisible (true);
		}
	};

	private Action m_aboutAction = new AbstractAction ()
	{
		private static final long serialVersionUID = 7089364664624793507L;

		{
			putValue (Action.SHORT_DESCRIPTION, "About");
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_aboutListener.actionPerformed (e);
		}
	};

	private Action m_aboutAction2 = new AbstractAction ("About")
	{
		private static final long serialVersionUID = -1495062852291512816L;

		{
			putValue (Action.MNEMONIC_KEY, (int)'a');
		}

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_aboutListener.actionPerformed (e);
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
		try
		{
			m_frame.setIconImage (ImageIO.read (ClassLoader.getSystemResource ("META-INF/jgvt/jgvt.png")));
		}
		catch (IOException ex)
		{
		}
		m_frame.setSize (1024, 768);

		setupActions ();
		createMenuBar ();
		m_frame.getRootPane ().setJMenuBar (m_menuBar);

		Container contentPane = m_frame.getContentPane ();
		contentPane.setLayout (new BorderLayout ());

		createToolBar ();
		contentPane.add (m_toolBar, BorderLayout.NORTH);

		m_statusBar = new StatusBar ();

		createGraphComp (controller);
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

		m_exportAction.putValue (Action.SMALL_ICON, icons.SAVE);
		m_exportAction2.putValue (Action.SMALL_ICON, icons.SAVE_SMALL);
		m_searchCommitAction.putValue (Action.SMALL_ICON, icons.SEARCH);
		m_searchCommitAction2.putValue (Action.SMALL_ICON, icons.SEARCH_SMALL);
		m_searchBranchAction.putValue (Action.SMALL_ICON, icons.SEARCHBRANCH);
		m_searchBranchAction2.putValue (Action.SMALL_ICON, icons.SEARCHBRANCH_SMALL);
		m_searchTagAction.putValue (Action.SMALL_ICON, icons.SEARCHTAG);
		m_searchTagAction2.putValue (Action.SMALL_ICON, icons.SEARCHTAG_SMALL);
		m_zoomInAction.putValue (Action.SMALL_ICON, icons.ZOOMIN);
		m_zoomInAction2.putValue (Action.SMALL_ICON, icons.ZOOMIN_SMALL);
		m_zoomOutAction.putValue (Action.SMALL_ICON, icons.ZOOMOUT);
		m_zoomOutAction2.putValue (Action.SMALL_ICON, icons.ZOOMOUT_SMALL);
		m_zoomResetAction.putValue (Action.SMALL_ICON, icons.ZOOMRESET);
		m_zoomResetAction2.putValue (Action.SMALL_ICON, icons.ZOOMRESET_SMALL);
		m_aboutAction.putValue (Action.SMALL_ICON, icons.ABOUT);
		m_aboutAction2.putValue (Action.SMALL_ICON, icons.ABOUT_SMALL);
	}

	private void createMenuBar ()
	{
		m_menuBar = new JMenuBar ();

		JMenu menu;
		JMenu subMenu;

		menu = new JMenu ("File");
		menu.setMnemonic ('F');
		menu.add (new JMenuItem (m_exportAction2));
		menu.addSeparator ();
		menu.add (new JMenuItem (m_preferenceAction));
		menu.addSeparator ();
		menu.add (new JMenuItem (m_exitAction));
		m_menuBar.add (menu);

		menu = new JMenu ("Graph");
		menu.setMnemonic ('G');
		menu.add (new JMenuItem (m_refreshAction));
		menu.addSeparator ();
		menu.add (new JMenuItem (m_editListAction));
		m_menuBar.add (menu);

		menu = new JMenu ("Search");
		menu.setMnemonic ('R');
		menu.add (new JMenuItem (m_searchCommitAction2));
		menu.add (new JMenuItem (m_searchBranchAction2));
		menu.add (new JMenuItem (m_searchTagAction2));
		m_menuBar.add (menu);

		menu = new JMenu ("Compare");
		menu.setMnemonic ('C');
		menu.add (new JMenuItem (m_rememberAction));
		menu.add (new JMenuItem (m_clearRememberAction));
		menu.add (new JMenuItem (m_locateRememberAction));
		menu.add (new JMenuItem (m_compareRememberAction));
		m_menuBar.add (menu);

		menu = new JMenu ("Zoom");
		menu.setMnemonic ('Z');
		menu.add (new JMenuItem (m_zoomInAction2));
		menu.add (new JMenuItem (m_zoomOutAction2));
		menu.add (new JMenuItem (m_zoomResetAction2));
		m_menuBar.add (menu);

		menu = new JMenu ("Help");
		menu.setMnemonic ('H');
		subMenu = new JMenu ("Debug");
		subMenu.add (new JMenuItem (m_branchLogAction));
		menu.add (subMenu);
		menu.add (new JMenuItem (m_aboutAction2));
		m_menuBar.add (menu);
	}

	private void createToolBar ()
	{
		m_toolBar = new JToolBar ();
		m_toolBar.setFloatable (false);
		m_toolBar.add (new ToolBarButton (m_exportAction));
		m_toolBar.addSeparator ();
		m_toolBar.add (new ToolBarButton (m_searchCommitAction));
		m_toolBar.add (new ToolBarButton (m_searchBranchAction));
		m_toolBar.add (new ToolBarButton (m_searchTagAction));
		m_toolBar.addSeparator ();
		m_toolBar.add (new ToolBarButton (m_zoomInAction));
		m_toolBar.add (new ToolBarButton (m_zoomOutAction));
		m_toolBar.add (new ToolBarButton (m_zoomResetAction));
		m_toolBar.addSeparator ();
		m_toolBar.add (new ToolBarButton (m_aboutAction));
		m_toolBar.addSeparator ();
		m_rememberButton = new RememberButton (m_controller);
		m_toolBar.add (m_rememberButton);
	}

	private void createGraphComp (Controller controller)
	{
		m_graph = new GVTGraph ();
		m_graph.setStylesheet (GVTGraphFactory.GRAPH_STYLE);
		// a bit odd to use UNDO, but that's how JGraphX's selection works.
		m_graph.getSelectionModel ().addListener (mxEvent.UNDO, m_selectNodeListener);

		m_graphComp = new GVTGraphComponent (controller, m_graph);
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
			@Override
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
			@Override
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
		if (node == null)
			return;

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
			JFileChooser chooser = new JFileChooser (Main.pref.getExportDirectory ());
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

	public JFrame getFrame ()
	{
		return m_frame;
	}
}
