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
 * For a branch A's last node.  For its children, if only one of them
 * has the last node as parent 0.  Then we connect this node with that
 * children.
 *
 * @author	Heng Yuan
 */
class TrustParent0Algorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();

			RelationNode foundChildNode = null;
			for (RelationNode childNode : lastNode.getChildren ())
			{
				if (!DiscoveryUtils.isFirstInBranch (childNode))
					continue;
				if (childNode.getParents ()[0] != lastNode)
					continue;

				if (foundChildNode == null)
				{
					foundChildNode = childNode;
				}
				else
				{
					foundChildNode = null;
					break;
				}
			}

			if (foundChildNode != null)
			{
				branch.mergeChild (foundChildNode.getRelationBranch ());

				log.log (lastNode, "TP0", iteration);
				checkBranches.add (branch);
			}
		}
	}
}
