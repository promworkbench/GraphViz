package org.processmining.plugins.graphviz.visualisation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanelState.Coords;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

/*
 * Obtained and adapted from
 * https://today.java.net/pub/a/today/2007/03/27/navigable
 * -image-panel.html#memory-and-cpu-usage-considerations
 */

/**
 * <p>
 * <code>NavigableImagePanel</code> is a lightweight container displaying an
 * image that can be zoomed in and out and panned with ease and simplicity,
 * using an adaptive rendering for high quality display and satisfactory
 * performance.
 * </p>
 * <p>
 * <h3>Image</h3>
 * <p>
 * An image is loaded either via a constructor:
 * </p>
 * 
 * <pre>
 * NavigableImagePanel panel = new NavigableImagePanel(image);
 * </pre>
 * 
 * or using a setter:
 * 
 * <pre>
 * NavigableImagePanel panel = new NavigableImagePanel();
 * panel.setImage(image);
 * </pre>
 * 
 * When an image is set, it is initially painted centered in the component, at
 * the largest possible size, fully visible, with its aspect ratio is preserved.
 * This is defined as 100% of the image size and its corresponding zoom level is
 * 1.0. </p> <h3>Zooming</h3>
 * <p>
 * Zooming can be controlled interactively, using either the mouse scroll wheel
 * (default) or the mouse two buttons, or programmatically, allowing the
 * programmer to implement other custom zooming methods. If the mouse does not
 * have a scroll wheel, set the zooming device to mouse buttons:
 * 
 * <pre>
 * panel.setZoomDevice(ZoomDevice.MOUSE_BUTTON);
 * </pre>
 * 
 * The left mouse button works as a toggle switch between zooming in and zooming
 * out modes, and the right button zooms an image by one increment (default is
 * 20%). You can change the zoom increment value by:
 * 
 * <pre>
 * panel.setZoomIncrement(newZoomIncrement);
 * </pre>
 * 
 * If you intend to provide programmatic zoom control, set the zoom device to
 * none to disable both the mouse wheel and buttons for zooming purposes:
 * 
 * <pre>
 * panel.setZoomDevice(ZoomDevice.NONE);
 * </pre>
 * 
 * and use <code>setZoom()</code> to change the zoom level.
 * </p>
 * <p>
 * Zooming is always around the point the mouse pointer is currently at, so that
 * this point (called a zooming center) remains stationary ensuring that the
 * area of an image we are zooming into does not disappear off the screen. The
 * zooming center stays at the same location on the screen and all other points
 * move radially away from it (when zooming in), or towards it (when zooming
 * out). For programmatically controlled zooming the zooming center is either
 * specified when <code>setZoom()</code> is called:
 * 
 * <pre>
 * panel.setZoom(newZoomLevel, newZoomingCenter);
 * </pre>
 * 
 * or assumed to be the point of an image which is the closest to the center of
 * the panel, if no zooming center is specified:
 * 
 * <pre>
 * panel.setZoom(newZoomLevel);
 * </pre>
 * 
 * </p>
 * <p>
 * There are no lower or upper zoom level limits.
 * </p>
 * <h3>Navigation</h3>
 * <p>
 * <code>NavigableImagePanel</code> does not use scroll bars for navigation, but
 * relies on a navigation image located in the upper left corner of the panel.
 * The navigation image is a small replica of the image displayed in the panel.
 * When you click on any point of the navigation image that part of the image is
 * displayed in the panel, centered. The navigation image can also be zoomed in
 * the same way as the main image.
 * </p>
 * <p>
 * In order to adjust the position of an image in the panel, it can be dragged
 * with the mouse, using the left button.
 * </p>
 * <p>
 * For programmatic image navigation, disable the navigation image:
 * 
 * <pre>
 * panel.setNavigationImageEnabled(false)
 * </pre>
 * 
 * and use <code>getImageOrigin()</code> and <code>setImageOrigin()</code> to
 * move the image around the panel.
 * </p>
 * <h3>Rendering</h3>
 * <p>
 * <code>NavigableImagePanel</code> uses the Nearest Neighbor interpolation for
 * image rendering (default in Java). When the scaled image becomes larger than
 * the original image, the Bilinear interpolation is applied, but only to the
 * part of the image which is displayed in the panel. This interpolation change
 * threshold can be controlled by adjusting the value of
 * <code>HIGH_QUALITY_RENDERING_SCALE_THRESHOLD</code>.
 * </p>
 */
