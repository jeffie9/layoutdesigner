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
package net.sf.layoutdesigner.util;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import net.sf.layoutdesigner.scenery.Branch;
import net.sf.layoutdesigner.scenery.RailCar;
import net.sf.layoutdesigner.scenery.SceneryManager;


public final class Geometry {
	public static final double CLOSE_ENOUGH = 10.0;  // use for drop operations
	public static final double POINT_ERROR = 0.01;   // use to determine points equal
	
	public static double calcDirection(Point2D ptFrom, Point2D ptTo) {
		return calcDirection(ptFrom.getX(), ptFrom.getY(), ptTo.getX(), ptTo.getY());
	}

	public static double calcDirection(Point2D ptFrom, Point2D ptTo, Point2D ptCenter) {
		if (ptCenter == null) {
			return calcDirection(ptFrom.getX(), ptFrom.getY(), ptTo.getX(), ptTo.getY());
		}
		
		double directionToCenter = calcDirection(ptFrom.getX(), ptFrom.getY(), ptCenter.getX(), ptCenter.getY());
		double crossProduct = crossProductZ(ptFrom, ptTo, ptCenter);
		
		if (crossProduct > 0.0) {
			return directionToCenter - (Math.PI / 2.0);
		} else {
			return directionToCenter + (Math.PI / 2.0);
		}
	}
	
	public static double calcDirection(double x1, double y1, double x2, double y2) {
		double direction;
		
		double dx = x2 - x1;
		double dy = y2 - y1;
		if (dy == 0.0) {
			if (dx > 0) {
				direction = Math.PI;
			} else {
				direction = 0.0;
			}
		} else if (dx == 0.0) {
			if (dy > 0.0) {
				direction = -Math.PI / 2.0;
			} else {
				direction = Math.PI / 2.0;
			}
		} else {
			// need distance to normalize vector
			double dx2 = Math.pow(dx, 2.0); 
			double dy2 = Math.pow(dy, 2.0);
			double dist2 = dx2 + dy2;
			double dxu = Math.sqrt(dx2 / dist2) * (-dx / Math.abs(dx));
			double dyu = Math.sqrt(dy2 / dist2) * (-dy / Math.abs(dy));
			direction = Math.atan2(dyu, dxu);
		}
		return direction;
	}
	
	public static Point2D calcUnitVector(double dx, double dy) {
		Point2D unit;
		if (dy == 0.0) {
			if (dx > 0) {
				unit = new Point2D.Double(-1.0, 0.0);
			} else {
				unit = new Point2D.Double(1.0, 0.0);
			}
		} else if (dx == 0.0) {
			if (dy > 0.0) {
				unit = new Point2D.Double(0.0, -1.0);
			} else {
				unit = new Point2D.Double(0.0, 1.0);
			}
		} else {
			// need distance to normalize vector
			double dx2 = Math.pow(dx, 2.0); 
			double dy2 = Math.pow(dy, 2.0);
			double dist2 = dx2 + dy2;
			double dxu = Math.sqrt(dx2 / dist2) * (-dx / Math.abs(dx));
			double dyu = Math.sqrt(dy2 / dist2) * (-dy / Math.abs(dy));
			unit = new Point2D.Double(dxu, dyu);
		}
		return unit;
	}

