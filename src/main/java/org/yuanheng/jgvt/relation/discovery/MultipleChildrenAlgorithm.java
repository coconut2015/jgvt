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
package org.yuanheng.jgvt.relation.discovery;

import java.util.HashMap;
import java.util.Set;

import org.yuanheng.jgvt.relation.BranchLog;
import org.yuanheng.jgvt.relation.RelationBranch;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * When we have multiple children (greater than 2).  We check if 50%
 * (not counting the child branch to be merged) of child branches
 * merge to a particular branch.  That popular child branch is merged
 * with parent.
 *
 * @author	Heng Yuan
 */
class MultipleChildrenAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
	{
		HashMap<RelationBranch, Integer> childBranchMap = new HashMap<RelationBranch, Integer> ();
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();

			if (lastNode.getChildren ().length > 2)
			{
				childBranchMap.clear ();
				for (RelationNode child : lastNode.getChildren ())
				{
					// Only consider all first in branch cases.
					// Otherwise, sometimes things can get screwed up.
					if (!DiscoveryUtils.isFirstInBranch (child))
						break;
					childBranchMap.put (child.getRelationBranch (), 0);
				}
				if (childBranchMap.size () != lastNode.getChildren ().length)
				{
					childBranchMap.clear ();
					continue;
				}
				int halfCount = lastNode.getChildren ().length / 2;
DoneSearch:
				for (RelationBranch childBranch : childBranchMap.keySet ())
				{
					RelationNode childLastNode = childBranch.getLast ();
					if (childLastNode.getChildren ().length == 0)
					{
						continue;
					}
					if (childLastNode.getChildren ().length == 1)
					{
						RelationBranch toMergeChildBranch = childLastNode.getChildren ()[0].getRelationBranch ();
						Integer value = childBranchMap.get (toMergeChildBranch);
						if (value == null)
						{
							continue;
						}
						int count = value + 1;
						if (count >= halfCount)
						{
							// found it.
							branch.mergeChild (toMergeChildBranch);
							log.log (lastNode, "MC", iteration);
							checkBranches.add (branch);
							break DoneSearch;
						}
						childBranchMap.put (toMergeChildBranch, count);
					}
				}
				childBranchMap.clear ();
			}
		}
	}
}