public class NavigableSVGPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3285916948952045282L;

	/**
	 * <p>
	 * Identifies a change to the zoom level.
	 * </p>
	 */
	public static final String ZOOM_LEVEL_CHANGED_PROPERTY = "zoomLevel";

	/**
	 * <p>
	 * Identifies a change to the zoom increment.
	 * </p>
	 */
	public static final String ZOOM_INCREMENT_CHANGED_PROPERTY = "zoomIncrement";

	/**
	 * <p>
	 * Identifies that the image in the panel has changed.
	 * </p>
	 */
	public static final String IMAGE_CHANGED_PROPERTY = "image";

	private NavigableSVGPanelState state = new NavigableSVGPanelState();
	protected SVGDiagram image;

	private Point mousePosition;

	private WheelZoomDevice wheelZoomDevice = null;
	private ButtonZoomDevice buttonZoomDevice = null;

	/**
	 * <p>
	 * Defines zoom devices.
	 * </p>
	 */
	public static class ZoomDevice {
		/**
		 * <p>
		 * Identifies that the panel does not implement zooming, but the
		 * component using the panel does (programmatic zooming method).
		 * </p>
		 */
		public static final ZoomDevice NONE = new ZoomDevice("none");

		/**
		 * <p>
		 * Identifies the left and right mouse buttons as the zooming device.
		 * </p>
		 */
		public static final ZoomDevice MOUSE_BUTTON = new ZoomDevice("mouseButton");

		/**
		 * <p>
		 * Identifies the mouse scroll wheel as the zooming device.
		 * </p>
		 */
		public static final ZoomDevice MOUSE_WHEEL = new ZoomDevice("mouseWheel");

		private String zoomDevice;

		private ZoomDevice(String zoomDevice) {
			this.zoomDevice = zoomDevice;
		}

		public String toString() {
			return zoomDevice;
		}
	}

	private class WheelZoomDevice implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			Point p = e.getPoint();
			boolean zoomIn = (e.getWheelRotation() < 0);
			if (isInNavigationImage(p)) {
				if (zoomIn) {
					zoomNavigationImage(1.0 + state.getZoomIncrement());
				} else {
					zoomNavigationImage(1.0 - state.getZoomIncrement());
				}
			} else if (isInImage(p)) {
				if (zoomIn) {
					zoomImage(1.0 + state.getZoomIncrement(), p);
				} else {
					zoomImage(1.0 - state.getZoomIncrement(), p);
				}
			}
		}
	}

	private class ButtonZoomDevice extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			Point p = e.getPoint();
			if (SwingUtilities.isRightMouseButton(e)) {
				if (isInNavigationImage(p)) {
					zoomNavigationImage(1.0 - state.getZoomIncrement());
				} else if (isInImage(p)) {
					zoomImage(1.0 - state.getZoomIncrement(), p);
				}
			} else {
				if (isInNavigationImage(p)) {
					zoomNavigationImage(1.0 + state.getZoomIncrement());
				} else if (isInImage(p)) {
					zoomImage(1.0 + state.getZoomIncrement(), p);
				}
			}
		}
	}

	private Action zoomInAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent e) {
			setZoom(1.0 + state.getZoomIncrement());
		}
	};

	private Action zoomOutAction = new AbstractAction() {
		private static final long serialVersionUID = 7842478506942554961L;

		public void actionPerformed(ActionEvent e) {
			setZoom(1.0 - state.getZoomIncrement());
		}
	};

	private Action walkAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("DOWN")) {
				setState(state.getOriginX(), state.getOriginY() - 10, state.getScale(), state.getNavScale(),
						state.getInitialScale());
			} else if (command.equals("UP")) {
				setState(state.getOriginX(), state.getOriginY() + 10, state.getScale(), state.getNavScale(),
						state.getInitialScale());
			} else if (command.equals("LEFT")) {
				setState(state.getOriginX() + 10, state.getOriginY(), state.getScale(), state.getNavScale(),
						state.getInitialScale());
			} else if (command.equals("RIGHT")) {
				setState(state.getOriginX() - 10, state.getOriginY(), state.getScale(), state.getNavScale(),
						state.getInitialScale());
			}
			repaint();
		}
	};

	/**
	 * <p>
	 * Creates a new navigable image panel with the given image and the mouse
	 * scroll wheel as the zooming device.
	 * </p>
	 */
	public NavigableSVGPanel(SVGDiagram image) {
		setOpaque(false);
		setDoubleBuffered(true);
		setFocusable(true);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (isFullImageInPanel()) {
					centerImage();
				} else if (isImageEdgeInPanel()) {
					scaleOrigin();
				}
				repaint();
				state.setPreviousPanelSize(getSize());
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				Point pointPanelCoordinates = e.getPoint();
				if (SwingUtilities.isLeftMouseButton(e) && isInNavigationImage(pointPanelCoordinates)) {
					displayImageAt(pointPanelCoordinates);
				}
			}
		});

		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				Point point = e.getPoint();
				if (SwingUtilities.isLeftMouseButton(e) && !isInNavigationImage(point)) {
					moveImage(point);
				}
			}

			public void mouseMoved(MouseEvent e) {
				//we need the mouse position so that after zooming
				//that position of the image is maintained
				mousePosition = e.getPoint();
			}
		});

		setZoomDevice(ZoomDevice.MOUSE_WHEEL);

		//listen to ctrl + to zoom in
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK),
				"zoomIn"); // + key in English keyboardsc
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

		setImage(image, true);
	}

	private void addWheelZoomDevice() {
		if (wheelZoomDevice == null) {
			wheelZoomDevice = new WheelZoomDevice();
			addMouseWheelListener(wheelZoomDevice);
		}
	}

	private void addButtonZoomDevice() {
		if (buttonZoomDevice == null) {
			buttonZoomDevice = new ButtonZoomDevice();
			addMouseListener(buttonZoomDevice);
		}
	}

	private void removeWheelZoomDevice() {
		if (wheelZoomDevice != null) {
			removeMouseWheelListener(wheelZoomDevice);
			wheelZoomDevice = null;
		}
	}

	private void removeButtonZoomDevice() {
		if (buttonZoomDevice != null) {
			removeMouseListener(buttonZoomDevice);
			buttonZoomDevice = null;
		}
	}

	/**
	 * <p>
	 * Sets a new zoom device.
	 * </p>
	 * 
	 * @param newZoomDevice
	 *            specifies the type of a new zoom device.
	 */
	public void setZoomDevice(ZoomDevice newZoomDevice) {
		if (newZoomDevice == ZoomDevice.NONE) {
			removeWheelZoomDevice();
			removeButtonZoomDevice();
		} else if (newZoomDevice == ZoomDevice.MOUSE_BUTTON) {
			removeWheelZoomDevice();
			addButtonZoomDevice();
		} else if (newZoomDevice == ZoomDevice.MOUSE_WHEEL) {
			removeButtonZoomDevice();
			addWheelZoomDevice();
		}
	}

	/**
	 * <p>
	 * Gets the current zoom device.
	 * </p>
	 */
	public ZoomDevice getZoomDevice() {
		if (buttonZoomDevice != null) {
			return ZoomDevice.MOUSE_BUTTON;
		} else if (wheelZoomDevice != null) {
			return ZoomDevice.MOUSE_WHEEL;
		} else {
			return ZoomDevice.NONE;
		}
	}

	//Called from paintComponent() when a new image is set.
	private void initialiseState() {
		double xScale = (double) getWidth() / image.getWidth();
		double yScale = (double) getHeight() / image.getHeight();
		double scale = Math.min(xScale, yScale);

		double originX = (getWidth() - scale * image.getWidth()) / 2;
		double originY = (getHeight() - scale * image.getHeight()) / 2;

		double navScale = 1.0;

		setState(originX, originY, scale, navScale, scale);
	}

	//Centers the current image in the panel.
	private void centerImage() {
		double originX = (getWidth() - getScreenImageWidth()) / 2;
		double originY = (getHeight() - getScreenImageHeight()) / 2;
		setState(originX, originY, state.getScale(), state.getNavScale(), state.getInitialScale());
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
		SVGDiagram oldImage = this.image;
		this.image = image;
		image.setDeviceViewport(new Rectangle(0, 0, (int) image.getWidth(), (int) image.getHeight()));

		//Reset state so that initializeParameters() is called in paintComponent()
		//for the new image.
		if (resetView || state.getPanState() == null) {
			initialiseState();
		}

		firePropertyChange(IMAGE_CHANGED_PROPERTY, oldImage, image);
		repaint();
	}

	//The user clicked within the navigation image and this part of the image
	//is displayed in the panel.
	//The clicked point of the image is centered in the panel.
	private void displayImageAt(Point p) {
		Point scrImagePoint = navToZoomedImageCoords(p);
		double originX = -(scrImagePoint.x - getWidth() / 2);
		double originY = -(scrImagePoint.y - getHeight() / 2);

		setState(originX, originY, state.getScale(), state.getNavScale(), state.getInitialScale());

		repaint();
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

	//Used when the panel is resized
	//move the image accordingly
	private void scaleOrigin() {
		double originX = state.getOriginX() * getWidth() / state.getPreviousPanelSize().width;
		double originY = state.getOriginY() * getHeight() / state.getPreviousPanelSize().height;
		setState(originX, originY, state.getScale(), state.getNavScale(), state.getInitialScale());

		repaint();
	}

	/**
	 * <p>
	 * Sets the zoom level used to display the image.
	 * </p>
	 * <p>
	 * This method is used in programmatic zooming. The zooming center is the
	 * point of the image closest to the center of the panel. After a new zoom
	 * level is set the image is repainted.
	 * </p>
	 * 
	 * @param newZoom
	 *            the zoom level used to display this panel's image.
	 */
	public void setZoom(double newZoom) {
		Point zoomingCenter = new Point(getWidth() / 2, getHeight() / 2);
		zoomImage(newZoom, zoomingCenter);
	}

	/**
	 * <p>
	 * Sets the zoom level used to display the image, and the zooming center,
	 * around which zooming is done.
	 * </p>
	 * <p>
	 * This method is used in programmatic zooming. After a new zoom level is
	 * set the image is repainted.
	 * </p>
	 * 
	 * @param zoom
	 *            the zoom level used to display this panel's image.
	 */
	public void setZoom(double zoom, Point zoomingCenter) {
		double oldZoom = getZoom();
		zoomImage(zoom, zoomingCenter);
		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom), new Double(zoom));
		repaint();
	}

	/**
	 * <p>
	 * Sets a new zoom increment value.
	 * </p>
	 * 
	 * @param zoomIncrement
	 *            new zoom increment value
	 */
	public void setZoomIncrement(double zoomIncrement) {
		double oldZoomIncrement = state.getZoomIncrement();
		state.setZoomIncrement(zoomIncrement);
		firePropertyChange(ZOOM_INCREMENT_CHANGED_PROPERTY, new Double(oldZoomIncrement), new Double(zoomIncrement));
	}

	//Zooms an image in the panel by repainting it at the new zoom level.
	//The current mouse position is the zooming center.
	private void zoomImage(double zoomFactor, Point mousePosition) {
		Coords imageP = panelToImageCoords(mousePosition);
		double oldZoom = getZoom();

		double scale = state.getScale() * zoomFactor;

		NavigableSVGPanelZoomPanState intermediateState = new NavigableSVGPanelZoomPanState(state.getOriginX(),
				state.getOriginY(), scale, state.getNavScale(), state.getInitialScale());
		Coords panelP = imageToPanelCoords(imageP, intermediateState);

		double originX = state.getOriginX() + (mousePosition.x - (int) panelP.x);
		double originY = state.getOriginY() + (mousePosition.y - (int) panelP.y);

		setState(originX, originY, scale, state.getNavScale(), state.getInitialScale());

		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom), new Double(getZoom()));

		repaint();
	}

	//Zooms the navigation image
	private void zoomNavigationImage(double zoomFactor) {
		setState(state.getOriginX(), state.getOriginY(), state.getScale(), state.getNavScale() * zoomFactor,
				state.getInitialScale());

		repaint();
	}

	/**
	 * <p>
	 * Sets the image origin.
	 * </p>
	 * <p>
	 * Image origin is defined as the upper, left corner of the image in the
	 * panel's coordinate system. After a new origin is set, the image is
	 * repainted. This method is used for programmatic image navigation.
	 * </p>
	 * 
	 * @param x
	 *            the x coordinate of the new image origin
	 * @param y
	 *            the y coordinate of the new image origin
	 */
	public void setImageOrigin(int x, int y) {
		setImageOrigin(new Point(x, y));
	}

	/**
	 * <p>
	 * Sets the image origin.
	 * </p>
	 * <p>
	 * Image origin is defined as the upper, left corner of the image in the
	 * panel's coordinate system. After a new origin is set, the image is
	 * repainted. This method is used for programmatic image navigation.
	 * </p>
	 * 
	 * @param newOrigin
	 *            the value of a new image origin
	 */
	public void setImageOrigin(Point newOrigin) {
		double originX = newOrigin.x;
		double originY = newOrigin.y;

		setState(originX, originY, state.getScale(), state.getNavScale(), state.getInitialScale());
		repaint();
	}

	//Moves te image (by dragging with the mouse) to a new mouse position p.
	private void moveImage(Point p) {
		int xDelta = p.x - mousePosition.x;
		int yDelta = p.y - mousePosition.y;
		double originX = state.getOriginX() + xDelta;
		double originY = state.getOriginY() + yDelta;
		mousePosition = p;

		setState(originX, originY, state.getScale(), state.getNavScale(), state.getInitialScale());
		repaint();
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

		if (image == null) {
			return;
		}

		state.applyPanState();

		//set anti-aliasing if desired
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, state.isAntiAlias() ? RenderingHints.VALUE_ANTIALIAS_ON
				: RenderingHints.VALUE_ANTIALIAS_OFF);

		g2.setClip(0, 0, getWidth(), getHeight());

		drawSVG(g2, image, state.getOriginX(), state.getOriginY(), getScreenImageWidth(), getScreenImageHeight());

		//Draw navigation image
		if (state.isNavigationImageEnabled() && !isFullImageInPanel()) {
			drawSVG(g2, image, 0, 0, getScreenNavImageWidth(), getScreenNavImageHeight());
			int width = (int) getScreenNavImageWidth();
			int height = (int) getScreenNavImageHeight();
			g2.drawRect(0, 0, width, height);
			drawZoomAreaOutline(g);
		}
	}

	public static void drawSVG(Graphics2D g, SVGDiagram image, double x, double y, double width, double height) {

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

	//Paints a white outline over the navigation image indicating 
	//the area of the image currently displayed in the panel.
	private void drawZoomAreaOutline(Graphics g) {
		if (isFullImageInPanel()) {
			return;
		}

		double x = -state.getOriginX() * getScreenNavImageWidth() / getScreenImageWidth();
		double y = -state.getOriginY() * getScreenNavImageHeight() / getScreenImageHeight();
		double width = getWidth() * getScreenNavImageWidth() / getScreenImageWidth();
		double height = getHeight() * getScreenNavImageHeight() / getScreenImageHeight();

		if (x + width > getScreenNavImageWidth()) {
			width = getScreenNavImageWidth() - x;
		}
		if (y + height > getScreenNavImageHeight()) {
			height = getScreenNavImageHeight() - y;
		}
		g.setColor(state.getNavigationImageBorderColor());
		g.drawRect((int) x, (int) y, (int) width, (int) height);
	}
	
	public double getScreenImageWidth() {
		return getScreenImageWidth(state.getPanState());
	}

	public double getScreenImageWidth(NavigableSVGPanelZoomPanState panState) {
		return panState.getScale() * image.getWidth();
	}
	
	public double getScreenImageHeight() {
		return getScreenImageHeight(state.getPanState());
	}

	public double getScreenImageHeight(NavigableSVGPanelZoomPanState panState) {
		return panState.getScale() * image.getHeight();
	}

	public void setState(double originX, double originY, double scale, double navScale, double initialScale) {
		NavigableSVGPanelZoomPanState newPanState = new NavigableSVGPanelZoomPanState(originX, originY, scale,
				navScale, initialScale);
		if (isValid(newPanState) || !isValid(state.getPanState())) {
			state.submitPanState(newPanState);
		}
	}

	public boolean isValid(NavigableSVGPanelZoomPanState panState) {

		if (panState == null) {
			return false;
		}
		
		//the image cannot become too small
		if ((getScreenImageWidth(panState) < getWidth() * state.getMinimumScreenImageInPartOfPanel() && getScreenImageHeight(panState) < getHeight())
				|| (getScreenImageHeight(panState) < getHeight() * state.getMinimumScreenImageInPartOfPanel() && getScreenImageWidth(panState) < getWidth())) {
			return false;
		}

		//the image cannot go off screen
		if (panState.getOriginX() > getWidth() * (1 - state.getMinimumScreenImageInPartOfPanel())
				|| panState.getOriginY() > getHeight() * (1 - state.getMinimumScreenImageInPartOfPanel())
				|| panState.getOriginX() + getScreenImageWidth(panState) < getWidth()
						* state.getMinimumScreenImageInPartOfPanel()
				|| panState.getOriginY() + getScreenImageHeight(panState) < getHeight()
						* state.getMinimumScreenImageInPartOfPanel()) {
			return false;
		}

		return true;
	}

	public double getScreenNavImageWidth() {
		return getWidth() * state.getNavigationImageWidthInPartOfPanel() * state.getNavScale();
	}

	public double getScreenNavImageHeight() {
		return (getScreenNavImageWidth() / image.getWidth()) * image.getHeight();
	}

	//Converts this panel's coordinates into the original image coordinates
	public Coords panelToImageCoords(Point p) {
		return new Coords((p.x - state.getOriginX()) / state.getScale(), (p.y - state.getOriginY()) / state.getScale());
	}

	//Converts the original image coordinates into this panel's coordinates
	public Coords imageToPanelCoords(Coords p, NavigableSVGPanelZoomPanState state) {
		return new Coords((p.x * state.getScale()) + state.getOriginX(), (p.y * state.getScale()) + state.getOriginY());
	}

	//Converts the navigation image coordinates into the zoomed image coordinates	
	public Point navToZoomedImageCoords(Point p) {
		double x = p.x * getScreenImageWidth() / getScreenNavImageWidth();
		double y = p.y * getScreenImageHeight() / getScreenNavImageHeight();
		return new Point((int) x, (int) y);
	}

	//Tests whether a given point in the panel falls within the image boundaries.	
	public boolean isInImage(Point p) {
		if (isInNavigationImage(p)) {
			return false;
		}
		Coords coords = panelToImageCoords(p);
		int x = coords.getIntX();
		int y = coords.getIntY();
		return (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight());
	}

	//Tests whether a given point in the panel falls within the navigation image 
	//boundaries.	
	public boolean isInNavigationImage(Point p) {
		return (state.isNavigationImageEnabled() && p.x < getScreenNavImageWidth() && p.y < getScreenNavImageHeight());
	}

	//Used when the image is resized.
	public boolean isImageEdgeInPanel() {
		if (state.getPreviousPanelSize() == null) {
			return false;
		}

		return (state.getOriginX() > 0 && state.getOriginX() < state.getPreviousPanelSize().width || state.getOriginY() > 0
				&& state.getOriginY() < state.getPreviousPanelSize().height);
	}

	/**
	 * Tests whether the image is displayed in its entirety in the panel while
	 * allowing a margin of 1, due to rounding errors
	 * 
	 * @return
	 */
	public boolean isFullImageInPanel() {
		return (state.getOriginX() >= -1 && (state.getOriginX() + getScreenImageWidth()) <= getWidth()
				&& state.getOriginY() >= -1 && (state.getOriginY() + getScreenImageHeight()) <= getHeight());
	}

	public double getZoom() {
		return state.getScale() / state.getInitialScale();
	}

	/**
	 * <p>
	 * Gets the image origin.
	 * </p>
	 * <p>
	 * Image origin is defined as the upper, left corner of the image in the
	 * panel's coordinate system.
	 * </p>
	 * 
	 * @return the point of the upper, left corner of the image in the panel's
	 *         coordinates system.
	 */
	public Point getImageOrigin() {
		return new Point((int) state.getOriginX(), (int) state.getOriginY());
	}
}
