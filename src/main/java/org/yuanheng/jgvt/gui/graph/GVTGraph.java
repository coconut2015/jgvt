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

import org.yuanheng.jgvt.CommitUtils;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.view.mxGraph;

/**
 * @author Heng Yuan
 */
public class GVTGraph extends mxGraph
{
	public final static int DEFAULT_TOOLTIP_FLAG = CommitUtils.TOOLTIP_AUTHOR | CommitUtils.TOOLTIP_AUTHOR_TS | CommitUtils.TOOLTIP_COMMITTER | CommitUtils.TOOLTIP_COMMITTER_TS | CommitUtils.TOOLTIP_MESSAGE;

	private int m_toolTipFlag;
	private GVTTree m_tree;

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
		setAutoSizeCells (false);
		setSplitEnabled (false);
		setModel (new mxGraphModel ());
	}

	public GVTTree getTree ()
	{
		return m_tree;
	}

	public void setTree (GVTTree tree)
	{
		m_tree = tree;
	}

	/**
	 * Overriding the method to disable edge selection.
	 *
	 * @param	cell
	 * 			cell object
	 */
	public boolean isCellSelectable(Object cell)
	{
		return model.isVertex (cell);
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
		if (m_tree == null ||
			model.isEdge (cell))
		{
			return null;
		}

		GVTVertex v = (GVTVertex) model.getValue (cell);
		if (v == null)
			return null;

		return v.getToolTip ();
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
