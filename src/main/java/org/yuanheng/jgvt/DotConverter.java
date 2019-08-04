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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * @author	Heng Yuan
 */
class DotConverter
{
	public DotConverter ()
	{
	}

	private String getName (RelationNode node)
	{
		return "Node" + node.getCommit ().abbreviate (6).name ();
	}

	public void save (File file, String graphName, DotFileOptions options, RelationTree tree) throws IOException
	{
		PrintWriter pw = new PrintWriter (new FileWriter (file));
		pw.println ("digraph " + graphName + " {");

		Collection<RelationNode> nodes = tree.getNodes ();

		IdentityHashMap<RelationBranch, Object> branchSet = new IdentityHashMap<RelationBranch, Object> ();
		for (RelationNode node : nodes)
		{
			RelationBranch branch = node.getRelationBranch ();
			if (branchSet.containsKey (branch))
				continue;
			branchSet.put (branch, "");
			List<RelationNode> list = branch.getOrderedList ();
			if (options.groupNodes)
			{
				pw.println ("  subgraph cluster_" + branch.hashCode () + " {");
			}
			else
			{
				pw.println ("  subgraph branch_" + branch.hashCode () + " {");
			}
			boolean first = true;
			pw.print ("    ");
			for (RelationNode n : list)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					pw.print (" -> ");
				}
				pw.print (getName (n));
			}
			pw.println ();
			pw.println ("  }");
		}

		for (RelationNode node : nodes)
		{
			RelationBranch branch = node.getRelationBranch ();
			for (RelationNode parent : node.getParents ())
			{
				RelationBranch parentBranch = parent.getRelationBranch ();
				if (branch == parentBranch)
					continue;

				pw.println ("  " + getName (parent) + " -> " + getName (node));
			}
		}

		for (RelationNode node : nodes)
		{
			String annot = node.getAnnotation ();

			if (annot == null)
			{
				pw.println ("  " + getName (node) + " [ label = \"" + node.getCommit ().abbreviate (options.abbrevLength).name () + "\" ]");
			}
			else
			{
				pw.println ("  " + getName (node) + " [ label = \"" + annot + "\" ]");
			}
		}
		pw.println ("}");
		pw.close ();
	}
}
