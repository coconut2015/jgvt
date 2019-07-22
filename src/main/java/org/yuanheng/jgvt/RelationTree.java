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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

	public RelationNode getCommitNode (ObjectId id)
	{
		return m_nodeMap.get (id);
	}

	public RelationNode getCommitNode (RevCommit commit)
	{
		return m_nodeMap.get (commit.getId ());
	}

	public RelationNode getCommitNode (RevCommit commit, GitRepo gitRepo) throws GitAPIException
	{
		ObjectId commitId = commit.getId ();
		RelationNode commitNode = m_nodeMap.get (commitId);
		if (commitNode == null)
		{
			commitNode = new RelationNode (commit);
			m_nodeMap.put (commit.getId (), commitNode);

			commitNode.setTag (gitRepo.getTagMap ().get (commitId));
			commitNode.setBranch (gitRepo.getBranchMap ().get (commitId));
		}
		return commitNode;
	}

	public Collection<RelationNode> getNodes ()
	{
		return m_nodeMap.values ();
	}
}
