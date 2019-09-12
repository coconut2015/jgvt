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

import java.util.ArrayList;

/**
 * @author	Heng Yuan
 */
public class BranchLog
{
	public static class BranchLogEntry
	{
		public RelationNode node;
		public String algorithm;
		public int iteration;

		public BranchLogEntry (RelationNode node, String algorithm, int iteration)
		{
			this.node = node;
			this.algorithm = algorithm;
			this.iteration = iteration;
		}
	}

	private final ArrayList<BranchLogEntry> m_list;

	public BranchLog ()
	{
		m_list = new ArrayList<BranchLogEntry> ();
	}

	public void log (RelationNode node, String algorithm, int iteration)
	{
		m_list.add (new BranchLogEntry (node, algorithm, iteration));
	}

	public void clear ()
	{
		m_list.clear ();
	}

	public int size ()
	{
		return m_list.size ();
	}

	public BranchLogEntry get (int index)
	{
		return m_list.get (index);
	}
}
