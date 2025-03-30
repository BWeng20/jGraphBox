/*
 *  (c) copyright 2022 Bernd Wengenroth
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 * 
 */
package com.bw.jgraph.graph;

import com.bw.jgraph.io.GraphReader;
import com.bw.jgraph.io.GraphWriter;

import java.io.IOException;
import java.util.*;

/**
 * DataI/O serializer for Graphs.<br>
 * One instance is not thread-safe. A Serializer can be re-used, but not be used in parallel for
 * different graphs.
 */
public class GraphSerializer
{
	/**
	 * Resets the serializer.<br>
	 * Written elements are forgotten.<br>
	 * Is automatically called if a new graph is started.
	 */
	public void reset()
	{
		elementsWritten_.clear();
		elementsRead_.clear();
	}

	/**
	 * Controls if attributes are written by ordinals or by names.<br>
	 * Ordinals can be used if the ordinals are fixed and not dynamic assigned.<br>
	 * Default is usage of ordinals.
	 * @see Attribute
	 */
	public void setUseAttributeOrdinals( boolean useOrdinals )
	{
		useAttributeOrdinals = useOrdinals;
	}

	/**
	 * Writes edge data with the specified fieldId.<br>
	 * Can be used directly if called implements some other storage format, but normally this method would be called 
	 * internally from {@link #write(GraphWriter, Graph, int)}.<br>
	 * If one edge was written already by this instance it
	 * will be referenced via the element id.
	 * @param e The edge to write. Can be null.
	 */
	public void writeEdge(GraphWriter o, Edge e, int fieldId ) throws IOException
	{
		if ( e == null )
		{
			o.writeNull(fieldId);
		}
		else
		{
			writeElement( o, e,  fieldId++);
			if ( e != null )
			{
				writeNode(o, e.getSource(), fieldId++);
				writeNode(o, e.getTarget(), fieldId++);
				o.endElement();
			}
		}
	}

	/**
	 * Writes node data with the specified fieldId.<br>
	 * Can be used directly if caller implements some other storage format, but normally this method would be called
	 * internally from {@link #write(GraphWriter, Graph, int)}.<br>
	 * Connected nodes are written recursively. If one node was written already by this instance it
	 * will be referenced via the id. So also cyclic graphs can be written.
	 * @param n The node to write. Can be null.
	 */
	public void writeNode( GraphWriter o, Node n, int fieldId ) throws IOException
	{
		if ( n == null )
		{
			o.writeNull(fieldId);
		}
		else
		{
			writeElement( o, n,  fieldId++);
			for (Edge e : n.edges)
				writeEdge(o, e, fieldId++);
			o.endElement();
		}
	}

	/**
	 * Writes a graph with the specified fieldId.<br>
	 */
	public void write(GraphWriter o, Graph g, int fieldId ) throws IOException
	{
		reset();
		if ( o == null )
		{
			o.writeNull(fieldId);
		}
		else
		{
			writeNode( o, g.getRoot(), 1 );
		}
	}

	/**
	 * Read edge data.<br>
	 * Can be used directly if called implements some other storage format, but normally this method would be called 
	 * internally from {@link #read(GraphReader)}.<br>
	 */
	public Edge readEdge( GraphReader i ) throws IOException
	{
		if ( i.isFieldNull() )
		{
			i.skip();
			return null;
		}
		else if ( i.isFieldNumeric())
		{
			return (Edge)elementsRead_.get(i.readNumber().intValue());
		}
		else
		{
			Edge e = new Edge();
			readElement(i, e);
			Node source = readNode(i);
			Node target = readNode(i);
			e.setSourceAndTarget( source, target );
			return e;
		}
	}

	/**
	 * Reads a node.<br>
	 * Can be used directly if called implements some other storage format, but normally this method would be called
	 * internally from {@link #read(GraphReader)}.<br>
	 */
	public Node readNode(GraphReader i ) throws IOException
	{
		if ( i.isFieldNull() )
		{
			i.skip();
		}
		else
		{
			if (i.isFieldNumeric())
			{
				return (Node) elementsRead_.get(i.readNumber()
												 .intValue());
			}
			else if (i.isFieldObject())
			{
				Node node = new Node();
				readElement(i, node);
				while (i.hasNextField())
					node.edges.add(readEdge(i));
				return node;
			}
		}
		return null;
	}


	/**
	 * Reads a graph from the current input field.<br>
	 */
	public Graph read(GraphReader i ) throws IOException
	{
		reset();
		if ( i.isFieldNull() )
		{
			i.skip();
			return null;
		}
		else
		{
			Graph g = new Graph();
			g.setRoot( readNode( i ) );
			return g;
		}
	}

	/**
	 * Write common data for graph elements.
	 */
	private boolean writeElement( GraphWriter o, GraphElement e, int fieldId ) throws IOException
	{
		if ( e == null )
		{
			o.writeNull(fieldId);
			return false;
		}
		else if (elementsWritten_.contains(e.id))
		{
			o.writeInt( fieldId, e.id );
			return false;
		}
		else
		{
			elementsWritten_.add(Integer.valueOf(e.id));

			o.startElement( fieldId );
			int oId = 1;
			o.writeInt( oId++, e.id );
			for (Iterator<Attribute> it = e.attributes(); it.hasNext(); )
			{
				final Attribute a = it.next();
				Object val = e.getAttribute(a);
				if ( val != null )
				{
					if ( useAttributeOrdinals )
						o.writeInt( oId++, a.ordinal );
					else
						o.writeString( oId++, a.name );
					o.writeObject( oId++, val, false );
				}
			}
			o.writeNull( oId++ );
			return true;
		}
	}

	/**
	 * Write common data for graph elements.
	 */
	private void readElement( GraphReader i, GraphElement e ) throws IOException
	{
		Attribute a;
		i.startObject();
		int id = i.readNumber().intValue();
		elementsRead_.put(Integer.valueOf(id), e);

		while ( i.hasNextField() )
		{
			if( i.isFieldNull() )
			{
				i.skip();
				break;
			}
			if ( i.isFieldNumeric() )
			{
				a = Attribute.getAttribute( i.readNumber().intValue() );
			}
			else
			{
				a = Attribute.getAttribute( i.readString());
			}
			e.setAttribute( a, i.readObject());
		}
	}

	/** Remember ids of written elements. */
	private Set<Integer> elementsWritten_ = new HashSet();

	/**
	 * Remembers read elements for later references.<br>
	 * Key is the read id, the element id is different - assigned as normal from id-generator.
	 */
	private Map<Integer,GraphElement> elementsRead_ = new HashMap<>();

	/**
	 * Controls usage of attributes.
	 * @see #setUseAttributeOrdinals(boolean)
	 */
	private boolean useAttributeOrdinals = true;

}
