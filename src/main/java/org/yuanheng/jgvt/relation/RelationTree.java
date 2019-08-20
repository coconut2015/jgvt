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
import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.GitRepo;

/**
 * @author	Heng Yuan
 */
public class RelationTree
{
	private static void debug (String msg)
	{
		System.out.println (msg);
	}

	private final static Comparator<RelationNode> s_sortByDate = new Comparator<RelationNode> ()
	{
		@Override
		public int compare (RelationNode o1, RelationNode o2)
		{
			return o1.getCommit ().getCommitTime () - o2.getCommit ().getCommitTime ();
		}
	};

	private final Map<ObjectId, RelationNode> m_nodeMap;
	private ArrayList<RelationBranch> m_branches;

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
				Arrays.sort (children, s_sortByDate);
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

	private void resetVisit ()
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

	private void discoverBranches (RelationNode startNode)
	{
		ArrayList<RelationNode> stack = new ArrayList<RelationNode> ();

		// initiate the tree with the main branch on the left.
		RelationNode node = startNode;
		RelationBranch mainBranch = new RelationBranch (startNode);
		for (;;)
		{
			stack.add (node);
			RelationNode[] parents = node.getParents ();
			if (parents.length > 0)
			{
				if (parents[0].getRelationBranch () != null)
				{
					break;
				}

				// set this child as the parent's first
				parents[0].setNthChild (node, 0);
				node = parents[0];
				mainBranch.add (node);
			}
			else
			{
				break;
			}
		}

		// now walk the tree
		while (stack.size () > 0)
		{
			node = stack.remove (stack.size () - 1);
			LayoutInfo layoutInfo = node.getLayoutInfo ();  
			RelationBranch branch = node.getRelationBranch ();
			if (layoutInfo.isVisited ())
				continue;

			if (branch == null)
			{
				branch = new RelationBranch (node);
			}
			layoutInfo.visit ();

			// scan parent first
			int index = 0;
			for (RelationNode parent : node.getParents ())
			{
				++index;
				RelationBranch parentBranch = parent.getRelationBranch ();
				if (parentBranch != null)
				{
					continue;
				}

				if (index == 0)
				{
					branch.add (parent);
				}
				else
				{
					// parent is the END of a branch which merges
					// to the node.
					new RelationBranch (parent);
				}
				stack.add (parent);
			}
		}
	}

