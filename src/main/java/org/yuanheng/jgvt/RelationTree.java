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

import java.util.*;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class RelationTree
{
	private final Map<ObjectId, RelationNode> m_nodeMap;

	public RelationTree ()
	{
		m_nodeMap = new HashMap<ObjectId, RelationNode> ();
	}

	public RelationNode getNode (ObjectId id)
	{
		return m_nodeMap.get (id);
	}

	public RelationNode getNode (RevCommit commit)
	{
		return m_nodeMap.get (commit.getId ());
	}

	public RelationNode getNode (RevCommit commit, GitRepo gitRepo) throws GitAPIException
	{
		ObjectId commitId = commit.getId ();
		RelationNode node = m_nodeMap.get (commitId);
		if (node == null)
		{
			node = new RelationNode (commit);
			m_nodeMap.put (commit.getId (), node);

			node.setTag (gitRepo.getTagMap ().get (commitId));
			node.setBranch (gitRepo.getBranchMap ().get (commitId));
		}
		return node;
	}

	public Collection<RelationNode> getNodes ()
	{
		return m_nodeMap.values ();
	}

	public void resetLayout ()
	{
		for (RelationNode node : m_nodeMap.values ())
		{
			node.getLayoutInfo ().resetVisit ();
		}
	}

	public List<RelationNode> getRoots ()
	{
		ArrayList<RelationNode> rootNodes = new ArrayList<RelationNode> ();

		for (RelationNode node : m_nodeMap.values ())
		{
			if (node.getParents ().length == 0)
				rootNodes.add (node);
		}
		return rootNodes;
	}

	public List<RelationNode> getLeaves ()
	{
		ArrayList<RelationNode> rootNodes = new ArrayList<RelationNode> ();

		for (RelationNode node : m_nodeMap.values ())
		{
			if (node.getChildren ().length == 0)
				rootNodes.add (node);
		}
		return rootNodes;
	}
}
