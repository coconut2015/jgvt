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

import org.eclipse.jgit.api.errors.GitAPIException;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxRectangle;

/**
 * @author Heng Yuan
 */
class GVTGraphFactory
{
	public final static String COMMIT_STYLE = "commitEdge";
	public final static String BRANCH_STYLE = "branchEdge";
	public final static String MERGE_STYLE = "mergeEdge";

	private final GVTGraph m_graph;

	public GVTGraphFactory (GVTGraph graph)
	{
		m_graph = graph;
	}

	private void resize (mxGraphModel model, Object vertex)
	{
		mxGeometry g = (mxGeometry) model.getGeometry (vertex).clone ();
		mxRectangle bounds = m_graph.getView ().getState (vertex).getLabelBounds ();
		g.setHeight (bounds.getHeight () + 10); // 10 is for padding
		m_graph.cellsResized (new Object[]{ vertex }, new mxRectangle[]{ g });
	}

	public void updateGraphModel (RelationTree relTree, int toolTipFlag) throws GitAPIException
	{
		GVTTree tree = new GVTTree ();
		m_graph.setTree (tree);

		mxGraphModel model = (mxGraphModel) m_graph.getModel ();
		model.beginUpdate ();
		model.clear ();

		Object parent = m_graph.getDefaultParent ();
		HashMap<GVTVertex, Object> vMap = new HashMap<GVTVertex, Object> ();

		// 1st create all the vertices
		for (RelationNode node : relTree.getNodes ())
		{
			GVTVertex v = tree.createVertex (node, toolTipFlag);

			Object vertex = m_graph.insertVertex (parent, null, v, 0, 0, 30, 30);
			vMap.put (v, vertex);
		}

		// 2nd add edges
		for (GVTVertex v : tree.getVertices ())
		{
			RelationNode node = tree.getNode (v);
			Object vertex = vMap.get (v);

			for (RelationNode parentNode : node.getParents ())
			{
				GVTVertex parentV = tree.getVertex (parentNode);
				Object parentVertex = vMap.get (parentV);

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

		model.beginUpdate ();

		for (Object vertex : vMap.values ())
		{
			m_graph.updateCellSize (vertex);
			resize(model, vertex);
		}

		model.endUpdate ();
	}
}
