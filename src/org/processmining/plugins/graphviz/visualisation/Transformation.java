package org.processmining.plugins.graphviz.visualisation;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

public class Transformation {
	final double x;
	final double y;
	final double scale;

	public Transformation(double x, double y, double scale) {
		this.x = x;
		this.y = y;
		this.scale = scale;
	}

	public double dx(ZoomPanState panState) {
		return x + panState.getDeltaOriginX();
	}

	public double dy(ZoomPanState panState) {
		return y + panState.getDeltaOriginY();
	}

	public double dscale(ZoomPanState panState) {
		return scale * panState.getDeltaScale();
	}

	public void transform(Graphics2D g, ZoomPanState panState) {
		g.translate(dx(panState), dy(panState));
		g.scale(dscale(panState), dscale(panState));
	}

	public void inverseTransform(Graphics2D g, ZoomPanState panState) {
		g.scale(1 / dscale(panState), 1 / dscale(panState));
		g.translate(-dx(panState), -dy(panState));
	}

	public Point2D transformToPanel(Point2D p, ZoomPanState panState) {
		return new Point2D.Double(p.getX() * dscale(panState) + dx(panState), p.getY() * dscale(panState) + dy(panState));
	}

	public Point2D transformToImage(Point p, ZoomPanState panState) {
		return new Point2D.Double((p.x - dx(panState)) / dscale(panState), (p.y - dy(panState)) / dscale(panState));
	}
}