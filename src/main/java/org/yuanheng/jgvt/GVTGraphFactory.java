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
import org.eclipse.jgit.lib.ObjectId;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

/**
 * @author Heng Yuan
 */
class GVTGraphFactory
{
	public final static String COMMIT_STYLE = "commitEdge";
	public final static String BRANCH_STYLE = "branchEdge";
	public final static String MERGE_STYLE = "mergeEdge";

	private final mxGraph m_graph;

	public GVTGraphFactory (mxGraph graph)
	{
		m_graph = graph;
	}

	private void resize (mxCell vertex)
	{
		mxGeometry g = (mxGeometry)vertex.getGeometry ().clone ();
		mxRectangle bounds = m_graph.getView ().getState (vertex).getLabelBounds ();
		g.setHeight (bounds.getHeight () + 10); // 10 is for padding
		m_graph.cellsResized (new Object[]{ vertex }, new mxRectangle[]{ g });
	}

	private mxCell getVertex (Object parent, HashMap<ObjectId, mxCell> vertexMap, RelationNode node) throws GitAPIException
	{
		mxCell vertex = vertexMap.get (node.getCommit ().getId ());
		if (vertex == null)
		{
			vertex = (mxCell) m_graph.insertVertex (parent, null, node, 0, 0, 30, 30);
			vertexMap.put (node.getCommit ().getId (), vertex);
		}
		return vertex;
	}

	public void updateGraphModel (RelationTree relTree) throws GitAPIException
	{
		mxGraphModel model = (mxGraphModel) m_graph.getModel ();
		model.beginUpdate ();
		model.clear ();

		Object parent = m_graph.getDefaultParent ();
		HashMap<ObjectId, mxCell> map = new HashMap<ObjectId, mxCell> ();

		for (RelationNode node : relTree.getNodes ())
		{
			mxCell vertex = getVertex (parent, map, node);
			for (RelationNode parentNode : node.getParents ())
			{
				mxCell parentVertex = getVertex (parent, map, parentNode);
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

		for (mxCell vertex : map.values ())
		{
			m_graph.updateCellSize (vertex);
			resize(vertex);
		}

		model.endUpdate ();
	}
}
