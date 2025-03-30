package com.bw.jgraph.ui;

import com.bw.jgraph.geometry.ClosestPoint;
import com.bw.jgraph.graph.Edge;
import com.bw.jgraph.graph.Graph;
import com.bw.jgraph.graph.GraphElement;
import com.bw.jgraph.graph.GraphEvent;
import com.bw.jgraph.graph.Node;
import com.bw.jgraph.ui.impl.NodeLabelVisual;
import com.bw.jgraph.ui.impl.TreeLayout;
import com.bw.jgraph.ui.impl.TreeRectangleGeometry;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Panel to show a graph.
 */
public class GraphPanel extends JPanel
{

    private final Graph graph_ = new Graph();
    private Geometry geo_;
    private NodeVisual nodeVisual_;
    private EdgeVisual edgeVisual_;
    private boolean dragging_ = false;
    private Point2D.Float graphOrigin_ = new Point2D.Float(0, 0);
    private final GraphMouseHandler mouseHandler_;
    private final GeometryListener sizeListener_ = (geo, e) -> SwingUtilities.invokeLater(() -> updateSize());

    /**
     * Counts paint calls. Can be used to show some fps indicator.
     */
    public int paintCount_ = 0;

    public GraphPanel()
    {
        setLayout(null);
        mouseHandler_ = new GraphMouseHandler(this);
        addMouseListener(mouseHandler_);
        addMouseMotionListener(mouseHandler_);
        addMouseWheelListener(mouseHandler_);

        graph_.addGraphListener((ev) ->
        {
            graphChanged(ev);
        });
    }

    private void graphChanged(GraphEvent ev)
    {
        switch (ev.type)
        {
            case EDGE_ADDED:
                break;
            case EDGE_REMOVED:
                break;
            case ROOT_CHANGED:
                geo_.clear();
                doLayoutGraph();
                break;
        }
    }

    /**
     * Sets the node visual to use.
     */
    public void setNodeVisual(NodeVisual v)
    {
        Node root = graph_.getRoot();
        if (root != null && this.geo_ != null)
        {
            this.geo_.removeDependency(sizeListener_, root);
        }

        if (v == null)
        {
            v = new NodeLabelVisual(new TreeLayout(new TreeRectangleGeometry()), new VisualSettings());
        }

        this.geo_ = v.getGeometry();
        if (root != null)
        {
            this.geo_.addDependency(sizeListener_, root);
        }

        this.geo_.clear();
        this.graphOrigin_.x = 0;
        this.graphOrigin_.y = 0;
        this.nodeVisual_ = v;
    }

    public NodeVisual getNodeVisual()
    {
        return nodeVisual_;
    }

    public void setEdgeVisual(EdgeVisual v)
    {
        edgeVisual_ = v;
    }

    @Override
    public void updateUI()
    {
        super.updateUI();

        if (geo_ != null)
        {
            geo_.clear();
            graphOrigin_.x = 0;
            graphOrigin_.y = 0;
        }
    }

    public float getScale() {
        if ( nodeVisual_ != null )
            return nodeVisual_.getVisualSettings().scale_;
        else
            return 1f;
    }

    public void setScale(float scale) {
        if ( nodeVisual_ != null ) {
            nodeVisual_.getVisualSettings().scale_ = scale;
            updateSize();
        }
    }


