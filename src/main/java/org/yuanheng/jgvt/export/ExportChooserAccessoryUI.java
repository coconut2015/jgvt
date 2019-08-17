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
package org.yuanheng.jgvt.export;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * @author	Heng Yuan
 */
public class ExportChooserAccessoryUI extends JPanel
{
	private final static String DEFAULT = "default";
	private static final long serialVersionUID = -1520455470287181897L;

	private final JFileChooser m_chooser;
	private HashMap<String, JComponent> m_compMap = new HashMap<String, JComponent> ();

	private PropertyChangeListener m_listener = new PropertyChangeListener ()
	{
		@Override
		public void propertyChange (PropertyChangeEvent e)
		{
			String prop = e.getPropertyName ();
			if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals (prop))
			{
				ExportFileFilter oldFilter = (ExportFileFilter) e.getOldValue ();
				ExportFileFilterUI oldUI = oldFilter.getUI ();
				if (oldUI != null)
				{
					oldUI.saveOptions (oldFilter);
				}
				ExportFileFilter newFilter = (ExportFileFilter) e.getNewValue ();
				ExportFileFilterUI newUI = newFilter.getUI ();
				if (newUI != null)
				{
					newUI.loadOptions (newFilter);
				}
				switchTo (newFilter.getExtension ());
			}
			else if ("JFileChooserDialogIsClosingProperty".equals (prop))
			{
				ExportFileFilter filter = (ExportFileFilter) m_chooser.getFileFilter ();
				ExportFileFilterUI ui = filter.getUI ();
				if (ui != null)
				{
					ui.saveOptions (filter);
				}
			}
		}
	};

	public ExportChooserAccessoryUI (JFileChooser chooser)
	{
		m_chooser = chooser;
		setLayout (new CardLayout ());
		add (new JPanel (), DEFAULT);
		chooser.addPropertyChangeListener (m_listener);
	}

	public void addUI (String ext, JComponent comp)
	{
		m_compMap.put (ext, comp);
		add (comp, DotFileFilter.EXT);
	}

	public void switchTo (String ext)
	{
		CardLayout card = (CardLayout)getLayout ();
		if (m_compMap.get (ext) == null)
		{
			card.show (this, DEFAULT);
		}
		else
		{
			card.show (this, ext);
		}
	}
}
