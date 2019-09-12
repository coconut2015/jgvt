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
 * For this case, the first node of branch A has two parent branches B
 * and C.  Both B and C ends right there.  C is a side branch of B.
 *
 * Then we merge A and B.
 *
 * @author	Heng Yuan
 */
class MergePullRequestAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length == 2)
			{
				RelationNode leftParent = firstNode.getParents ()[0];
				RelationNode rightParent = firstNode.getParents ()[1];

				if (DiscoveryUtils.isLastInBranch (leftParent) &&
					DiscoveryUtils.isLastInBranch (rightParent))
				{
					RelationBranch leftParentBranch = leftParent.getRelationBranch ();
					RelationBranch rightParentBranch = rightParent.getRelationBranch ();

					if (DiscoveryUtils.isSideBranch (rightParentBranch, leftParentBranch))
					{
						branch.mergeParent (rightParentBranch);

						log.log (firstNode, "MPR right", iteration);
						checkBranches.add (branch);
					}
					else if (DiscoveryUtils.isSideBranch(leftParentBranch, rightParentBranch))
					{
						branch.mergeParent (leftParentBranch);

						log.log (firstNode, "MPR left", iteration);
						checkBranches.add (branch);
					}
				}
			}
		}
	}
}
