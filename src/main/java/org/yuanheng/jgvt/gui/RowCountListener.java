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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * @author	Heng Yuan
 */
class RowCountListener implements RowSorterListener, PropertyChangeListener, TableModelListener
{
	private final JTable m_table;
	private final JLabel m_label;

	public RowCountListener (JTable table, JLabel label)
	{
		m_table = table;
		m_label = label;
	}
	@Override
	public void propertyChange (PropertyChangeEvent evt)
	{
		updateCount ();
	}

	@Override
	public void sorterChanged (RowSorterEvent e)
	{
		updateCount ();
	}

	@Override
	public void tableChanged (TableModelEvent e)
	{
		updateCount ();
	}

	private void updateCount ()
	{
		m_label.setText ("" + m_table.getRowCount ());
	}
}