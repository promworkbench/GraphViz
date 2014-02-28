package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Dimension;
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
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Title;
import com.kitfox.svg.xml.StyleAttribute;

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

	//settings
	private double navigationImageWidthInPartOfPanel = 0.1;
	private double minimumScreenImageInPartOfPanel = 0.1;
	private Color navigationImageBorderColor = Color.black;
	private boolean navigationImageEnabled = true;
	private boolean antiAlias = true;
	private double zoomIncrement = 0.2;

	protected State state = null;
	protected SVGDiagram image;

	private Point mousePosition;
	private Dimension previousPanelSize;

	private WheelZoomDevice wheelZoomDevice = null;
	private ButtonZoomDevice buttonZoomDevice = null;
	
	private boolean showBoundingBoxes = false;

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

	//This class is required for high precision image coordinates translation.
	protected class Coords {
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

	private class WheelZoomDevice implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			Point p = e.getPoint();
			boolean zoomIn = (e.getWheelRotation() < 0);
			if (state.isInNavigationImage(p)) {
				if (zoomIn) {
					zoomNavigationImage(1.0 + zoomIncrement);
				} else {
					zoomNavigationImage(1.0 - zoomIncrement);
				}
			} else if (state.isInImage(p)) {
				if (zoomIn) {
					zoomImage(1.0 + zoomIncrement, p);
				} else {
					zoomImage(1.0 - zoomIncrement, p);
				}
			}
		}
	}

	private class ButtonZoomDevice extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			Point p = e.getPoint();
			if (SwingUtilities.isRightMouseButton(e)) {
				if (state.isInNavigationImage(p)) {
					zoomNavigationImage(1.0 - zoomIncrement);
				} else if (state.isInImage(p)) {
					zoomImage(1.0 - zoomIncrement, p);
				}
			} else {
				if (state.isInNavigationImage(p)) {
					zoomNavigationImage(1.0 + zoomIncrement);
				} else if (state.isInImage(p)) {
					zoomImage(1.0 + zoomIncrement, p);
				}
			}
		}
	}

	private Action zoomInAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent e) {
			setZoom(1.0 + zoomIncrement);
		}
	};

	private Action zoomOutAction = new AbstractAction() {
		private static final long serialVersionUID = 7842478506942554961L;

		public void actionPerformed(ActionEvent e) {
			setZoom(1.0 - zoomIncrement);
		}
	};

	private Action walkAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("DOWN")) {
				state = state.update(state.originX, state.originY - 10, state.scale, state.navScale);
			} else if (command.equals("UP")) {
				state = state.update(state.originX, state.originY + 10, state.scale, state.navScale);
			} else if (command.equals("LEFT")) {
				state = state.update(state.originX + 10, state.originY, state.scale, state.navScale);
			} else if (command.equals("RIGHT")) {
				state = state.update(state.originX - 10, state.originY, state.scale, state.navScale);
			}
			repaint();
		}
	};

	protected class State {
		private final double originX;
		private final double originY;
		private final double scale;
		private final double navScale;

		private final double initialScale;

		public State(double originX, double originY, double scale, double navScale, double initialScale) {
			this.originX = originX;
			this.originY = originY;
			this.scale = scale;
			this.navScale = navScale;
			this.initialScale = initialScale;
		}

		public State update(double originX, double originY, double scale, double navScale) {
			State newState = new State(originX, originY, scale, navScale, initialScale);
			if (newState.isValid() || !isValid()) {
				return newState;
			}
			return this;
		}

		private boolean isValid() {

			//the image cannot become too small
			if ((getScreenImageWidth() < getWidth() * minimumScreenImageInPartOfPanel && getScreenImageHeight() < getHeight())
					|| (getScreenImageHeight() < getHeight() * minimumScreenImageInPartOfPanel && getScreenImageWidth() < getWidth())) {
				return false;
			}

			//the image cannot go off screen
			if (originX > getWidth() * (1 - minimumScreenImageInPartOfPanel)
					|| originY > getHeight() * (1 - minimumScreenImageInPartOfPanel)
					|| originX + getScreenImageWidth() < getWidth() * minimumScreenImageInPartOfPanel
					|| originY + getScreenImageHeight() < getHeight() * minimumScreenImageInPartOfPanel) {
				return false;
			}

			return true;
		}

		public double getScreenImageWidth() {
			return scale * image.getWidth();
		}

		public double getScreenImageHeight() {
			return scale * image.getHeight();
		}

		public double getScreenNavImageWidth() {
			return getWidth() * navigationImageWidthInPartOfPanel * navScale;
		}

		public double getScreenNavImageHeight() {
			return (getScreenNavImageWidth() / image.getWidth()) * image.getHeight();
		}

		//Converts this panel's coordinates into the original image coordinates
		public Coords panelToImageCoords(Point p) {
			return new Coords((p.x - originX) / scale, (p.y - originY) / scale);
		}

		//Converts the original image coordinates into this panel's coordinates
		public Coords imageToPanelCoords(Coords p) {
			return new Coords((p.x * scale) + originX, (p.y * scale) + originY);
		}

		//Converts the navigation image coordinates into the zoomed image coordinates	
		public Point navToZoomedImageCoords(Point p) {
			double x = p.x * getScreenImageWidth() / getScreenNavImageWidth();
			double y = p.y * getScreenImageHeight() / getScreenNavImageHeight();
			return new Point((int) x, (int) y);
		}

		//Tests whether a given point in the panel falls within the image boundaries.	
		public boolean isInImage(Point p) {
			Coords coords = panelToImageCoords(p);
			int x = coords.getIntX();
			int y = coords.getIntY();
			return (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight());
		}

		//Tests whether a given point in the panel falls within the navigation image 
		//boundaries.	
		public boolean isInNavigationImage(Point p) {
			return (isNavigationImageEnabled() && p.x < getScreenNavImageWidth() && p.y < getScreenNavImageHeight());
		}

		//Used when the image is resized.
		public boolean isImageEdgeInPanel() {
			if (previousPanelSize == null) {
				return false;
			}

			return (originX > 0 && originX < previousPanelSize.width || originY > 0
					&& originY < previousPanelSize.height);
		}

		//Tests whether the image is displayed in its entirety in the panel
		//while allowing a margin of 1, due to rounding errors
		public boolean isFullImageInPanel() {
			return (originX >= -1 && (originX + getScreenImageWidth()) <= getWidth() && originY >= -1 && (originY + getScreenImageHeight()) <= getHeight());
		}

		public double getZoom() {
			return scale / initialScale;
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
		 * @return the point of the upper, left corner of the image in the
		 *         panel's coordinates system.
		 */
		public Point getImageOrigin() {
			return new Point((int) originX, (int) originY);
		}
	}

	/**
	 * <p>
	 * Creates a new navigable image panel with no default image and the mouse
	 * scroll wheel as the zooming device.
	 * </p>
	 */
	public NavigableSVGPanel() {
		setOpaque(false);
		setDoubleBuffered(true);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (state != null) {
					if (state.isFullImageInPanel()) {
						centerImage();
					} else if (state.isImageEdgeInPanel()) {
						scaleOrigin();
					}
					repaint();
				}
				previousPanelSize = getSize();
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				Point pointPanelCoordinates = e.getPoint();
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (state.isInNavigationImage(e.getPoint())) {
						displayImageAt(pointPanelCoordinates);
					}
				}
			}
		});

		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && !state.isInNavigationImage(e.getPoint())) {
					Point p = e.getPoint();
					moveImage(p);
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
	}

	/**
	 * <p>
	 * Creates a new navigable image panel with the specified image and the
	 * mouse scroll wheel as the zooming device.
	 * </p>
	 */
	public NavigableSVGPanel(SVGDiagram image) {
		this();
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
	private void initializeParams() {
		double xScale = (double) getWidth() / image.getWidth();
		double yScale = (double) getHeight() / image.getHeight();
		double scale = Math.min(xScale, yScale);

		double originX = (getWidth() - scale * image.getWidth()) / 2;
		double originY = (getHeight() - scale * image.getHeight()) / 2;

		double navScale = 1.0;

		state = new State(originX, originY, scale, navScale, scale);

	}

	//Centers the current image in the panel.
	private void centerImage() {
		double originX = (getWidth() - state.getScreenImageWidth()) / 2;
		double originY = (getHeight() - state.getScreenImageHeight()) / 2;
		state = state.update(originX, originY, state.scale, state.navScale);
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
		SVGDiagram oldImage = this.image;
		this.image = image;
		image.setDeviceViewport(new Rectangle(0, 0, (int) image.getWidth(), (int) image.getHeight()));
		
		//Reset state so that initializeParameters() is called in paintComponent()
		//for the new image.
		if (resetView) {
			state = null;
		}

		firePropertyChange(IMAGE_CHANGED_PROPERTY, oldImage, image);
		repaint();
	}

	//The user clicked within the navigation image and this part of the image
	//is displayed in the panel.
	//The clicked point of the image is centered in the panel.
	private void displayImageAt(Point p) {
		Point scrImagePoint = state.navToZoomedImageCoords(p);
		double originX = -(scrImagePoint.x - getWidth() / 2);
		double originY = -(scrImagePoint.y - getHeight() / 2);

		state = state.update(originX, originY, state.scale, state.navScale);

		repaint();
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
		navigationImageEnabled = enabled;
		repaint();
	}

	//Used when the panel is resized
	//move the image accordingly
	private void scaleOrigin() {
		double originX = state.originX * getWidth() / previousPanelSize.width;
		double originY = state.originY * getHeight() / previousPanelSize.height;
		state = state.update(originX, originY, state.scale, state.navScale);

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
	 * @param newZoom
	 *            the zoom level used to display this panel's image.
	 */
	public void setZoom(double newZoom, Point zoomingCenter) {
		double oldZoom = state.getZoom();

		zoomImage(newZoom, zoomingCenter);

		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom), new Double(state.getZoom()));

		repaint();
	}

	/**
	 * <p>
	 * Gets the current zoom increment.
	 * </p>
	 * 
	 * @return the current zoom increment
	 */
	public double getZoomIncrement() {
		return zoomIncrement;
	}

	/**
	 * <p>
	 * Sets a new zoom increment value.
	 * </p>
	 * 
	 * @param newZoomIncrement
	 *            new zoom increment value
	 */
	public void setZoomIncrement(double newZoomIncrement) {
		double oldZoomIncrement = zoomIncrement;
		zoomIncrement = newZoomIncrement;
		firePropertyChange(ZOOM_INCREMENT_CHANGED_PROPERTY, new Double(oldZoomIncrement), new Double(zoomIncrement));
	}

	//Zooms an image in the panel by repainting it at the new zoom level.
	//The current mouse position is the zooming center.
	private void zoomImage(double zoomFactor, Point mousePosition) {
		Coords imageP = state.panelToImageCoords(mousePosition);
		double oldZoom = state.getZoom();

		double scale = state.scale * zoomFactor;

		State intermediateState = new State(state.originX, state.originY, scale, state.navScale, state.initialScale);
		Coords panelP = intermediateState.imageToPanelCoords(imageP);

		double originX = state.originX + (mousePosition.x - (int) panelP.x);
		double originY = state.originY + (mousePosition.y - (int) panelP.y);

		state = state.update(originX, originY, scale, state.navScale);

		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom), new Double(state.getZoom()));

		repaint();
	}

	//Zooms the navigation image
	private void zoomNavigationImage(double zoomFactor) {
		state = state.update(state.originX, state.originY, state.scale, state.navScale * zoomFactor);

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

		state = state.update(originX, originY, state.scale, state.navScale);
		repaint();
	}

	//Moves te image (by dragging with the mouse) to a new mouse position p.
	private void moveImage(Point p) {
		int xDelta = p.x - mousePosition.x;
		int yDelta = p.y - mousePosition.y;
		double originX = state.originX + xDelta;
		double originY = state.originY + yDelta;
		mousePosition = p;

		state = state.update(originX, originY, state.scale, state.navScale);
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

		if (state == null) {
			initializeParams();
		}

		//set anti-aliasing if desired
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON
				: RenderingHints.VALUE_ANTIALIAS_OFF);

		g2.setClip(0, 0, getWidth(), getHeight());

		drawSVG(g2, image, state.originX, state.originY, state.getScreenImageWidth(), state.getScreenImageHeight());

		//Draw navigation image
		if (isNavigationImageEnabled() && !state.isFullImageInPanel()) {
			drawSVG(g2, image, 0, 0, state.getScreenNavImageWidth(), state.getScreenNavImageHeight());
			drawZoomAreaOutline(g);
		}
	}

	private void drawSVG(Graphics2D g, SVGDiagram image, double x, double y, double width, double height) {

		double scaleX = width / image.getWidth();
		double scaleY = height / image.getHeight();

		g.translate(x, y);
		g.scale(scaleX, scaleY);

		try {
			image.render(g);
		} catch (SVGException e) {
			e.printStackTrace();
		}

		//draw bounding boxes
		if (isShowBoundingBoxes()) {
			List<Group> groups = getSVGElementAt(image.getRoot(), new Point(0, 0));
			for (Group group : groups) {
				try {
					Rectangle2D rectangle = getBoundingBoxOf(group);
					g.drawRect((int) rectangle.getX(), (int) rectangle.getY(), (int) rectangle.getWidth(),
							(int) rectangle.getHeight());
				} catch (SVGException e) {
					e.printStackTrace();
				}
	
			}
		}
		
		g.scale(1 / scaleX, 1 / scaleY);
		g.translate(-x, -y);
	}

	//Paints a white outline over the navigation image indicating 
	//the area of the image currently displayed in the panel.
	private void drawZoomAreaOutline(Graphics g) {
		if (state.isFullImageInPanel()) {
			return;
		}

		double x = -state.originX * state.getScreenNavImageWidth() / state.getScreenImageWidth();
		double y = -state.originY * state.getScreenNavImageHeight() / state.getScreenImageHeight();
		double width = getWidth() * state.getScreenNavImageWidth() / state.getScreenImageWidth();
		double height = getHeight() * state.getScreenNavImageHeight() / state.getScreenImageHeight();

		if (x + width > state.getScreenNavImageWidth()) {
			width = state.getScreenNavImageWidth() - x;
		}
		if (y + height > state.getScreenNavImageHeight()) {
			height = state.getScreenNavImageHeight() - y;
		}
		g.setColor(navigationImageBorderColor);
		g.drawRect((int) x, (int) y, (int) width, (int) height);
	}

	public Color getNavigationImageBorderColor() {
		return navigationImageBorderColor;
	}

	public void setNavigationImageBorderColor(Color navigationImageBorderColor) {
		this.navigationImageBorderColor = navigationImageBorderColor;
	}

	public double getNavigationImageWidthInPartOfPanel() {
		return navigationImageWidthInPartOfPanel;
	}

	public void setNavigationImageWidthInPartOfPanel(double navigationImageWidthInPartOfPanel) {
		this.navigationImageWidthInPartOfPanel = navigationImageWidthInPartOfPanel;
	}

	private List<Group> getSVGElementAt(SVGElement element, Point point) {
		//System.out.println(" examine " + element + " " + element.getTagName() + " " + element.getId());

		List<Group> result = new LinkedList<Group>();

		if (element instanceof Title) {
			Title t = (Title) element;
			if (t.getParent() instanceof Group) {
				Group parent = (Group) t.getParent();
				try {
					StyleAttribute sty = new StyleAttribute("class");
					sty.setName("transform");
					if (parent.getPresentationAttributes().contains("transform")) {
						parent.getPres(sty);
						System.out.println("   transform " + sty.getStringValue());

						System.out.println("  group discovered " + t.getText());
						System.out.println("   bounding box " + getBoundingBoxOf(parent));
						System.out.println("   " + parent.getInlineAttributes());
						System.out.println("   " + parent.getPresentationAttributes());

						parent.getPres(sty);
						System.out.println("   class " + sty.getStringValue());
					}

					//System.out.println("  parent " + t.getParent());
					//System.out.println("  parent is group");
					//System.out.println("  ===== point is in group ===== ");

					result.add(parent);
				} catch (SVGException e) {
					e.printStackTrace();
				}
			}
		}

		for (int i = 0; i < element.getNumChildren(); i++) {
			SVGElement child = element.getChild(i);
			result.addAll(getSVGElementAt(child, point));
		}

		return result;
	}

	private Rectangle2D getBoundingBoxOf(Group element) throws SVGException {
		//get the bounding box
		Rectangle2D boundingBox = element.getBoundingBox();

		//Shape shape = element.getShape();
		//Rectangle2D boundingBox = shape.getBounds2D();

		//transform the bounding box
		int x = (int) (boundingBox.getX());
		//int y = (int) (boundingBox.getY() + image.getHeight());
		int y = (int) (boundingBox.getY());
		int width = (int) (boundingBox.getWidth());
		int height = (int) (boundingBox.getHeight());

		Rectangle boundingBoxTransformed = new Rectangle();
		boundingBoxTransformed.setBounds(x, y, width, height);

		return boundingBoxTransformed;
	}

	public boolean isShowBoundingBoxes() {
		return showBoundingBoxes;
	}

	public void setShowBoundingBoxes(boolean showBoundingBoxes) {
		this.showBoundingBoxes = showBoundingBoxes;
	}
}
