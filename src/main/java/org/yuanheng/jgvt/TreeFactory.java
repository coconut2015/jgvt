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
import org.eclipse.jgit.revwalk.RevCommit;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

/**
 * @author Heng Yuan
 */
class TreeFactory
{
	public final static String COMMIT_STYLE = "commitEdge";
	public final static String BRANCH_STYLE = "branchEdge";
	public final static String MERGE_STYLE = "mergeEdge";

	private final mxGraph m_graph;
	private final GitRepo m_gitRepo;

	public TreeFactory (mxGraph graph, GitRepo gitRepo)
	{
		m_graph = graph;
		m_gitRepo = gitRepo;
	}

	private GVTNode createNode (RevCommit commit) throws GitAPIException
	{
		GVTNode node = new GVTNode (commit);
		ObjectId commitId = commit.getId ();

		node.setTag (m_gitRepo.getTagMap ().get (commitId));
		node.setBranch (m_gitRepo.getBranchMap ().get (commitId));

		return node;
	}

	private void resize (mxCell vertex)
	{
		mxGeometry g = (mxGeometry)vertex.getGeometry ().clone ();
		mxRectangle bounds = m_graph.getView ().getState (vertex).getLabelBounds ();
		g.setHeight (bounds.getHeight () + 10); // 10 is for padding
		m_graph.cellsResized (new Object[]{ vertex }, new mxRectangle[]{ g });
	}

	private mxCell getVertex (Object parent, HashMap<ObjectId, mxCell> vertexMap, RevCommit commit) throws GitAPIException
	{
		mxCell vertex = vertexMap.get (commit.getId ());
		if (vertex == null)
		{
			GVTNode node = createNode (commit);
			vertex = (mxCell) m_graph.insertVertex (parent, null, node, 0, 0, 30, 30);
			vertexMap.put (commit.getId (), vertex);
		}
		return vertex;
	}

	public void updateGraphModel (Iterable<RevCommit> commitLogs) throws GitAPIException
	{
		mxGraphModel model = (mxGraphModel) m_graph.getModel ();
		model.beginUpdate ();
		model.clear ();

		Object parent = m_graph.getDefaultParent ();
		HashMap<ObjectId, mxCell> map = new HashMap<ObjectId, mxCell> ();

		for (RevCommit commit : commitLogs)
		{
			mxCell vertex = getVertex (parent, map, commit);
			int numParents = commit.getParentCount ();
			for (int i = 0; i < numParents; ++i)
			{
				mxCell parentVertex = getVertex (parent, map, commit.getParent (i));
				m_graph.insertEdge (parent, null, null, parentVertex, vertex, COMMIT_STYLE);
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
