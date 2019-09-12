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

import java.util.Set;

import org.yuanheng.jgvt.relation.BranchLog;
import org.yuanheng.jgvt.relation.RelationBranch;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * For this case, main branch A has a side branch B.  Then A and B
 * are merged to create branch C.  Eventually, C merges back to A.
 * This is an issue usually because C's parents A and B are swapped
 * (i.e. index 0 is A, and index 1 is B).
 *
 * In this case, consider merging B and C.
 *
 * @author	Heng Yuan
 */
class MergeParentAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			RelationNode lastNode = branch.getLast ();

			if (firstNode.getParents ().length == 2 &&
				lastNode.getChildren ().length == 1)
			{
				RelationNode leftParent = firstNode.getParents ()[0];
				RelationBranch leftParentBranch = leftParent.getRelationBranch ();
				RelationNode rightParent = firstNode.getParents ()[1];
				RelationBranch rightParentBranch = rightParent.getRelationBranch ();

				if (DiscoveryUtils.isLastInBranch (rightParent))
				{
					if (DiscoveryUtils.isSideBranch (leftParentBranch, rightParentBranch) &&
						DiscoveryUtils.isMergeTo (branch, leftParentBranch))
					{
						branch.mergeParent (rightParentBranch);

						log.log (firstNode, "MP right", iteration);
						checkBranches.add (branch);
					}
				}
				else if (DiscoveryUtils.isLastInBranch (leftParent))
				{
					if (DiscoveryUtils.isSideBranch(rightParentBranch, leftParentBranch) &&
						DiscoveryUtils.isMergeTo (branch, rightParentBranch))
					{
						branch.mergeParent (leftParentBranch);

						log.log (firstNode, "MP left", iteration);
						checkBranches.add (branch);
					}
				}
			}
		}
	}
}
