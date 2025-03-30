package com.bw.jgraph.ui.impl;

import com.bw.jgraph.graph.Edge;
import com.bw.jgraph.graph.GraphElement;
import com.bw.jgraph.graph.Node;
import com.bw.jgraph.io.Icons;
import com.bw.jgraph.ui.Connector;
import com.bw.jgraph.ui.Geometry;
import com.bw.jgraph.ui.GraphicContext;
import com.bw.jgraph.ui.Layout;
import com.bw.jgraph.ui.NodeVisual;
import com.bw.jgraph.ui.VisualSettings;
import com.bw.jgraph.ui.VisualState;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Shows only raw text.
 */
public class NodeVisualBase implements NodeVisual
{

	protected int margin_y = 5;
	protected int margin_x = 5;
	protected int margin_y2 = 10;
	protected int margin_x2 = 10;
	protected int clickMargin_ = 10;
	protected Geometry geo;
	protected Layout layout;

	protected Node focusedNode;

	protected Icon expandIcon;
	protected Icon collapseIcon;

	protected static Icon defaultExpandIcon;
	protected static Icon defaultCollapseImage;
	protected static int defaultExpandWidth = 10;

	protected Stroke borderStroke;
	protected Stroke focusStroke;

	protected VisualSettings settings;

	@Override
	public VisualSettings getVisualSettings()
	{
		return settings;
	}

	@Override
	public Rectangle2D.Float getVisualBounds(Node n)
	{
		Rectangle2D.Float r = geo.getBounds(n);
		if (r == null)
		{
			r = new Rectangle2D.Float();
		}
		return r;
	}

	static
	{
		defaultExpandIcon = Icons.get("expand");
		defaultCollapseImage = Icons.get("collapse");
	}

