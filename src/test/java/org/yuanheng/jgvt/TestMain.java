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

import org.junit.Test;

/**
 * Dummy test class to make lauching the gui easier.
 *
 * @author	Heng Yuan
 */
public class TestMain
{
	@Test
	public void testMain () throws Exception
	{
		Main.main(new String[] {"../sqlite-jdbc/README.md"});
//		Main.main(new String[] {"../bsonspec.org/implementations.html"});
	}
}
