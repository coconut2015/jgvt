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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.*;
import org.yuanheng.jgvt.gui.GUI;

/**
 * @author	Heng Yuan
 */
public class Main
{
	private static Options createOptions ()
	{
		Options options = new Options ();
		options.addOption ("h", "help", false, "print this message");
		options.addOption ("s", true, "specify the last commit of the main branch");
		return options;
	}
	public static void main (String[] args) throws Exception
	{
		GitRepo gitRepo = null;
		File dir = null;
		File file = null;
		String startCommit = null;

		try
		{
			CommandLineParser parser = new DefaultParser();
			Options options = createOptions ();
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption ('h'))
			{
				HelpFormatter formatter = new HelpFormatter ();
				formatter.printHelp ("jgvt", options);
				System.exit (0);
			}
			if (cmd.hasOption ('s'))
			{
				startCommit = cmd.getOptionValue ('s');
			}
			args = cmd.getArgs ();

			if (args.length > 0)
			{
				file = new File (args[0]);
				if (file.exists ())
				{
					if (file.isDirectory ())
					{
						dir = file;
						file = null;
					}
					else if (file.isFile ())
					{
						dir = file.getParentFile ();
					}
				}
				else
				{
					System.out.println ("Error: File does not exist.");
					System.exit (1);;
				}
			}
			else
			{
				dir = new File (".").getCanonicalFile ();
				file = null;
			}
		}
		catch (Exception ex)
		{
			System.out.println ("Error: " + ex.getMessage ());
			System.exit (1);
		}

		try
		{
			gitRepo = new GitRepo (dir);
		}
		catch (Exception ex)
		{
			System.out.println ("Error: Not in a git repo.");
			System.exit (1);;
		}

		if (gitRepo == null)
		{
			System.exit (1);;
		}

		// disable JGraphX drag-n-drop error logging
		Logger.getGlobal ().setLevel (Level.OFF);;

		Controller controller = new Controller (gitRepo, dir, file);
		GUI gui = new GUI (controller);
		controller.generateTree (startCommit);
		gui.setVisible (true);
		controller.centerTree ();
		SwingUtilities.invokeLater (new Runnable ()
		{
			public void run ()
			{
				gui.getGraphComponent ().requestFocus ();
			}
		});

		gui.waitForClose ();
		System.exit (0);
	}
}
