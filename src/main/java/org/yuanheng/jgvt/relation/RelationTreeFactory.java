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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.yuanheng.jgvt.GitRepo;

/**
 * @author	Heng Yuan
 */
public class RelationTreeFactory
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

	public RelationTreeFactory (GitRepo gitRepo, List<Pattern> importantBranchNames)
	{
		m_gitRepo = gitRepo;
		m_importantBranchNames = importantBranchNames;
	}

	private static RelationNode getImportantNode (RelationTree tree, List<Pattern> importantBranches, GitRepo gitRepo) throws GitAPIException
	{
		if (importantBranches.size () == 0)
			return null;

		Ref matchRef = null;
		int matchIndex = Integer.MAX_VALUE;

		List<Ref> branches = gitRepo.getAllBranches ();
		ExitLoop:
		for (Ref ref : branches)
		{
			String name = ref.getName ();
			for (int i = 0; i < importantBranches.size (); ++i)
			{
				if (importantBranches.get (i).matcher (name).matches ())
				{
					if (i < matchIndex)
					{
						matchRef = ref;
						matchIndex = i;
						if (i == 0)
							break ExitLoop;
					}
					break;
				}
			}
		}
		if (matchRef == null)
			return null;
		return tree.getNode (matchRef.getObjectId ());
	}

	private void layoutBranches (RelationTree tree, RelationNode startNode) throws GitAPIException, IOException
	{
		Set<RelationBranch> branchSet = tree.getBranchSet ();
		LayoutMatrix matrix = new LayoutMatrix ();
		LinkedList<LayoutState> states = new LinkedList<LayoutState> ();
		{
			RelationBranch mainBranch = startNode.getRelationBranch ();
			LayoutState state = new LayoutState (mainBranch);
			state.setX (0);
			state.setY (0);
			states.add (state);
			mainBranch.getLayoutInfo ().visit ();
			matrix.take (0, 0, state.size () - 1);
			branchSet.remove (mainBranch);
		}

		for (;;)
		{
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
					// a bit of optimization
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
					branchSet.remove (childBranch);
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
					matrix.take (checkX, y, y2);
					childState.setX (checkX);
					childState.setY (y);
					states.add (childState);
				}
			}
			if (branchSet.size () == 0)
				break;
			// we need to find anchor points for the remaining branches
			RelationBranch toRemove = null;
ExitAnchorBranch:
			for (RelationBranch branch : branchSet)
			{
				List<RelationNode> nodes = branch.getOrderedList ();
				for (int index = 0; index < nodes.size (); ++index)
				{
					RelationNode node = nodes.get (index);
					for (RelationNode child : node.getChildren ())
					{
						if (child.getLayoutInfo ().getY () >= 0)
						{
							branch.getLayoutInfo ().visit ();

							toRemove = branch;
							LayoutState state = new LayoutState (branch);
							int checkX = child.getLayoutInfo ().getX () + 1;
							int y = child.getLayoutInfo ().getY () - index - 1;
							if (y < 0)
								y = 0;
							int y2 = y + nodes.size () - 1;
							while (matrix.isTaken (checkX, y, y2))
								++checkX;
							matrix.take (checkX, y, y2);
							state.setX (checkX);
							state.setY (y);
							states.add (state);
							break ExitAnchorBranch;
						}
					}
				}
			}
			if (toRemove == null)
			{
				// find the top most branch
				for (RelationBranch branch : branchSet)
				{
					List<RelationNode> nodes = branch.getOrderedList ();
					if (nodes.get (0).getParents ().length == 0)
					{
						toRemove = branch;
						LayoutState state = new LayoutState (branch);
						int checkX = 1;
						int y = 0;
						int y2 = y + nodes.size () - 1;
						while (matrix.isTaken (checkX, y, y2))
							++checkX;
						matrix.take (checkX, y, y2);
						state.setX (checkX);
						state.setY (y);
						states.add (state);
						break;
					}
				}
				if (toRemove == null)
					break;
			}
			branchSet.remove (toRemove);
		}
	}

	public RelationTree createTree (String startCommit, Iterable<RevCommit> commitLogs) throws GitAPIException, IOException
	{
		RelationTree tree = new RelationTree ();

		// First pass to construct the node graph to construct basic node
		// relationship.
		tree.addNodes (commitLogs, m_gitRepo);

		RelationNode startNode = null;
		if (startCommit == null)
		{
			startNode = getImportantNode (tree, m_importantBranchNames, m_gitRepo);
			if (startNode == null)
			{
				System.out.println ("Unable to determine the main branch.  Please specify the last commit of the main branch.");
				System.exit (1);
			}
		}
		else
		{
			m_gitRepo.fetch ();
			try
			{
				ObjectId id = m_gitRepo.getRepo ().resolve (startCommit);
				startNode = tree.getNode (id);
			}
			catch (Exception ex)
			{
			}
			if (startNode == null)
			{
				System.out.println ("Invalid commit name.");
				System.exit (1);
			}
		}
		tree.inferBranches (startNode);

		// Third pass to layout the branches
		layoutBranches (tree, startNode);

		return tree;
	}
}
