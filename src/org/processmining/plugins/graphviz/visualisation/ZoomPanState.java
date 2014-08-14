package org.processmining.plugins.graphviz.visualisation;

import javax.swing.JComponent;

import com.kitfox.svg.SVGDiagram;

public class ZoomPanState {
	private double deltaOriginX;
	private double deltaOriginY;
	private double deltaScale;
	private double deltaNavScale;
	private Transformation transformation;

	public ZoomPanState() {
		reset();
	}

	public ZoomPanState(ZoomPanState panState) {
		this.deltaOriginX = panState.getDeltaOriginX();
		this.deltaOriginY = panState.getDeltaOriginY();
		this.deltaScale = panState.getDeltaScale();
		this.deltaNavScale = panState.getDeltaNavScale();
	}

	public void reset() {
		this.deltaOriginX = 0;
		this.deltaOriginY = 0;
		this.deltaScale = 1.0;
		this.deltaNavScale = 1;
	}

	public double getDeltaOriginX() {
		return deltaOriginX;
	}

	public void setDeltaOriginX(double deltaOriginX) {
		this.deltaOriginX = deltaOriginX;
	}

	public double getDeltaOriginY() {
		return deltaOriginY;
	}

	public void setDeltaOriginY(double deltaOriginY) {
		this.deltaOriginY = deltaOriginY;
	}

	public double getDeltaScale() {
		return deltaScale;
	}

	public void setDeltaScale(double deltaScale) {
		this.deltaScale = deltaScale;
	}

	public double getDeltaNavScale() {
		return deltaNavScale;
	}

	public void setDeltaNavScale(double deltaNavScale) {
		this.deltaNavScale = deltaNavScale;
	}

	public Transformation getTransformation(SVGDiagram diagram, JComponent panel) {
		if (transformation == null) {
			updateTransformation(diagram, panel);
		}
		return transformation;
	}

	public void updateTransformation(SVGDiagram diagram, JComponent panel) {
		transformation = ZoomPan.getImage2PanelTransformation(diagram, panel);
	}	
	
	public void resetTransformation() {
		transformation = null;
	}

	public String toString() {
		return "dx: " + deltaOriginX + ", dy: " + deltaOriginY + ", ds: " + deltaScale + ", dns: " + deltaNavScale;
	}

}
