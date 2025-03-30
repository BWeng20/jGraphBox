package com.bw.jgraph.ui;

import com.bw.jgraph.graph.Attribute;
import com.bw.jgraph.graph.Edge;
import com.bw.jgraph.graph.GraphElement;
import com.bw.jgraph.graph.GraphUtil;
import com.bw.jgraph.graph.Node;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface NodeVisual
{
	public static final Attribute NODE_TEXT = GraphUtil.TEXT_ATTRIBUTE;

	public void paint(GraphicContext ctx, Node node);

	/**
	 * Updates the geometry of a node according to its visual properties.
	 *
	 * @param g    The graphic from which metrics are derived.
	 * @param node The node for which the geometry shall be updated.
	 */
	public void updateGeometry(Graphics2D g, Node node);

	public Geometry getGeometry();

	public Layout getLayout();

	public int getHorizontalMargin();

	public int getVerticalMargin();

	public void setHorizontalMargin(int margin);

	public void setVerticalMargin(int margin);

	/**
	 * Get the rectangle that covers all visuals of it.<br>
	 * Returned object shall NOT be modified.
	 */
	public Rectangle2D.Float getVisualBounds(Node n);

	/**
	 * Expand or collapse the node.
	 */
	public void expand(Edge edge, boolean expand);

	/**
	 * Check expandable state of a node.
	 */
	public default boolean isExpandable(Node node)
	{
		return node != null && node.children()
								   .hasNext();
	}

	/**
	 * Check expanded state.
	 */
	public boolean isExpanded(Edge edge);

	public default VisualState getState(GraphElement element)
	{
		return getGeometry().getVisualState(element);
	}

	/**
	 * Calls if used clicked on the node.
	 *
	 * @param node The node that is clicked
	 * @param p    The clicked point, relative to node origin.
	 * @return a sub-element that is hit or null.
	 */
	public GraphElement click(Node node, Point2D.Float p);

	/**
	 * Calls if used clicked on the node.
	 *
	 * @param node The node that is pressed
	 * @param p    The pressed point, relative to node origin.
	 * @return a sub-element that is hit or null.
	 */
	public GraphElement pressed(Node node, Point2D.Float p);

	/**
	 * Calls if used release the mouse button.
	 */
	public void released();

	VisualSettings getVisualSettings();
}
