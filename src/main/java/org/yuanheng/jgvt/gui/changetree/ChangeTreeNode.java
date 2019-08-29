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
package org.yuanheng.jgvt.gui.changetree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.yuanheng.jgvt.Controller;

/**
 * @author	Heng Yuan
 */
public abstract class ChangeTreeNode implements TreeNode
{
	private TreeNode m_parent;
	private ArrayList<ChangeTreeNode> m_children;
	private WeakReference<String> m_html;

	public ChangeTreeNode (ChangeTreeNode parent)
	{
		m_parent = parent;
	}

	public Integer getAdded ()
	{
		return null;
	}

	public Integer getDeleted ()
	{
		return null;
	}

	public void clear ()
	{
		if (m_children != null)
		{
			m_children.clear ();
		}
	}

	public void add (ChangeTreeNode node)
	{
		if (m_children == null)
			m_children = new ArrayList<ChangeTreeNode> ();
		m_children.add (node);
	}

	@Override
	public TreeNode getChildAt (int childIndex)
	{
		return m_children.get (childIndex);
	}

	@Override
	public int getChildCount ()
	{
		if (m_children == null)
			return 0;
		return m_children.size ();
	}

	@Override
	public TreeNode getParent ()
	{
		return m_parent;
	}

	public void setParent (TreeNode parent)
	{
		m_parent = parent;
	}

	@Override
	public int getIndex (TreeNode node)
	{
		if (m_children == null)
			return -1;
		return m_children.indexOf (node);
	}

	@Override
	public boolean getAllowsChildren ()
	{
		return true;
	}

	@Override
	public boolean isLeaf ()
	{
		return m_children == null || m_children.size () == 0;
	}

	public List<ChangeTreeNode> getChildren ()
	{
		return m_children;
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public Enumeration children ()
	{
		return Collections.enumeration (m_children);
	}

	protected void clearHtml ()
	{
		m_html = null;
	}

	abstract String computeHtml (Controller controller);

	public String getHtml (Controller controller)
	{
		String html = null;
		if (m_html != null)
		{
			html = m_html.get ();
		}
		if (html == null)
		{
			html = computeHtml (controller);
			if (html == null)
				return null;
			m_html = new WeakReference<String> (html);
		}
		return html;
	}
}
