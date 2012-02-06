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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.layoutdesigner.scenery.Junction.BranchInfo;
import net.sf.layoutdesigner.util.Geometry;


public class Branch implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger("TrainApp");

	public enum PowerDirection {FORWARD, BACKWARD, NEUTRAL};
	private List<Shape> shapes;
	private List<Junction> junctions;
	
	public Branch() {
		shapes = new ArrayList<Shape>();
	}
	
	public void addShape(Shape shape) {
		// make sure shapes are put in order: P1 of new shape connects to P2 of last shape
//		if (shapes.size() > 0) {
//			Point2D[] points = Geometry.getPointsFromShape(shape);
//			Point2D firstPoint = Geometry.getPointsFromShape(shapes.get(0))[0];
//			Point2D lastPoint = Geometry.getPointsFromShape(shapes.get(shapes.size() - 1))[1];
//			// reverse points if backwards
//			if (points[1].distance(firstPoint) < 0.01 || points[0].distance(lastPoint) < 0.01) {
//				if (shape instanceof Line2D) {
//					Line2D line = (Line2D) shape;
//					line.setLine(line.getP2(), line.getP1());
//				} else if (shape instanceof Arc2D) {
//					Arc2D arc = (Arc2D) shape;
//					arc.setAngleStart(arc.getEndPoint());
//					arc.setAngleExtent(-arc.getAngleExtent());
//				}
//				// TODO add to beginning or end of List
//			}
//		}
		shapes.add(shape);
	}
	
	public List<Shape> getShapes() {
		return shapes;
	}
	
	public Shape getShape(int shapeIndex) {
		return shapes.get(shapeIndex);
	}
	
	public int getShapeCount() {
		return shapes.size();
	}
	
	public void render(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		for (Shape shape : shapes) {
			g2.draw(shape);
			
			// decorate for debugging
			Point2D[] points = Geometry.getPointsFromShape(shape);
			for (int i = 0; i < 2; i++) {
				Ellipse2D ellipse = new Ellipse2D.Double(points[i].getX() - 1.5, points[i].getY() - 1.5, 3.0, 3.0);
				g2.fill(ellipse);
//				Line2D line = new Line2D.Double(points[i].getX() - 5.0, points[i].getY() - 5.0, points[i].getX() + 5.0, points[i].getY() + 5.0);
//				g2.draw(line);
//				line.setLine(points[i].getX() - 5.0, points[i].getY() + 5.0, points[i].getX() + 5.0, points[i].getY() - 5.0);
//				g2.draw(line);
			}
		}
	}
	
	/**
	 * Checks if a point is on a shape in this branch
	 * For efficiency, returns the Shape the point is on
	 * @param p2
	 * @return the Shape the point is on otherwise null
	 */
	public Shape testPointOnBranch(Point2D p2) {
		Shape result = null;
		for (Shape shape : shapes) {
			if (shape instanceof Line2D) {
				Line2D line = (Line2D) shape;
				if (line.ptLineDist(p2) < 10.0) {
					result = shape;
					break;
				}
			}
		}
		return result;
	}
	
	public Object[] getNextShapeAtPoint(Shape currentShape, Point2D endPoint) {
		for (Shape shape : shapes) {
			if (shape != currentShape) {
				Point2D[] points = Geometry.getPointsFromShape(shape);
				if (endPoint.distance(points[0]) < 0.01) {
					return new Object[] {shape, points[1]};
				} else if (endPoint.distance(points[1]) < 0.01) {
					return new Object[] {shape, points[0]};
				}
			}
		}
		return null;
	}
	
	public FindPointResults findNextPoint(Shape shape, Point2D currentPoint, double mag, PowerDirection powerDir) {
		FindPointResults results = new FindPointResults();
		
		if (shape instanceof Line2D) {
			Line2D line = (Line2D) shape;
			Point2D towardsPt, fromPt;
			// need to follow convention when creating the branch
			if (powerDir == PowerDirection.FORWARD) {
				towardsPt = line.getP2();
				fromPt = line.getP1();
			} else {
				towardsPt = line.getP1();
				fromPt = line.getP2();
			}
			
			results.direction = Geometry.calcDirection(fromPt.getX(), fromPt.getY(), towardsPt.getX(), towardsPt.getY());
			
			// rotate around currentPoint
			results.transform = AffineTransform.getRotateInstance(
					results.direction, 
					currentPoint.getX(),
					currentPoint.getY());
			
			// see if mag distance overruns line
			double distToEnd = towardsPt.distance(currentPoint);
			if (mag > distToEnd) {
				results.overrun = mag - distToEnd;
				results.newPt = new Point2D.Double(towardsPt.getX(), towardsPt.getY());
			} else {
				results.overrun = 0;
				// create point with simple transform by mag
				results.newPt = new Point2D.Double(currentPoint.getX() - mag + results.overrun, currentPoint.getY());
				results.transform.transform(results.newPt, results.newPt);
			}
			//logger.fine("overrun: " + results.overrun + ", distToEnd: " + distToEnd + ", mag: " + mag);
			
			results.displacement = new Point2D.Double(results.newPt.getX() - currentPoint.getX(), results.newPt.getY() - currentPoint.getY());
			
			System.out.println("towardsPt: (" + towardsPt.getX() + ", " + towardsPt.getY() + "), fromPt: (" + fromPt.getX() + ", " + fromPt.getY() + ")");
			System.out.println("currentPoint: (" + currentPoint.getX() + ", " + currentPoint.getY() + "), newPt: (" + results.newPt.getX() + ", " + results.newPt.getY() + "), overrun: " + results.overrun + ", displacement: " + results.displacement + ", direction: " + results.direction + "\n");
			
		} else if (shape instanceof Arc2D) {
			Arc2D arc = (Arc2D) shape;
			
			System.out.println("Center point: " + arc.getCenterX() + ", " + arc.getCenterY());
			System.out.println("Start Point: " + arc.getStartPoint().getX() + ", " + arc.getStartPoint().getY());
			System.out.println("Current Point: " + currentPoint.getX() + ", " + currentPoint.getY());

			
			// find the angle at the current point
			double currentAngle = Math.atan2(arc.getCenterY() - currentPoint.getY(), currentPoint.getX() - arc.getCenterX());

			// find the angle to displace by mag
			double radius = arc.getStartPoint().distance(arc.getCenterX(), arc.getCenterY());
			double angleDisplace = (double) mag / radius;
			double newAngle;
			if (arc.getAngleExtent() > 0) {
				newAngle = currentAngle + angleDisplace;
			} else {
				newAngle = currentAngle - angleDisplace;
			}
			System.out.println("current angle: " + currentAngle + ", new angle: " + newAngle);
			
			//System.out.println("start angle: " + arc.getAngleStart() + ", extent: " + arc.getAngleExtent() + ", end angle: " + (arc.getAngleStart() + arc.getAngleExtent()) + ", new angle: " + Math.toDegrees(newAngle));
			//System.out.println("start angle: " + Math.toRadians(arc.getAngleStart()) + ", extent: " + Math.toRadians(arc.getAngleExtent()) + ", end angle: " + Math.toRadians(arc.getAngleStart() + arc.getAngleExtent()) + ", new angle: " + newAngle);
			
			System.out.println("newAngle inside: " + arc.containsAngle(Math.toDegrees(newAngle)));

			// calculate overrun
			if (!arc.containsAngle(Math.toDegrees(newAngle))) {
				double startAngle = Math.atan2(arc.getCenterY() - arc.getStartPoint().getY(), arc.getStartPoint().getX() - arc.getCenterX());
				double endAngle = startAngle + Math.toRadians(arc.getAngleExtent());
				endAngle = Geometry.normalizeRadians(endAngle);
				double extraAngle = Math.abs(newAngle - endAngle);
				results.overrun = extraAngle * radius;
				// recalculate new angle, it is the end angle
				newAngle = endAngle;
				System.out.println("end of arc - overrun: " + results.overrun + ", extra angle:" + extraAngle + ", start angle: " + startAngle + ", end angle: " + endAngle + ", extent: " + Math.toRadians(arc.getAngleExtent()) + ", arc.startAngle: " + Math.toRadians(arc.getAngleStart()));
			}

			// find the result point
			double newX = arc.getCenterX() + Math.cos(newAngle) * radius;
			double newY = arc.getCenterY() - Math.sin(newAngle) * radius;
			
			results.newPt = new Point2D.Double(newX, newY);
			if (arc.getAngleExtent() > 0) {
				results.direction = -newAngle + (Math.PI / 2.0);
			} else {
				results.direction = -newAngle - (Math.PI / 2.0);
			}
			System.out.println();
		}
		
		return results;
	}
	
	public FindPointResults findNextPoint(Shape shape, Point2D currentPoint, double distance, Point2D pointTowards) {
		FindPointResults results = new FindPointResults();
		
		if (shape instanceof Line2D) {
			Line2D line = (Line2D) shape;
			Point2D pointFrom = pointTowards.equals(line.getP2()) ? line.getP1() : line.getP2();
			
			results.direction = Geometry.calcDirection(pointFrom.getX(), pointFrom.getY(), pointTowards.getX(), pointTowards.getY());
			
			// rotate around currentPoint
			results.transform = AffineTransform.getRotateInstance(
					results.direction, 
					currentPoint.getX(),
					currentPoint.getY());
			
			// see if distance overruns line
			double distToEnd = pointTowards.distance(currentPoint);
			if (distance > distToEnd) {
				results.overrun = distance - distToEnd;
				results.newPt = new Point2D.Double(pointTowards.getX(), pointTowards.getY());
			} else {
				results.overrun = 0;
				// create point with simple transform by mag
				results.newPt = new Point2D.Double(currentPoint.getX() - distance + results.overrun, currentPoint.getY());
				results.transform.transform(results.newPt, results.newPt);
			}
			//logger.fine("overrun: " + results.overrun + ", distToEnd: " + distToEnd + ", mag: " + mag);
			
			results.displacement = new Point2D.Double(results.newPt.getX() - currentPoint.getX(), results.newPt.getY() - currentPoint.getY());
			
			//System.out.println("towardsPt: (" + pointTowards.getX() + ", " + pointTowards.getY() + "), fromPt: (" + pointFrom.getX() + ", " + pointFrom.getY() + ")");
			//System.out.println("currentPoint: (" + currentPoint.getX() + ", " + currentPoint.getY() + "), newPt: (" + results.newPt.getX() + ", " + results.newPt.getY() + "), overrun: " + results.overrun + ", displacement: " + results.displacement + ", direction: " + results.direction + "\n");
			
		} else if (shape instanceof Arc2D) {
			Arc2D arc = (Arc2D) shape;
			
			//System.out.println("Center point: " + arc.getCenterX() + ", " + arc.getCenterY());
			//System.out.println("Start Point: " + arc.getStartPoint().getX() + ", " + arc.getStartPoint().getY());
			//System.out.println("Current Point: " + currentPoint.getX() + ", " + currentPoint.getY());

			
			// find the angle at the current point
			double currentAngle = Math.atan2(arc.getCenterY() - currentPoint.getY(), currentPoint.getX() - arc.getCenterX());
			//double towardsAngle = Math.atan2(arc.getCenterY() - pointTowards.getY(), pointTowards.getX() - arc.getCenterX());
			Point2D pointFrom = pointTowards.equals(arc.getStartPoint()) ? arc.getEndPoint() : arc.getStartPoint();
			double crossProduct = (pointFrom.getX() - arc.getCenterX()) * (pointTowards.getY() - arc.getCenterY()) -
				(pointFrom.getY() - arc.getCenterY()) * (pointTowards.getX() - arc.getCenterX());

			boolean headingRight = pointTowards.distance(arc.getStartPoint()) < Geometry.POINT_ERROR;
			
			// find the angle to displace by distance
			double radius = arc.getWidth() / 2.0;
			double angleDisplace = distance / radius;
			double newAngle;
			if (headingRight) {
				newAngle = currentAngle - angleDisplace;
			} else {
				newAngle = currentAngle + angleDisplace;
			}
			//System.out.println("current angle: " + Math.toDegrees(currentAngle) + ", new angle: " + Math.toDegrees(newAngle) + ", arc.startAngle: " + arc.getAngleStart() + ", arc.endAngle: " + (arc.getAngleStart() + arc.getAngleExtent()) + ", cross: " + crossProduct);
			
			//System.out.println("start angle: " + arc.getAngleStart() + ", extent: " + arc.getAngleExtent() + ", end angle: " + (arc.getAngleStart() + arc.getAngleExtent()) + ", new angle: " + Math.toDegrees(newAngle));
			//System.out.println("start angle: " + Math.toRadians(arc.getAngleStart()) + ", extent: " + Math.toRadians(arc.getAngleExtent()) + ", end angle: " + Math.toRadians(arc.getAngleStart() + arc.getAngleExtent()) + ", new angle: " + newAngle);
			
			//System.out.println("newAngle inside: " + arc.containsAngle(Math.toDegrees(newAngle)));

			// calculate overrun
			if (!arc.containsAngle(Math.toDegrees(newAngle))) {
				double startAngle = Math.atan2(arc.getCenterY() - arc.getStartPoint().getY(), arc.getStartPoint().getX() - arc.getCenterX());
				double endAngle = Math.atan2(arc.getCenterY() - pointTowards.getY(), pointTowards.getX() - arc.getCenterX());
				endAngle = Geometry.normalizeRadians(endAngle);
				double extraAngle = Math.abs(newAngle - endAngle);
				results.overrun = extraAngle * radius;
				// recalculate new angle, it is the end angle
				newAngle = endAngle;
				results.newPt = new Point2D.Double(pointTowards.getX(), pointTowards.getY());
				//System.out.println("end of arc - overrun: " + results.overrun + ", extra angle:" + extraAngle + ", start angle: " + startAngle + ", end angle: " + endAngle + ", extent: " + Math.toRadians(arc.getAngleExtent()) + ", arc.startAngle: " + Math.toRadians(arc.getAngleStart()));
			} else {
				// find the result point
				double newX = arc.getCenterX() + Math.cos(newAngle) * radius;
				double newY = arc.getCenterY() - Math.sin(newAngle) * radius;
				
				results.newPt = new Point2D.Double(newX, newY);
			}

			if (headingRight) {
				results.direction = -newAngle - (Math.PI / 2.0);
			} else {
				results.direction = -newAngle + (Math.PI / 2.0);
			}
			//System.out.println();
		}
		
		
		
		return results;
	}
	
	public List<Shape> findShapesAtPoint(Point2D pt, double howClose) {
		List<Shape> shapeList = new ArrayList<Shape>();
		for (Shape shape : shapes) {
			if (shape instanceof Line2D) {
				double dist = ((Line2D)shape).ptSegDist(pt.getX(), pt.getY());
				if (dist < howClose) {
					shapeList.add(shape);
				}
			} else if (shape instanceof Arc2D) {
				Arc2D arc = (Arc2D) shape;
				double direction = Math.toDegrees(Geometry.calcDirection(pt.getX(), -pt.getY(), arc.getCenterX(), -arc.getCenterY()));
				if (arc.containsAngle(direction) || 
						arc.getStartPoint().distance(pt) < howClose || 
						arc.getEndPoint().distance(pt) < howClose) {
					double radius = arc.getStartPoint().distance(arc.getCenterX(), arc.getCenterY());
					double distance = pt.distance(arc.getCenterX(), arc.getCenterY());
					if (distance > radius - howClose && distance < radius + howClose) {
						shapeList.add(shape);
					}
				}
			}
		}

		return shapeList;
	}

	public class FindPointResults {
		public Point2D newPt;
		public AffineTransform transform;
		public double direction;
		public double overrun;
		public Point2D displacement;
	}

	
}
