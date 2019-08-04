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

	private void resize (mxGraphModel model, Object vertex)
	{
		mxGeometry g = (mxGeometry) model.getGeometry (vertex).clone ();
		mxRectangle bounds = m_graph.getView ().getState (vertex).getLabelBounds ();
		g.setHeight (bounds.getHeight () + 10); // 10 is for padding
		m_graph.cellsResized (new Object[]{ vertex }, new mxRectangle[]{ g });
	}

	private Object getVertex (Object parent, HashMap<ObjectId, Object> vertexMap, RelationNode node, int toolTipFlag) throws GitAPIException
	{
		ObjectId id = node.getCommit ().getId ();
		Object vertex = vertexMap.get (id);
		if (vertex == null)
		{
			GVTVertex v = new GVTVertex (id);
			v.setName (node.toString ());
			v.setToolTip (node.getTooltip (toolTipFlag));
			vertex = m_graph.insertVertex (parent, null, v, 0, 0, 30, 30);
			vertexMap.put (id, vertex);
		}
		return vertex;
	}

	public void updateGraphModel (RelationTree relTree, int toolTipFlag) throws GitAPIException
	{
		mxGraphModel model = (mxGraphModel) m_graph.getModel ();
		model.beginUpdate ();
		model.clear ();

		Object parent = m_graph.getDefaultParent ();
		HashMap<ObjectId, Object> vertexMap = new HashMap<ObjectId, Object> ();

		for (RelationNode node : relTree.getNodes ())
		{
			Object vertex = getVertex (parent, vertexMap, node, toolTipFlag);
			for (RelationNode parentNode : node.getParents ())
			{
				Object parentVertex = getVertex (parent, vertexMap, parentNode, toolTipFlag);
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

		for (Object vertex : vertexMap.values ())
		{
			m_graph.updateCellSize (vertex);
			resize(model, vertex);
		}

		model.endUpdate ();
	}
}
