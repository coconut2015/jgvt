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

import org.yuanheng.jgvt.Main;
import org.yuanheng.jgvt.relation.BranchLog;
import org.yuanheng.jgvt.relation.RelationBranch;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * For this case, the first node of branch A has two parent nodes B and C.
 * B already has a child in its branch, but C only has a single child in A.
 *
 * Case 1: React-33d439 PSBP left (left parent has only 1 child)
 * Case 2: React-0c8934 PSBP left (left parent has only 1 joinable child)
 *
 * @author	Heng Yuan
 */
class ParentSideBranchParentAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 2)
				continue;

			RelationNode leftParent = firstNode.getParents ()[0];
			RelationNode rightParent = firstNode.getParents ()[1];
			RelationBranch leftParentBranch = leftParent.getRelationBranch ();
			RelationBranch rightParentBranch = rightParent.getRelationBranch ();

			if (!DiscoveryUtils.isLastInBranch (rightParent))
			{
				if (leftParent.getChildren ().length == 1 ||
					DiscoveryUtils.hasSingleJoinChild (leftParent) == firstNode)
				{
					branch.mergeParent (leftParentBranch);

					log.log (firstNode, "PSBP left", iteration);
					checkBranches.add (branch);
				}
			}
			else if (!Main.pref.getLeftOnly () &&
					 !DiscoveryUtils.isLastInBranch (leftParent))
			{
				if (rightParent.getChildren ().length == 1 ||
					DiscoveryUtils.hasSingleJoinChild (rightParent) == firstNode)
				{
					branch.mergeParent (rightParentBranch);

					log.log (firstNode, "PSBP right", iteration);
					checkBranches.add (branch);
				}
			}
		}
	}
}
