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

import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

public class AnimatableSVGPanel extends NavigableSVGPanel {

	private static final long serialVersionUID = -767306259426707029L;

	private double animationMaxTime = 20.0;
	private boolean repeat = true;

	private long animationLastTimeUpdated;
	private SVGUniverse universe = null;
	private double animationCurrentTime = 0.0;
	private javax.swing.Timer animationTimer;

	private Rectangle controls;
	private Rectangle controlsPlayPause;
	private Rectangle controlsProgressLine;
	private boolean mouseIsInControls;

	private Action timeStepAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent arg0) {
			long now = System.currentTimeMillis();
			animationCurrentTime = animationCurrentTime + (now - animationLastTimeUpdated) / 1000.0;
			while (animationCurrentTime > animationMaxTime) {
				if (repeat) {
					animationCurrentTime -= animationMaxTime;
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
			if (controls.contains(point)) {
				if (controlsProgressLine.contains(point)) {
					//clicked in progress line area
					double progress = (e.getX() - controlsProgressLine.x) / (controlsProgressLine.width * 1.0);
					seek(progress * animationMaxTime);
				} else if (controlsPlayPause.contains(point)) {
					//clicked on play/pause button
					startStop();
				}
			}
		}
	};

	private MouseMotionListener mouseMovesAdapter = new MouseMotionListener() {

		public void mouseDragged(MouseEvent e) {

		}

		public void mouseMoved(MouseEvent e) {
			boolean newb = controls.contains(e.getPoint());
			if (newb != mouseIsInControls) {
				makeUpdate();
			}
			mouseIsInControls = newb;
		}
	};

	public AnimatableSVGPanel(SVGUniverse universe) {
		this.universe = universe;

		//prepare animation timer
		animationTimer = new Timer(30, timeStepAction);
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

	protected void paintComponent(Graphics g) {
		//update universe time
		universe.setCurTime(animationCurrentTime);
		try {
			universe.updateTime();
		} catch (SVGException e) {
			e.printStackTrace();
		}

		super.paintComponent(g);

		//draw the overlay controls
		drawOverlayControls(g);
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
		double progress = animationCurrentTime / animationMaxTime;
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

	public void start() {
		animationLastTimeUpdated = System.currentTimeMillis();
		animationTimer.start();
	}

	public void stop() {
		animationTimer.stop();
		makeUpdate();
	}

	public void rewind() {
		animationCurrentTime = 0.0;
	}

	public void seek(double newTime) {
		animationCurrentTime = newTime;
		makeUpdate();
	}

	public double getAnimationMaxTime() {
		return animationMaxTime;
	}

	public void setAnimationMaxTime(double animationMaxTime) {
		this.animationMaxTime = animationMaxTime;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
}