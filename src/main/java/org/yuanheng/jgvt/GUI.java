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
package org.yuanheng.jgvt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;


/**
 * This class handle all GUI related property settings.
 *
 * @author Heng Yuan
 */
public class GUI
{
	private final static String TITLE = "Java Git Version Tree";
	private static int TOOLTIP_DELAY = 100;

	public static String STYLE_TAG_FONTCOLOR = "tagFontColor";
	public static String STYLE_BRANCH_FONTSTYLE = "branchFontStyle";
	public static String STYLE_BRANCH_FILLCOLOR = "branchFillColor";
	public static String STYLE_REGULAR_FONTSTYLE = "regularFontStyle";
	public static String STYLE_REGULAR_FILLCOLOR = "regularFillColor";

	private static String STYLE_VERTEX_SHAPE = "GVTVertex";

	private static mxStylesheet GRAPH_STYLE;

	{
		mxGraphics2DCanvas.putShape (STYLE_VERTEX_SHAPE, new GVTVertexShape ());

		GRAPH_STYLE = new mxStylesheet();

		Map<String, Object> commitEdge = new HashMap<String, Object>();
		commitEdge.put(mxConstants.STYLE_ROUNDED, true);
		commitEdge.put(mxConstants.STYLE_ORTHOGONAL, false);
		commitEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		commitEdge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		commitEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
		commitEdge.put(mxConstants.STYLE_STROKECOLOR, "#000000");

		Map<String, Object> branchEdge = new HashMap<String, Object>();
		branchEdge.put(mxConstants.STYLE_ROUNDED, true);
		branchEdge.put(mxConstants.STYLE_ORTHOGONAL, false);
		branchEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		branchEdge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		branchEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
		branchEdge.put(mxConstants.STYLE_STROKECOLOR, "#000000");

		Map<String, Object> mergeEdge = new HashMap<String, Object>();
		mergeEdge.put(mxConstants.STYLE_ROUNDED, true);
		mergeEdge.put(mxConstants.STYLE_ORTHOGONAL, false);
		mergeEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		mergeEdge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		mergeEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		mergeEdge.put(mxConstants.STYLE_STROKECOLOR, "#ff0000");

		GRAPH_STYLE.setDefaultEdgeStyle(commitEdge);
		GRAPH_STYLE.putCellStyle (TreeFactory.COMMIT_STYLE, commitEdge);
		GRAPH_STYLE.putCellStyle (TreeFactory.BRANCH_STYLE, branchEdge);
		GRAPH_STYLE.putCellStyle (TreeFactory.MERGE_STYLE, mergeEdge);

		Map<String, Object> vertexStyle = new HashMap<String, Object>();
		vertexStyle.put(mxConstants.STYLE_AUTOSIZE, 1);
		vertexStyle.put(mxConstants.STYLE_SHAPE, STYLE_VERTEX_SHAPE);
		vertexStyle.put(mxConstants.STYLE_FONTFAMILY, "Verdana");
		vertexStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		vertexStyle.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
		vertexStyle.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
		vertexStyle.put(STYLE_BRANCH_FONTSTYLE, mxConstants.FONT_BOLD);
		vertexStyle.put(STYLE_BRANCH_FILLCOLOR, "#7f0000");
		vertexStyle.put(STYLE_REGULAR_FILLCOLOR, "#a0c8f0");
		vertexStyle.put(STYLE_REGULAR_FONTSTYLE, 0);
		vertexStyle.put(STYLE_TAG_FONTCOLOR, "#7f0000");

		GRAPH_STYLE.setDefaultVertexStyle (vertexStyle);
	}

	private final JFrame m_frame;
	private JMenuBar m_menuBar;
	private JToolBar m_toolBar;
	private final StatusBar m_statusBar;
	private GVTGraph m_graph;
	private mxGraphComponent m_graphComp;
	private mxGraphLayout m_graphLayout;

	private ActionListener m_exitListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			System.exit (0);
		}
	};

	public GUI (Controller controller)
	{
		try
		{
			UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex)
		{
		}

		m_frame = new JFrame ();
		m_frame.setTitle (TITLE);
		m_frame.setSize (1024, 768);

		createMenuBar ();
		m_frame.getRootPane ().setJMenuBar (m_menuBar);

		Container contentPane = m_frame.getContentPane ();
		contentPane.setLayout (new BorderLayout ());

		createToolBar ();
		contentPane.add (m_toolBar, BorderLayout.NORTH);

		m_statusBar = new StatusBar ();
		contentPane.add (m_statusBar, BorderLayout.SOUTH);

		createGraphComp ();
		contentPane.add (m_graphComp, BorderLayout.CENTER);

		ToolTipManager.sharedInstance().setInitialDelay (TOOLTIP_DELAY);

		controller.setGUI (this);
	}

	private void createMenuBar ()
	{
		m_menuBar = new JMenuBar ();

		JMenu menu;
		JMenuItem item;

		menu = new JMenu ("File");
		menu.addSeparator ();
		item = new JMenuItem ("Exit");
		item.setMnemonic ('x');
		item.addActionListener (m_exitListener);
		menu.add (item);
		menu.setMnemonic ('F');

		m_menuBar.add (menu);
	}

	private void createToolBar ()
	{
		m_toolBar = new JToolBar ();
		m_toolBar.setFloatable (false);
	}

	private void createGraphComp ()
	{
		m_graph = new GVTGraph ();
		m_graph.setStylesheet (GRAPH_STYLE);

		createGraphLayout ();

		m_graphComp = new mxGraphComponent (m_graph);
		m_graphComp.setConnectable (false);
		m_graphComp.setAutoScroll (true);
		m_graphComp.getViewport().setOpaque(true);
		m_graphComp.getViewport().setBackground(Color.WHITE);
		m_graphComp.setToolTips (true);

		System.out.println ("transfer: " + m_graphComp.getTransferHandler ());
	}

	private void createGraphLayout ()
	{
		mxHierarchicalLayout layout = new mxHierarchicalLayout (m_graph);
		m_graphLayout = layout;

		layout.setInterRankCellSpacing (10);
		layout.setIntraCellSpacing (300);
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

	public mxGraph getGraph ()
	{
		return m_graph;
	}

	public void updateGraphLayout ()
	{
		m_graphLayout.execute(m_graph.getDefaultParent());
	}
}
