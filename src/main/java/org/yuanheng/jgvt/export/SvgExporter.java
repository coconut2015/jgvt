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

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.yuanheng.jgvt.gui.graph.GVTGraph;

import com.mxgraph.util.mxCellRenderer;

/**
 * @author Heng Yuan
 */
class SvgExporter
{
	private final GVTGraph m_graph;

	public SvgExporter (GVTGraph graph)
	{
		m_graph = graph;
	}

	public void save (File file) throws IOException, TransformerException
	{
		Document document = mxCellRenderer.createSvgDocument (m_graph, null, 1, Color.WHITE, null);

        Transformer transformer = TransformerFactory.newInstance().newTransformer ();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
	}
}
