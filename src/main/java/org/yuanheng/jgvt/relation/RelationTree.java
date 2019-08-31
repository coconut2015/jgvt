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
package org.yuanheng.jgvt.relation;

import java.util.*;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.yuanheng.jgvt.GitRepo;

/**
 * @author	Heng Yuan
 */
public class RelationTree
{
	private final Map<ObjectId, RelationNode> m_nodeMap;

	RelationTree ()
	{
		m_nodeMap = new HashMap<ObjectId, RelationNode> ();
	}

	private RelationNode getNode (RevCommit commit, GitRepo gitRepo) throws GitAPIException
	{
		ObjectId commitId = commit.getId ();
		RelationNode node = m_nodeMap.get (commitId);
		if (node == null)
		{
			node = new RelationNode (commit);
			m_nodeMap.put (commit.getId (), node);

			node.addTag (gitRepo.getTagMap ().get (commitId));
			node.addBranch (gitRepo.getBranchMap ().get (commitId));
		}
		return node;
	}

	/**
	 * Add nodes to the tree.
	 *
	 * @param	commitLogs
	 *			a list of git commits
	 * @param	gitRepo
	 * 			the git repo
	 * @throws	GitAPIException
	 * 			in case of git error
	 */
	public void addNodes (Iterable<RevCommit> commitLogs, GitRepo gitRepo) throws GitAPIException
	{
		for (RevCommit commit : commitLogs)
		{
			RelationNode node = getNode (commit, gitRepo);

			for (RevCommit parentCommit : commit.getParents ())
			{
				RelationNode parentNode = getNode (parentCommit, gitRepo);

				node.addParent (parentNode);
			}
		}

		// sort the children
		for (RelationNode node : getNodes ())
		{
			RelationNode[] children = node.getChildren ();
			if (children.length > 1)
			{
				Arrays.sort (children, RelationNode.sortByDateComparator);
			}
		}
	}

	public RelationNode getNode (ObjectId id)
	{
		return m_nodeMap.get (id);
	}

	public RelationNode getNode (RevCommit commit)
	{
		return m_nodeMap.get (commit.getId ());
	}

	public Collection<RelationNode> getNodes ()
	{
		return m_nodeMap.values ();
	}

	public void resetVisit ()
	{
		for (RelationNode node : m_nodeMap.values ())
		{
			node.resetVisit ();
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

	/**
	 * Find all the branches in the tree.
	 *
	 * @return	all the unique branches in the tree.
	 */
	public HashSet<RelationBranch> getBranchSet ()
	{
		// scan and put all the branches in a list
		HashSet<RelationBranch> branchSet = new HashSet<RelationBranch> ();
		for (RelationNode node : getNodes ())
		{
			branchSet.add (node.getRelationBranch ());
		}
		return branchSet;
	}
}
