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
package org.yuanheng.jgvt.gui.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;

import org.yuanheng.jgvt.relation.RelationNode;

import com.mxgraph.swing.mxGraphComponent;

/**
 * @author	Heng Yuan
 */
public class GVTGraphComponent extends mxGraphComponent
{
	private static final long serialVersionUID = 1573837130692041836L;

	private ComponentListener m_resizeListener = new ComponentAdapter ()
	{
		@Override
		public void componentResized (ComponentEvent e)
		{
			Dimension d = getViewport ().getExtentSize ();
			int hInc = (int)Math.max (10, d.width * 0.05);
			getHorizontalScrollBar ().setUnitIncrement (hInc);
			int vInc = (int)Math.max (10, d.height * 0.05);
			getVerticalScrollBar ().setUnitIncrement (vInc);
		}
	};

	/**
	 * @param	graph
	 * 			jgvt graph
	 */
	public GVTGraphComponent (GVTGraph graph)
	{
		super (graph);
		addComponentListener (m_resizeListener);

		setConnectable (false);
		setAutoScroll (true);
		getViewport ().setOpaque (true);
		getViewport ().setBackground (Color.WHITE);
		setToolTips (true);
	}

	public void center (RelationNode node)
	{
		GVTGraph graph = (GVTGraph)this.graph;
		Object cell = graph.getTree ().getVertex (node);
		scrollCellToVisible (cell, true);

		// fake selection event
		MouseEvent e = new MouseEvent (this, MouseEvent.MOUSE_CLICKED, 0, MouseEvent.BUTTON1_MASK, 0, 0, 1, false);
		selectCellForEvent (cell, e);
	}

	@Override
	protected void processMouseWheelEvent (MouseWheelEvent e)
	{
		if (e.getModifiers () == MouseEvent.CTRL_MASK)
		{
			if (e.getWheelRotation() < 0)
			{
				zoomIn ();
			}
			else
			{
				zoomOut ();
			}
		}
		else
		{
			super.processMouseWheelEvent (e);
		}
	}
}
