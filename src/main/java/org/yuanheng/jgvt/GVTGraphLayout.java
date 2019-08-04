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

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxIGraphModel;

/**
 * @author	Heng Yuan
 */
class GVTGraphLayout extends mxGraphLayout
{
	public static double START_X = 100.0;
	public static double START_Y = 50.0;
	/**
	 * The distance to the immediate child in the same branch.
	 */
	public static double CHILD_SPACING = 50.0;
	/**
	 * The distance between two adjacent branches.
	 */
	public static double BRANCH_SPACING = 140.0;

	/**
	 * @param graph
	 */
	public GVTGraphLayout (GVTGraph graph)
	{
		super (graph);
	}

	@Override
	public void execute (Object parent)
	{
		super.execute (parent);

		RelationTree tree = ((GVTGraph)graph).getTree ();
		if (tree == null)
			return;

		mxIGraphModel model = graph.getModel ();
		model.beginUpdate();

		int childCount = model.getChildCount(parent);
		for (int i = 0; i < childCount; i++)
		{
			Object cell = model.getChildAt(parent, i);
			if (model.isVertex (cell))
			{
				GVTVertex v = (GVTVertex)model.getValue (cell);
				RelationNode node = tree.getNode (v.getId ());
				LayoutInfo layoutInfo = node.getLayoutInfo ();
				double x = layoutInfo.getX () * BRANCH_SPACING + START_X;
				double y = layoutInfo.getY () * CHILD_SPACING + START_Y;
				setVertexLocation (cell, x, y);
			}
		}

		for (int i = 0; i < childCount; i++)
		{
			Object cell = model.getChildAt(parent, i);
			if (model.isEdge (cell))
			{
				graph.resetEdge (cell);
			}
		}

		model.endUpdate ();
	}
}
