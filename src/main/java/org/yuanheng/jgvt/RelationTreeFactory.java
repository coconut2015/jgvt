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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class RelationTreeFactory
{
	public static List<Pattern> getDefaultImportantBranchNames ()
	{
		ArrayList<Pattern> branches = new ArrayList<Pattern> ();
		branches.add (Pattern.compile ("(.*/)?master"));
		branches.add (Pattern.compile ("(.*/)?gh-pages"));
		return branches;
	}

	private final GitRepo m_gitRepo;
	private final List<Pattern> m_importantBranchNames;

	RelationTreeFactory (GitRepo gitRepo, List<Pattern> importantBranchNames)
	{
		m_gitRepo = gitRepo;
		m_importantBranchNames = importantBranchNames;
	}

	private static List<RelationNode> getBranches (RelationTree tree, List<Pattern> importantBranches, Map<String, ObjectId> reverseBranchMap)
	{
		ArrayList<RelationNode> branches = new ArrayList<RelationNode> ();
		if (importantBranches.size () == 0)
			return branches;

		for (String branchName : reverseBranchMap.keySet ())
		{
			for (int i = 0; i < importantBranches.size (); ++i)
			{
				if (importantBranches.get (i).matcher (branchName).matches ())
				{
					branches.add (tree.getNode (reverseBranchMap.get (branchName)));
					break;
				}
			}
		}
		return branches;
	}

	private void layoutBranches (RelationTree tree, List<RelationNode> importantBranches) throws GitAPIException, IOException
	{
		LayoutMatrix matrix = new LayoutMatrix ();
		LinkedList<LayoutState> states = new LinkedList<LayoutState> ();
		RelationNode startNode = null;
		if (importantBranches.size () == 0)
		{
			throw new RuntimeException ("Not yet implemented.");
		}
		else
		{
			startNode = importantBranches.get (0);
			RelationBranch mainBranch = startNode.getRelationBranch ();
			LayoutInfo layoutInfo = mainBranch.getLayoutInfo ();
			layoutInfo.setX (0);
			layoutInfo.setY (0);

			LayoutState state = new LayoutState (mainBranch);
			state.setX (0);
			state.setY (0);
			states.add (state);
			mainBranch.getLayoutInfo ().visit ();
			matrix.take (0, 0, state.size () - 1);
		}

		while (states.size () > 0)
		{
			LayoutState state = states.getLast ();

			if (!state.hasNext ())
			{
				states.removeLast ();
				continue;
			}

			RelationNode node = state.next ();
			RelationBranch branch = node.getRelationBranch ();
			LayoutInfo layoutInfo = node.getLayoutInfo ();

			int x = state.getX ();
			int y = state.getY ();

			layoutInfo.setX (x);
			layoutInfo.setY (y);
			++y;
			state.setY (y);

			int index = 0;
			for (RelationNode child : node.getChildren ())
			{
				if (index == 0 && branch.has (child))
				{
					++index;
					continue;
				}
				++index;
				RelationBranch childBranch = child.getRelationBranch ();
				LayoutInfo childLayoutInfo = childBranch.getLayoutInfo ();
				if (childLayoutInfo.isVisited ())
				{
					// we do not need to do anything other than having
					// a merge-out arrow from parent to child.
					child.setRelation (node, RelationType.MERGE);
					continue;
				}
				childLayoutInfo.visit ();
				if (child.getParents ()[0] == node)
				{
					child.setRelation (node, RelationType.BRANCH);
				}
				else
				{
					child.setRelation (node, RelationType.MERGE);
				}
				LayoutState childState = new LayoutState (childBranch);
				int checkX = x + 1;
				int y2 = y + childState.size () - 1;
				while (matrix.isTaken (checkX, y, y2))
					++checkX;
				childState.setX (checkX);
				childState.setY (y);
				matrix.take (checkX, y, y2);
				states.add (childState);
			}
		}
	}
	public RelationTree createTree (Iterable<RevCommit> commitLogs) throws GitAPIException, MissingObjectException, IncorrectObjectTypeException, IOException
	{
		RelationTree tree = new RelationTree ();

		// First pass to construct the node graph to construct basic node
		// relationship.
		tree.addNodes (commitLogs, m_gitRepo);

		// Second pass to reconstruct branches
		Map<String, ObjectId> reverseBranchMap = m_gitRepo.getReverseBranchMap ();
		List<RelationNode> importantBranches = getBranches (tree, m_importantBranchNames, reverseBranchMap);

		tree.inferBranches (importantBranches);

		// Third pass to layout the branches
		layoutBranches (tree, importantBranches);

		return tree;
	}
}
