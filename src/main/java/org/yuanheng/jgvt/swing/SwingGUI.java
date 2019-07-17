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
package org.yuanheng.jgvt.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.TreeFactory;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;


/**
 * This class handle all GUI related property settings.
 *
 * @author Heng Yuan
 */
public class SwingGUI
{
	private final static String TITLE = "Java Git Version Tree";
	private static int TOOLTIP_DELAY = 100;

	private static mxStylesheet GRAPH_STYLE;

	{
		Map<String, Object> commitEdge = new HashMap<String, Object>();
		commitEdge.put(mxConstants.STYLE_ROUNDED, false);
		commitEdge.put(mxConstants.STYLE_ORTHOGONAL, false);
		commitEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_TOPTOBOTTOM);
		commitEdge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		commitEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
		commitEdge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		commitEdge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		commitEdge.put(mxConstants.STYLE_STROKECOLOR, "#000000");

		Map<String, Object> branchEdge = new HashMap<String, Object>();
		branchEdge.put(mxConstants.STYLE_ROUNDED, false);
		branchEdge.put(mxConstants.STYLE_ORTHOGONAL, false);
		branchEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		branchEdge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		branchEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.NONE);
		branchEdge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		branchEdge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		branchEdge.put(mxConstants.STYLE_STROKECOLOR, "#000000");

		Map<String, Object> mergeEdge = new HashMap<String, Object>();
		mergeEdge.put(mxConstants.STYLE_ROUNDED, true);
		mergeEdge.put(mxConstants.STYLE_ORTHOGONAL, false);
		mergeEdge.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		mergeEdge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		mergeEdge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		mergeEdge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		mergeEdge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		mergeEdge.put(mxConstants.STYLE_STROKECOLOR, "#ff0000");

		GRAPH_STYLE = new mxStylesheet();
		GRAPH_STYLE.setDefaultEdgeStyle(commitEdge);
		GRAPH_STYLE.putCellStyle (TreeFactory.COMMIT_STYLE, commitEdge);
		GRAPH_STYLE.putCellStyle (TreeFactory.BRANCHEDGE_STYLE, branchEdge);
		GRAPH_STYLE.putCellStyle (TreeFactory.MERGE_STYLE, mergeEdge);

		Map<String, Object> hashVertex = new HashMap<String, Object>();
		hashVertex.put(mxConstants.STYLE_AUTOSIZE, 1);
		hashVertex.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		hashVertex.put(mxConstants.STYLE_FILLCOLOR, "#a0c8f0");
		hashVertex.put(mxConstants.STYLE_FONTFAMILY, "Verdana");
		hashVertex.put(mxConstants.STYLE_FONTCOLOR, "#000000");

		Map<String, Object> tagVertex = new HashMap<String, Object>();
		tagVertex.put(mxConstants.STYLE_AUTOSIZE, 1);
		tagVertex.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		tagVertex.put(mxConstants.STYLE_FILLCOLOR, "#f0c8a0");
		tagVertex.put(mxConstants.STYLE_FONTFAMILY, "Verdana");
		tagVertex.put(mxConstants.STYLE_FONTCOLOR, "#000000");

		Map<String, Object> branchVertex = new HashMap<String, Object>();
		branchVertex.put(mxConstants.STYLE_AUTOSIZE, 1);
		branchVertex.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		branchVertex.put(mxConstants.STYLE_FILLCOLOR, "#c80000");
		branchVertex.put(mxConstants.STYLE_FONTFAMILY, "Verdana");
		branchVertex.put(mxConstants.STYLE_FONTCOLOR, "#000000");

		GRAPH_STYLE.setDefaultVertexStyle (hashVertex);
		GRAPH_STYLE.putCellStyle (TreeFactory.HASH_STYLE, hashVertex);
		GRAPH_STYLE.putCellStyle (TreeFactory.TAG_STYLE, tagVertex);
		GRAPH_STYLE.putCellStyle (TreeFactory.BRANCH_STYLE, branchVertex);
	}

	private final JFrame m_frame;
	private JMenuBar m_menuBar;
	private JToolBar m_toolBar;
	private final StatusBar m_statusBar;
	private mxGraph m_graph;
	private mxGraphComponent m_graphComp;

	public SwingGUI (Controller controller)
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
		m_frame.setSize (640, 480);

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

		m_graphComp = new mxGraphComponent(m_graph);
		m_graphComp.setConnectable (false);
		m_graphComp.setAutoScroll (true);
		m_graphComp.getViewport().setOpaque(true);
		m_graphComp.getViewport().setBackground(Color.WHITE);
		m_graphComp.setToolTips (true);
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
}
