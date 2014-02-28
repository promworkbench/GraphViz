package org.processmining.plugins.graphviz.visualisation;

import java.awt.Graphics;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Timer;

import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

public class AnimatableSVGPanel extends NavigableSVGPanel {

	private static final long serialVersionUID = -767306259426707029L;

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

	public AnimatableSVGPanel(SVGUniverse universe) {
		this.universe = universe;
		
		//prepare animation timer
		animationTimer = new Timer(40, timeStepAction);
		startAnimation();
	}

	protected void paintComponent(Graphics g) {
		if (animationTimer.isRunning()) {
			//update universe time
			universe.setCurTime(System.currentTimeMillis() / 1000.0);
			try {
				universe.updateTime();
			} catch (SVGException e) {
				e.printStackTrace();
			}
		}
		
		super.paintComponent(g);
	}
	
	public void startAnimation() {
		animationTimer.start();
	}
}
