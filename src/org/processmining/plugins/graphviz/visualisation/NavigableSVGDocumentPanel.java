package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.processmining.plugins.graphviz.visualisation.ZoomPan.BB;
import org.processmining.plugins.graphviz.visualisation.export.ExportDialog;
import org.w3c.dom.svg.SVGDocument;

/*
 * Obtained and adapted from
 * https://today.java.net/pub/a/today/2007/03/27/navigable
 * -image-panel.html#memory-and-cpu-usage-considerations
 */

public class NavigableSVGDocumentPanel extends JPanel {

	private static final long serialVersionUID = -3285916948952045282L;

	protected final NavigableSVGPanelState state = new NavigableSVGPanelState();
	protected SVGDocument svgDocument = null;
	protected final JPanel panel;
	private Point mousePosition;
	protected boolean preventDragImage = false;
	private MouseMotionListener helperControlsMouseMovesAdapter = new MouseMotionListener() {

		public void mouseMoved(MouseEvent e) {
			if (helperControlsArc != null && e != null && e.getPoint() != null) {
				boolean n = helperControlsArc.contains(e.getPoint());
				if (n != mouseIsInHelperControls) {
					repaint();
				}
				mouseIsInHelperControls = n;
			}
		}

		public void mouseDragged(MouseEvent arg0) {

		}
	};

