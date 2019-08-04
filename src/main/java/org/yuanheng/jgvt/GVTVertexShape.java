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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.shape.mxRectangleShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

/**
 * @author Heng Yuan
 */
public class GVTVertexShape extends mxRectangleShape
{
	public void paintShape(mxGraphics2DCanvas canvas, mxCellState state)
	{
		RelationTree tree = ((GVTGraph)state.getView ().getGraph ()).getTree ();
		Map<String, Object> style = state.getStyle();
		mxCell cell = (mxCell) state.getCell ();

		if (cell.isVertex () && tree != null)
		{
			style.put (mxConstants.STYLE_FONTSTYLE, style.get (GUI.STYLE_REGULAR_FONTSTYLE));
			style.put (mxConstants.STYLE_FILLCOLOR, style.get (GUI.STYLE_REGULAR_FILLCOLOR));
		}
		super.paintShape (canvas, state);

		RelationNode node = null;
		if (cell.isVertex () && tree != null)
		{
			GVTVertex vertex = (GVTVertex)cell.getValue ();
			node = tree.getNode (vertex.getId ());
		}
		if (node == null)
			return;
		String annot = node.getAnnotation ();
		if (annot != null)
		{
			Graphics2D g = canvas.getGraphics ();

			Rectangle rect = new Rectangle ((int) (state.getX ()), (int) (state.getY ()), (int)state.getWidth (), (int)state.getHeight ());
			rect.x += rect.getWidth ();

			g.setFont (mxUtils.getFont (state.getStyle ()));
			g.setColor (Color.decode ((String) state.getStyle ().get (GUI.STYLE_TAG_FONTCOLOR)));

			drawStringLeft (g, rect, annot);
		}
	}

	private void drawStringLeft (Graphics2D g, Rectangle rect, String s)
	{
		FontMetrics metrics = g.getFontMetrics ();
		int height = metrics.getHeight ();
		int ascent = metrics.getAscent ();

		g.drawString (s, rect.x, (float) (rect.getCenterY () - height / 2.0) + ascent);
	}
}