	/**
	 * A simple merge of two branches if the parent branch only has 1 child,
	 * which happens to be the child branch.
	 */
	private void branchMergeCaseSingleChild (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			List<RelationNode> nodeList = branch.getOrderedList ();
			RelationNode firstNode = nodeList.get (0);
			if (firstNode.getParents ().length > 0)
			{
				RelationNode parent = firstNode.getParents ()[0];
				if (parent.getRelationBranch () != branch &&
					parent.getChildren ().length == 1)
				{
					// this node is the only child of the parent node,
					// and the parent is in a different branch.
					// merge the two branches.
					branch.merge (parent.getRelationBranch ());

					debug (CommitUtils.getName (firstNode.getCommit ()) + " 1CM");
					checkBranches.add (branch);
				}
			}
		}
	}

	/**
	 * Find all the branches in the tree.
	 *
	 * @return	all the unique branches in the tree.
	 */
	private Set<RelationBranch> getBranchSet ()
	{
		// scan and put all the branches in a list
		HashSet<RelationBranch> branchSet = new HashSet<RelationBranch> ();
		for (RelationNode node : getNodes ())
		{
			branchSet.add (node.getRelationBranch ());
		}
		return branchSet;
	}

	/**
	 * For this case, we have two child branches B and C for a branch A.
	 * However, child branch B eventually merges to child branch C.  In
	 * this case, we merge A and C.
	 */
	private void branchMergeCaseTwoChildren (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			List<RelationNode> nodeList = branch.getOrderedList ();
			RelationNode lastNode = nodeList.get (nodeList.size () - 1);

			if (lastNode.getChildren ().length == 2)
			{
				RelationBranch leftBranch = lastNode.getChildren ()[0].getRelationBranch ();
				RelationBranch rightBranch = lastNode.getChildren ()[1].getRelationBranch ();

				if (branch == leftBranch ||
					branch == rightBranch ||
					leftBranch == rightBranch)
					continue;

				RelationNode leftNode = lastNode.getChildren ()[0];
				List<RelationNode> leftList = leftBranch.getOrderedList ();
				// make sure the left child is the first node of the child branch
				if (leftList.get (0) != leftNode)
					continue;

				RelationNode rightNode = lastNode.getChildren ()[1];
				List<RelationNode> rightList = rightBranch.getOrderedList ();
				// make sure the right child is the first node of the child branch
				if (rightList.get (0) != rightNode)
					continue;

				// now check if leftBranch merges to rightBranch or
				// vice versa
				RelationNode leftLast = leftList.get (leftList.size () - 1);
				if (leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightBranch)
				{
					branch.merge (rightBranch);

					debug (CommitUtils.getName (lastNode.getCommit ()) + " 2CM right");
					checkBranches.add (branch);
					continue;
				}

				RelationNode rightLast = rightList.get (rightList.size () - 1);
				if (rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftBranch)
				{
					branch.merge (leftBranch);

					debug (CommitUtils.getName (lastNode.getCommit ()) + " 2CM left");
					checkBranches.add (branch);
					continue;
				}
			}
		}
	}

	/**
	 * For this case, we have a branch A which has one child merge to
	 * branch B, and its other child in branch C also merge to branch B.
	 * In this case, consider merging A and C.
	 *
	 * This case is different from branchMergeCaseTwoChildren in that
	 * branch B could be the parent of A.
	 */
	private void branchMergeCaseRepeatMerge (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			List<RelationNode> nodeList = branch.getOrderedList ();
			RelationNode lastNode = nodeList.get (nodeList.size () - 1);

			if (lastNode.getChildren ().length == 2)
			{
				RelationBranch leftBranch = lastNode.getChildren ()[0].getRelationBranch ();
				RelationBranch rightBranch = lastNode.getChildren ()[1].getRelationBranch ();

				if (branch == leftBranch ||
					branch == rightBranch ||
					leftBranch == rightBranch)
					continue;

				RelationNode leftNode = lastNode.getChildren ()[0];
				List<RelationNode> leftList = leftBranch.getOrderedList ();

				RelationNode rightNode = lastNode.getChildren ()[1];
				List<RelationNode> rightList = rightBranch.getOrderedList ();

				// now check if leftBranch merges to rightBranch or
				// vice versa
				RelationNode leftLast = leftList.get (leftList.size () - 1);
				if (leftNode == leftList.get (0) &&
					leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightBranch &&
					rightList.get (0) != rightNode)
				{
					branch.merge (leftBranch);

					debug (CommitUtils.getName (lastNode.getCommit ()) + " RM left");
					checkBranches.add (branch);
					continue;
				}

				RelationNode rightLast = rightList.get (rightList.size () - 1);
				if (rightNode == rightList.get (0) &&
					rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftBranch &&
					leftList.get (0) != leftNode)
				{
					branch.merge (rightBranch);

					debug (CommitUtils.getName (lastNode.getCommit ()) + " RM right");
					checkBranches.add (branch);
					continue;
				}
			}
		}
	}

	/**
	 * For this case, main branch A has a side branch B.  Then A and B
	 * are merged to create branch C.  Eventually, C merges back to A.
	 * This is an issue usually because C's parents A and B are swapped
	 * (i.e. index 0 is A, and index 1 is B).
	 *
	 * In this case, consider merging B and C.
	 */
	private void branchMergeCaseSwapMergeParents (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			List<RelationNode> nodeList = branch.getOrderedList ();
			RelationNode firstNode = nodeList.get (0);
			RelationNode lastNode = nodeList.get (nodeList.size () - 1);

			if (firstNode.getParents ().length == 2 &&
				lastNode.getChildren ().length == 1)
			{
				RelationNode leftParent = firstNode.getParents ()[0];
				RelationBranch leftParentBranch = leftParent.getRelationBranch ();
				RelationNode rightParent = firstNode.getParents ()[1];
				RelationBranch rightParentBranch = rightParent.getRelationBranch ();

				if (branch == leftParentBranch ||
					branch == rightParentBranch ||
					leftParentBranch == rightParentBranch)
					continue;

				List<RelationNode> rightList = rightParentBranch.getOrderedList ();
				if (rightParent != rightList.get (rightList.size () - 1))
					continue;

				RelationNode rightParentBranchFirstNode = rightList.get (0);
				if (rightParentBranchFirstNode.getParents ().length == 1 &&
					rightParentBranchFirstNode.getParents ()[0].getRelationBranch () == leftParentBranch &&
					lastNode.getChildren ()[0].getRelationBranch () == leftParentBranch)
				{
					firstNode.getParents ()[0] = rightParent;
					firstNode.getParents ()[1] = leftParent;
					branch.merge (rightParentBranch);

					debug (CommitUtils.getName (firstNode.getCommit ()) + " SMP");
					checkBranches.add (branch);
				}
			}
		}
	}

	/**
	 * For this case, if branch A firstNode's parent 0 is the last
	 * node of branch B.  Then parent 0 has a merge arrow to branch C, and
	 * C merges to branch A.
	 *
	 * In this case, we merge A and B.
	 */
	private void branchMergeCaseMergeOutMergeIn (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getOrderedList ().get (0);
			if (firstNode.getParents ().length != 2)
				continue;

			RelationNode leftParent = firstNode.getParents ()[0];
			RelationNode rightParent = firstNode.getParents ()[1];
			RelationBranch leftParentBranch = leftParent.getRelationBranch ();
			RelationBranch rightParentBranch = rightParent.getRelationBranch ();

			if (branch == leftParentBranch ||
				branch == rightParentBranch ||
				leftParentBranch == rightParentBranch)
				continue;

			List<RelationNode> leftList = leftParentBranch.getOrderedList ();
			if (leftParent != leftList.get (leftList.size () - 1))
				continue;

			boolean hasMergeToRight = false;
			for (RelationNode child : leftParent.getChildren ())
			{
				if (child.getRelationBranch () == rightParentBranch)
				{
					hasMergeToRight = true;
					break;
				}
			}
			if (hasMergeToRight)
			{
				branch.merge (leftParentBranch);

				debug (CommitUtils.getName (firstNode.getCommit ()) + " MOMI");
				checkBranches.add (branch);
			}
		}
	}

	/**
	 * We need to expand the search since the branchSet only contains the ones
	 * just got modified.  We need to include parents and children the modified
	 * branches.
	 *
	 * @param	branchSet
	 *			the modified branches
	 */
	private void expandSearch (Set<RelationBranch> branchSet)
	{
		HashSet<RelationBranch> expandSet = new HashSet<RelationBranch> ();

		for (RelationBranch branch: branchSet)
		{
			if (branch.size () == 0)
				continue;
			List<RelationNode> nodes = branch.getOrderedList ();
			RelationNode firstNode = nodes.get (0);
			for (RelationNode parent : firstNode.getParents ())
			{
				expandSet.add (parent.getRelationBranch ());
			}
			RelationNode lastNode = nodes.get (nodes.size () - 1);
			for (RelationNode child : lastNode.getChildren ())
			{
				expandSet.add (child.getRelationBranch ());
			}
		}

		branchSet.addAll (expandSet);
	}

	/**
	 * Infer branches from the tree nodes, and a list of important branches.
	 *
	 * @param	importantBranches
	 * 			List of important branches.
	 * @throws	GitAPIException
	 * 			in case of git error
	 */
	public void inferBranches (RelationNode startNode) throws GitAPIException
	{
		// See if we can trace from the main branch and collect branches.
		discoverBranches (startNode);

		// Now find remaining nodes with multiple parents.
		ArrayList<RelationNode> multiParentNodes = new ArrayList<RelationNode> ();
		for (RelationNode node : getNodes ())
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			if (node.getParents ().length > 1)
			{
				multiParentNodes.add (node);
			}
		}
		Collections.sort (multiParentNodes, s_sortByDate);
		for (RelationNode node : multiParentNodes)
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			discoverBranches (node);
		}

		// find any remaining branches starting from leaves.
		List <RelationNode> leaves = getLeaves ();
		Collections.sort (leaves, s_sortByDate);
		for (RelationNode node : leaves)
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			discoverBranches (node);
		}

		@SuppressWarnings ("unchecked")
		HashSet<RelationBranch>[] branchSets = (HashSet<RelationBranch>[]) new HashSet<?>[2]; 
		branchSets[0] = new HashSet<RelationBranch> ();
		branchSets[1] = new HashSet<RelationBranch> ();
		{
			HashSet<RelationBranch> set = branchSets[0];
			for (RelationNode node : getNodes ())
			{
				set.add (node.getRelationBranch ());
			}
		}
		int index = 0;
		debug ("index: " + index + ": " + branchSets[index].size ());
		while (branchSets[index].size () > 0)
		{
			int nextIndex = 1 - index;
			branchSets[nextIndex].clear ();
			debug ("index: " + index + ": " + branchSets[index].size ());

			// perform simple branch merging
			branchMergeCaseSingleChild (branchSets[index], branchSets[nextIndex]);
	
			// perform slightly more complicated merging
			branchMergeCaseTwoChildren (branchSets[index], branchSets[nextIndex]);

			branchMergeCaseRepeatMerge (branchSets[index], branchSets[nextIndex]);

			branchMergeCaseSwapMergeParents (branchSets[index], branchSets[nextIndex]);

			branchMergeCaseMergeOutMergeIn (branchSets[index], branchSets[nextIndex]);

			index = nextIndex;
			expandSearch(branchSets[index]);
		}

		// scan and put all the branches in a list
		Set<RelationBranch> branchSet = getBranchSet ();
		m_branches = new ArrayList<RelationBranch> ();
		m_branches.addAll (branchSet);

		resetVisit ();
	}
}
