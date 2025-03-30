package com.bw.jgraph.ui.impl;

import com.bw.jgraph.graph.Edge;
import com.bw.jgraph.graph.GraphElement;
import com.bw.jgraph.graph.Node;
import com.bw.jgraph.ui.Geometry;
import com.bw.jgraph.ui.GraphicContext;
import com.bw.jgraph.ui.Layout;
import com.bw.jgraph.ui.NodeDecorator;
import com.bw.jgraph.ui.NodeVisual;
import com.bw.jgraph.ui.VisualSettings;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

public class DecoratorNodeVisual extends NodeVisualBase
{

    final Map<Integer, NodeDecorator> decorators = new HashMap<>();
    final NodeVisual nodeVisual;

    @Override
    public void paint(GraphicContext ctx, Node node)
    {
        NodeDecorator d = decorators.get(node.id);
        if (d != null)
        {
            d.decorate(ctx, node);
        }
        nodeVisual.paint(ctx, node);
    }

    @Override
    public VisualSettings getVisualSettings()
    {
        return nodeVisual.getVisualSettings();
    }

    @Override
    public Rectangle2D.Float getVisualBounds(Node node)
    {
        Rectangle2D.Float r = nodeVisual.getVisualBounds(node);
        if (!Geometry.isEmpty(r))
        {
            NodeDecorator d = decorators.get(node.id);
            if (d != null)
            {
                Rectangle2D.Float r2 = d.getBounds(node);
                if (r2 != null)
                {
                    Rectangle2D.Float rn = new Rectangle2D.Float();
                    Rectangle.union(r, r2, rn);
                    r = rn;
                }
            }
        }
        return r;
    }

    @Override
    public void updateGeometry(Graphics2D g, Node node)
    {
        nodeVisual.updateGeometry(g, node);
    }

    @Override
    public Geometry getGeometry()
    {
        return nodeVisual.getGeometry();
    }

    @Override
    public Layout getLayout()
    {
        return nodeVisual.getLayout();
    }

    @Override
    public int getHorizontalMargin()
    {
        return nodeVisual.getHorizontalMargin();
    }

    @Override
    public int getVerticalMargin()
    {
        return nodeVisual.getVerticalMargin();
    }

    @Override
    public void setHorizontalMargin(int margin)
    {
        nodeVisual.setHorizontalMargin(margin);
    }

    @Override
    public void setVerticalMargin(int margin)
    {
        nodeVisual.setVerticalMargin(margin);
    }

    @Override
    public void expand(Edge edge, boolean expand)
    {
        nodeVisual.expand(edge, expand);
    }


    @Override
    public GraphElement click(Node node, Point2D.Float p)
    {
        return nodeVisual.click(node, p);
    }

    @Override
    public GraphElement pressed(Node node, Point2D.Float p)
    {
        return nodeVisual.pressed(node, p);
    }

    @Override
    public void released()
    {
        nodeVisual.released();
    }

    public DecoratorNodeVisual(NodeVisual nodeVisual)
    {
        super(nodeVisual.getLayout(), nodeVisual.getVisualSettings());
        this.nodeVisual = nodeVisual;
    }

    public void addDecorator(Node node, NodeDecorator nd)
    {
        NodeDecorator d = decorators.get(node.id);
        if (d != null)
        {
            if (d instanceof NodeDecoratorComposer)
            {
                ((NodeDecoratorComposer) d).addDecorator(nd);
            }
            else
            {
                decorators.put(node.id, new NodeDecoratorComposer(d, nd));
            }
        }
        else
        {
            decorators.put(node.id, nd);
        }
        nd.install(node);
    }

    public void removeDecorator(Node node, NodeDecorator nd)
    {
        NodeDecorator d = decorators.remove(node.id);
        if (d != null)
        {
            if (d instanceof NodeDecoratorComposer)
            {
                NodeDecoratorComposer dc = (NodeDecoratorComposer) d;
                dc.removeDecorator(nd);
                if (dc.size() > 0)
                {
                    decorators.put(node.id, dc);
                }
            }
            else if (d != nd)
            {
                decorators.put(node.id, d);
            }
            nd.uninstall(node);
        }
    }

}
