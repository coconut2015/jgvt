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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.yuanheng.jgvt.gui.GUI;
import org.yuanheng.jgvt.relation.RelationEditList;

/**
 * @author	Heng Yuan
 */
public class Main
{
	public final static Configs configs = new Configs ();
	public static Preference pref = Preference.getPreference (null);
	public static RelationEditList editList = new RelationEditList ();

	private static Options createOptions ()
	{
		Options options = new Options ();
		options.addOption ("b", "branch", false, "list branches");
		options.addOption ("d", "debug", false, "print debug messages");
		options.addOption ("h", "help", false, "print this message");
		options.addOption ("i", "important", true, "specify an important branch name.  The option can be specified multiple times in order of importance.");
		options.addOption ("t", "tag", false, "list tags");
		return options;
	}

	private static void printRefs (List<Ref> refs)
	{
		for (Ref ref : refs)
		{
			System.out.println (ref.getName ());
		}
	}

	private static void listBranches (GitRepo gitRepo) throws GitAPIException
	{
		printRefs (gitRepo.getAllBranches ());
	}

	private static void listTags (GitRepo gitRepo) throws GitAPIException
	{
		printRefs (gitRepo.getTags ());
	}

	public static void main (String[] args) throws Exception
	{
		// disable JGraphX drag-n-drop error logging
		Logger.getGlobal ().setLevel (Level.OFF);;

		GitRepo gitRepo = null;
		File dir = null;
		File file = null;
		CommandLine cmd = null;

		ArrayList<String> importantBranchNames = new ArrayList<String> ();
		try
		{
			CommandLineParser parser = new DefaultParser();
			Options options = createOptions ();
			cmd = parser.parse(options, args);

			for (Option option : cmd.getOptions ())
			{
				switch (option.getId ())
				{
					case 'h':
					{
						HelpFormatter formatter = new HelpFormatter ();
						formatter.printHelp ("java -jar jgvt.jar [options] [repo directory]", options);
						System.exit (0);
						break;
					}
					case 'i':
					{
						String name = option.getValue ().trim ();
						if (name.length () > 0)
						{
							importantBranchNames.add (name);
						}
						break;
					}
					case 'd':
					{
						configs.debug = true;
						break;
					}
				}
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

		if (cmd.hasOption ('b'))
		{
			listBranches (gitRepo);
			System.exit (0);
		}
		if (cmd.hasOption ('t'))
		{
			listTags (gitRepo);
			System.exit (0);
		}

		pref = Preference.getPreference (gitRepo);
		editList = RelationEditList.read (gitRepo);

		Controller controller = new Controller ();
		GUI gui = new GUI (controller);
		try
		{
			controller.setRepo (gitRepo, importantBranchNames);
		}
		catch (Throwable t)
		{
			t.printStackTrace ();
			System.exit (1);
		}
		gui.setVisible (true);
		controller.centerTree ();
		SwingUtilities.invokeLater (() -> { gui.getGraphComponent ().requestFocus (); });

		gui.waitForClose ();
		System.exit (0);
	}
}
