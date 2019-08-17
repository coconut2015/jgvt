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
package org.yuanheng.jgvt.gui;

import java.util.List;

import org.yuanheng.jgvt.ChangeInfo;
import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.gui.changetree.ChangeTreeCommit;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
class CommitPane extends ChangePane
{
	private static final long serialVersionUID = -3411516962635048642L;

	private RelationNode m_node;

	public CommitPane (Controller controller)
	{
		super (controller, new ChangeTreeCommit ());
	}

	public void select (RelationNode node)
	{
		if (m_node == node)
			return;
		m_node = node;

		((ChangeTreeCommit)getRoot ()).setNode (node);
		List<ChangeInfo> changes = getController ().getGitRepo ().getChanges (node.getCommit ());
		setChanges (changes);
	}
}
