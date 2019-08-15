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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.yuanheng.jgvt.ChangeInfo;
import org.yuanheng.jgvt.gui.treetable.JTreeTable;
import org.yuanheng.jgvt.gui.treetable.TreeTableModel;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
public class ChangeTree extends JTreeTable
{
	public static Color COLOR_ADDED = new Color (36, 159, 64);
	public static Color COLOR_DELETED = new Color (203, 36, 49);

	public static Color BG_COLOR_LINE_ADDED = new Color (230, 255, 237);
	public static Color BG_COLOR_LINE_DELETED = new Color (255, 238, 240);

	private static final long serialVersionUID = -4267180235096815912L;

	private TableCellRenderer m_addedCellRenderer = new DefaultTableCellRenderer.UIResource ()
	{
		private static final long serialVersionUID = 1286185083801051075L;

		{
            setHorizontalAlignment(JLabel.RIGHT);
            setBackground (BG_COLOR_LINE_ADDED);
            setFont(getFont().deriveFont(Font.BOLD));
		}
	};

	private TableCellRenderer m_deletedCellRenderer = new DefaultTableCellRenderer.UIResource ()
	{
		private static final long serialVersionUID = -2822845514556118452L;

		{
            setHorizontalAlignment(JLabel.RIGHT);
            setBackground (BG_COLOR_LINE_DELETED);
            setFont(getFont().deriveFont(Font.BOLD));
		}
	};

	private TreeCellRenderer m_changeTreeCellRenderer = new DefaultTreeCellRenderer ()
	{
		private static final long serialVersionUID = 7735958871205684849L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			Component comp = super.getTreeCellRendererComponent (tree, value, selected, expanded, leaf, row, hasFocus);

			if (!selected && value instanceof ChangeTreeFile)
			{
				ChangeTreeFile n = (ChangeTreeFile) value;
				switch (n.getChangeType ())
				{
					case ADD:
					case COPY:
						comp.setForeground (COLOR_ADDED);
						break;
					case DELETE:
						comp.setForeground (COLOR_DELETED);
						break;
					default:
						break;
				}
			}
			return comp;
		}
	};

	public ChangeTree ()
	{
		super (new ChangeTreeTableModel ());
		getColumnModel().getColumn(ChangeTreeTableModel.COL_ADDED).setCellRenderer (m_addedCellRenderer);
		getColumnModel().getColumn(ChangeTreeTableModel.COL_DELETED).setCellRenderer (m_deletedCellRenderer);
		setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		getTree ().setCellRenderer (m_changeTreeCellRenderer);
		autoFitColumn (ChangeTreeTableModel.COL_ADDED, 1);
		autoFitColumn (ChangeTreeTableModel.COL_DELETED, 1);
	}

	public TreeTableModel getTreeTableModel ()
	{
		return (TreeTableModel) getTree ().getModel ();
	}

	private void autoFitColumn (int column, int rowCount)
	{
		int maxWidth = 8;
		TableColumn tableColumn = getColumnModel().getColumn(column);
		{
			TableCellRenderer renderer = tableColumn.getHeaderRenderer();
			if (renderer == null)
			{
				renderer = getTableHeader().getDefaultRenderer();
		    }
			Component comp = renderer.getTableCellRendererComponent(this, tableColumn.getHeaderValue(), false, false, -1, column);
			maxWidth = comp.getPreferredSize().width;
		}
		for (int row = 0; row < rowCount; ++row)
		{
			TableCellRenderer renderer = getCellRenderer (row, column);
			Component comp = prepareRenderer(renderer, row, column);
			maxWidth = Math.max(comp.getPreferredSize().width + 1 , maxWidth);
		}
		maxWidth += 20;	// add some margin
		tableColumn.setPreferredWidth (maxWidth);
		tableColumn.setMinWidth (maxWidth);
		tableColumn.setMaxWidth (maxWidth);
	}

	public void setList (RelationNode node, List<ChangeInfo> list)
	{
		ChangeTreeRoot root = (ChangeTreeRoot) getTreeTableModel ().getRoot ();
		root.setNode (node);
		root.clear ();

		HashMap<String, ChangeTreeDirectory> dirMap = new HashMap<String, ChangeTreeDirectory> ();

		int maxAdded = 0;
		int maxDeleted = 0;
		for (ChangeInfo info : list)
		{
			maxAdded = Math.max (maxAdded, info.getAdded ());
			maxDeleted = Math.max (maxDeleted, info.getDeleted ());
			ChangeTreeFile file = new ChangeTreeFile (root, info);
			String dir = file.getDirectory ();
			if (dir.length () > 0)
			{
				ChangeTreeDirectory dirNode = dirMap.get (dir);
				if (dirNode == null)
				{
					dirNode = new ChangeTreeDirectory (root, dir);
					root.add (dirNode);
					dirMap.put (dir, dirNode);
				}
				dirNode.add (file);
			}
			else
			{
				root.add (file);
			}
		}

		((ChangeTreeTableModel)getTreeTableModel ()).fireTreeStructureChanged (root);

		for (int i = 0; i < tree.getRowCount (); ++i)
		{
		    tree.expandRow(i);
		}
		autoFitColumn (ChangeTreeTableModel.COL_ADDED, tree.getRowCount ());
		autoFitColumn (ChangeTreeTableModel.COL_DELETED, tree.getRowCount ());
		doLayout ();
	}

	public String getSelectedNodeHtml ()
	{
		int row = getSelectedRow ();
		if (row < 0)
			return null;
		ChangeTreeNode cn = (ChangeTreeNode)getModel ().getValueAt (row, 0);
		return cn.getHtml ();
	}
}