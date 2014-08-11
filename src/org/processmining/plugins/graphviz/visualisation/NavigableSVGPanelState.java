package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

public class NavigableSVGPanelState {

	//This class is required for high precision image coordinates translation.
	public static class Coords {
		public double x;
		public double y;

		public Coords(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public int getIntX() {
			return (int) Math.round(x);
		}

		public int getIntY() {
			return (int) Math.round(y);
		}

		public Point toPoint() {
			return new Point(getIntX(), getIntY());
		}

		public String toString() {
			return "[Coords: x=" + x + ",y=" + y + "]";
		}
	}

	private boolean navigationImageEnabled = true;
	private Dimension previousPanelSize;
	private NavigableSVGPanelZoomPanState panState = null;
	private NavigableSVGPanelZoomPanState newPanState = null;

	//settings
	private double minimumScreenImageInPartOfPanel = 0.1;
	private double navigationImageWidthInPartOfPanel = 0.1;
	private Color navigationImageBorderColor = Color.black;
	private boolean antiAlias = true;
	private double zoomIncrement = 0.2;

	public double getOriginX() {
		return panState.getOriginX();
	}

	public double getOriginY() {
		return panState.getOriginY();
	}

	public double getScale() {
		return panState.getScale();
	}

	public double getNavScale() {
		return panState.getNavScale();
	}

	public double getInitialScale() {
		return panState.getInitialScale();
	}

	public NavigableSVGPanelZoomPanState getPanState() {
		return panState;
	}
	
	/**
	 * Sets a new pan state for inclusion. Note that it will not be effective
	 * until @applyPanState is called.
	 * 
	 * @param panState
	 */
	public void submitPanState(NavigableSVGPanelZoomPanState panState) {
		this.newPanState = panState;
	}
	
	/**
	 * Apply a previously submitted new pan state
	 */
	public void applyPanState() {
		if (newPanState != null) {
			panState = newPanState;
			newPanState = null;
		}
	}

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

	public double getZoomIncrement() {
		return zoomIncrement;
	}

	public void setZoomIncrement(double zoomIncrement) {
		this.zoomIncrement = zoomIncrement;
	}

}
