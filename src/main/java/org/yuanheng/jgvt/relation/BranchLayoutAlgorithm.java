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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author	Heng Yuan
 */
public class BranchLayoutAlgorithm
{
	public static void layoutBranches (RelationTree tree)
	{
		if (tree.size () == 0)
		{
			return;
		}

		for (RelationNode node : tree.getNodes ())
		{
			node.resetLayout ();
		}
		RelationNode startNode = tree.getStartNode ();
		Set<RelationBranch> branchSet = tree.getBranchSet ();
		for (RelationBranch branch : branchSet)
		{
			branch.resetLayout ();
		}

		LayoutMatrix matrix = new LayoutMatrix ();
		LinkedList<LayoutState> states = new LinkedList<LayoutState> ();
		{
			RelationBranch mainBranch = startNode.getRelationBranch ();
			LayoutState state = new LayoutState (mainBranch);
			state.setX (0);
			state.setY (0);
			states.add (state);
			mainBranch.visit ();
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
					if (childBranch.isVisited ())
					{
						if (child.getParents ()[0] == node)
						{
							child.setRelation (node, RelationType.BRANCH);
						}
						else
						{
							// we do not need to do anything other than having
							// a merge-out arrow from parent to child.
							child.setRelation (node, RelationType.MERGE);
						}
						continue;
					}
					branchSet.remove (childBranch);
					childBranch.visit ();
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
					int y1 = y - childBranch.indexOf (child);
					if (y1 < 0)
						y1 = 0;
					int y2 = y1 + childState.size () - 1;
					while (matrix.isTaken (checkX, y1, y2))
						++checkX;
					matrix.take (checkX, y1, y2);
					childState.setX (checkX);
					childState.setY (y1);
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
							branch.visit ();

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
}
