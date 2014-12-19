package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.animation.AnimationElement;

/*
 * Panel that adds animation/video playback to the SVG panel
 */
public class AnimatableSVGPanel extends NavigableSVGPanel {

	private static final long serialVersionUID = -767306259426707029L;

	private double animationMinTime = 0.0;
	private double animationMaxTime = 20.0;
	private boolean repeat = true;
	private boolean animationEnabled = false;

	private long animationLastTimeUpdated;
	private double animationCurrentTime = animationMinTime;
	private javax.swing.Timer animationTimer;

	private Rectangle controls;
	private Rectangle controlsPlayPause;
	private Rectangle controlsProgressLine;
	private boolean mouseIsInControls;
	private boolean startOnMouseRelease = false;

	private Action timeStepAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent arg0) {
			long now = System.currentTimeMillis();
			animationCurrentTime = animationCurrentTime + (now - animationLastTimeUpdated) / 1000.0;
			while (animationCurrentTime > animationMaxTime) {
				if (repeat) {
					animationCurrentTime -= (animationMaxTime - animationMinTime);
				} else {
					stop();
					rewind();
				}
			}
			animationLastTimeUpdated = now;
			repaint();
		}
	};

	private Action startStopAction = new AbstractAction() {
		private static final long serialVersionUID = 8861192606334538705L;

		public void actionPerformed(ActionEvent arg0) {
			startStop();
		}
	};

	private MouseAdapter controlsClickedAdapter = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			Point point = e.getPoint();
			if (animationEnabled && controls.contains(point)) {
				if (controlsProgressLine.contains(point)) {
					//clicked in progress line area
					double progress = (e.getX() - controlsProgressLine.x) / (controlsProgressLine.width * 1.0);
					seek(animationMinTime + progress * (animationMaxTime - animationMinTime));
				} else if (controlsPlayPause.contains(point)) {
					//clicked on play/pause button
					startStop();
				}
			}
		}
		
		public void mouseReleased(MouseEvent e) { 
			if (startOnMouseRelease) {
				start();
				startOnMouseRelease = false;
			}
		}
	};

	private MouseMotionListener mouseMovesAdapter = new MouseMotionListener() {

		public void mouseDragged(MouseEvent e) {
			//dragging moves the animation circle, if in progress line area
			Point point = e.getPoint();
			if (animationEnabled && controls.contains(point) && controlsProgressLine.contains(point)) {
				startOnMouseRelease = startOnMouseRelease || isPlaying();
				double progress = (e.getX() - controlsProgressLine.x) / (controlsProgressLine.width * 1.0);
				seek(animationMinTime + progress * (animationMaxTime - animationMinTime));
				stop();
			}
		}

		public void mouseMoved(MouseEvent e) {
			if (animationEnabled && controls != null && e != null && e.getPoint() != null) {
				boolean newb = controls.contains(e.getPoint());
				if (newb != mouseIsInControls) {
					makeUpdate();
				}
				mouseIsInControls = newb;
				preventDragImage = newb;
			} else {
				preventDragImage = false;
			}
		}
	};

	public AnimatableSVGPanel(SVGDiagram image) {
		super(image);

		//prepare animation timer
		double[] e = getExtremeTimes(image.getRoot());
		setAnimationExtremeTimes(e[0], e[1]);
		animationTimer = new Timer(30, timeStepAction);
		rewind();
		start();

		//set up event listener for controls
		addMouseListener(controlsClickedAdapter);
		addMouseMotionListener(mouseMovesAdapter);
		controlsProgressLine = new Rectangle();
		controlsPlayPause = new Rectangle();

		//set up keyboard shortcuts
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK),
				"startStopAnimation");
		getActionMap().put("startStopAnimation", startStopAction);
	}

	public void setImage(SVGDiagram image, boolean resetView) {
		super.setImage(image, resetView);

		//prepare animation timer
		double[] e = getExtremeTimes(image.getRoot());
		setAnimationExtremeTimes(e[0], e[1]);
	}

	protected void paintComponent(Graphics g) {
		
		if (animationEnabled) {
			//animation is enabled, use current animation time
			image.getUniverse().setCurTime(animationCurrentTime);
		} else {
			//if animation is disabled, use the begin time
			image.getUniverse().setCurTime(animationMinTime);
		}
		try {
			image.getUniverse().updateTime();
		} catch (SVGException e) {
			e.printStackTrace();
		}

		super.paintComponent(g);

		//draw the overlay controls
		if (animationEnabled) {
			drawOverlayControls(g);
		}
	}

	private void drawOverlayControls(Graphics g) {
		Color backupColour = g.getColor();

		int alpha = 20;
		if (mouseIsInControls) {
			alpha = 180;
		}

		g.setColor(new Color(0, 0, 0, alpha));
		int width = getWidth() - 100;
		int height = 50;
		int x = (getWidth() - width) / 2;
		int y = getHeight() - 2 * height;
		g.fillRoundRect(x, y, width, height, 10, 10);
		controls = new Rectangle(x, y, width, height);

		//play button
		g.setColor(new Color(255, 255, 255, alpha));
		controlsPlayPause.setBounds(x + 10, y + 10, 30, height);
		if (!animationPlaying()) {
			//play button
			Polygon triangle = new Polygon();
			triangle.addPoint(x + 10, y + 10);
			triangle.addPoint(x + 10, y + height - 10);
			triangle.addPoint(x + 10 + 25, y + (height / 2));
			g.fillPolygon(triangle);
		} else {
			//pause button
			g.fillRoundRect(x + 10, y + 10, 10, height - 20, 5, 5);
			g.fillRoundRect(x + 25, y + 10, 10, height - 20, 5, 5);
		}

		//progress line
		int startLineX = x + 50;
		int endLineX = x + width - 20;
		int lineY = y + height / 2;
		g.drawLine(startLineX, lineY, endLineX, lineY);
		double progress = (animationCurrentTime - animationMinTime) / (animationMaxTime - animationMinTime);
		controlsProgressLine.setBounds(startLineX, y, endLineX - startLineX, height);
		if (mouseIsInControls) {
			g.fillOval((int) (startLineX + (endLineX - startLineX) * progress) - 5, lineY - 5, 10, 10);
		}

		g.setColor(backupColour);
	}

	private void makeUpdate() {
		if (!animationPlaying()) {
			repaint();
		}
	}

	public boolean animationPlaying() {
		return animationTimer.isRunning();
	}

	public void startStop() {
		if (animationPlaying()) {
			stop();
		} else {
			start();
		}
	}
	
	public boolean isPlaying() {
		return animationTimer.isRunning();
	}

	/**
	 * Starts the animation if enabled and not playing. Might cause
	 * timing\rendering glitches if called repeatedly.
	 */
	public void start() {
		animationLastTimeUpdated = System.currentTimeMillis();
		animationTimer.start();
	}

	/**
	 * Stops the animation.
	 */
	public void stop() {
		animationTimer.stop();
		makeUpdate();
	}

	/**
	 * Resets the animation to its earliest time. Does not alter
	 * stop/start/enabledness.
	 */
	public void rewind() {
		animationCurrentTime = animationMinTime;
	}

	/**
	 * Move the animation to newTime (in ms).
	 * 
	 * @param newTime
	 */
	public void seek(double newTime) {
		animationCurrentTime = newTime;
		makeUpdate();
	}

	public double getAnimationMaxTime() {
		return animationMaxTime;
	}

	public double getAnimationMinTime() {
		return animationMinTime;
	}

	public void setAnimationExtremeTimes(double animationMinTime, double animationMaxTime) {
		if (animationMinTime != Double.POSITIVE_INFINITY) {
			this.animationMinTime = animationMinTime;
		}
		if (animationMaxTime != Double.NEGATIVE_INFINITY) {
			this.animationMaxTime = animationMaxTime;
		}
		if (animationCurrentTime < this.animationMinTime || animationCurrentTime > this.animationMaxTime) {
			rewind();
			makeUpdate();
		}
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public static double[] getExtremeTimes(SVGElement e) {
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;
		if (e instanceof AnimationElement) {
			min = Math.min(min, ((AnimationElement) e).evalStartTime());
			max = Math.max(max, ((AnimationElement) e).evalStartTime() + ((AnimationElement) e).evalDurTime());
		}

		//recurse
		for (int i = 0; i < e.getNumChildren(); i++) {
			double[] ex = getExtremeTimes(e.getChild(i));
			min = Math.min(min, ex[0]);
			max = Math.max(max, ex[1]);
		}

		return new double[] { min, max };
	}

	public boolean isEnableAnimation() {
		return animationEnabled;
	}

	/**
	 * Sets whether the animation is enabled. Does not call start/stop/rewind.
	 * 
	 * @param enableAnimation
	 */
	public void setEnableAnimation(boolean enableAnimation) {
		this.animationEnabled = enableAnimation;
	}
}