	public static Point2D calcUnitVector(Point2D p1, Point2D p2) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		return calcUnitVector(dx, dy);
	}
	
	public static void translate(Shape shape, double dx, double dy) {
		AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
		transform(shape, at);
	}

	public static void rotate(Shape shape, double theta, double cx, double cy) {
		AffineTransform at = AffineTransform.getRotateInstance(theta, cx, cy);
		transform(shape, at);
	}
	
	public static void transform(Shape shape, AffineTransform at) {
		// at least Path2D has a transform
		if (shape instanceof RailCar) {
			RailCar car = (RailCar) shape;
			car.transform(at);
		} else {
			Point2D[] points = getPointsFromShape(shape);
			at.transform(points, 0, points, 0, points.length);
			alterShapePoints(shape, points);
		}
	}
	
	public static void alterShapePoints(Shape shape, Point2D[] points) {
		if (shape instanceof Line2D) {
			Line2D line = (Line2D) shape;
			line.setLine(points[0], points[1]);
		} else if (shape instanceof Arc2D) {
			// only works for circular arcs (not all ellipses will work as coded)
			Arc2D arc = (Arc2D) shape;
			// width and height are not reliable after scale transformation
			double radius = points[0].distance(points[2]);
			//arc.setArc(points[2].getX() - arc.getWidth()/2.0, points[2].getY() - arc.getHeight()/2.0, arc.getWidth(), arc.getHeight(), arc.getAngleStart(), arc.getAngleExtent(), arc.getArcType());
			arc.setArc(points[2].getX() - radius, points[2].getY() - radius, radius * 2.0, radius * 2.0, arc.getAngleStart(), arc.getAngleExtent(), arc.getArcType());
			// needs reset after rotate transform
			arc.setAngleStart(points[0]);
		} else if (shape instanceof Ellipse2D) {
			Ellipse2D ell = (Ellipse2D) shape;
			ell.setFrame(points[0].getX(), points[0].getY(), ell.getWidth(), ell.getHeight());
		}		
	}
	
	public static Shape[] findShapesAtPoint(Point2D pt, double howClose) {
		// iterate through shapes in all branches
		List<Shape> shapeList = new ArrayList<Shape>();
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			Shape[] shapes = findShapesAtPoint(pt, branch, howClose);
			if (shapes != null) {
				for (int i = 0; i < shapes.length; i++) {
					shapeList.add(shapes[i]);
				}
			}
		}
		if (shapeList.size() > 0) {
			return shapeList.toArray(new Shape[shapeList.size()]);
		}
		return null;
	}
	

	public static Shape[] findShapesAtPoint(Point2D pt, Branch branch, double howClose) {
		List<Shape> shapes = new ArrayList<Shape>();
		for (Shape shape : branch.getShapes()) {
			if (shape instanceof Line2D) {
				double dist = ((Line2D)shape).ptSegDist(pt.getX(), pt.getY());
				if (dist < howClose) {
					shapes.add(shape);
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
						shapes.add(shape);
					}
				}
			}
		}
		if (shapes.size() > 0) {
			return shapes.toArray(new Shape[shapes.size()]);
		}
		return null;
	}
	
	public static Point2D[] getPointsFromShape(Shape shape) {
		Point2D[] points = null;
		if (shape instanceof Line2D) {
			points = new Point2D[2];
			Line2D line = (Line2D) shape;
			points[0] = line.getP1();
			points[1] = line.getP2();
		} else if (shape instanceof Arc2D) {
			points = new Point2D[3];
			Arc2D arc = (Arc2D) shape;
			points[0] = arc.getStartPoint();
			points[1] = arc.getEndPoint();
			points[2] = new Point2D.Double(arc.getCenterX(), arc.getCenterY());
		} else if (shape instanceof Ellipse2D) {
			points = new Point2D[1];
			Ellipse2D ell = (Ellipse2D) shape;
			points[0] = new Point2D.Double(ell.getX(), ell.getY());
		}
		return points;
	}
	
	public static AffineTransform getSnapToTransform(Shape anchorShape, Shape floatShape) {
		AffineTransform at = null;
		Point2D[] anchorPoints = Geometry.getPointsFromShape(anchorShape);
		Point2D[] floatPoints = Geometry.getPointsFromShape(floatShape);
		int i, j = 0;
		boolean pointsClose = false;
		// see if new shape dropped near existing end point
		search_points:
		for (i = 0; i < 2; i++) {
			for (j = 0; j < 2; j++) {
				if (floatPoints[i].distance(anchorPoints[j]) < CLOSE_ENOUGH) {
					pointsClose = true;
					break search_points;
				}
			}
		}
		if (pointsClose) {
			// keep our sanity: create a new reference for each point
			// i and j set in loops above
			Point2D anchorNear, anchorFar, floatNear, floatFar;
			if (j ==0) {
				anchorNear = anchorPoints[0];
				anchorFar = anchorPoints[1];
			} else {
				anchorNear = anchorPoints[1];
				anchorFar = anchorPoints[0];
			}
			if (i == 0) {
				floatNear = floatPoints[0];
				floatFar = floatPoints[1];
			} else {
				floatNear = floatPoints[1];
				floatFar = floatPoints[0];
			}

			// find directions to rotate to be aligned with existing shape
			// TODO could probably be done with vector addition instead
			double anchorDirection = 0.0;
			// three points means an arc
			if (anchorPoints.length == 3) {
				anchorDirection = calcDirection(anchorPoints[2], anchorNear) +
					getTangentModifier(anchorNear, anchorFar, anchorPoints[2]);
			} else {
				anchorDirection = calcDirection(anchorFar, anchorNear);
			}
			double floatDirection = 0.0;
			if (floatPoints.length == 3) {
				floatDirection = calcDirection(floatPoints[2], floatNear) +
					getTangentModifier(floatFar, floatNear, floatPoints[2]);
			} else {
				floatDirection = calcDirection(floatNear, floatFar);
			}
			double theta = anchorDirection - floatDirection;
			
			// move new shape to join existing end point
			// remember last transformation is first applied
			at = new AffineTransform();
			at.translate(anchorNear.getX(), anchorNear.getY());  // final move to anchor point
			at.rotate(theta);                                    // rotate about origin
			at.translate(-floatNear.getX(), -floatNear.getY());  // move to origin
			
		}
		
		return at;
	}
	
	public static AffineTransform getSnapToTransform(Shape anchorShape, Shape floatShape, Point2D anchorPoint, Point2D floatPoint) {
		Point2D[] anchorPoints = Geometry.getPointsFromShape(anchorShape);
		Point2D[] floatPoints = Geometry.getPointsFromShape(floatShape);
		Point2D otherAnchor = (anchorPoint.equals(anchorPoints[0]) ? anchorPoints[1] : anchorPoints[0]);
		Point2D otherFloat = (floatPoint.equals(floatPoints[0]) ? floatPoints[1] : floatPoints[0]);
		
		double anchorDirection = 0.0;
		// three points means an arc
		if (anchorPoints.length == 3) {
			anchorDirection = calcDirection(anchorPoints[2], anchorPoint) +
				getTangentModifier(anchorPoint, otherAnchor, anchorPoints[2]);
		} else {
			anchorDirection = calcDirection(otherAnchor, anchorPoint);
		}
		double floatDirection = 0.0;
		if (floatPoints.length == 3) {
			floatDirection = calcDirection(floatPoints[2], floatPoint) +
				getTangentModifier(otherFloat, floatPoint, floatPoints[2]);
		} else {
			floatDirection = calcDirection(floatPoint, otherFloat);
		}
		double theta = anchorDirection - floatDirection;

		AffineTransform at = new AffineTransform();
		at.translate(anchorPoint.getX(), anchorPoint.getY());  // final move to anchor point
		at.rotate(theta);                                      // rotate about origin
		at.translate(-floatPoint.getX(), -floatPoint.getY());  // move to origin
		return at;
	}
	
	/**
	 * Only works for this application because all arcs are circular (constant radius)
	 * @param arc
	 * @param atPoint
	 * @param toPoint
	 * @return the direction of the tangent at the point in radians
	 */
	public static double getTangentModifier(Point2D fromPoint, Point2D toPoint, Point2D centerPoint) {
		// compute Z portion of 2D cross product to determine sign
		// v0[0] * v1[1] - v0[1] * v1[0]
		double crossProduct = (fromPoint.getX() - centerPoint.getX()) * (toPoint.getY() - centerPoint.getY()) -
			(fromPoint.getY() - centerPoint.getY()) * (toPoint.getX() - centerPoint.getX());
		if (crossProduct > 0.0000000211) {
			return (-Math.PI / 2.0);
		} else if (crossProduct < -0.0000000211) {
				return (Math.PI / 2.0);
		} else {
			return 0.0;
		}
	}
	
	public static Point2D findPointNearEndPoint(Shape shape, Point2D point) {
		Point2D nearestEndPoint = null;
		if (shape instanceof Line2D) {
			Line2D line = (Line2D) shape;
			// see if near an end point
			if (line.getP1().distance(point) < CLOSE_ENOUGH) {
				nearestEndPoint = line.getP1();
			} else if (line.getP2().distance(point) < CLOSE_ENOUGH) {
				nearestEndPoint = line.getP2();
			}
		} else if (shape instanceof Arc2D) {
			Arc2D arc = (Arc2D) shape;
			// see if near an end point
			if (arc.getStartPoint().distance(point) < CLOSE_ENOUGH) {
				nearestEndPoint = arc.getStartPoint();
			} else if (arc.getEndPoint().distance(point) < CLOSE_ENOUGH) {
				nearestEndPoint = arc.getEndPoint();
			}
		}
		return nearestEndPoint;
	}
	
	public static Point2D findPointOnShapeNearPoint(Shape shape, Point2D point) {
		Point2D pointOnLine = point;
		if (shape instanceof Line2D) {
			Line2D line = (Line2D) shape;
			double distance = line.ptSegDist(point);
			// get direction perpendicular to line
			double direction = calcDirection(line.getP1(), line.getP2()) -
				line.relativeCCW(point) * Math.PI / 2.0;
			// find point on perpendicular [distance] away from point
			pointOnLine = new Point2D.Double(
					point.getX() + Math.cos(direction) * distance,
					point.getY() + Math.sin(direction) * distance);
			// TODO when off segment should be an end point
		} else if (shape instanceof Arc2D) {
			Arc2D arc = (Arc2D) shape;
			// get direction from center to point
			double direction = calcDirection(point.getX(), point.getY(), arc.getCenterX(), arc.getCenterY());
			// get point at radius distance
			double distance = arc.getWidth() / 2.0;
			pointOnLine = new Point2D.Double(
					arc.getCenterX() + Math.cos(direction) * distance,
					arc.getCenterY() + Math.sin(direction) * distance);
			// TODO when off segment should be an end point
		}
		return pointOnLine;
	}
	
	public static Shape[] splitShapeAtPoint(Shape shape, Point2D point) {
		Shape[] splitShapes = new Shape[2];
		if (shape instanceof Line2D) {
			Line2D line = (Line2D) shape;
			splitShapes[0] = new Line2D.Double(line.getX1(), line.getY1(), point.getX(), point.getY());
			splitShapes[1] = new Line2D.Double(point.getX(), point.getY(), line.getX2(), line.getY2());
		} else if (shape instanceof Arc2D) {
			Arc2D arc = (Arc2D) shape;
			double direction = Math.toDegrees(calcDirection(arc.getCenterX(), arc.getCenterY(), point.getX(), point.getY()));
			double extent1 = direction - arc.getAngleStart();
			double extent2 = arc.getAngleExtent() - extent1;
			splitShapes[0] = new Arc2D.Double(arc.getX(), arc.getY(), arc.getWidth(), arc.getHeight(), arc.getAngleStart(), extent1, arc.getArcType());
			splitShapes[1] = new Arc2D.Double(arc.getX(), arc.getY(), arc.getWidth(), arc.getHeight(), direction, extent2, arc.getArcType());
		}	
		return splitShapes;
	}
	
	public static Point2D getOppositePoint(Shape shape, Point2D point) {
		Point2D[] points = getPointsFromShape(shape);
		if (point.equals(points[0]) || point.distance(points[0]) < POINT_ERROR) {
			return points[1];
		} else if (point.equals(points[1]) || point.distance(points[1]) < POINT_ERROR) {
			return points[0];
		} else {
			// assuming point parameter is an end point, this should not happen
			return null;
		}
	}
	
	public static double crossProductZ(Point2D fromPoint, Point2D toPoint, Point2D centerPoint) {
		// Z-component of cross product
		// v0[0] * v1[1] - v0[1] * v1[0]
		double crossProduct = (fromPoint.getX() - centerPoint.getX()) * (toPoint.getY() - centerPoint.getY()) -
			(fromPoint.getY() - centerPoint.getY()) * (toPoint.getX() - centerPoint.getX());
		return crossProduct;
	}
	
	public static Point2D getCommonPoint(Shape s1, Shape s2) {
		Point2D[] pts1 = getPointsFromShape(s1);
		Point2D[] pts2 = getPointsFromShape(s2);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				if (pts1[i].distance(pts2[j]) < POINT_ERROR) {
					return pts1[i];  // or pts2[j]
				}
			}
		}
		return null;
	}

	public static Point2D[] getClosePoints(Shape s1, Shape s2) {
		Point2D[] pts1 = getPointsFromShape(s1);
		Point2D[] pts2 = getPointsFromShape(s2);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				if (pts1[i].distance(pts2[j]) < CLOSE_ENOUGH) {
					return new Point2D[] {pts1[i], pts2[j]};
				}
			}
		}
		return null;
	}

	public static double normalizeRadians(double angle) {
		if (angle > Math.PI) {
			if (angle <= (Math.PI * 3.0)) {
				angle = angle - (Math.PI * 2.0);
			} else {
				angle = Math.IEEEremainder(angle, (Math.PI * 2.0));
				if (angle == -Math.PI) {
					angle = Math.PI;
				}
			}
		} else if (angle <= -Math.PI) {
			if (angle > (-Math.PI * 3.0)) {
				angle = angle + (Math.PI * 2.0);
			} else {
				angle = Math.IEEEremainder(angle, (Math.PI * 2.0));
				if (angle == -Math.PI) {
					angle = Math.PI;
				}
			}
		}
		return angle;
	}

}
