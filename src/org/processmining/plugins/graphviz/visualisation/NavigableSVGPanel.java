package org.processmining.plugins.graphviz.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.NoninvertibleTransformException;
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

import org.processmining.plugins.graphviz.visualisation.export.ExportDialog;
import org.processmining.plugins.graphviz.visualisation.listeners.ImageTransformationChangedListener;
import org.processmining.plugins.graphviz.visualisation.listeners.ZoomPanChangedListener;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

public class NavigableSVGPanel extends JPanel {

	private static final long serialVersionUID = -3285916948952045282L;

	protected final JPanel panel;

	//state variables and constants
	protected SVGDiagram image;
	protected AffineTransform image2user = new AffineTransform();
	private AffineTransform user2image = new AffineTransform();
	private Point lastMousePosition;
	private Dimension lastPanelDimension = null;
	protected boolean preventDragImage = false;
	private final static double zoomIncrement = 1.8;

	private Point2D highLightUserCoordinates;
	private Point2D highLightImageCoordinates;

	//navigation variables and constants
	private double navigationScale = 1.0;
	public final static double navigationImageWidthInPartOfPanel = 0.1;
	public final static Color navigationImageBorderColor = Color.black;
	public final static float dash1[] = { 10.0f };
	public final static BasicStroke navigationImageOutlineStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

	//listeners
	private ImageTransformationChangedListener imageTransformationChangedListener = null;

	//helper controls variables and constants
	private Arc2D helperControlsArc = null;
	private boolean mouseIsInHelperControls = false;
	public static final int helperControlsWidth = 300;
	public static final Font helperControlsFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private static final Font helperControlsButtonFont = new Font("TimesRoman", Font.PLAIN, 20);
	private static final String helperControlsButtonString = "?";

	protected List<String> helperControlsShortcuts = new ArrayList<>(Arrays.asList("up/down", "left/right", "ctrl +",
			"ctrl -", "ctrl 0", "ctrl s"));
	protected List<String> helperControlsExplanations = new ArrayList<>(Arrays.asList("pan up/down", "pan left/right",
			"zoom in", "zoom out", "reset view", "save image"));

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
			try {
				zoomIn(new Point(panel.getWidth() / 2, panel.getHeight() / 2));
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	private Action zoomOutAction = new AbstractAction() {
		private static final long serialVersionUID = 7842478506942554961L;

		public void actionPerformed(ActionEvent e) {
			//zoom in of the center of the panel
			try {
				zoomOut(new Point(panel.getWidth() / 2, panel.getHeight() / 2));
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	private Action viewResetAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			//reset the view
			try {
				resetView();
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	private Action walkAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("DOWN")) {
				image2user.translate(0, -10);
			} else if (command.equals("UP")) {
				image2user.translate(0, 10);
			} else if (command.equals("LEFT")) {
				image2user.translate(10, 0);
			} else if (command.equals("RIGHT")) {
				image2user.translate(-10, 0);
			}
			try {
				user2image = image2user.createInverse();
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	public NavigableSVGPanel(final SVGDiagram newImage) {
		panel = this;
		setOpaque(false);
		setDoubleBuffered(true);
		setFocusable(true);
		setImage(newImage, false);
		setupListeners();
	}

	public void setupListeners() {
		//set up resize listener
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				try {
					if (image2user.isIdentity()) {
						resetView();
						lastPanelDimension = new Dimension(getWidth(), getHeight());
					} else {
						//on resizing, keep the center in center, and scale proportionally to the width.
						double zoom = lastPanelDimension.getWidth() / getWidth();
						user2image.translate(lastPanelDimension.getWidth() / 2.0, lastPanelDimension.getHeight() / 2.0);
						user2image.scale(zoom, zoom);
						lastPanelDimension = new Dimension(getWidth(), getHeight());
						user2image.translate(-lastPanelDimension.getWidth() / 2.0, -lastPanelDimension.getHeight() / 2.0);
						image2user = user2image.createInverse();
					}
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
				}
				updateTransformation();
				repaint();
			}
		});

		//set up mouse click listener
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				Point point = e.getPoint();
				lastMousePosition = point;
				if (SwingUtilities.isLeftMouseButton(e) && isInNavigation(point)) {
					//clicked in navigation
					try {
						centerImageAround(point);
					} catch (NoninvertibleTransformException e1) {
						e1.printStackTrace();
					}
					updateTransformation();
					repaint();
				}
			}
		});

