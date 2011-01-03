/*
 * $Id$
 * 
 * Copyright © 2010 Jeff Eltgroth.
 * 
 * This file is part of Layout Designer.
 *
 * Layout Designer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Layout Designer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Layout Designer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package train.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import train.file.XMLReader;
import train.scenery.Branch;
import train.scenery.SceneryManager;
import train.util.Geometry;

@SuppressWarnings("serial")
public class LayoutEditPanel extends JPanel {

	public enum Mode {SELECT, MOVE, ADD, ROTATE, SIZE, INSPECT};
	private Mode mode;
	
	private List<Shape> selectedShapes;
	private GridPanel canvas;
	private MouseAdapter mouseAdapter;
    protected DropTarget dropTarget;
	
	public LayoutEditPanel() {
        super(new BorderLayout());

        mode = Mode.SELECT;
        mouseAdapter = new EditMouseAdapter();
        
        JPanel centerPanel = new JPanel(new GridLayout(0,1));
        
        canvas = new GridPanel();
        JScrollPane scrollPane = new JScrollPane(canvas,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(200, 75));
        centerPanel.add(scrollPane);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        add(centerPanel, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar("Object Library");
        toolBar.setOrientation(JToolBar.VERTICAL);
        loadLibrary(toolBar);
        add(toolBar, BorderLayout.LINE_END);
        
        // User input objects
        selectedShapes = new ArrayList<Shape>();
        canvas.setSelectedShapes(selectedShapes);
        canvas.addMouseListener(mouseAdapter);
        canvas.addMouseMotionListener(mouseAdapter);
        
        ToTransferHandler toHandler = new ToTransferHandler();
        canvas.setTransferHandler(toHandler);
        //dropTarget = new DropTarget(canvas, DnDConstants.ACTION_COPY, new DropTargetHandler(), true, null);

		SceneryManager.getInstance().addPropertyChangeListener("layout", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				canvas.setSize(SceneryManager.getInstance().getWidth(), 
						SceneryManager.getInstance().getHeight());
				canvas.setPreferredSize(new Dimension(SceneryManager.getInstance().getWidth(), 
						SceneryManager.getInstance().getHeight()));
			}});

	}
	
	public void loadLibrary(JToolBar toolBar) {
		XMLReader reader = new XMLReader();
		List<Shape> shapes = null;
		try {
			shapes = reader.readLibraryFile(new File("scenery/library.xml"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Shape shape : shapes) {
	        ShapeLabel label = new ShapeLabel(shape);
	        label.addMouseListener(mouseAdapter);
	        label.addMouseMotionListener(mouseAdapter);
	        toolBar.add(label);
		}
	}
	
	public void selectAllShapes() {
		selectedShapes.clear();
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			for (Shape shape : branch.getShapes()) {
				selectedShapes.add(shape);
			}
		}
		canvas.repaint();
	}
	
	public void deselectAllShapes() {
		selectedShapes.clear();
		canvas.repaint();
	}
	
	public void paste() {
		// TODO handle paste action
		System.out.println("Paste something from clipboard");
	}
	
	public void delete() {
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			for (Shape shape : selectedShapes) {
				branch.getShapes().remove(shape);
			}
		}
		selectedShapes.clear();
		canvas.repaint();
	}
	
	public void moveSelectedShapes(Point from, Point to) {
		double dx = to.getX() - from.getX();
		double dy = to.getY() - from.getY();
		AffineTransform transform = AffineTransform.getTranslateInstance(dx, dy);
		transformSelectedShapes(transform);
	}
	
	public void rotateSelectedShapes(Point from, Point to, Point center) {
		// calculate the angle of rotation about the center
		double fromAngle = Geometry.calcDirection(from.getX(), from.getY(), center.getX(), center.getY());
		double toAngle = Geometry.calcDirection(to.getX(), to.getY(), center.getX(), center.getY());
		System.out.println("rotate angle: " + Math.toDegrees(toAngle) + " - " + Math.toDegrees(fromAngle) + " = " + Math.toDegrees(toAngle - fromAngle));
		AffineTransform transform = AffineTransform.getRotateInstance(toAngle - fromAngle, center.getX(), center.getY());
		System.out.println("transform: " + transform);
		transformSelectedShapes(transform);
	}
	
	public void snapSelectedShapes() {
		// find all shapes near every selected point
		// remove the shapes that are selected
		// whatever is left can be snapped to
		List<Shape> allShapes = new ArrayList<Shape>();
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			for (Shape shape : branch.getShapes()) {
				allShapes.add(shape);
			}
		}
		allShapes.removeAll(selectedShapes);
		
		// there could be more than one shape at a junction
		List<Shape> anchorShapes = new ArrayList<Shape>();
		List<Shape> floatShapes = new ArrayList<Shape>();
		for (Shape selectedShape : selectedShapes) {
			for (Shape otherShape : allShapes) {
				if (Geometry.getClosePoints(selectedShape, otherShape) != null) {
					anchorShapes.add(otherShape);
					floatShapes.add(selectedShape);
				}
			}
		}
		
		// easy case of one anchor shape
		if (anchorShapes.size() == 1) {
			// points return in same order as parameters
			Point2D[] closePoints = Geometry.getClosePoints(anchorShapes.get(0), floatShapes.get(0));
			AffineTransform at = Geometry.getSnapToTransform(anchorShapes.get(0), floatShapes.get(0), closePoints[0], closePoints[1]);
			transformSelectedShapes(at);
		}
		
		// for multiple shapes, find the best snap using the smallest displacement
		// floatShapes is supposed to have duplicate entries
		if (anchorShapes.size() > 1) {
			double[] differences = new double[anchorShapes.size()];
			Point2D[] closePoints = null;
			for (int i = 0; i < anchorShapes.size(); i++) {
				Point2D[] points = Geometry.getPointsFromShape(anchorShapes.get(i));
				closePoints = Geometry.getClosePoints(anchorShapes.get(i), floatShapes.get(i));
				double anchorDirection = Geometry.calcDirection(closePoints[0], 
						Geometry.getOppositePoint(anchorShapes.get(i), closePoints[0]), 
						points.length > 2 ? points[2] : null);
				points = Geometry.getPointsFromShape(floatShapes.get(i));
				double floatDirection = Geometry.calcDirection(closePoints[1], 
						Geometry.getOppositePoint(floatShapes.get(i), closePoints[1]), 
						points.length > 2 ? points[2] : null);
				// reverse floatDirection to compare closest to zero angle
				floatDirection = floatDirection + Math.PI;
				differences[i] = Math.abs(Geometry.normalizeRadians(anchorDirection - floatDirection));
				
//				System.out.println("snap to: anchorDirection: " + Math.toDegrees(anchorDirection) + 
//						" floatDirection: " + Math.toDegrees(floatDirection) + 
//						" difference: " + Math.toDegrees(differences[i]));
			}
			double min = differences[0];
			int index = 0;
			for (int i = 0; i < differences.length; i++) {
				if (differences[i] < min) {
					min = differences[i];
					index = i;
				}
			}
			
			closePoints = Geometry.getClosePoints(anchorShapes.get(index), floatShapes.get(index));
			AffineTransform at = Geometry.getSnapToTransform(anchorShapes.get(index), floatShapes.get(index), closePoints[0], closePoints[1]);
			transformSelectedShapes(at);
		}
	}
	
	public void sizeSelectedShapes(Point oldPoint, Point newPoint) {
		// TODO handle more than one shape at a time
		if (selectedShapes.size() == 1) {
			Point2D[] points = Geometry.getPointsFromShape(selectedShapes.get(0));
			double span = points[0].distance(points[1]);
			Point2D anchorPoint = null;
			Point2D floatPoint = null;
			if (newPoint.distance(points[0]) < span / 2.0) {
				anchorPoint = points[1];
				floatPoint = points[0];
			} else if (newPoint.distance(points[1]) < span / 2.0) {
				anchorPoint = points[0];
				floatPoint = points[1];
			} else {
				System.out.println("points are not close enough NPE sure to follow");
			}

			// simplified stretching of shape by dragging one point
//			floatPoint.setLocation(newPoint);
//			Geometry.alterShapePoints(selectedShapes.get(0), points);
			
			// scale transform is not doing what I expected
			double newSpan = anchorPoint.distance(newPoint);
			double scale = newSpan / span;  // can get smaller too
			
			double dx0 = floatPoint.getX() - anchorPoint.getX();
			double dy0 = floatPoint.getY() - anchorPoint.getY();
			double dx1 = newPoint.getX() - anchorPoint.getX();
			double dy1 = newPoint.getY() - anchorPoint.getY();
			double xscale = dx0 == 0.0 ? 1.0 : dx1 / dx0;
			double yscale = dy0 == 0.0 ? 1.0 : dy1 / dy0;
			double fudge = 0.0;
			if (dy0 == 0.0 || dx0 == 0.0) {
				// this will cause the X or Y to have a difference during scale
				fudge = 0.000001;
			}
			
			AffineTransform at = new AffineTransform();
			at.translate(anchorPoint.getX(), anchorPoint.getY());
			at.scale(xscale, yscale);
			at.rotate(fudge);
			at.translate(-anchorPoint.getX(), -anchorPoint.getY());
			System.out.println("dx0: " + dx0 + " dx1: " + dx1 + " xscale: " + xscale + " xdiff: " + (dx1-dx0));
			System.out.println("dy0: " + dy0 + " dy1: " + dy1 + " yscale: " + yscale + " ydiff: " + (dy1-dy0));
			//System.out.println("scale: " + scale + " span: " + span + " newspan: " + newSpan + " diff: " + (-newSpan + span));
			System.out.println("BEFORE " + points[0] + ", " + points[1]);
			transformSelectedShapes(at);
			Point2D[] points2 = Geometry.getPointsFromShape(selectedShapes.get(0));
			System.out.println("AFTER " + points2[0] + ", " + points2[1]);
			System.out.println("xdiff: " + (points2[0].getX() - points[0].getX()) + " ydiff: " + (points2[0].getY() - points[0].getY()));
		}
	}
	
	private void transformSelectedShapes(AffineTransform at) {
		for (Shape shape : selectedShapes) {
			Geometry.transform(shape, at);
		}
		SceneryManager.getInstance().fixJunctions();
	}
	
	public void snapSelectedPoint(Point location) {
		// TODO handle more than one shape at a time
		if (selectedShapes.size() == 1) {
			Point2D[] points = Geometry.getPointsFromShape(selectedShapes.get(0));
			double span = points[0].distance(points[1]);
			Point2D anchorPoint = null;
			Point2D floatPoint = null;
			if (location.distance(points[0]) < span / 2.0) {
				anchorPoint = points[1];
				floatPoint = points[0];
			} else if (location.distance(points[1]) < span / 2.0) {
				anchorPoint = points[0];
				floatPoint = points[1];
			} else {
				System.out.println("points are not close enough NPE sure to follow");
			}

			// find shape at point
			// find closest point from shape
			// translate free end
			Shape[] shapes = Geometry.findShapesAtPoint(floatPoint, Geometry.CLOSE_ENOUGH);
			if (shapes != null) {
				Point2D[] closePoints = null;
				for (int i = 0; i < shapes.length; i++) {
					if (selectedShapes.get(0) != shapes[i]) {
						// only need one of the shapes for the endpoint
						closePoints = Geometry.getClosePoints(selectedShapes.get(0), shapes[i]);
						break;
					}
				}
				if (closePoints != null) {
					floatPoint.setLocation(closePoints[1]);
					Geometry.alterShapePoints(selectedShapes.get(0), points);
				}
			}
			
		}
		
	}
	
	public Point calcSelectedShapesCentroid() {
		if (selectedShapes.size() == 0) {
			return new Point(0, 0);
		}
		
		// see if a single shape is selected
		// if on another end point, rotate about that point
		if (selectedShapes.size() == 1) {
			Point2D common = null;
			for (Branch branch : SceneryManager.getInstance().getBranches()) {
				for (Shape shape : branch.getShapes()) {
					common = Geometry.getCommonPoint(selectedShapes.get(0), shape);
					if (common != null) {
						return new Point((int) common.getX(), (int) common.getY());
					}
				}
			}
		}
		
		// calculate center of the group
		double centerX = 0.0; 
		double centerY = 0.0;
		double i = 0.0;
		for (Shape shape : selectedShapes) {
			double cx, cy;
			if (shape instanceof Line2D) {
				Line2D line = (Line2D) shape;
				// center of line is average of endpoints
				cx = (line.getX1() + line.getX2()) / 2.0;  
				cy = (line.getY1() + line.getY2()) / 2.0;  
			} else if (shape instanceof Arc2D) {
				Arc2D arc = (Arc2D) shape;
				cx = arc.getCenterX();
				cy = arc.getCenterY();
			} else {
				cx = cy = 0.0;
			}

			// calculate running average of each coordinate
			// avg = (avg * ((double)i/(i+1))) + (series[i] * ((double)1/(i+1)));
			centerX = (centerX * (i/(i+1.0))) + (cx * (1/(i+1.0)));
			centerY = (centerY * (i/(i+1.0))) + (cy * (1/(i+1.0)));
			i = i + 1.0;
		}
		return new Point((int) centerX, (int) centerY);
	}
	
	public Point scalePoint(Point2D point) {
		return new Point((int)(point.getX() / canvas.scale), 
				(int)(point.getY() / canvas.scale));
	}
	
	public class EditMouseAdapter extends MouseAdapter {
		Point lastPoint, centerPoint;
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (mode == Mode.MOVE) { 
				moveSelectedShapes(lastPoint, scalePoint(e.getPoint()));
		        lastPoint = scalePoint(e.getPoint());
		        canvas.repaint();
			} else if (mode == Mode.ROTATE) {
		        rotateSelectedShapes(lastPoint, scalePoint(e.getPoint()), centerPoint);
		        lastPoint = scalePoint(e.getPoint());
		        canvas.repaint();
			} else if (mode == Mode.SIZE) {
				sizeSelectedShapes(lastPoint, scalePoint(e.getPoint()));
				lastPoint = scalePoint(e.getPoint());
				canvas.repaint();
			} else if (mode == Mode.ADD) {
				Point pointOnCanvas = new Point((int)(e.getLocationOnScreen().getX() - canvas.getLocationOnScreen().getX()),
						(int)(e.getLocationOnScreen().getY() - canvas.getLocationOnScreen().getY()));
				Point pt = scalePoint(pointOnCanvas);

	        	Rectangle rect = new Rectangle(pointOnCanvas);
	        	for (int i = 0; i < canvas.dragShape.length; i++) {
		        	rect.add(canvas.dragShape[i].getBounds());
		        	Geometry.translate(canvas.dragShape[i], pt.getX() - lastPoint.getX(), pt.getY() - lastPoint.getY());
		        	rect.add(canvas.dragShape[i].getBounds());
	        	}
	        	rect.grow(10, 10);
	        	rect.setLocation((int) (pointOnCanvas.getX() - rect.getWidth() / 2.0), 
	        			(int) (pointOnCanvas.getY() - rect.getHeight() / 2.0));
	        	canvas.repaint(rect);
	        	//((Graphics2D) canvas.getGraphics()).draw(rect);
	        	lastPoint = pt;
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (mode == Mode.INSPECT) {
				// just looking at point values if any under mouse
				Point pt = scalePoint(e.getPoint());
		        List<Point2D> pointList = new ArrayList<Point2D>();
				for (Branch branch : SceneryManager.getInstance().getBranches()) {
		        	for (Shape shape : branch.getShapes()) {
		        		Point2D[] points = Geometry.getPointsFromShape(shape);
		        		for (int i = 0; i < 2; i++) {
		        			if (pt.distance(points[i]) < Geometry.CLOSE_ENOUGH) {
		        				pointList.add(points[i]);
		        			}
		        		}
		        	}
		        }
				if (pointList.size() > 0) {
					for (Point2D point : pointList) {
						System.out.println("Point: " + point.getX() + ", " + point.getY());
					}
					System.out.println();
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getSource() != canvas) {
				mode = Mode.ADD;
				Point pointOnCanvas = new Point((int)(e.getLocationOnScreen().getX() - canvas.getLocationOnScreen().getX()),
						(int)(e.getLocationOnScreen().getY() - canvas.getLocationOnScreen().getY()));
		        lastPoint = scalePoint(pointOnCanvas);
		        Shape shape = ((ShapeLabel) e.getSource()).shape;
		        canvas.dragShape = new Shape[1];
		        if (shape instanceof Line2D) {
		        	canvas.dragShape[0] = (Line2D)((Line2D) shape).clone();
		        } else if (shape instanceof Arc2D) {
		        	canvas.dragShape[0] = (Arc2D)((Arc2D) shape).clone();
		        }
		        // shapes in library are created at origin, simple translate should suffice
        		Geometry.translate(canvas.dragShape[0], lastPoint.getX(), lastPoint.getY());
			} else if (mode == Mode.SELECT) {
				lastPoint = scalePoint(e.getPoint());
		        Shape[] shapes = Geometry.findShapesAtPoint(lastPoint, Geometry.CLOSE_ENOUGH);
		        if (shapes != null) {
		        	if (!selectedShapes.contains(shapes[0])) {
		        		selectedShapes.add(shapes[0]);
		        	} else {
		        		if (e.isControlDown()) {
		        			mode = Mode.ROTATE;
		    		        centerPoint = calcSelectedShapesCentroid();
		        		} else {
			        		Point2D endPoint = Geometry.findPointNearEndPoint(shapes[0], lastPoint);
			        		if (endPoint != null) {
			        			mode = Mode.SIZE;
			        		} else {
			        			mode = Mode.MOVE;
			        		}
		        		}
		        	}
		        } else {
		        	selectedShapes.clear();
		        }
		        canvas.repaint();
			} else if (mode == Mode.MOVE) {
		        lastPoint = scalePoint(e.getPoint());
			} else if (mode == Mode.ROTATE) {
		        lastPoint = scalePoint(e.getPoint());
		        centerPoint = calcSelectedShapesCentroid();
			} else if (mode == Mode.SIZE) {
		        lastPoint = scalePoint(e.getPoint());
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (mode == Mode.MOVE) {
				// TODO mouse drag should have taken care of this (delta is always 0)
		        moveSelectedShapes(lastPoint, scalePoint(e.getPoint()));
		        snapSelectedShapes();
		        canvas.repaint();
			} else if (mode == Mode.ROTATE) {
				// TODO mouse drag should have taken care of this (delta is always 0)
		        rotateSelectedShapes(lastPoint, scalePoint(e.getPoint()), centerPoint);
		        snapSelectedShapes();
		        canvas.repaint();
			} else if (mode == Mode.SIZE) {
				snapSelectedPoint(scalePoint(e.getPoint()));
				canvas.repaint();
			} else if (mode == Mode.ADD) {
				if (canvas.dragShape.length == 1) {
					SceneryManager.getInstance().addShapeToOpenPoint(canvas.dragShape[0]);
				} else {
					SceneryManager.getInstance().addSwitchToOpenPoint(canvas.dragShape);
				}
	        	canvas.dragShape = null;
	        	canvas.repaint();
			}
			mode = Mode.SELECT;
		}
		
	}
	
	

	
	
	class ToTransferHandler extends TransferHandler {
		Icon image = null;
		
		@Override
		public Icon getVisualRepresentation(Transferable t) {
			if (image == null) {
				// TODO use transferable to find correct image
				java.net.URL imgURL = ToTransferHandler.class.getResource("images/Straignt80.png");
		        if (imgURL != null) {
		            image = new ImageIcon(imgURL);
		        } else {
		            System.err.println("Couldn't find file: images/Straignt80.png");
		        }
				
			}
			return image;
		}
		
		@Override
        public boolean canImport(TransferHandler.TransferSupport support) {
        	System.out.println("canImport " + System.currentTimeMillis());
			support.setDropAction(COPY);
        	return true;
        }
        
		@Override
        public boolean importData(TransferHandler.TransferSupport support) {
        	System.out.println("importData: " + support);
        	Point pt = support.getDropLocation().getDropPoint();
        	Shape shape = new Line2D.Double(pt.getX(), pt.getY(), pt.getX() + 100.0, pt.getY() + 100.0);
        	SceneryManager.getInstance().addShape(shape);
        	canvas.repaint();
        	return true;
        }
	}
	
	class DropTargetHandler extends DropTargetAdapter {
		Point lastPoint;
		
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			super.dragEnter(dtde);
			lastPoint = scalePoint(dtde.getLocation());
        	String objectType = null;
			try {
				objectType = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if ("Straight".equals(objectType)) {
        		canvas.dragShape = new Shape[1];
        		canvas.dragShape[0] = new Line2D.Double(lastPoint.getX() - 28.14582562299426, lastPoint.getY(), lastPoint.getX() + 28.14582562299426, lastPoint.getY());
        	} else if ("Half-Straight".equals(objectType)) {
        		canvas.dragShape = new Shape[1];
        		canvas.dragShape[0] = new Line2D.Double(lastPoint.getX() - 14.07291281149713, lastPoint.getY(), lastPoint.getX() + 14.07291281149713, lastPoint.getY());
        	} else if ("Curve".equals(objectType)) {
        		canvas.dragShape = new Shape[1];
        		canvas.dragShape[0] = new Arc2D.Double(lastPoint.getX() - 80.0, lastPoint.getY() - 20.0, 97.5, 97.5, 30.0, 30.0, Arc2D.OPEN);
        	} else if ("Half-Curve".equals(objectType)) {
        		canvas.dragShape = new Shape[1];
        		canvas.dragShape[0] = new Arc2D.Double(lastPoint.getX() - 80.0, lastPoint.getY() - 20.0, 97.5, 97.5, 30.0, 15.0, Arc2D.OPEN);
        	} else if ("Left-Switch".equals(objectType)) {
        		canvas.dragShape = new Shape[2];
        		canvas.dragShape[0] = new Arc2D.Double(0.0, 0.0, 97.5, 97.5, 0.0, 15.0, Arc2D.OPEN);
        		canvas.dragShape[1] = new Line2D.Double(97.5, 48.75, 97.5, 48.75 - 28.14582562299426);
        		Geometry.translate(canvas.dragShape[0], lastPoint.getX() - 97.5, lastPoint.getY() - 48.75);
        		Geometry.translate(canvas.dragShape[1], lastPoint.getX() - 97.5, lastPoint.getY() - 48.75);
        	} else if ("Right-Switch".equals(objectType)) {
        		canvas.dragShape = new Shape[2];
        		canvas.dragShape[0] = new Arc2D.Double(0.0, 0.0, 97.5, 97.5, 165.0, 15.0, Arc2D.OPEN);
        		canvas.dragShape[1] = new Line2D.Double(0.0, 48.75, 0.0, 48.75 - 28.14582562299426);
        		Geometry.translate(canvas.dragShape[0], lastPoint.getX(), lastPoint.getY() - 48.75);
        		Geometry.translate(canvas.dragShape[1], lastPoint.getX(), lastPoint.getY() - 48.75);
        	} else if ("Junction".equals(objectType)) {
        		canvas.dragShape = new Shape[1];
        		canvas.dragShape[0] = new Ellipse2D.Double(lastPoint.getX() - 5.0, lastPoint.getY() - 5.0, 10.0, 10.0);
        	} else {
        		System.out.println("Unknown object type: " + objectType);
        	}
		}
		
		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			//System.out.println("dragOver " + System.currentTimeMillis());
			Point pt = scalePoint(dtde.getLocation());

        	Rectangle rect = new Rectangle(dtde.getLocation());
        	for (int i = 0; i < canvas.dragShape.length; i++) {
	        	rect.add(canvas.dragShape[i].getBounds());
	        	Geometry.translate(canvas.dragShape[i], pt.getX() - lastPoint.getX(), pt.getY() - lastPoint.getY());
	        	rect.add(canvas.dragShape[i].getBounds());
        	}
        	rect.grow(10, 10);
        	rect.setLocation((int) (dtde.getLocation().getX() - rect.getWidth() / 2.0), 
        			(int) (dtde.getLocation().getY() - rect.getHeight() / 2.0));
        	canvas.repaint(rect);
        	//((Graphics2D) canvas.getGraphics()).draw(rect);
        	lastPoint = pt;
		}
		
		@Override
		public void drop(DropTargetDropEvent dtde) {
			String objectType = null;
			try {
				objectType = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Dropping " + objectType);
			if (canvas.dragShape.length == 1) {
				SceneryManager.getInstance().addShapeToOpenPoint(canvas.dragShape[0]);
			} else {
				SceneryManager.getInstance().addSwitchToOpenPoint(canvas.dragShape);
			}
        	canvas.dragShape = null;
        	canvas.repaint();
		}
		
		@Override
		public void dragExit(DropTargetEvent dte) {
			super.dragExit(dte);
			System.out.println("dragExit");
        	Rectangle rect = canvas.dragShape[0].getBounds();
        	for (int i = 1; i < canvas.dragShape.length; i++) {
        		rect.add(canvas.dragShape[i].getBounds());
        	}
        	canvas.dragShape = null;
        	rect.grow(10, 10);
        	canvas.repaint(rect);
		}
		
	}
	
	class ShapeLabel extends JPanel {
		Shape shape;
		
		public ShapeLabel(Shape shape) {
			super();
			this.shape = shape;
			//setPreferredSize(new Dimension(100, 100));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.addRenderingHints(new RenderingHints(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON));
			g2.scale(1.0, 1.0);
			AffineTransform saveTransform = g2.getTransform();
			g2.translate(getWidth()/2, getHeight()/2);
			g2.draw(shape);
			g2.setTransform(saveTransform);
		}
		
		@Override
		public Dimension getPreferredSize() {
			Rectangle2D rect = shape.getBounds2D();
			return rect.getBounds().getSize();
		}

	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame("Layout Editor Test");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				SceneryManager.getInstance().createTestScenery();
				LayoutEditPanel panel = new LayoutEditPanel();
				panel.setPreferredSize(new Dimension(1000, 600));
				f.add(panel);
				f.pack();
				f.setVisible(true);
			}
		});
	}

}
