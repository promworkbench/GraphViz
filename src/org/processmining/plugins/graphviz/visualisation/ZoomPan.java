package org.processmining.plugins.graphviz.visualisation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.kitfox.svg.SVGDiagram;

public class ZoomPan {

	private static double zoomIncrement = 1.8;

	public static Transformation getImage2PanelTransformation(SVGDiagram diagram, JComponent panel) {
		double scaleX = panel.getWidth() / (double) diagram.getWidth();
		double scaleY = panel.getHeight() / (double) diagram.getHeight();
		double scale = Math.min(scaleX, scaleY);

		double width = scale * diagram.getWidth();
		double height = scale * diagram.getHeight();

		double x = (panel.getWidth() - width) / 2;
		double y = (panel.getHeight() - height) / 2;

		return new Transformation(x, y, scale);
	}

	/**
	 * When the window is resized, the origin should change
	 * 
	 * @param panState
	 * @param panel
	 */
	public static void onResize(ZoomPanState panState, SVGDiagram diagram, JPanel panel, Dimension oldPanelSize) {
		panState.updateTransformation(diagram, panel);
		panState.setDeltaOriginX(panState.getDeltaOriginX() * panel.getWidth() / oldPanelSize.width);
		panState.setDeltaOriginY(panState.getDeltaOriginY() * panel.getHeight() / oldPanelSize.height);
	}

	/**
	 * Process the panning
	 * 
	 * @param panState
	 * @param oldMousePosition
	 * @param newMousePosition
	 */
	public static void onPan(ZoomPanState panState, Point oldMousePosition, Point newMousePosition) {
		double xDelta = newMousePosition.x - oldMousePosition.x;
		double yDelta = newMousePosition.y - oldMousePosition.y;

		panState.setDeltaOriginX(panState.getDeltaOriginX() + xDelta);
		panState.setDeltaOriginY(panState.getDeltaOriginY() + yDelta);
	}

	/**
	 * Process zooming: true = zoom in; false = zoom out
	 * 
	 * @param zoomTrueInFalseOut
	 */
	public static void onZoom(boolean zoomTrueInFalseOut, Point mousePoint, ZoomPanState panState, SVGDiagram diagram,
			JComponent panel) {

		//get the mouse position on the image
		Transformation t = panState.getTransformation(diagram, panel);
		Point2D mousePointImage = t.transformToImage(mousePoint, panState);

		//scale
		if (zoomTrueInFalseOut) {
			//zoom in
			panState.setDeltaScale(panState.getDeltaScale() * zoomIncrement);
		} else {
			//zoom out
			panState.setDeltaScale(panState.getDeltaScale() / zoomIncrement);
		}

		//get the clicked point on the image in new after-scaling panel coordinates
		Point2D mousePointScaled = t.transformToPanel(mousePointImage, panState);

		//correct the origin
		panState.setDeltaOriginX(panState.getDeltaOriginX() + (mousePoint.x - mousePointScaled.getX()));
		panState.setDeltaOriginY(panState.getDeltaOriginY() + (mousePoint.y - mousePointScaled.getY()));
	}

	/**
	 * Returns whether the image is completely visible in the panel
	 * 
	 * @param panState
	 * @param diagram
	 * @param panel
	 * @return
	 */
	public static boolean isImageCompletelyInPanel(ZoomPanState panState, SVGDiagram diagram, JComponent panel) {
		//get the mouse position on the image
		Transformation t = panState.getTransformation(diagram, panel);

		Point2D nw = t.transformToPanel(new Point(0, 0), panState);
		if (nw.getX() < 0 || nw.getY() < 0) {
			return false;
		}

		Point2D se = t.transformToPanel(new Point2D.Double(diagram.getWidth(), diagram.getHeight()), panState);
		if (se.getX() > panel.getWidth() || se.getY() > panel.getHeight()) {
			return false;
		}

		return true;
	}

	/**
	 * Returns whether the given point (in panel coordinates) is within the image boundaries
	 * @param p
	 * @param panState
	 * @param diagram
	 * @param panel
	 * @return
	 */
	public static boolean isInImage(Point p, ZoomPanState panState, SVGDiagram diagram, JComponent panel) {
		Transformation t = panState.getTransformation(diagram, panel);
		Point2D pI = t.transformToImage(p, panState);
		return (pI.getX() >= 0 && pI.getX() < diagram.getWidth() && pI.getY() >= 0 && pI.getY() < diagram.getHeight());
	}
}
