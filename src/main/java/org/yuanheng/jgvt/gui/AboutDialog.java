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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.yuanheng.jgvt.Utils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Paddings;

/**
 * @author	Heng Yuan
 */
class AboutDialog extends JDialog
{
	private static final long serialVersionUID = -7348843726431478709L;

	private final Action m_okAction = new AbstractAction ("OK")
	{
		private static final long serialVersionUID = 7743178620912963719L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			AboutDialog.this.dispose ();
		}
	};

	private final HyperlinkListener m_urlHandler = new HyperlinkListener ()
	{
		@Override
		public void hyperlinkUpdate (HyperlinkEvent e)
		{
			try
			{
				if (e.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
				{
					Utils.browse (e.getURL ().toURI ());
				}
			}
			catch (Exception ex)
			{
			}
		}
	};

	public AboutDialog (JFrame parent)
	{
		super (parent);

		setTitle ("About");
		setModal (true);

		JEditorPane htmlPane = new JEditorPane ();
		htmlPane.addHyperlinkListener (m_urlHandler);
		htmlPane.setEditable (false);
		try
		{
			htmlPane.setPage (AboutDialog.class.getResource ("about.html"));
		}
		catch (Exception ex)
		{
		}
		JPanel buttomPanel = ButtonBarBuilder.create ().addGlue ().addButton (m_okAction).build ();
		buttomPanel.setBorder (Paddings.BUTTON_BAR_PAD);

		JPanel contentPane = new JPanel (new BorderLayout ());
		contentPane.setBorder (Paddings.DIALOG);
		contentPane.add (new JScrollPane (htmlPane), BorderLayout.CENTER);
		contentPane.add (buttomPanel, BorderLayout.SOUTH);
		setContentPane (contentPane);
		setSize (480, 400);
	}
}
