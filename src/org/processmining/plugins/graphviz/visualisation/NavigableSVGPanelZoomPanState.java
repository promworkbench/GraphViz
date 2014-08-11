package org.processmining.plugins.graphviz.visualisation;

public class NavigableSVGPanelZoomPanState {
	private final double originX;
	private final double originY;
	private final double scale;
	private final double navScale;
	private final double initialScale;

	public NavigableSVGPanelZoomPanState(double originX, double originY, double scale, double navScale, double initialScale) {
		this.originX = originX;
		this.originY = originY;
		this.scale = scale;
		this.navScale = navScale;
		this.initialScale = initialScale;
	}

	public double getOriginX() {
		return originX;
	}

	public double getOriginY() {
		return originY;
	}

	public double getScale() {
		return scale;
	}

	public double getNavScale() {
		return navScale;
	}
	
	public double getInitialScale() {
		return initialScale;
	}
}