		//set up drag listener
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				Point point = e.getPoint();
				if (SwingUtilities.isLeftMouseButton(e) && !isInNavigation(point) && !preventDragImage) {
					if (lastMousePosition != null) {
						double dx = (point.x - lastMousePosition.x) / image2user.getScaleX();
						double dy = (point.y - lastMousePosition.y) / image2user.getScaleY();

						//pan with the difference in user coordinates between the last known mouse position and this one
						image2user.translate(dx, dy);
						try {
							user2image = image2user.createInverse();
						} catch (NoninvertibleTransformException e1) {
							e1.printStackTrace();
						}
					}
					lastMousePosition = point;
					updateTransformation();
					repaint();
				}
			}

			public void mouseMoved(MouseEvent e) {

			}
		});

		//set up scroll listener
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point p = e.getPoint();
				boolean zoomIn = (e.getWheelRotation() < 0);

				if (isInNavigation(p)) {
					//zoom navigation
					if (zoomIn) {
						zoomNavigation(1.2);
					} else {
						zoomNavigation(0.8);
					}
				} else if (isInImage(p)) {
					//zoom image
					try {
						if (zoomIn) {
							zoomIn(p);
						} else {
							zoomOut(p);
						}
						updateTransformation();
					} catch (NoninvertibleTransformException ex) {
						ex.printStackTrace();
					}
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

		//listen to ctrl s to save image
		{
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK),
					"saveAs"); // - key
			final NavigableSVGPanel panel = this;
			getActionMap().put("saveAs", new AbstractAction() {
				private static final long serialVersionUID = -4780600363000017631L;

				public void actionPerformed(ActionEvent arg0) {
					new ExportDialog(panel);
				}
			});
		}

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

	protected void setMouseExit(Container c) {
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
	public void setImage(SVGDiagram image, boolean resetView) {
		if (image == null) {
			System.out.println("invalid dot given");
			throw new NullPointerException("invalid dot given");
		}
		this.image = image;
		image.setDeviceViewport(new java.awt.Rectangle(0, 0, (int) image.getWidth(), (int) image.getHeight()));

		if (resetView) {
			try {
				resetView();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			updateTransformation();
		}

		repaint();
	}

	/**
	 * Scale and center the image just in the viewport.
	 * 
	 * @throws NoninvertibleTransformException
	 */
	public void resetView() throws NoninvertibleTransformException {
		double scaleX = getWidth() / (double) image.getWidth();
		double scaleY = getHeight() / (double) image.getHeight();
		double scale = Math.min(scaleX, scaleY);

		double width = scale * image.getWidth();
		double height = scale * image.getHeight();

		double x = (getWidth() - width) / 2.0;
		double y = (getHeight() - height) / 2.0;

		image2user.setToIdentity();
		image2user.translate(x, y);
		image2user.scale(scale, scale);
		user2image = image2user.createInverse();
	}

	/**
	 * The user clicked within the navigation image and this part of the image
	 * is displayed in the panel. The clicked point of the image is centered in
	 * the panel.
	 * 
	 * @param pointInNavigationCoordinates
	 * @throws NoninvertibleTransformException
	 */
	public void centerImageAround(Point pointInNavigationCoordinates) throws NoninvertibleTransformException {
		//transform to image coordinates
		Point2D pImage = transformNavigationToImage(pointInNavigationCoordinates);

		//transform to user coordinates
		image2user.transform(pImage, pImage);

		//compute difference
		double dx = (getWidth() / 2.0 - pImage.getX()) / image2user.getScaleX();
		double dy = (getHeight() / 2.0 - pImage.getY()) / image2user.getScaleY();

		//and translate
		image2user.translate(dx, dy);
		user2image = image2user.createInverse();
	}

	/**
	 * Zoom the navigation
	 * 
	 * @param zoomFactor
	 */
	private void zoomNavigation(double zoomFactor) {
		navigationScale *= zoomFactor;
	}

	/**
	 * Paints the panel and its image at the current zoom level, location, and
	 * interpolation method dependent on the image scale.</p>
	 * 
	 * @param g
	 *            the <code>Graphics</code> context for painting
	 */
	protected void paintComponent(Graphics g) {
		if (isPaintingForPrint()) {
			super.paintComponent(g); // Paints the background
		}

		Graphics2D g2 = (Graphics2D) g;

		if (image == null) {
			return;
		}

		//set anti-aliasing if desired
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//set clipping mask to save a few cpu/gpu cycles
		if (!isPaintingForPrint()) {
			g2.setClip(0, 0, getWidth(), getHeight());
		}

		//draw image
		if (!isPaintingForPrint()) {
			g2.transform(image2user);
		}
		paintImage(g2);
		if (!isPaintingForPrint()) {
			g2.transform(user2image);
		}

		g2.setColor(Color.red);
		if (highLightUserCoordinates != null) {
			g2.fillRect((int) highLightUserCoordinates.getX(), (int) highLightUserCoordinates.getY(), 100, 100);
		}

		//Draw navigation image
		if (!isPaintingForPrint() && !isImageCompletelyInPanel()) {
			int width = (int) Math.round(getNavigationWidth());
			int height = (int) Math.round(getNavigationHeight());
			drawSVG(g2, image, 0, 0, width, height);
			g2.drawRect(0, 0, width, height);
			drawNavigationOutline(g2);
		}

		//draw helper controls
		if (!isPaintingForPrint()) {
			drawHelperControls(g2);
		}
	}

	protected void paintImage(Graphics2D g) {
		try {
			image.render(g);
		} catch (SVGException e) {
			e.printStackTrace();
		}

		g.setColor(Color.green);
		if (highLightImageCoordinates != null) {
			g.fillRect((int) highLightImageCoordinates.getX(), (int) highLightImageCoordinates.getY(), 100, 100);
		}
	}

	/**
	 * Zoom in keeping the given point at its place
	 * 
	 * @param aroundInUserCoordinates
	 * @throws NoninvertibleTransformException
	 */
	private void zoomIn(Point2D aroundInUserCoordinates) throws NoninvertibleTransformException {
		user2image.translate(aroundInUserCoordinates.getX(), aroundInUserCoordinates.getY());
		user2image.scale(1 / zoomIncrement, 1 / zoomIncrement);
		user2image.translate(-aroundInUserCoordinates.getX(), -aroundInUserCoordinates.getY());
		image2user = user2image.createInverse();
	}

	/**
	 * Zoom out keeping the given point at its place
	 * 
	 * @param aroundInUserCoordinates
	 * @throws NoninvertibleTransformException
	 */
	private void zoomOut(Point2D aroundInUserCoordinates) throws NoninvertibleTransformException {
		user2image.translate(aroundInUserCoordinates.getX(), aroundInUserCoordinates.getY());
		user2image.scale(zoomIncrement, zoomIncrement);
		user2image.translate(-aroundInUserCoordinates.getX(), -aroundInUserCoordinates.getY());
		image2user = user2image.createInverse();
	}

	/**
	 * Draw an svg image at the given coordinates and of the given size.
	 * 
	 * @param g
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void drawSVG(Graphics2D g, SVGDiagram image, int x, int y, int width, int height) {

		double scaleX = width / image.getWidth();
		double scaleY = height / image.getHeight();

		g.translate(x, y);
		g.scale(scaleX, scaleY);

		try {
			image.render(g);
		} catch (SVGException e) {
			e.printStackTrace();
		}

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
	private void drawNavigationOutline(Graphics2D g) {

		//get edges of panel in image coordinates

		Point2D.Double nw = new Point.Double(0, 0);
		image2user.transform(nw, nw);
		Point2D.Double se = new Point.Double(panel.getWidth(), panel.getHeight());
		image2user.transform(se, se);

		//transform to navigation coordinates
		Point2D nwNav = transformImageToNavigation(nw);
		Point2D seNav = transformImageToNavigation(se);

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
		g.setColor(navigationImageBorderColor);
		g.setStroke(navigationImageOutlineStroke);
		g.drawRect(x, y, width, height);
		g.setStroke(backupStroke);

	}

	/**
	 * Draws the little help-question-mark in the right bottom corner and the
	 * help text that appears when hovering over it.
	 * 
	 * @param g
	 */
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

	/**
	 * 
	 * @return The width of the navigation part in user coordinates.
	 */
	private double getNavigationWidth() {
		return getWidth() * navigationImageWidthInPartOfPanel * navigationScale;
	}

	/**
	 * 
	 * @return The height of the navigation part in user coordinates.
	 */
	private double getNavigationHeight() {
		return (getNavigationWidth() / image.getWidth()) * image.getHeight();
	}

	/**
	 * Transforms the given point in navigation coordinates to image coordinates
	 * 
	 * @param p
	 * @return
	 */
	public Point2D transformNavigationToImage(Point2D p) {
		double x = p.getX() * image.getWidth() / getNavigationWidth();
		double y = p.getY() * image.getHeight() / getNavigationHeight();

		return new Point2D.Double(x, y);
	}

	/**
	 * Transforms the given point in image coordinates to navigation coordinates
	 * 
	 * @param p
	 * @return
	 */
	public Point2D transformImageToNavigation(Point2D p) {
		double x = p.getX() * getNavigationWidth() / image.getWidth();
		double y = p.getY() * getNavigationHeight() / image.getHeight();

		return new Point2D.Double(x, y);
	}

	/**
	 * Returns whether a point (in user coordinates) is in the image and not in
	 * the navigation image.
	 * 
	 * @param pointInUserCoordinates
	 * @return
	 */
	public boolean isInImage(Point pointInUserCoordinates) {
		if (isInNavigation(pointInUserCoordinates)) {
			return false;
		}
		return getImageBoundingBoxInUserCoordinates().contains(pointInUserCoordinates);
	}

	/**
	 * Returns whether a point (in user coordinates) is in the navigation image.
	 * 
	 * @param pointInUserCoordinates
	 * @return
	 */
	public boolean isInNavigation(Point pointInUserCoordinates) {
		return (pointInUserCoordinates.x < getNavigationWidth() && pointInUserCoordinates.y < getNavigationHeight());
	}

	/**
	 * 
	 * @return The currently registered shortcuts, which are displayed when
	 *         hovering over the question mark in the lower right corner.
	 */
	public List<String> getHelperControlsShortcuts() {
		return helperControlsShortcuts;
	}

	/**
	 * Sets the shortcuts, displayed when hovering over the question mark in the
	 * lower right corner.
	 * 
	 * @param helperControlsShortcuts
	 */
	public void setHelperControlsShortcuts(List<String> helperControlsShortcuts) {
		this.helperControlsShortcuts = helperControlsShortcuts;
	}

	/**
	 * @return The currently registered shortcut explanations, which are
	 *         displayed when hovering over the question mark in the lower right
	 *         corner.
	 */
	public List<String> getHelperControlsExplanations() {
		return helperControlsExplanations;
	}

	/**
	 * Sets the shortcut explanations, displayed when hovering over the question
	 * mark in the lower right corner.
	 * 
	 * @param helperControlsExplanations
	 */
	public void setHelperControlsExplanations(List<String> helperControlsExplanations) {
		this.helperControlsExplanations = helperControlsExplanations;
	}

	/**
	 * 
	 * @return The currently displaying svg image.
	 */
	public SVGDiagram getImage() {
		return image;
	}

	/**
	 * 
	 * @return The bounding box of the image in user coordinates, truncated
	 *         (approximately) to the visible area.
	 */
	public Rectangle getVisibleImageBoundingBoxInUserCoordinates() {
		//transform point (0,0) to user coordinates
		Point2D.Double nw = new Point.Double(0, 0);
		user2image.transform(nw, nw);
		double x1 = Math.min(Math.max(0, nw.getX()), getWidth());
		double y1 = Math.min(Math.max(0, nw.getY()), getHeight());

		//transform the other corner to user coordinates
		Point2D se = new Point2D.Double(image.getWidth(), image.getHeight());
		user2image.transform(se, se);
		double x2 = Math.min((Math.max(0, se.getX())), getWidth());
		double y2 = Math.min((Math.max(0, se.getY())), getHeight());

		return new Rectangle((int) Math.min(x1, x2), (int) Math.min(y1, y2), (int) Math.abs(x2 - x1), (int) Math.abs(y2
				- y1));
	}

	/**
	 * 
	 * @return the bounding box of the complete image in user coordinates
	 */
	public Rectangle getImageBoundingBoxInUserCoordinates() {
		//transform point (0,0) to user coordinates
		Point2D.Double nw = new Point.Double(0, 0);
		image2user.transform(nw, nw);
		double x1 = nw.getX();
		double y1 = nw.getY();

		//transform the other corner to user coordinates
		Point2D se = new Point2D.Double(image.getWidth(), image.getHeight());
		image2user.transform(se, se);
		double x2 = se.getX();
		double y2 = se.getY();

		return new Rectangle((int) Math.min(x1, x2), (int) Math.min(y1, y2), (int) Math.abs(x2 - x1), (int) Math.abs(y2
				- y1));
	}

	/**
	 * Returns whether the image is completely visible in the panel
	 * 
	 * @param panState
	 * @param diagram
	 * @param panel
	 * @return
	 */
	public boolean isImageCompletelyInPanel() {
		//check the northwest corner
		Point2D.Double nw = new Point.Double(0, 0);
		image2user.transform(nw, nw);
		if (nw.getX() < 0 || nw.getY() < 0) {
			return false;
		}

		//check the southeast corner
		Point2D.Double se = new Point2D.Double(image.getWidth(), image.getHeight());
		image2user.transform(se, se);
		if (se.getX() > panel.getWidth() || se.getY() > panel.getHeight()) {
			return false;
		}

		return true;
	}

	@Deprecated
	public void setZoomPanChangedListener(ZoomPanChangedListener listener) {

	}

	public void setImageTransformationChangedListener(ImageTransformationChangedListener listener) {
		this.imageTransformationChangedListener = listener;
	}

	private void updateTransformation() {
		if (imageTransformationChangedListener != null) {
			imageTransformationChangedListener.imageTransformationChanged(image2user, user2image);
		}
	}
	
	public Point2D transformUser2Image(Point2D p) {
		return user2image.transform(p, null);
	}
	
	public Point2D transformImage2User(Point2D p) {
		return image2user.transform(p, null);
	}

}
