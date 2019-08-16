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

import com.mxgraph.shape.mxCurveShape;
import com.mxgraph.util.mxLine;
import com.mxgraph.util.mxPoint;

/**
 * @author	Heng Yuan
 */
class GVTMergeEdgeShape extends mxCurveShape
{
	protected mxLine getMarkerVector(List<mxPoint> points, boolean source,
			double markerSize)
	{
		int n = points.size();
		mxPoint p0 = (source) ? points.get(1) : points.get(n - 2);
		mxPoint pe = (source) ? points.get(0) : points.get(n - 1);
		int count = 1;
		
		// Uses next non-overlapping point
		while (count < n - 1 && Math.round(p0.getX() - pe.getX()) == 0 && Math.round(p0.getY() - pe.getY()) == 0)
		{
			p0 = (source) ? points.get(1 + count) : points.get(n - 2 - count);
			count++;
		}
		
		return new mxLine(p0, pe);
	}
}
