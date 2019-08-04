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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author	Heng Yuan
 */
class GVTTree
{
	private final HashMap<GVTVertex, RelationNode> m_nodeMap;
	private final ArrayList<GVTVertex> m_vertices;
	private final HashMap<RelationNode, GVTVertex> m_reverseMap;

	public GVTTree ()
	{
		m_nodeMap = new HashMap<GVTVertex, RelationNode> ();
		m_reverseMap = new HashMap<RelationNode, GVTVertex> ();
		m_vertices = new ArrayList<GVTVertex> ();
	}

	public GVTVertex createVertex (RelationNode node, int toolTipFlag)
	{
		int id = m_vertices.size ();
		GVTVertex v = new GVTVertex (id);

		m_nodeMap.put (v, node);
		m_reverseMap.put (node, v);
		m_vertices.add (v);

		v.setName (node.toString ());
		v.setToolTip (node.getTooltip (toolTipFlag));

		return v;
	}

	public RelationNode getNode (GVTVertex v)
	{
		return m_nodeMap.get (v);
	}

	public GVTVertex getVertex (int id)
	{
		return m_vertices.get (id);
	}

	public GVTVertex getVertex (RelationNode node)
	{
		return m_reverseMap.get (node);
	}

	public List<GVTVertex> getVertices ()
	{
		return m_vertices;
	}

	public int size ()
	{
		return m_vertices.size ();
	}
}
