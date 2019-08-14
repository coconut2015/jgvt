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

import java.util.HashMap;

import org.yuanheng.jgvt.relation.RelationNode;

/**
 * Since there is a possibility that GVTVertex could be re-created.  We want
 * to make sure GVTVertex instance itself is not used as a key anywhere.
 * Instead, we use the id of GVTVertex as the key.
 *
 * @author	Heng Yuan
 */
public class GVTTree
{
	private final HashMap<Integer, RelationNode> m_nodeMap;
	private final HashMap<RelationNode, Integer> m_reverseMap;
	private final HashMap<Integer, Object> m_vertexMap;

	public GVTTree ()
	{
		m_nodeMap = new HashMap<Integer, RelationNode> ();
		m_reverseMap = new HashMap<RelationNode, Integer> ();
		m_vertexMap = new HashMap<Integer, Object> ();
	}

	public GVTVertex createVertex (RelationNode node, int toolTipFlag)
	{
		int id = m_nodeMap.size ();
		GVTVertex v = new GVTVertex (id);

		m_nodeMap.put (id, node);
		m_reverseMap.put (node, id);

		v.setName (node.toString ());
		v.setToolTip (node.getTooltip (toolTipFlag));

		return v;
	}

	public RelationNode getNode (GVTVertex v)
	{
		return m_nodeMap.get (v.getId ());
	}

	public RelationNode getNode (int id)
	{
		return m_nodeMap.get (id);
	}

	public Integer getVertexId (RelationNode node)
	{
		return m_reverseMap.get (node);
	}

	public void link (int id, Object vertex)
	{
		m_vertexMap.put (id, vertex);
	}

	public Object getVertex (Integer id)
	{
		return m_vertexMap.get (id);
	}

	public Object getVertex (RelationNode node)
	{
		return getVertex (getVertexId (node));
	}

	public int size ()
	{
		return m_nodeMap.size ();
	}
}
