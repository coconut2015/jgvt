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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;

/**
 * @author Heng Yuan
 */
class GVTGraphFactory
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

	public final static String COMMIT_STYLE = "commitEdge";
	public final static String BRANCH_STYLE = "branchEdge";
	public final static String MERGE_STYLE = "mergeEdge";

	public static String STYLE_TAG_FONTCOLOR = "tagFontColor";
	public static String STYLE_BRANCH_FONTSTYLE = "branchFontStyle";
	public static String STYLE_BRANCH_FILLCOLOR = "branchFillColor";
	public static String STYLE_REGULAR_FONTSTYLE = "regularFontStyle";
	public static String STYLE_REGULAR_FILLCOLOR = "regularFillColor";

	private static String STYLE_VERTEX_SHAPE = "GVTVertex";

	public static mxStylesheet GRAPH_STYLE;

	static
	{
		mxConstants.RECTANGLE_ROUNDING_FACTOR = 0.5;

		mxGraphics2DCanvas.putShape (STYLE_VERTEX_SHAPE, new GVTVertexShape ());

		GRAPH_STYLE = new mxStylesheet ();

		Map<String, Object> commitEdge = new HashMap<String, Object> ();
		commitEdge.put (mxConstants.STYLE_ROUNDED, false);
		commitEdge.put (mxConstants.STYLE_ORTHOGONAL, false);
		commitEdge.put (mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_TOPTOBOTTOM);
		commitEdge.put (mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		commitEdge.put (mxConstants.STYLE_ENDARROW, mxConstants.NONE);
		commitEdge.put (mxConstants.STYLE_STROKECOLOR, "#000000");

		Map<String, Object> branchEdge = new HashMap<String, Object> ();
		branchEdge.put (mxConstants.STYLE_ROUNDED, false);
		branchEdge.put (mxConstants.STYLE_ORTHOGONAL, false);
		branchEdge.put (mxConstants.STYLE_EDGE, mxConstants.ELBOW_VERTICAL);
		branchEdge.put (mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		branchEdge.put (mxConstants.STYLE_ENDARROW, mxConstants.NONE);
		branchEdge.put (mxConstants.STYLE_STROKECOLOR, "#007f00");

		Map<String, Object> mergeEdge = new HashMap<String, Object> ();
		mergeEdge.put (mxConstants.STYLE_ROUNDED, true);
		mergeEdge.put (mxConstants.STYLE_ORTHOGONAL, false);
		mergeEdge.put (mxConstants.STYLE_EDGE, mxConstants.ELBOW_VERTICAL);
		mergeEdge.put (mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		mergeEdge.put (mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		mergeEdge.put (mxConstants.STYLE_STROKECOLOR, "#ff0000");

		GRAPH_STYLE.setDefaultEdgeStyle (commitEdge);
		GRAPH_STYLE.putCellStyle (GVTGraphFactory.COMMIT_STYLE, commitEdge);
		GRAPH_STYLE.putCellStyle (GVTGraphFactory.BRANCH_STYLE, branchEdge);
		GRAPH_STYLE.putCellStyle (GVTGraphFactory.MERGE_STYLE, mergeEdge);

		Map<String, Object> vertexStyle = new HashMap<String, Object> ();
		vertexStyle.put (mxConstants.STYLE_AUTOSIZE, 0);
		vertexStyle.put (mxConstants.STYLE_ROUNDED, true);
		vertexStyle.put (mxConstants.STYLE_SHAPE, STYLE_VERTEX_SHAPE);
		vertexStyle.put (mxConstants.STYLE_FONTFAMILY, "Verdana");
		vertexStyle.put (mxConstants.STYLE_FONTCOLOR, "#000000");
		vertexStyle.put (mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
		vertexStyle.put (mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
		vertexStyle.put (STYLE_REGULAR_FILLCOLOR, "#a0c8f0");
		vertexStyle.put (STYLE_REGULAR_FONTSTYLE, 0);
		vertexStyle.put (STYLE_TAG_FONTCOLOR, "#7f0000");

		GRAPH_STYLE.setDefaultVertexStyle (vertexStyle);
	}

	private final GVTGraph m_graph;

	public GVTGraphFactory (GVTGraph graph)
	{
		m_graph = graph;
	}

	public static mxRectangle computeBounds (int charLen)
	{
		char[] chars = new char[charLen];
		for (int i = 0; i < charLen; ++i)
			chars[i] = 'M';
		String str = new String (chars);
		return mxUtils.getLabelPaintBounds (str, GRAPH_STYLE.getDefaultVertexStyle (), false, new mxPoint (), null, 1.0, false);
	}

	public void updateGraphModel (RelationTree relTree, int toolTipFlag) throws GitAPIException
	{
		GVTTree tree = new GVTTree ();
		m_graph.setTree (tree);

		mxRectangle vertexBound = computeBounds (6);

		mxGraphModel model = (mxGraphModel) m_graph.getModel ();
		model.beginUpdate ();
		model.clear ();

		Object parent = m_graph.getDefaultParent ();
		HashMap<Integer, Object> vMap = new HashMap<Integer, Object> ();

		// 1st create all the vertices
		for (RelationNode node : relTree.getNodes ())
		{
			GVTVertex v = tree.createVertex (node, toolTipFlag);
			LayoutInfo layoutInfo = node.getLayoutInfo ();
			double x = layoutInfo.getX () * BRANCH_SPACING + START_X;
			double y = layoutInfo.getY () * CHILD_SPACING + START_Y;

			Object vertex = m_graph.insertVertex (parent, null, v, x, y, vertexBound.getWidth (), vertexBound.getHeight ());
			vMap.put (v.getId (), vertex);
		}

		// 2nd add edges
		int numVertices = tree.size ();
		for (int id = 0; id < numVertices; ++id)
		{
			RelationNode node = tree.getNode (id);
			Object vertex = vMap.get (id);

			for (RelationNode parentNode : node.getParents ())
			{
				int parentId = tree.getVertex (parentNode);
				Object parentVertex = vMap.get (parentId);

				String edgeStyle = COMMIT_STYLE;
				switch (node.getRelation (parentNode))
				{
					case CHILD:
						edgeStyle = COMMIT_STYLE;
						break;
					case MERGE:
						edgeStyle = MERGE_STYLE;
						break;
					case BRANCH:
						edgeStyle = BRANCH_STYLE;
						break;
				}
				m_graph.insertEdge (parent, null, null, parentVertex, vertex, edgeStyle);
			}
		}

		model.endUpdate ();
	}
}