	public NodeVisualBase(Layout layout, VisualSettings settings)
	{
		this.geo = layout.getGeometry();
		this.layout = layout;
		this.expandIcon = defaultExpandIcon;
		this.collapseIcon = defaultCollapseImage;

		this.borderStroke = new BasicStroke(1.5f);
		final float[] dashes =
				{
						5
				};
		this.focusStroke = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dashes, 0.0f);
		this.settings = settings;
	}

	@Override
	public int getHorizontalMargin()
	{
		return margin_x;
	}

	@Override
	public int getVerticalMargin()
	{
		return margin_y;
	}

	@Override
	public void setHorizontalMargin(int margin)
	{
		margin_x = margin;
		margin_x2 = margin << 1;
	}

	@Override
	public void setVerticalMargin(int margin)
	{
		margin_y = margin;
		margin_y2 = margin << 1;
	}

	@Override
	public boolean isExpanded(Edge edge)
	{
		if (edge != null)
		{
			VisualState s = getGeometry().getVisualState(edge.getSource());
			Connector c = s.connectors.get(edge.getTarget().id);
			return c != null && c.expanded;
		}
		return false;
	}

	@Override
	public void expand(Edge edge, boolean expand)
	{
		final Node node = edge.getSource();
		VisualState state = getState(node);
		Connector c = state.getConnector(edge.getTarget());
		if (expand != c.expanded)
		{
			geo.beginUpdate();
			c.expanded = expand;
			if (expand)
			{
				for (Iterator<Node> it = node.children(); it.hasNext(); )
				{
					geo.setVisibility(it.next(), true);
				}
				layout.placeChildren(node);
			}
			else
			{
				for (Node n : node.getTreeDescendantNodes())
				{
					geo.setVisibility(n, false);
					for (Connector cc : geo.getVisualState(n).connectors.values())
						cc.expanded = false;

				}
			}
			geo.endUpdate();
		}
	}


	/**
	 * Paint the border of a node.
	 *
	 * @param ctx   Drawing context
	 * @param node  The node to draw for.
	 * @param state The current visual state of the node.
	 */
	public void paintBorder(GraphicContext ctx, Node node, VisualState state, Rectangle bounds)
	{
		boolean focused = (focusedNode == node);

		final Graphics2D g = ctx.g2D_;
		if (settings.node_.opaque)
		{
			g.setColor(settings.node_.background);
			g.fillRect(bounds.x + margin_x, bounds.y + margin_y, bounds.width - margin_x2, bounds.height - margin_y2);
		}

		g.setColor(settings.node_.border);
		g.setStroke(borderStroke);
		g.drawRect(bounds.x + margin_x, bounds.y + margin_y, bounds.width - margin_x2, bounds.height - margin_y2);
		if (focused)
		{
			g.setColor(Color.GRAY);
			g.setStroke(focusStroke);
			g.drawRect(bounds.x + margin_x / 2, bounds.y + margin_y / 2, bounds.width - margin_x + 1, bounds.height - margin_y + 1);
		}
		paintConnectors(g, node, state, bounds, focused);
	}

	public void paintConnectors(Graphics g, Node node, VisualState state, Rectangle bounds, boolean focused)
	{
		// @TODO: for now only outgoing
		for (Iterator<Node> ci = node.children(); ci.hasNext(); )
		{
			Connector c = state.connectors.get(ci.next().id);
			if (c != null)
			{
				final Icon img = c.expanded ? collapseIcon : expandIcon;
				if ( img != null)
				img.paintIcon(null, g, bounds.x + (int) (c.xOffset - (img.getIconWidth() / 2f)),
						bounds.y + (int) (c.yOffset - (img.getIconHeight() / 2f)));
			}
		}
	}

	@Override
	public void paint(GraphicContext ctx, Node node)
	{
		final VisualState s = geo.getVisualState(node);
		final Rectangle bounds = Geometry.toRect(s.boundingBox);

		final Graphics2D g = ctx.g2D_;

		if (ctx.debug_)
		{
			if (s.boundingBox != null)
			{
				g.setPaint(ctx.debugPaint_);
				g.setStroke(ctx.debugStroke_);
				g.draw(s.boundingBox);
			}
		}
		paintBorder(ctx, node, s, bounds);

		final FontMetrics m = g.getFontMetrics();
		String text = (String)node.getAttribute(NODE_TEXT);

		final int lineHeight = m.getHeight();
		final int x = bounds.x + margin_x2;
		int y = bounds.y + margin_y2 + lineHeight - m.getDescent();
		int i1 = 0;
		final int n = text.length();
		do
		{
			int i2 = text.indexOf('\n', i1);
			if (i2 < 0)
			{
				i2 = n;
			}
			g.drawString(text.substring(i1, i2), x, y);
			y += lineHeight;
			i1 = i2 + 1;
		} while (i1 >= 0 && i1 < n);
	}

	@Override
	public void updateGeometry(Graphics2D g, Node node)
	{
		geo.beginUpdate();
		final FontMetrics m = g.getFontMetrics();
		String text = (String)node.getAttribute(NODE_TEXT);

		Rectangle2D.Float r = new Rectangle2D.Float();
		int lineHeight = m.getHeight();
		int i1 = 0;
		final int n = text.length();
		do
		{
			int i2 = text.indexOf('\n', i1);
			if (i2 < 0)
			{
				i2 = n;
			}
			Rectangle rt = m.getStringBounds(text, i1, i2, g)
							.getBounds();
			int w = rt.width;
			r.height += lineHeight;
			if (w > r.width)
			{
				r.width = w;
			}
			i1 = i2 + 1;
		} while (i1 >= 0 && i1 < n);

		r.height += (4 * margin_y);
		r.width += (4 * margin_x);

		geo.setBounds(node, r);
		updateVisibility(node);
		geo.endUpdate();
	}

	/**
	 * Updates visibility according to parents expand state.
	 */
	protected void updateVisibility(Node node)
	{
		Iterator<Edge> it = node.incoming();
		boolean visible = !it.hasNext();
		while (it.hasNext())
		{
			if (isExpanded(it.next()))
			{
				visible = true;
				break;
			}
		}
		geo.setVisibility(node, visible);

	}

	@Override
	public Geometry getGeometry()
	{
		return geo;
	}

	@Override
	public Layout getLayout()
	{
		return layout;
	}

	@Override
	public GraphElement click(Node node, Point2D.Float p)
	{
		VisualState s = geo.getVisualState(node);
		Connector c = s.getConnectorAt(p, settings.snapRadius_ + expandIcon.getIconWidth() / 2f);
		if (c != null)
		{
			final boolean newExpanded = !c.expanded;

			for (Iterator<Edge> it = node.outgoing(true); it.hasNext(); )
			{
				Edge edge = it.next();
				Connector c2 = s.connectors.get(edge.getTarget().id);
				if (c2 != null && c2.xOffset == c.xOffset && c2.yOffset == c.yOffset)
				{
					expand(edge, newExpanded);
				}
			}
		}
		return c;
	}

	public void setFocusedNode(Node node)
	{
		focusedNode = node;
	}

	@Override
	public GraphElement pressed(Node node, Point2D.Float graphPoint)
	{
		setFocusedNode(node);
		VisualState s = geo.getVisualState(node);
		if (s != null)
		{
			return s.getConnectorAt(graphPoint, settings.snapRadius_ + expandIcon.getIconWidth() / 2f);
		}
		return null;
	}

	@Override
	public void released()
	{
	}

}
