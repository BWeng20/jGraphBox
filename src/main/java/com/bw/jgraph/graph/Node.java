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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A node of the graph.<br>
 * A node owns attributes (via GraphElement) and directed edges.
 */
public class Node extends GraphElement
{

    List<Edge> edges = new ArrayList<>();

    /**
     * Iterate across all linked parent (via incoming edges).
     */
    public Iterator<Node> parents()
    {
        return new TransformedIterator<Edge, Node>(incoming(), Edge::getSource );
    }

    /**
     * Iterate across all linked children (via outgoing acyclic edges).
     */
    public Iterator<Node> children()
    {
        return new TransformedIterator<Edge, Node>(outgoing(false), Edge::getTarget );
    }

    /**
     * Iterate across all incoming edges.
     */
    public Iterator<Edge> incoming()
    {
        return new FilteredIterator<Edge>(edges.iterator(),
                (Predicate<Edge>) edge -> edge.getSource() != Node.this);
    }

    /**
     * Iterate across all outgoing edges.
     *
     * @param cyclic if true cyclic edged are also considered.
     */
    public Iterator<Edge> outgoing(boolean cyclic)
    {
        return new FilteredIterator<Edge>(edges.iterator(),
                (Predicate<Edge>) edge -> edge.getTarget() != Node.this && (cyclic || !edge.isCyclic()));
    }

    /**
     * Checks if this node is an ancestor of the other node.<br>
     * Remind that a node is not an ancestor of itself.
     */
    public boolean isAncestor(Node node)
    {
        if (this == node)
        {
            return false;
        }
        for (Iterator<Node> it = children(); it.hasNext();)
        {
            Node c = it.next();
            if (node == c || c.isAncestor(node))
            {
                return true;
            }
        }
        return false;
    }

    public List<Node> getTreeNodes()
    {
        List<Node> tree = new ArrayList<>();
        getTreeNodes(tree);
        return tree;
    }

    public void getTreeNodes(Collection<Node> tree)
    {
        tree.add(this);
        getTreeDescendantNodes(tree);
    }

    public List<Node> getTreeDescendantNodes()
    {
        List<Node> tree = new ArrayList<>();
        getTreeDescendantNodes(tree);
        return tree;
    }

    public void getTreeDescendantNodes(Collection<Node> tree)
    {
        for (Iterator<Node> it = children(); it.hasNext();)
        {
            Node n = it.next();
            if (n != null)
            {
                n.getTreeNodes(tree);
            }
        }
    }
    
    public boolean walkTreeNodes(Predicate<Node> pre)
    {
        if ( pre.test(this) )
        {
            for (Iterator<Node> it = children(); it.hasNext();)
            {
                Node n = it.next();
                if (n != null && !n.walkTreeNodes(pre) )
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Compares node attributes.<br>
     * Edges are not compared.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof Node)
        {
            return super.equals(obj);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return super.appendTo(new StringBuilder().append("Node ")).toString();
    }
}
