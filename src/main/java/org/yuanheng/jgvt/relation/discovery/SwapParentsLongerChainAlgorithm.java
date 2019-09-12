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
 * For this case, branch A's first node is the 2nd to the last of branch B.
 * branch A's second node's second parent is the last node of the branch B.
 * In this case, branch A's second node's parents should be swapped such
 * that we get a much longer branch B.
 *
 * Case: React-0db777
 *
 * @author	Heng Yuan
 */
class SwapParentsLongerChainAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
	{
		for (RelationBranch branch : branches)
		{
			// if the chain is smaller than 3, not worth doing it.
			if (branch.size () < 3)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 1)
				continue;
			RelationNode secondNode = branch.getOrderedList ().get (1);
			if (secondNode.getParents ().length != 2)
				continue;

			RelationBranch parentBranch = firstNode.getParents ()[0].getRelationBranch ();
			if (secondNode.getParents ()[1].getRelationBranch () != parentBranch)
				continue;
			if (parentBranch.getLast () != secondNode.getParents ()[1])
				continue;

			// the first node is in a branch by itself
			new RelationBranch (firstNode);

			branch.mergeParent (parentBranch);

			log.log (firstNode, "SPLC", iteration);
			checkBranches.add (branch);
		}
	}
}
