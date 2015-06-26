package org.processmining.plugins.graphviz.visualisation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.w3c.dom.svg.SVGDocument;

import com.kitfox.svg.SVGDiagram;

public class ZoomPan {

	private static double zoomIncrement = 1.8;

	@Deprecated
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

	public static Transformation getImage2PanelTransformation(SVGDocument document, JComponent panel) {
		BB bbox = getBoundingBox(document);

		double scaleX = panel.getWidth() / bbox.getWidth();
		double scaleY = panel.getHeight() / bbox.getHeight();
		double scale = Math.min(scaleX, scaleY);

		double width = scale * bbox.getWidth();
		double height = scale * bbox.getHeight();

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
	@Deprecated
	public static void onResize(ZoomPanState panState, SVGDiagram diagram, JPanel panel, Dimension oldPanelSize) {
		panState.updateTransformation(diagram, panel);
		panState.setDeltaOriginX(panState.getDeltaOriginX() * panel.getWidth() / oldPanelSize.width);
		panState.setDeltaOriginY(panState.getDeltaOriginY() * panel.getHeight() / oldPanelSize.height);
	}

	/**
	 * When the window is resized, the origin should change
	 * 
	 * @param panState
	 * @param panel
	 */
	public static void onResize(ZoomPanState panState, SVGDocument document, JPanel panel, Dimension oldPanelSize) {
		panState.updateTransformation(document, panel);
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
	@Deprecated
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
	 * Process zooming: true = zoom in; false = zoom out
	 * 
	 * @param zoomTrueInFalseOut
	 */
	public static void onZoom(boolean zoomTrueInFalseOut, Point mousePoint, ZoomPanState panState,
			SVGDocument document, JComponent panel) {

		//get the mouse position on the image
		Transformation t = panState.getTransformation(document, panel);
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
	@Deprecated
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
	 * Returns whether the image is completely visible in the panel
	 * 
	 * @param panState
	 * @param document
	 * @param panel
	 * @return
	 */
	public static boolean isImageCompletelyInPanel(ZoomPanState panState, SVGDocument document, JComponent panel) {
		//get the mouse position on the image
		Transformation t = panState.getTransformation(document, panel);

		Point2D nw = t.transformToPanel(new Point(0, 0), panState);
		if (nw.getX() < 0 || nw.getY() < 0) {
			return false;
		}

		BB bb = getBoundingBox(document);
		Point2D se = t.transformToPanel(new Point2D.Double(bb.getWidth(), bb.getHeight()), panState);
		if (se.getX() > panel.getWidth() || se.getY() > panel.getHeight()) {
			return false;
		}

		return true;
	}

	/**
	 * Returns whether the given point (in panel coordinates) is within the
	 * image boundaries
	 * 
	 * @param p
	 * @param panState
	 * @param diagram
	 * @param panel
	 * @return
	 */
	@Deprecated
	public static boolean isInImage(Point p, ZoomPanState panState, SVGDiagram diagram, JComponent panel) {
		Transformation t = panState.getTransformation(diagram, panel);
		Point2D pI = t.transformToImage(p, panState);
		return (pI.getX() >= 0 && pI.getX() < diagram.getWidth() && pI.getY() >= 0 && pI.getY() < diagram.getHeight());
	}

	/**
	 * Returns whether the given point (in panel coordinates) is within the
	 * image boundaries
	 * 
	 * @param p
	 * @param panState
	 * @param diagram
	 * @param panel
	 * @return
	 */
	public static boolean isInImage(Point p, ZoomPanState panState, SVGDocument document, JComponent panel) {
		Transformation t = panState.getTransformation(document, panel);
		Point2D pI = t.transformToImage(p, panState);
		BB bb = getBoundingBox(document);
		return (pI.getX() >= 0 && pI.getX() < bb.getWidth() && pI.getY() >= 0 && pI.getY() < bb.getHeight());
	}

	public static class BB {
		private float width;
		private float height;

		public BB(float width, float height) {
			this.width = width;
			this.height = height;
		}

		public float getWidth() {
			return width;
		}

		public float getHeight() {
			return height;
		}
	}

	public static BB getBoundingBox(SVGDocument document) {
		return new BB(document.getRootElement().getWidth().getBaseVal().getValue(), document.getRootElement()
				.getHeight().getBaseVal().getValue());
	}
}
