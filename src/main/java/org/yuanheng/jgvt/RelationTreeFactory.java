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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class RelationTreeFactory
{
	private final GitRepo m_gitRepo;

	RelationTreeFactory (GitRepo gitRepo)
	{
		m_gitRepo = gitRepo;
	}

	private RelationNode createNode (RevCommit commit) throws GitAPIException
	{
		RelationNode node = new RelationNode (commit);
		ObjectId commitId = commit.getId ();

		node.setTag (m_gitRepo.getTagMap ().get (commitId));
		node.setBranch (m_gitRepo.getBranchMap ().get (commitId));

		return node;
	}

	private RelationNode getNode (Map<ObjectId, RelationNode> nodeMap, RevCommit commit) throws GitAPIException
	{
		RelationNode node = nodeMap.get (commit.getId ());
		if (node == null)
		{
			node = createNode (commit);
			nodeMap.put (commit.getId (), node);
		}
		return node;
	}

	public RelationTree createTree (Iterable<RevCommit> commitLogs) throws GitAPIException
	{
		HashMap<ObjectId, RelationNode> nodeMap = new HashMap<ObjectId, RelationNode> ();

		// First pass to construct the node graph to construct basic node
		// relationship.
		for (RevCommit commit : commitLogs)
		{
			RelationNode node = getNode (nodeMap, commit);

			for (RevCommit parentCommit : commit.getParents ())
			{
				RelationNode parentNode = getNode (nodeMap, parentCommit);

				node.addParent (parentNode);
			}
		}
		return new RelationTree (nodeMap);
	}
}
