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

/**
 * @author	Heng Yuan
 */
public class Debug
{
	public static void println (String msg)
	{
		if (Main.configs.debug)
		{
			System.out.println (msg);
		}
	}

	public static void printStackTrace (Throwable t)
	{
		if (Main.configs.debug)
		{
			t.printStackTrace ();
		}
	}
}
