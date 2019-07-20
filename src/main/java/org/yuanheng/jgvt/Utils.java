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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author	Heng Yuan
 */
class Utils
{
	public static Path getRelativePath (File file, File root) throws IOException
	{
		file = file.getCanonicalFile ();
		root = root.getCanonicalFile ();
		Path filePath = Paths.get (file.toURI ());
		Path rootPath = Paths.get (root.toURI ());
		return rootPath.relativize (filePath);
	}
}