	private Action zoomInAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent e) {
			//zoom in on the center of the panel
			Point p = new Point(panel.getWidth() / 2, panel.getHeight() / 2);
			ZoomPan.onZoom(true, p, state.getZoomPanState(), svgDocument, panel);
			repaint();
		}
	};

	private Action zoomOutAction = new AbstractAction() {
		private static final long serialVersionUID = 7842478506942554961L;

		public void actionPerformed(ActionEvent e) {
			//zoom in of the center of the panel
			Point p = new Point(panel.getWidth() / 2, panel.getHeight() / 2);
			ZoomPan.onZoom(false, p, state.getZoomPanState(), svgDocument, panel);
			repaint();
		}
	};

	private Action viewResetAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			//reset the view
			state.getZoomPanState().reset();
			repaint();
		}
	};

	private Action walkAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			ZoomPanState panState = state.getZoomPanState();
			if (command.equals("DOWN")) {
				panState.setDeltaOriginY(panState.getDeltaOriginY() - 10);
			} else if (command.equals("UP")) {
				panState.setDeltaOriginY(panState.getDeltaOriginY() + 10);
			} else if (command.equals("LEFT")) {
				panState.setDeltaOriginX(panState.getDeltaOriginX() + 10);
			} else if (command.equals("RIGHT")) {
				panState.setDeltaOriginX(panState.getDeltaOriginX() - 10);
			}
			repaint();
		}
	};

	private Action saveAsAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent e) {
			new ExportDialog(NavigableSVGDocumentPanel.this);
		}
	};

	private static Font helperControlsButtonFont = new Font("TimesRoman", Font.PLAIN, 20);
	private static String helperControlsButtonString = "?";
	private Arc2D helperControlsArc = null;
	private boolean mouseIsInHelperControls = false;
	private static int helperControlsWidth = 300;
	private static Font helperControlsFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	protected List<String> helperControlsShortcuts = new ArrayList<>(Arrays.asList("up/down", "left/right", "ctrl +",
			"ctrl -", "ctrl 0", "ctrl s"));
	protected List<String> helperControlsExplanations = new ArrayList<>(Arrays.asList("pan up/down", "pan left/right",
			"zoom in", "zoom out", "reset view", "save image"));

	public NavigableSVGDocumentPanel(final SVGDocument image) {
		setOpaque(false);
		setDoubleBuffered(true);
		setFocusable(true);
		setImage(image, true);
		panel = this;
		init();
	}

	private void init() {
		//resize listener
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (state.getPreviousPanelSize() != null) {
					ZoomPan.onResize(state.getZoomPanState(), svgDocument, panel, state.getPreviousPanelSize());
				}
				state.setPreviousPanelSize(getSize());

				repaint();
			}
		});

		//mouse click listener
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				Point point = e.getPoint();
				mousePosition = point;
				if (SwingUtilities.isLeftMouseButton(e) && isInNavigation(point)) {
					//clicked in navigation
					displayImageAt(point);
					repaint();
				}
			}
		});

		//drag listener
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				Point point = e.getPoint();
				if (SwingUtilities.isLeftMouseButton(e) && !isInNavigation(point) && !preventDragImage) {
					if (mousePosition != null) {
						ZoomPan.onPan(state.getZoomPanState(), mousePosition, point);
					}
					mousePosition = point;

					repaint();
				}
			}

			public void mouseMoved(MouseEvent e) {

			}
		});

		//scroll listener
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point p = e.getPoint();
				boolean zoomIn = (e.getWheelRotation() < 0);

				if (isInNavigation(p)) {
					//scroll navigation
					if (zoomIn) {
						zoomNavigation(1.2);
					} else {
						zoomNavigation(0.8);
					}
				} else if (isInImage(p)) {
					//scroll image
					ZoomPan.onZoom(zoomIn, p, state.getZoomPanState(), svgDocument, panel);
				}
				repaint();
			}
		});

		//listen to ctrl + to zoom in
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK),
				"zoomIn"); // + key in English keyboards
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK),
				"zoomIn"); // + key in non-English keyboards
		getInputMap(WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_MASK), "zoomIn"); // + key on the numpad
		getActionMap().put("zoomIn", zoomInAction);

		//listen to ctrl - to zoom out
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK),
				"zoomOut"); // - key
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK),
				"zoomOut"); // - key on the numpad
		getActionMap().put("zoomOut", zoomOutAction);

		//listen to arrow keys to walk around
		registerKeyboardAction(walkAction, "DOWN", KeyStroke.getKeyStroke("DOWN"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(walkAction, "UP", KeyStroke.getKeyStroke("UP"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(walkAction, "LEFT", KeyStroke.getKeyStroke("LEFT"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(walkAction, "RIGHT", KeyStroke.getKeyStroke("RIGHT"), JComponent.WHEN_IN_FOCUSED_WINDOW);

		//listen to ctrl 0 to reset view
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK),
				"viewReset"); // - key
		getActionMap().put("viewReset", viewResetAction);

		//listen to ctrl s to save view
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), "saveAs"); // - key
		getActionMap().put("saveAs", saveAsAction);

		//add mouse motion listener for helper controls
		addMouseMotionListener(helperControlsMouseMovesAdapter);
		setMouseExit(this);
	}

	private MouseListener exitListener = new MouseListener() {
		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
			mouseIsInHelperControls = false;
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
		}
	};

	private void setMouseExit(Container c) {
		c.addMouseListener(exitListener);
		if (c.getParent() != null) {
			setMouseExit(c);
		}
	}

	/**
	 * <p>
	 * Sets an image for display in the panel.
	 * </p>
	 * 
	 * @param image
	 *            an image to be set in the panel
	 */
	public void setImage(SVGDocument image, boolean resetView) {
		if (image == null) {
			System.out.println("invalid dot given");
			throw new NullPointerException("invalid dot given");
		}
		this.svgDocument = image;

		state.getZoomPanState().resetTransformation();
		if (resetView) {
			state.getZoomPanState().reset();
		}

		repaint();
	}

	public SVGDocument getSVGDocument() {
		return svgDocument;
	}

	//The user clicked within the navigation image and this part of the image
	//is displayed in the panel.
	//The clicked point of the image is centered in the panel.
	private void displayImageAt(Point navPoint) {
		//transform to image coordinates
		Point2D pImage = transformNavigationToImage(navPoint);

		//transform to panel coordinates
		Transformation t = ZoomPan.getImage2PanelTransformation(svgDocument, panel);
		Point2D p = t.transformToPanel(pImage, state.getZoomPanState());

		Point2D center = new Point(getWidth() / 2, getHeight() / 2);

		state.getZoomPanState().setDeltaOriginX(state.getZoomPanState().getDeltaOriginX() + (center.getX() - p.getX()));
		state.getZoomPanState().setDeltaOriginY(state.getZoomPanState().getDeltaOriginY() + (center.getY() - p.getY()));
	}

	/**
	 * <p>
	 * Enables/disables navigation with the navigation image.
	 * </p>
	 * <p>
	 * Navigation image should be disabled when custom, programmatic navigation
	 * is implemented.
	 * </p>
	 * 
	 * @param enabled
	 *            true when navigation image is enabled, false otherwise.
	 */
	public void setNavigationImageEnabled(boolean enabled) {
		state.setNavigationImageEnabled(enabled);
		repaint();
	}

	/**
	 * Zoom the navigation
	 * 
	 * @param zoomFactor
	 */
	private void zoomNavigation(double zoomFactor) {
		state.setNavigationScale(state.getNavigationScale() * zoomFactor);
	}

	/**
	 * Paints the panel and its image at the current zoom level, location, and
	 * interpolation method dependent on the image scale.</p>
	 * 
	 * @param g
	 *            the <code>Graphics</code> context for painting
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g); // Paints the background

		Graphics2D g2 = (Graphics2D) g;

		if (svgDocument == null) {
			return;
		}

		//set anti-aliasing if desired
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, state.isAntiAlias() ? RenderingHints.VALUE_ANTIALIAS_ON
				: RenderingHints.VALUE_ANTIALIAS_OFF);

		//set clipping mask to save a few cpu/gpu cycles
		g2.setClip(0, 0, getWidth(), getHeight());

		//draw image
		drawImage(g2, svgDocument);

		//draw helper controls
		drawHelperControls(g2);
	}

	private void drawImage(Graphics2D g, SVGDocument document) {
		UserAgent userAgent = new UserAgentAdapter();
		DocumentLoader loader = new DocumentLoader(userAgent);
		BridgeContext ctx = new BridgeContext(userAgent, loader);
		ctx.setDynamicState(BridgeContext.DYNAMIC);
		GVTBuilder builder = new GVTBuilder();
		GraphicsNode rootGN = builder.build(ctx, document);

		Transformation t = state.getZoomPanState().getTransformation(document, panel);
		t.transform(g, state.getZoomPanState());

		rootGN.paint(g);

		t.inverseTransform(g, state.getZoomPanState());

		//Draw navigation image
		if (state.isNavigationImageEnabled()
				&& !ZoomPan.isImageCompletelyInPanel(state.getZoomPanState(), document, panel)) {
			int width = (int) Math.round(getNavigationWidth());
			int height = (int) Math.round(getNavigationHeight());
			drawSVG(g, document, rootGN, 0, 0, width, height);
			g.drawRect(0, 0, width, height);
			drawNavigationOutline(g, t);
		}
	}

	public static void drawSVG(Graphics2D g, SVGDocument document, GraphicsNode rootGN, int x, int y, int width,
			int height) {
		BB b = ZoomPan.getBoundingBox(document);
		double scaleX = width / b.getWidth();
		double scaleY = height / b.getHeight();

		g.translate(x, y);
		g.scale(scaleX, scaleY);

		rootGN.paint(g);

		g.scale(1 / scaleX, 1 / scaleY);
		g.translate(-x, -y);
	}

	/**
	 * Paints an outline over the navigation to denote what part of the main
	 * image is displayed.
	 * 
	 * @param g
	 * @param t
	 */
	private void drawNavigationOutline(Graphics2D g, Transformation t) {

		//get edges of panel in image coordinates
		Point2D nwImage = t.transformToImage(new Point(0, 0), state.getZoomPanState());
		Point2D seImage = t.transformToImage(new Point(panel.getWidth(), panel.getHeight()), state.getZoomPanState());

		//transform to navigation coordinates
		Point2D nwNav = transformImageToNavigation(nwImage);
		Point2D seNav = transformImageToNavigation(seImage);

		int x = (int) Math.round(nwNav.getX());
		int y = (int) Math.round(nwNav.getY());
		int width = (int) Math.round(seNav.getX() - nwNav.getX());
		int height = (int) Math.round(seNav.getY() - nwNav.getY());

		if (x + width > getNavigationWidth()) {
			width = (int) Math.round(getNavigationWidth() - x);
		}
		if (y + height > getNavigationHeight()) {
			height = (int) Math.round(getNavigationHeight() - y);
		}
		Stroke backupStroke = g.getStroke();
		g.setColor(state.getNavigationImageBorderColor());
		g.setStroke(state.getNavigationOutlineStroke());
		g.drawRect(x, y, width, height);
		g.setStroke(backupStroke);

	}

	private void drawHelperControls(Graphics g) {
		Color backupColour = g.getColor();
		Font backupFont = g.getFont();

		FontMetrics fm = getFontMetrics(helperControlsButtonFont);
		int width = fm.stringWidth(helperControlsButtonString);

		//draw the background arc
		if (mouseIsInHelperControls) {
			g.setColor(new Color(0, 0, 0, 180));
		} else {
			g.setColor(new Color(0, 0, 0, 20));
		}
		helperControlsArc = new Arc2D.Float(Arc2D.PIE);
		helperControlsArc.setFrame(getWidth() - 25, getHeight() - 25, 50, 50);
		helperControlsArc.setAngleStart(90);
		helperControlsArc.setAngleExtent(90);
		g.fillArc(getWidth() - 25, getHeight() - 25, 50, 50, 90, 90);

		//draw the helper panel
		if (mouseIsInHelperControls) {
			int x = getWidth() - (25 + helperControlsWidth);
			int y = getHeight() - (helperControlsShortcuts.size() * 20 - 10);

			//background
			g.setColor(new Color(0, 0, 0, 180));
			g.fillRoundRect(x - 15, y - 20, helperControlsWidth, helperControlsShortcuts.size() * 20 + 20, 10, 10);

			//text
			g.setColor(new Color(255, 255, 255, 220));
			g.setFont(helperControlsFont);
			for (int i = 0; i < helperControlsShortcuts.size(); i++) {
				g.drawString(
						String.format("%-12s", helperControlsShortcuts.get(i)) + " "
								+ helperControlsExplanations.get(i), x, y);
				y += 20;
			}
		}

		//draw the question mark
		if (mouseIsInHelperControls) {
			g.setColor(new Color(255, 255, 255, 128));
		} else {
			g.setColor(new Color(0, 0, 0, 128));
		}
		g.setFont(helperControlsButtonFont);
		g.drawString(helperControlsButtonString, getWidth() - width - 3, getHeight() - 3);

		//revert colour and font
		g.setColor(backupColour);
		g.setFont(backupFont);
	}

	private double getNavigationWidth() {
		return getWidth() * state.getNavigationImageWidthInPartOfPanel() * state.getNavigationScale();
	}

	private double getNavigationHeight() {
		return (getNavigationWidth() / getImageWidth()) * getImageHeight();
	}

	/**
	 * Transforms the given point in navigation coordinates to image coordinates
	 * 
	 * @param p
	 * @return
	 */
	public Point2D transformNavigationToImage(Point2D p) {
		double x = p.getX() * getImageWidth() / getNavigationWidth();
		double y = p.getY() * getImageHeight() / getNavigationHeight();

		return new Point2D.Double(x, y);
	}

	/**
	 * Transforms the given point in image coordinates to navigation coordinates
	 * 
	 * @param p
	 * @return
	 */
	public Point2D transformImageToNavigation(Point2D p) {
		double x = p.getX() * getNavigationWidth() / getImageWidth();
		double y = p.getY() * getNavigationHeight() / getImageHeight();

		return new Point2D.Double(x, y);
	}

	/**
	 * Returns whether a point (in panel coordinates) is in the image and not in
	 * the navigation image.
	 * 
	 * @param p
	 * @return
	 */
	public boolean isInImage(Point p) {
		if (isInNavigation(p)) {
			return false;
		}
		return ZoomPan.isInImage(p, state.getZoomPanState(), svgDocument, panel);
	}

	/**
	 * Returns whether a point (in panel coordinates) is in the navigation
	 * image.
	 * 
	 * @param p
	 * @return
	 */
	public boolean isInNavigation(Point p) {
		return (state.isNavigationImageEnabled() && p.x < getNavigationWidth() && p.y < getNavigationHeight());
	}

	public List<String> getHelperControlsShortcuts() {
		return helperControlsShortcuts;
	}

	public void setHelperControlsShortcuts(List<String> helperControlsShortcuts) {
		this.helperControlsShortcuts = helperControlsShortcuts;
	}

	public List<String> getHelperControlsExplanations() {
		return helperControlsExplanations;
	}

	public void setHelperControlsExplanations(List<String> helperControlsExplanations) {
		this.helperControlsExplanations = helperControlsExplanations;
	}

	public float getImageWidth() {
		return svgDocument.getRootElement().getWidth().getBaseVal().getValue();
	}

	public float getImageHeight() {
		return svgDocument.getRootElement().getHeight().getBaseVal().getValue();
	}
}