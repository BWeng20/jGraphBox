package com.bw.jgraph.ui;

import com.bw.jgraph.graph.Edge;

import java.awt.Point;

public interface EdgeVisual
{
	public VisualEdgeSettings getVisualSettings();

	/**
	 * Paint the edge.
	 * This method is called <i>before</i> all nodes are painted.
	 */
	public void paint(GraphicContext ctx, Edge edge);

	/**
	 * Paint the end point.<br>/
	 * Needed for decorated edges to paint a pretty connection/overlapping.<br>
	 * This method is called <i>after</i> all nodes are painted.
	 */
	public void paintEndPoint(GraphicContext ctx, Edge edge);

	/**
	 * Paint the start point.<br>/
	 * Needed for decorated point to paint a pretty connection/overlapping.<br>
	 * This method is called <i>after</i> all nodes are painted.
	 */
	public void paintStartPoint(GraphicContext ctx, Edge edge);

	/**
	 * Gets the max width of the edge.
	 */
	public float getMaxWidth();

	/**
	 * Calls if used clicked on the edge.
	 * Coordinates are relative to edge origin.
	 */
	public void click(Edge edge, Point p);

	/**
	 * Calls if used clicked on the edge.
	 * Coordinates are relative to edge origin.
	 */
	public void pressed(Edge edge, Point p);

	/**
	 * Calls if used release the mouse button.
	 */
	public void released();
}
