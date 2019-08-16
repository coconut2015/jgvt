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

import java.util.List;

import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle.mxEdgeStyleFunction;

/**
 * @author	Heng Yuan
 */
class GVTMergeEdgeStyle implements mxEdgeStyleFunction
{
	public static double ANGLE = 15 * Math.PI / 180;

	public void apply (mxCellState state, mxCellState source, mxCellState target, List<mxPoint> hints, List<mxPoint> result)
	{
		final double epsilon = 0.001;

		mxPoint start = new mxPoint(state.getView ().getRoutingCenterX(source), state.getView ().getRoutingCenterY(source));
		mxPoint end = new mxPoint(state.getView ().getRoutingCenterX(target), state.getView ().getRoutingCenterY(target));

		double x1 = start.getX ();
		double y1 = start.getY ();
		double x2 = end.getX ();
		double y2 = end.getY ();

		double dx = x2 - x1;
		double dy = y2 - y1;

		double midX = x1 + dx / 2;
		double midY = y1 + dy / 2;

		double length = Math.sqrt (dx * dx + dy * dy);
		double height = Math.tan (ANGLE) * length * 0.5;

		mxPoint pt = new mxPoint ();

		if (Math.abs (dx) < epsilon)
		{
			pt.setX (x1 + height);
			pt.setY (y1 + dy / 2);
		}
		else if (Math.abs (dy) < epsilon)
		{
			pt.setX (x1 + dx / 2);
			pt.setY (y1 + height);
		}
		else
		{
			double angle = Math.atan (Math.abs (dy) / Math.abs (dx));
			double newX = - Math.sin(angle) * height;
			double newY = Math.cos(angle) * height;

			if (dx < 0)
				newX = -newX;
			if (dy < 0)
				newY = -newY;

			pt.setX (midX + newX);
			pt.setY (midY + newY);
		}
		result.add (pt);
	}
}