    @Override
    protected void paintComponent(Graphics g)
    {
        Node root = graph_.getRoot();
        VisualSettings settings = nodeVisual_.getVisualSettings();
        final float scale = settings.scale_;
        if (root != null)
        {
            GraphicContext ctx = new GraphicContext(g);
            ctx.debug_ = settings.debug_;
            if (ctx.debug_)
            {
                ctx.debugPaint_ = settings.debugPaint_;
                ctx.debugStroke_ = settings.debugStoke_;
            }

            ctx.currentBackground_ = settings.background_ == null ? getBackground() : settings.background_;

            try
            {
                final Graphics2D g2 = ctx.g2D_;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.scale(scale, scale);
                g2.translate(graphOrigin_.x, graphOrigin_.y);

                Rectangle clipping = g2.getClipBounds();
                g2.setPaint(ctx.currentBackground_);
                g2.fillRect(clipping.x, clipping.y, clipping.width, clipping.height);

                if (settings.debug_)
                {
                    g2.setColor(Color.BLUE);
                    g2.setStroke(settings.debugStoke_);
                    g2.drawRect(clipping.x, clipping.y, clipping.width - 1, clipping.height - 1);
                }

                g2.setPaint(settings.edge_.color);
                ctx.currentColor_ = settings.edge_.color;
                paintTreeEdges(ctx, clipping, root);

                g2.setPaint(settings.node_.border);
                ctx.currentColor_ = settings.node_.border;
                paintTreeNodes(ctx, clipping, root);
                if (settings.edge_.decorate)
                {
                    g2.setPaint(settings.edge_.color);
                    ctx.currentColor_ = settings.edge_.color;
                    paintTreeEdgeEndPoints(ctx, clipping, root);
                }
            }
            finally
            {
                ctx.dispose();
                ++paintCount_;
            }
        }
        else if (isOpaque())
        {
            Rectangle clipping = g.getClipBounds();
            g.setColor(settings.background_ == null ? getBackground() : settings.background_);
            g.fillRect(clipping.x, clipping.y, clipping.width, clipping.height);
        }
    }

    public void doLayoutGraph()
    {
        Node root = graph_.getRoot();
        if (root != null)
        {
            Graphics2D g2 = (Graphics2D) getGraphics();
            if ( g2 != null ) {
                geo_.beginUpdate();
                updateGeometry(g2, root);
                nodeVisual_.getLayout()
                        .placeChildren(root);
                geo_.endUpdate();
                updateSize();
            }
        }
    }

    protected void updateGeometry(Graphics2D g, Node root)
    {
        nodeVisual_.updateGeometry(g, root);
        for (Iterator<Node> it = root.children(); it.hasNext(); )
        {
            Node c = it.next();
            updateGeometry(g, c);
        }
    }

    protected void paintTreeNodes(GraphicContext ctx, Rectangle area, Node n)
    {
        if (nodeVisual_.getVisualBounds(n)
                       .intersects(area))
        {
            nodeVisual_.paint(ctx, n);
        }
        for (Iterator<Edge> e = n.outgoing(false); e.hasNext(); )
        {
            Edge edge = e.next();
            if (nodeVisual_.isExpanded(edge))
            {
                paintTreeNodes(ctx, area, edge.getTarget());
            }
        }
    }

    protected void paintTreeEdges(GraphicContext ctx, Rectangle area, Node n)
    {
        VisualSettings settings = nodeVisual_.getVisualSettings();
        ctx.g2D_.setColor(settings.edge_.color);
        ctx.g2D_.setStroke(new BasicStroke(settings.edge_.width));

        for (Iterator<Edge> e = n.outgoing(true); e.hasNext(); )
        {
            final Edge edge = e.next();
            if (nodeVisual_.isExpanded(edge))
            {

                final Node t = edge.getTarget();
                edgeVisual_.paint(ctx, edge);
                if (!edge.isCyclic())
                {
                    paintTreeEdges(ctx, area, t);
                }
            }
        }
    }

    protected void paintTreeEdgeEndPoints(GraphicContext ctx, Rectangle area, Node n)
    {
        for (Iterator<Edge> e = n.outgoing(false); e.hasNext(); )
        {
            final Edge edge = e.next();
            if (nodeVisual_.isExpanded(edge))
            {
                final Node t = edge.getTarget();
                edgeVisual_.paintEndPoint(ctx, edge);
                if (!edge.isCyclic() && t != n)
                {
                    paintTreeEdgeEndPoints(ctx, area, t);
                }
            }
        }
    }

    /**
     * @param p Point in Graph-coordinates
     */
    protected Node getNodeAt(Node root, Point2D p)
    {
        final Rectangle2D.Float org = geo_.getBounds(root);
        final float sn = nodeVisual_.getVisualSettings().snapRadius_;
        final Rectangle2D.Float b = new Rectangle2D.Float(org.x - sn, org.y - sn, org.width + 2 * sn, org.height + 2 * sn);

        if (b.contains(p))
        {
            return root;
        }
        else
        {
            for (Iterator<Edge> e = root.outgoing(false); e.hasNext(); )
            {
                final Edge edge = e.next();
                if (nodeVisual_.isExpanded(edge))
                {
                    final Node t = edge.getTarget();
                    Node n = getNodeAt(t, p);
                    if (n != null)
                    {
                        return n;
                    }
                }
            }
        }
        return null;
    }

