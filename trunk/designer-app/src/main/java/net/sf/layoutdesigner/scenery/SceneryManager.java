/*
 * $Id$
 * 
 * Copyright � 2010 Jeff Eltgroth.
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
package net.sf.layoutdesigner.scenery;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import net.sf.layoutdesigner.file.XMLReader;
import net.sf.layoutdesigner.file.XMLWriter;
import net.sf.layoutdesigner.util.Geometry;


public final class SceneryManager {
	private static Logger logger = Logger.getLogger("TrainApp");
	private static final SceneryManager instance = new SceneryManager();
	
	private List<Train> trains;
	private List<Branch> branches;
	private List<Junction> junctions;
	private PropertyChangeSupport propertyChangeSupport;
	private int width;
	private int height;
	private Map<String, Image> trainImages;
	
	private SceneryManager() {
		trains = new ArrayList<Train>();
		branches = new ArrayList<Branch>();
		junctions = new ArrayList<Junction>();
		trainImages = new HashMap<String, Image>();
		loadTrainImages();
		propertyChangeSupport = new PropertyChangeSupport(this);
		width = 1000;
		height = 1000;
	}
	
	public static SceneryManager getInstance() {
		return instance;
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public Image getTrainImage(String key) {
		return trainImages.get(key);
	}

	public void renderScenery(Graphics2D g2) {
		for (Branch branch : branches) {
			branch.render(g2);
		}
		for (Junction junction : junctions) {
			junction.render(g2);
		}
		for (Train train : trains) {
			train.render(g2);
		}
	}
	
	public void updateScenery() {
		for (Train train : trains) {
			train.update();
		}
	}
	
	public List<Junction> getJunctions() {
		return junctions;
	}
	
	public List<Branch> getBranches() {
		return branches;
	}
	
	public List<Train> getTrains() {
		return trains;
	}
	
	public void addShapeToOpenPoint(Shape shape) {
		Point2D[] points = Geometry.getPointsFromShape(shape);
		Point2D[] openPoints = findOpenPoints();
		int pointIndex = -1;
		int openPointIndex = -1;
		
		search_points:
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < openPoints.length; j++) {
				if (points[i].distance(openPoints[j]) < Geometry.CLOSE_ENOUGH) {
					pointIndex = i;
					openPointIndex = j;
					break search_points;
				}
			}
		}
		
		if (pointIndex >= 0 && openPointIndex >= 0) {
			// find existing branch and shape
			Shape existingShape = null;
			Branch existingBranch = null;
			for (Branch branch : branches) {
				Shape[] shapes = Geometry.findShapesAtPoint(openPoints[openPointIndex], branch, Geometry.POINT_ERROR);
				if (shapes != null) {
					// TODO might want to look for closest shape
					existingShape = shapes[0]; 
				}
				if (existingShape != null) {
					existingBranch = branch;
					break;
				}
			}
			assert(existingBranch != null);
			AffineTransform at = Geometry.getSnapToTransform(existingShape, shape, openPoints[openPointIndex], points[pointIndex]);
			Geometry.transform(shape, at);
			existingBranch.addShape(shape);
		} else {
			System.out.println("No open point, adding in place");
			addShape(shape);
		}
		
	}
	
	public void addShape(Shape shape) {
		Point2D[] points = Geometry.getPointsFromShape(shape);
		int pointIndex = 0;
		
		Shape existingShape = null;
		Branch branch = null;
		Branch existingBranch = null;
		// see if new shape is near an existing branch
		search_branches:
		for (Iterator<Branch> iter = branches.iterator(); iter.hasNext(); ) {
			branch = iter.next();
			for (int i = 0; i < 2; i++) {
				// look for a shape at one of the end points of the new shape
				Shape[] shapes = Geometry.findShapesAtPoint(points[i], branch, Geometry.CLOSE_ENOUGH);
				if (shapes != null) {
					// TODO might want to look for closest shape
					existingShape = shapes[0]; 
				}
				if (existingShape != null) {
					pointIndex = i;
					existingBranch = branch;
					break search_branches;
				}
			}
		}
		
		if (existingShape == null) {
			System.out.println("Creating new branch - no points close");
			branch = new Branch();
			branches.add(branch);
		} else {
			AffineTransform at = Geometry.getSnapToTransform(existingShape, shape);
			if (at != null) {
				Geometry.transform(shape, at);
				// TODO see if shape will be at end of branch or if need to create a replacement branch and a junction
				// called from addShapeToOpenPoint, so this is likely a junction
				Point2D common = Geometry.getCommonPoint(shape, existingShape);
				addJunction(branch, shape, Geometry.getCommonPoint(shape, existingShape));
			} else {
				// TODO maybe should not drop between points
				
				// new shape is dropped between end points
				System.out.println("Creating new branch - splitting shape and branch");
				branch = new Branch();
				branches.add(branch);
				
				// need to split existing shape and branch
				Point2D splitPoint = Geometry.findPointOnShapeNearPoint(existingShape, points[pointIndex]);
				// we fell through because the end points were not close, so the shape should split
				Shape[] splitShapes = Geometry.splitShapeAtPoint(existingShape, splitPoint);
				// go down branch in one direction to remove shapes and add to replacement branch
				// splitShapes[0] contains P1, splitShapes[1] contains P2
				if (splitShapes.length > 1 && splitShapes[1] != null) {
					int existingShapeIndex = existingBranch.getShapes().indexOf(existingShape);
					Branch replacementBranch = new Branch();
					replacementBranch.addShape(splitShapes[1]);
					for (int i = existingShapeIndex + 1; existingShapeIndex < existingBranch.getShapeCount() - 1; i++) {
						replacementBranch.addShape(existingBranch.getShapes().remove(existingShapeIndex + 1));
					}
					// remove existing shape and replace with split portion
					existingBranch.getShapes().remove(existingShapeIndex);
					existingBranch.addShape(splitShapes[0]);
					branches.add(replacementBranch);
				}
				// TODO create intersection
				// TODO straight on straight needs a curve at junction
			}
		}
		
		branch.addShape(shape);
	}
	
	public void addJunction(Branch branch, Shape shape, Point2D point) {
		Junction junction = getJunctionAtPoint(point);
		if (junction == null) {
			junction = new Junction(point);
			boolean firstBranch = true;
			for (Branch b : branches) {
				Shape[] shapes = Geometry.findShapesAtPoint(point, b, Geometry.POINT_ERROR);
				if (shapes != null) {
					for (int i = 0; i < shapes.length; i++) {
						junction.addBranch(b, shapes[i], firstBranch);
						firstBranch = false;
					}
				}
			}
			junctions.add(junction);
			propertyChangeSupport.firePropertyChange("junctions", null, junction);
		} else {
			junction.addBranch(branch, shape, false);
		}
	}

	public void addJunction(Point2D point) {
		// see if junction exists
		Junction junction = getJunctionAtPoint(point);

		if (junction == null) {
			junction = new Junction(point);
			junctions.add(junction);
		} else {
			junction.updateBranches();
		}
		
		propertyChangeSupport.firePropertyChange("junctions", null, junction);
		
		//System.out.println("Junction: " + junction);
		
	}
	
	public void addSwitchToOpenPoint(Shape[] shapes) {
		// now have to deal with more than two points
		Point2D[] openPoints = findOpenPoints();
		Point2D[] points = null;
		int pointIndex = -1;
		int openPointIndex = -1;
		int shapeIndex = -1;
		
		search_points:
		for (int k = 0; k < shapes.length; k++) {
			points = Geometry.getPointsFromShape(shapes[k]);
			pointIndex = openPointIndex = -1;
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < openPoints.length; j++) {
					if (points[i].distance(openPoints[j]) < Geometry.CLOSE_ENOUGH) {
						pointIndex = i;
						openPointIndex = j;
						shapeIndex = k;
						break search_points;
					}
				}
			}
		}
		
		if (pointIndex >= 0 && openPointIndex >= 0) {
			// find existing branch and shape
			Shape existingShape = null;
			Branch existingBranch = null;
			for (Branch branch : branches) {
				Shape[] shapes2 = Geometry.findShapesAtPoint(openPoints[openPointIndex], branch, Geometry.POINT_ERROR);
				if (shapes2 != null) {
					existingShape = shapes2[0]; 
				}
				if (existingShape != null) {
					existingBranch = branch;
					break;
				}
			}
			assert(existingBranch != null);
			AffineTransform at = Geometry.getSnapToTransform(existingShape, shapes[shapeIndex], openPoints[openPointIndex], points[pointIndex]);
			for (int i = 0; i < shapes.length; i++) {
				Geometry.transform(shapes[i], at);
				// TODO add new branches
				existingBranch.addShape(shapes[i]);
			}
		} else {
			// totally wrong, but just add for now
			Branch branch = branches.get(0);
			for (int i = 0; i < shapes.length; i++) {
				branch.addShape(shapes[i]);
			}
		}
	}
	
	public Junction getJunctionAtPoint(Point2D point) {
		for (Junction junction : junctions) {
			if (point.distance(junction.getLocation()) < Geometry.POINT_ERROR) {
				return junction;
			}
		}		
		return null;
	}
	
	public void fixJunctions() {
		// all you need to be a junction is have a point with three or more shapes
		
		// clear junction list and search for triplets or better
		junctions.clear();

		// find all points and add to HashMap for histogram
		// because some rounding happens in Double, use Point integer precision for key
		Map<Point, Integer> pointMap = new HashMap<Point, Integer>();
		for (Branch branch : branches) {
			for (Shape shape : branch.getShapes()) {
				Point2D[] points = Geometry.getPointsFromShape(shape);
				for (int i = 0; i < 2; i++) {
					boolean foundPoint = false;
					for (Point point : pointMap.keySet()) {
						if (points[i].distance(point) < 2.0) {
							Integer count = pointMap.get(point);
							pointMap.put(point, new Integer(count.intValue() + 1));
							foundPoint = true;
							break;
						}
					}
					if (!foundPoint) {
						Point point = new Point((int) Math.round(points[i].getX()), 
								(int) Math.round(points[i].getY()));
						pointMap.put(point, new Integer(1));
					}
					
//					Point point = new Point((int) Math.round(points[i].getX()), (int) Math.round(points[i].getY()));
//					if (pointMap.containsKey(point)) {
//						Integer count = pointMap.get(point);
//						pointMap.put(point, new Integer(count.intValue() + 1));
//					} else {
//						pointMap.put(point, new Integer(1));
//					}
				}
			}
		}
		
		// look for any set of triplet points to create new junction
		for (Point point : pointMap.keySet()) {
			Integer count = pointMap.get(point);
			if (count.intValue() > 2) {
				Shape[] shapes = Geometry.findShapesAtPoint(point, 1.0);
				if (shapes != null && shapes.length > 1) {
					Point2D commonPoint = Geometry.getCommonPoint(shapes[0], shapes[1]);
					if (commonPoint != null) {
						// Junction constructor takes care of the rest
						addJunction(commonPoint);
					}
				}
			}
		}
	}
	
	public List<Point2D> findPointsAtPoint(Point2D point) {
        List<Point2D> pointList = new ArrayList<Point2D>();
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
        	for (Shape shape : branch.getShapes()) {
        		Point2D[] points = Geometry.getPointsFromShape(shape);
        		for (int i = 0; i < 2; i++) {
        			if (point.distance(points[i]) < Geometry.POINT_ERROR) {
        				pointList.add(points[i]);
        			}
        		}
        	}
        }
		return pointList;
	}
	
	public Point2D[] findOpenPoints() {
		List<Point2D> allPointsList = new ArrayList<Point2D>();
		// find all points in all branches and create histogram
		for (Branch branch : branches) {
			for (Shape shape : branch.getShapes()) {
				Point2D[] points = Geometry.getPointsFromShape(shape);
				allPointsList.add(points[0]);
				allPointsList.add(points[1]);
			}
		}
		
		// certainly not the most efficient 
		// but check each point for proximity to all other points
		List<Point2D> pointList = new ArrayList<Point2D>();
		for (Point2D point : allPointsList) {
			boolean foundClose = false;
			for (Point2D pt : allPointsList) {
				if (point != pt && point.distance(pt) < Geometry.POINT_ERROR) {
					foundClose = true;
					break;
				}
			}
			if (!foundClose) {
				pointList.add(point);
			}
		}
		
		// also add Junctions
		for (Junction junction : junctions) {
			pointList.add(junction.getLocation());
		}
		
		return pointList.toArray(new Point2D[pointList.size()]);
	}
	
	public Point2D[] findOpenPoints1() {
		Map<Point2D, Integer> pointMap = new HashMap<Point2D, Integer>();
		// find all points in all branches and create histogram
		for (Branch branch : branches) {
			for (Shape shape : branch.getShapes()) {
				Point2D[] points = Geometry.getPointsFromShape(shape);
				for (int i = 0; i < 2; i++) {
					Integer value;
					if (pointMap.containsKey(points[i])) {
						value = new Integer(pointMap.get(points[i]).intValue() + 1);
					} else {
						value = new Integer(1);
					}
					pointMap.put(points[i], value);
				}
			}
		}
		
		// look for points with histo count of 1
		List<Point2D> pointList = new ArrayList<Point2D>();
		for (Point2D point : pointMap.keySet()) {
			Integer value = pointMap.get(point);
			if (value.intValue() == 1) {
				pointList.add(point);
			}
		}
		
		return pointList.toArray(new Point2D[pointList.size()]);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
	
	public void loadTrainImages() {
		File folder = new File("scenery/trains");
		File[] imageFiles = folder.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".png") || 
					name.toLowerCase().endsWith(".gif");
			}});
		for (int i = 0; i < imageFiles.length; i++) {
			Image img = null;
			try {
				img = ImageIO.read(imageFiles[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String name = imageFiles[i].getName().substring(0, imageFiles[i].getName().lastIndexOf("."));
			trainImages.put(name, img);
		}
	}
	
	public void createTestScenery() {
		width = 7500;
		height = 7500;
		
		// track templates
		Arc2D curve = new Arc2D.Double(450.0, 120.0, 195.0, 195.0, 60.0, 30.0, Arc2D.OPEN);
		Line2D straight = new Line2D.Double(0.0, 100.0, 112.583302491977, 100.0);
		
		Branch branch = new Branch();
		branches.add(branch);
		
		// first one goes where we put it
		Shape shape = new Arc2D.Double(curve.getX(), curve.getY(), curve.getWidth(), curve.getHeight(), curve.getAngleStart(), curve.getAngleExtent(), curve.getArcType());
		branch.addShape(shape);
		
		// the rest have to move
		AffineTransform at = null;
		Shape newShape = null;
		
		// right side curves
		for (int i = 0; i < 6; i++) {
			newShape = new Arc2D.Double(curve.getX(), curve.getY(), curve.getWidth(), curve.getHeight(), curve.getAngleStart(), curve.getAngleExtent(), curve.getArcType());
			at = Geometry.getSnapToTransform(shape, newShape, Geometry.getPointsFromShape(shape)[0], Geometry.getPointsFromShape(newShape)[1]);
			Geometry.transform(newShape, at);
			branch.addShape(newShape);
			shape = newShape;
		}
		
		// right-to-left diagonal
		for (int i = 0; i < 3; i++) {
			newShape = new Line2D.Double(straight.getP1(), straight.getP2());
			at = Geometry.getSnapToTransform(shape, newShape, Geometry.getPointsFromShape(shape)[0], Geometry.getPointsFromShape(newShape)[1]);
			Geometry.transform(newShape, at);
			branch.addShape(newShape);
			shape = newShape;
		}
		
		// one to change direction
		newShape = new Arc2D.Double(curve.getX(), curve.getY(), curve.getWidth(), curve.getHeight(), curve.getAngleStart(), curve.getAngleExtent(), curve.getArcType());
		at = Geometry.getSnapToTransform(shape, newShape, Geometry.getPointsFromShape(shape)[0], Geometry.getPointsFromShape(newShape)[0]);
		Geometry.transform(newShape, at);
		branch.addShape(newShape);
		shape = newShape;

		// left side curves
		for (int i = 0; i < 7; i++) {
			newShape = new Arc2D.Double(curve.getX(), curve.getY(), curve.getWidth(), curve.getHeight(), curve.getAngleStart(), curve.getAngleExtent(), curve.getArcType());
			at = Geometry.getSnapToTransform(shape, newShape, Geometry.getPointsFromShape(shape)[1], Geometry.getPointsFromShape(newShape)[0]);
			Geometry.transform(newShape, at);
			branch.addShape(newShape);
			shape = newShape;
		}
		
		// left-to-right diagonal
		for (int i = 0; i < 3; i++) {
			newShape = new Line2D.Double(straight.getP1(), straight.getP2());
			at = Geometry.getSnapToTransform(shape, newShape, Geometry.getPointsFromShape(shape)[1], Geometry.getPointsFromShape(newShape)[0]);
			Geometry.transform(newShape, at);
			branch.addShape(newShape);
			shape = newShape;
		}

		// one last curve
		newShape = new Arc2D.Double(curve.getX(), curve.getY(), curve.getWidth(), curve.getHeight(), curve.getAngleStart(), curve.getAngleExtent(), curve.getArcType());
		at = Geometry.getSnapToTransform(shape, newShape, Geometry.getPointsFromShape(shape)[1], Geometry.getPointsFromShape(newShape)[1]);
		Geometry.transform(newShape, at);
		branch.addShape(newShape);

		
		// rolling stock
		RailCar car = new RailCar();
		car.setLoc(Geometry.getPointsFromShape(shape)[1]);
		car.setBranch(branch);
		car.setShape(shape);
		car.setImage("loco01");
		car.setPointTowards(Geometry.getPointsFromShape(shape)[0]);
		car.move(0.0);

		Train train = new Train();
		train.addCar(car);
		train.setSpeed(0);
		trains.add(train);

		car = new RailCar();
		car.setLoc(Geometry.getPointsFromShape(shape)[1]);
		car.setBranch(branch);
		car.setShape(shape);
		car.setImage("boxcar01");
		car.setPointTowards(Geometry.getPointsFromShape(shape)[0]);
		car.move(-70.0);
		train.addCar(car);

		Point2D commonPoint = Geometry.getCommonPoint(branch.getShape(0), branch.getShape(branch.getShapeCount() - 1));
		//addJunction(branch, branch.getShape(0), commonPoint);
		//addJunction(branch, branch.getShape(branch.getShapeCount() - 1), commonPoint);

		//Junction junction = new Junction(Geometry.getPointsFromShape(branch.getShape(0))[1]);
		//junctions.add(junction);
		// two ends of the same branch
		//junction.addBranch(branch, branch.getShape(0), false);
		//junction.addBranch(branch, branch.getShape(branch.getShapeCount() - 1), false);
		
		newShape = new Line2D.Double(straight.getP1(), straight.getP2());
		at = Geometry.getSnapToTransform(branch.getShape(0), newShape, Geometry.getPointsFromShape(branch.getShape(0))[1], Geometry.getPointsFromShape(newShape)[0]);
		Geometry.transform(newShape, at);

		branch = new Branch();
		branches.add(branch);
		branch.addShape(newShape);
		//junction.addBranch(branch, branch.getShape(0), true);
		addJunction(commonPoint);

		fixJunctions();
		propertyChangeSupport.firePropertyChange("layout", null, instance);
	}
	
	public void writeToFile(File file) {
		try {
//			FileOutputStream fos = new FileOutputStream(file);
//			ObjectOutputStream oos = new ObjectOutputStream(fos);
//			oos.writeObject(branches);
//			oos.writeObject(trains);
//			oos.writeObject(junctions);
//			oos.close();

			XMLWriter writer = new XMLWriter();
			writer.writeFile(file);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception writing scenery objects to " + file.getName(), e);
		}
	}
	
	public void readFromFile(File file) {
		try {
//			FileInputStream fis = new FileInputStream(file);
//			ObjectInputStream ois = new ObjectInputStream(fis);
//			branches = (List<Branch>) ois.readObject();
//			trains = (List<Train>) ois.readObject();
//			junctions = (List<Junction>) ois.readObject();
//			ois.close();

			branches.clear();
			trains.clear();
			junctions.clear();
			
			XMLReader reader = new XMLReader();
			reader.readFile(file);
			
			fixJunctions();
			propertyChangeSupport.firePropertyChange("layout", null, instance);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception reading scenery objects to " + file.getName(), e);
		}
		
	}
	
}
