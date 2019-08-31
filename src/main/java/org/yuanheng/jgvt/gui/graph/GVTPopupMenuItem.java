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
package org.yuanheng.jgvt.gui.graph;

import javax.swing.JMenuItem;

import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
class GVTPopupMenuItem extends JMenuItem
{
	private static final long serialVersionUID = 1432886736834191255L;

	private final RelationNode m_node;

	public GVTPopupMenuItem (RelationNode node)
	{
		m_node = node;
		setText (CommitUtils.getName (node));
	}

	public RelationNode getNode ()
	{
		return m_node;
	}
}
