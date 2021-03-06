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

import java.util.List;

/**
 * @author	Heng Yuan
 */
class LayoutState
{
	private final List<RelationNode> m_nodes;
	private int m_current;
	private int m_x;
	private int m_y;

	public LayoutState (RelationBranch branch)
	{
		m_nodes = branch.getOrderedList ();
		m_current = -1;
	}

	public int size ()
	{
		return m_nodes.size ();
	}

	public boolean hasNext ()
	{
		return (m_current + 1) < m_nodes.size ();
	}

	public RelationNode next ()
	{
		return m_nodes.get (++m_current);
	}

	public int getX ()
	{
		return m_x;
	}

	public void setX (int x)
	{
		m_x = x;
	}

	public int getY ()
	{
		return m_y;
	}

	public void setY (int y)
	{
		m_y = y;
	}
}
