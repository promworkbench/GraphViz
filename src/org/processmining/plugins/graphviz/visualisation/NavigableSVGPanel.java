package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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

	private static final double SCREEN_NAV_IMAGE_FACTOR = 0.15; // 15% of panel's width
	private static final double NAV_IMAGE_FACTOR = 0.1; // 30% of panel's width

	private double zoomIncrement = 0.2;
	private double zoomFactor = 1.0 + zoomIncrement;
	private double navZoomFactor = 1.0 + zoomIncrement;
	private SVGDiagram image;
	private double initialScale = 1.0;
	private double scale = 1.0;
	private double navScale = 1.0;
	private double originX = 0;
	private double originY = 0;
	private Point mousePosition;
	private Dimension previousPanelSize;
	private boolean navigationImageEnabled = true;
	private boolean antiAlias = true;

	private Color navigationImageBorderColor = Color.white;

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

	//This class is required for high precision image coordinates translation.
	private class Coords {
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

		public String toString() {
			return "[Coords: x=" + x + ",y=" + y + "]";
		}
	}

	private class WheelZoomDevice implements MouseWheelListener {
		public void mouseWheelMoved(MouseWheelEvent e) {
			Point p = e.getPoint();
			boolean zoomIn = (e.getWheelRotation() < 0);
			if (isInNavigationImage(p)) {
				if (zoomIn) {
					navZoomFactor = 1.0 + zoomIncrement;
				} else {
					navZoomFactor = 1.0 - zoomIncrement;
				}
				zoomNavigationImage();
			} else if (isInImage(p)) {
				if (zoomIn) {
					zoomFactor = 1.0 + zoomIncrement;
				} else {
					zoomFactor = 1.0 - zoomIncrement;
				}
				zoomImage();
			}
		}
	}

	private class ButtonZoomDevice extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			Point p = e.getPoint();
			if (SwingUtilities.isRightMouseButton(e)) {
				if (isInNavigationImage(p)) {
					navZoomFactor = 1.0 - zoomIncrement;
					zoomNavigationImage();
				} else if (isInImage(p)) {
					zoomFactor = 1.0 - zoomIncrement;
					zoomImage();
				}
			} else {
				if (isInNavigationImage(p)) {
					navZoomFactor = 1.0 + zoomIncrement;
					zoomNavigationImage();
				} else if (isInImage(p)) {
					zoomFactor = 1.0 + zoomIncrement;
					zoomImage();
				}
			}
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
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (scale > 0.0) {
					if (isFullImageInPanel()) {
						centerImage();
					} else if (isImageEdgeInPanel()) {
						scaleOrigin();
					}
					repaint();
				}
				previousPanelSize = getSize();
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (isInNavigationImage(e.getPoint())) {
						Point p = e.getPoint();
						displayImageAt(p);
					}
				}
			}
		});

		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && !isInNavigationImage(e.getPoint())) {
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
	}

	/**
	 * <p>
	 * Creates a new navigable image panel with the specified image and the
	 * mouse scroll wheel as the zooming device.
	 * </p>
	 */
	public NavigableSVGPanel(SVGDiagram image) {
		this();
		setImage(image);
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
		initialScale = Math.min(xScale, yScale);
		scale = initialScale;

		//An image is initially centered
		centerImage();

	}

	//Centers the current image in the panel.
	private void centerImage() {
		originX = (getWidth() - getScreenImageWidth()) / 2;
		originY = (getHeight() - getScreenImageHeight()) / 2;
	}

	/**
	 * <p>
	 * Sets an image for display in the panel.
	 * </p>
	 * 
	 * @param image
	 *            an image to be set in the panel
	 */
	public void setImage(SVGDiagram image) {
		SVGDiagram oldImage = this.image;
		this.image = image;
		//Reset scale so that initializeParameters() is called in paintComponent()
		//for the new image.
		scale = initialScale;

		firePropertyChange(IMAGE_CHANGED_PROPERTY, oldImage, image);
		repaint();
	}

	//Converts this panel's coordinates into the original image coordinates
	private Coords panelToImageCoords(Point p) {
		return new Coords((p.x - originX) / scale, (p.y - originY) / scale);
	}

	//Converts the original image coordinates into this panel's coordinates
	private Coords imageToPanelCoords(Coords p) {
		return new Coords((p.x * scale) + originX, (p.y * scale) + originY);
	}

	//Converts the navigation image coordinates into the zoomed image coordinates	
	private Point navToZoomedImageCoords(Point p) {
		double x = p.x * getScreenImageWidth() / getScreenNavImageWidth();
		double y = p.y * getScreenImageHeight() / getScreenNavImageHeight();
		return new Point((int) x, (int) y);
	}

	//The user clicked within the navigation image and this part of the image
	//is displayed in the panel.
	//The clicked point of the image is centered in the panel.
	private void displayImageAt(Point p) {
		Point scrImagePoint = navToZoomedImageCoords(p);
		originX = -(scrImagePoint.x - getWidth() / 2);
		originY = -(scrImagePoint.y - getHeight() / 2);
		repaint();
	}

	//Tests whether a given point in the panel falls within the image boundaries.	
	private boolean isInImage(Point p) {
		Coords coords = panelToImageCoords(p);
		int x = coords.getIntX();
		int y = coords.getIntY();
		return (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight());
	}

	//Tests whether a given point in the panel falls within the navigation image 
	//boundaries.	
	private boolean isInNavigationImage(Point p) {
		return (isNavigationImageEnabled() && p.x < getScreenNavImageWidth() && p.y < getScreenNavImageHeight());
	}

	//Used when the image is resized.
	private boolean isImageEdgeInPanel() {
		if (previousPanelSize == null) {
			return false;
		}

		return (originX > 0 && originX < previousPanelSize.width || originY > 0 && originY < previousPanelSize.height);
	}

	//Tests whether the image is displayed in its entirety in the panel.
	private boolean isFullImageInPanel() {
		return (originX >= 0 && (originX + getScreenImageWidth()) < getWidth() && originY >= 0 && (originY + getScreenImageHeight()) < getHeight());
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
	private void scaleOrigin() {
		originX = originX * getWidth() / previousPanelSize.width;
		originY = originY * getHeight() / previousPanelSize.height;
		repaint();
	}

	//Converts the specified zoom level	to scale.
	private double zoomToScale(double zoom) {
		return initialScale * zoom;
	}

	/**
	 * <p>
	 * Gets the current zoom level.
	 * </p>
	 * 
	 * @return the current zoom level
	 */
	public double getZoom() {
		return scale / initialScale;
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
		setZoom(newZoom, zoomingCenter);
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
		Coords imageP = panelToImageCoords(zoomingCenter);
		if (imageP.x < 0.0) {
			imageP.x = 0.0;
		}
		if (imageP.y < 0.0) {
			imageP.y = 0.0;
		}
		if (imageP.x >= image.getWidth()) {
			imageP.x = image.getWidth() - 1.0;
		}
		if (imageP.y >= image.getHeight()) {
			imageP.y = image.getHeight() - 1.0;
		}

		Coords correctedP = imageToPanelCoords(imageP);
		double oldZoom = getZoom();
		scale = zoomToScale(newZoom);
		Coords panelP = imageToPanelCoords(imageP);

		originX += (correctedP.getIntX() - (int) panelP.x);
		originY += (correctedP.getIntY() - (int) panelP.y);

		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom), new Double(getZoom()));

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
	private void zoomImage() {
		Coords imageP = panelToImageCoords(mousePosition);
		double oldZoom = getZoom();
		scale *= zoomFactor;
		Coords panelP = imageToPanelCoords(imageP);

		originX += (mousePosition.x - (int) panelP.x);
		originY += (mousePosition.y - (int) panelP.y);

		firePropertyChange(ZOOM_LEVEL_CHANGED_PROPERTY, new Double(oldZoom), new Double(getZoom()));

		repaint();
	}

	//Zooms the navigation image
	private void zoomNavigationImage() {
		navScale *= navZoomFactor;
		repaint();
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
		return new Point((int) originX, (int) originY);
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
		originX = newOrigin.x;
		originY = newOrigin.y;
		repaint();
	}

	//Moves te image (by dragging with the mouse) to a new mouse position p.
	private void moveImage(Point p) {
		int xDelta = p.x - mousePosition.x;
		int yDelta = p.y - mousePosition.y;
		originX += xDelta;
		originY += yDelta;
		mousePosition = p;
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

		if (scale == 0.0) {
			initializeParams();
		}

		//set anti-aliasing if desired
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON
				: RenderingHints.VALUE_ANTIALIAS_OFF);
		
		g2.setClip(0, 0, getWidth(), getHeight());

		drawSVG(g2, image, originX, originY, getScreenImageWidth(), getScreenImageHeight());

		//Draw navigation image
		if (isNavigationImageEnabled()) {
			drawSVG(g2, image, 0, 0, getScreenNavImageWidth(), getScreenNavImageHeight());
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

		g.scale(1 / scaleX, 1 / scaleY);
		g.translate(-x, -y);
	}

	//Paints a white outline over the navigation image indicating 
	//the area of the image currently displayed in the panel.
	private void drawZoomAreaOutline(Graphics g) {
		if (isFullImageInPanel()) {
			return;
		}

		double x = -originX * getScreenNavImageWidth() / getScreenImageWidth();
		double y = -originY * getScreenNavImageHeight() / getScreenImageHeight();
		double width = getWidth() * getScreenNavImageWidth() / getScreenImageWidth();
		double height = getHeight() * getScreenNavImageHeight() / getScreenImageHeight();
		g.setColor(navigationImageBorderColor);
		g.drawRect((int) x, (int) y, (int) width, (int) height);
	}

	private double getScreenImageWidth() {
		return scale * image.getWidth();
	}

	private double getScreenImageHeight() {
		return scale * image.getHeight();
	}

	private double getScreenNavImageWidth() {
		return getWidth() * NAV_IMAGE_FACTOR;
	}

	private double getScreenNavImageHeight() {
		return (getScreenNavImageWidth() / image.getWidth()) * image.getHeight();
	}

	public Color getNavigationImageBorderColor() {
		return navigationImageBorderColor;
	}

	public void setNavigationImageBorderColor(Color navigationImageBorderColor) {
		this.navigationImageBorderColor = navigationImageBorderColor;
	}
}
