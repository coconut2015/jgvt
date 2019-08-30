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
import java.awt.Rectangle;
import java.awt.event.*;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.relation.RelationNode;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;

/**
 * @author	Heng Yuan
 */
public class GVTGraphComponent extends mxGraphComponent
{
	private static final long serialVersionUID = 1573837130692041836L;

	private final GVTPopupMenu m_popupMenu;

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

	private MouseListener m_mouseListener = new MouseAdapter ()
	{
	    @Override
		public void mouseClicked(MouseEvent e)
	    {
	    	if (e.getClickCount () == 1 &&
	    		e.getModifiers () == MouseEvent.BUTTON3_MASK)
	    	{
	    		Object cell = getCellAt(e.getX (), e.getY ());
                if (cell instanceof mxCell)
                {
                	if (graph.getModel ().isVertex (cell))
                	{
                		GVTVertex v = (GVTVertex) graph.getModel ().getValue (cell);
        				RelationNode node = ((GVTGraph)graph).getTree ().getNode (v);
        				m_popupMenu.show (node, e.getX (), e.getY ());
                	}
                }
	    	}
	    }
	};

	/**
	 * @param	graph
	 * 			jgvt graph
	 */
	public GVTGraphComponent (Controller controller, GVTGraph graph)
	{
		super (graph);
		addComponentListener (m_resizeListener);

		setConnectable (false);
		setAutoScroll (true);
		getViewport ().setOpaque (true);
		getViewport ().setBackground (Color.WHITE);
		setToolTips (true);

		getGraphControl ().addMouseListener (m_mouseListener);

		m_popupMenu = new GVTPopupMenu (controller, this);

		new mxRubberband (this)
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getModifiers () == (MouseEvent.CTRL_MASK | MouseEvent.BUTTON1_MASK))
				{
					super.mousePressed (e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
            {
				// have to get the bounds first.
                Rectangle rect = bounds;

				super.mouseReleased (e);

				if (rect != null &&
					rect.width > 0 &&
					rect.height > 0)
				{
					double oldScale = graph.getView ().getScale ();

					Dimension viewPortSize = getViewport ().getSize ();
					double newScale = Math.min (viewPortSize.getWidth () / rect.width, viewPortSize.getHeight () / rect.height);
					zoom (newScale);

					// Get the actual scale used.
					newScale = graph.getView ().getScale () / oldScale;

					Rectangle newRect = new Rectangle ();

					newRect.x = (int) (rect.x * newScale);
					newRect.y = (int) (rect.y * newScale);
					newRect.width = (int) (rect.width * newScale);
					newRect.height = (int) (rect.height * newScale);

					getGraphControl ().scrollRectToVisible (newRect);

				}
            }

			@Override
			public Object[] select(Rectangle rect, MouseEvent e)
			{
				return null;
			}
		};
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
