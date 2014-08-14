package org.processmining.plugins.graphviz.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;

public class NavigableSVGPanelState {

	private boolean navigationImageEnabled = true;
	private Dimension previousPanelSize;
	private final ZoomPanState zoomPanState = new ZoomPanState();
	private double navigationScale = 1.0;

	//settings
	private double minimumScreenImageInPartOfPanel = 0.1;
	private double navigationImageWidthInPartOfPanel = 0.1;
	private Color navigationImageBorderColor = Color.black;
	private boolean antiAlias = true;

	/**
	 * <p>
	 * Indicates whether navigation image is enabled.
	 * <p>
	 * 
	 * @return true when navigation image is enabled, false otherwise.
	 */
	public boolean isNavigationImageEnabled() {
		return navigationImageEnabled;
	}

	public void setNavigationImageEnabled(boolean navigationImageEnabled) {
		this.navigationImageEnabled = navigationImageEnabled;
	}

	public Dimension getPreviousPanelSize() {
		return previousPanelSize;
	}

	public void setPreviousPanelSize(Dimension previousPanelSize) {
		this.previousPanelSize = previousPanelSize;
	}

	public double getMinimumScreenImageInPartOfPanel() {
		return minimumScreenImageInPartOfPanel;
	}

	public void setMinimumScreenImageInPartOfPanel(double minimumScreenImageInPartOfPanel) {
		this.minimumScreenImageInPartOfPanel = minimumScreenImageInPartOfPanel;
	}

	public double getNavigationImageWidthInPartOfPanel() {
		return navigationImageWidthInPartOfPanel;
	}

	public void setNavigationImageWidthInPartOfPanel(double navigationImageWidthInPartOfPanel) {
		this.navigationImageWidthInPartOfPanel = navigationImageWidthInPartOfPanel;
	}

	public Color getNavigationImageBorderColor() {
		return navigationImageBorderColor;
	}

	public void setNavigationImageBorderColor(Color navigationImageBorderColor) {
		this.navigationImageBorderColor = navigationImageBorderColor;
	}

	public boolean isAntiAlias() {
		return antiAlias;
	}

	public void setAntiAlias(boolean antiAlias) {
		this.antiAlias = antiAlias;
	}

	public ZoomPanState getZoomPanState() {
		return zoomPanState;
	}

	final static float dash1[] = {10.0f};
	final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1,
			0.0f);
	public Stroke getNavigationOutlineStroke() {
		return dashed;
	}

	public double getNavigationScale() {
		return navigationScale;
	}

	public void setNavigationScale(double navigationScale) {
		this.navigationScale = navigationScale;
	}

}