    public Node getNodeAt(Point2D p)
    {
        Node root = graph_.getRoot();
        if (root == null)
        {
            return null;
        }
        else
        {
            return getNodeAt(root, screenToGraphCoordinates(p));
        }
    }

    public Node getNodeAt(int x, int y)
    {
        return getNodeAt(new Point2D.Float(x, y));
    }

    public Node getElementAt(Point2D p)
    {
        Node n = getNodeAt(p);
        if (n != null)
        {

        }
        return n;
    }

    public Node getElementAt(int x, int y)
    {
        return getElementAt(new Point2D.Float(x, y));
    }

    public Point2D.Float getNodeLocation(Node node)
    {
        Rectangle2D r = nodeVisual_.getGeometry()
                                   .getBounds(node);
        if (r != null)
        {
            final float scale = nodeVisual_.getVisualSettings().scale_;
            Point2D.Float pt = new Point2D.Float();
            pt.x = (float) (r.getX() + graphOrigin_.x) * scale;
            pt.y = (float) (r.getY() + graphOrigin_.y) * scale;
            return pt;
        }
        return null;
    }

    public void moveElementAlongShape(GraphElement e, Shape border, Point2D.Double p)
    {
        // @TODO
        Point2D.Double r = ClosestPoint.onShape(p, border, 0.2);
        Rectangle2D bounds = border.getBounds2D();
        geo_.moveElement(graph_, e, r.x - bounds.getMinX(), r.y - bounds.getMinY());
        repaint();
    }

    public void moveNode(Node node, double dx, double dy, boolean moveTree)
    {
        final float scale = nodeVisual_.getVisualSettings().scale_;
        if (moveTree)
        {
            geo_.moveTree(graph_, node, dx / scale, dy / scale);
        }
        else
        {
            geo_.moveElement(graph_, node, dx / scale, dy / scale);
        }
        repaint();
        if (!dragging_)
        {
            updateSize();
        }
    }

    public void moveOrigin(int dx, int dy)
    {
        final float scale = nodeVisual_.getVisualSettings().scale_;
        graphOrigin_.x += (int) (dx / scale);
        graphOrigin_.y += (int) (dy / scale);
        if (!dragging_)
        {
            updateSize();
        }
        repaint();
    }

    public Graph getGraph()
    {
        return graph_;
    }

    public void startNodeDrag()
    {
        dragging_ = true;
    }

    public void endNodeDrag()
    {
        dragging_ = false;
        updateSize();
    }

    protected void updateSize()
    {

        Node root = graph_.getRoot();
        if (root != null)
        {
            Rectangle2D r = geo_.getGraphBounds(root);
            final float scale = nodeVisual_.getVisualSettings().scale_;

            Dimension d = new Dimension((int) (scale * (r.getX() + graphOrigin_.x + r.getWidth() + 5)),
                    (int) (scale * (r.getY() + graphOrigin_.y + r.getHeight() + 5)));
            Dimension s = getSize();
            if ((d.width > s.width || d.height > s.height) || (d.width < (s.width - 20) || d.height < (s.height - 20)))
            {
                setPreferredSize(d);
                revalidate();
            }
        }
        else
        {
            graphOrigin_ = new Point2D.Float(0, 0);
            setPreferredSize(new Dimension(0, 0));
            revalidate();
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension d;
        if (isPreferredSizeSet())
        {
            d = super.getPreferredSize();
        }
        else
        {
            Node root = graph_.getRoot();
            if (root != null)
            {
                Rectangle2D r = geo_.getGraphBounds(root);
                if (r == null)
                {
                    doLayoutGraph();
                }
                else
                {
                    updateSize();
                }
                d = super.getPreferredSize();
            }
            else
            {
                d = getMinimumSize();
            }
        }
        return d;
    }

    public Point2D.Float screenToGraphCoordinates(Point2D p)
    {
        final float scale = nodeVisual_.getVisualSettings().scale_;
        Point2D.Float pf = new Point2D.Float(
                (float) (p.getX() / scale - graphOrigin_.x),
                (float) (p.getY() / scale - graphOrigin_.y)
        );
        return pf;
    }

}
