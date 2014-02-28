package org.processmining.plugins.graphviz.visualisation;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Timer;

import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGPanel;

 
public class AnimatableSVGPanel extends SVGPanel {
	
	//animation
	private boolean enableAnimation = true;
	private SVGUniverse universe = null;
	private javax.swing.Timer animationTimer;
	private double animationCurrentTime = 0.0;
	private double animationMaxTime = 20.0;
	private long animationLastTimeUpdated;


	
	private Action timeStepAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;
		public void actionPerformed(ActionEvent arg0) {
			long now = System.currentTimeMillis();
			animationCurrentTime = animationCurrentTime + (animationLastTimeUpdated - now) / 1000.0;
			while (animationCurrentTime > animationMaxTime) {
				animationCurrentTime -= animationMaxTime;
			}
			animationLastTimeUpdated = now;
			repaint();
		}
	};

	public AnimatableSVGPanel() {
		//prepare animation timer
		animationTimer = new Timer(50, timeStepAction);
	}

	private void abc() {		
		if (enableAnimation) {
			universe.setCurTime(System.currentTimeMillis() / 1000.0);
			try {
				universe.updateTime();
			} catch (SVGException e) {
				e.printStackTrace();
			}
		}

	}

	public void setEnableAnimation(boolean enableAnimation, SVGUniverse universe) {
		this.universe = universe;
		this.enableAnimation = enableAnimation;
		animationTimer.start();
	}
}
