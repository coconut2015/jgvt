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

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.view.mxGraph;

/**
 * @author Heng Yuan
 */
class GVTGraph extends mxGraph
{
	public final static int DEFAULT_TOOLTIP_FLAG = RelationNode.TOOLTIP_AUTHOR | RelationNode.TOOLTIP_AUTHOR_TS | RelationNode.TOOLTIP_COMMITTER | RelationNode.TOOLTIP_COMMITTER_TS;

	private int m_toolTipFlag;

	public GVTGraph ()
	{
		m_toolTipFlag = DEFAULT_TOOLTIP_FLAG;

		setCellsBendable (false);
		setCellsCloneable (false);
		setCellsDeletable (false);
		setCellsDisconnectable (false);
		setCellsEditable (false);
		setCellsMovable (true);
		setCellsResizable (false);
		setResetEdgesOnMove (true);

		setEnabled (true);

		setAllowDanglingEdges (false);
		setDropEnabled (false);
		setAutoSizeCells (true);
		setSplitEnabled (false);
		setModel (new mxGraphModel ());
	}

	/**
	 * Overriding the method to disable edge selection.
	 *
	 * @param	cell
	 * 			cell object
	 */
	public boolean isCellSelectable(Object cell)
	{
		return this.model.isVertex (cell);
	}

	/**
	 * Overriding the method to call CommitNode's getToolTip to get the
	 * tooltip needed.
	 *
	 * @param	cell
	 * 			cell object
	 */
	@Override
	public String getToolTipForCell (Object cell)
	{
		if (this.model.isEdge (cell))
			return null;

		RelationNode node = (RelationNode) this.model.getValue (cell);
		if (node == null)
			return null;

		return node.getTooltip (m_toolTipFlag);
	}

	public int getToolTipFlag ()
	{
		return m_toolTipFlag;
	}

	public void setToolTipFlag (int toolTipFlag)
	{
		m_toolTipFlag = toolTipFlag;
	}
}
