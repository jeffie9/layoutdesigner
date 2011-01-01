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
package train.scenery;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

import train.util.Geometry;

public class RailCar implements Serializable, Cloneable, Shape {
	private static final long serialVersionUID = 1L;
	private Image img;  // detailed image of car
	private Path2D polygon; // using path to outline image (which is coming later)
	private Point2D loc;  // center of the car
	private Branch branch;
	private Shape shape;
	private Point2D pointTowards;
	private double lastDirection;
	
	public RailCar() {
		// simple shape for development, use image later
		polygon = new Path2D.Double(Path2D.WIND_EVEN_ODD, 5);
		
		polygon.moveTo(-12.5, 0);
		polygon.lineTo(-7.5, -5);
		polygon.lineTo(12.5, -5);
		polygon.lineTo(12.5, 5);
		polygon.lineTo(-7.5, 5);
		
		polygon.closePath();
		
		// points straight up initially
		lastDirection = Math.toRadians(0.0);
	}
	
	public void restore(RailCar origCar) {
		//PathIterator pi = polygon.getPathIterator(null);
		//polygon.reset();
		//polygon.append(origCar.polygon.getPathIterator(null), false);
		polygon = origCar.polygon;
		loc = origCar.loc;
		branch = origCar.branch;
		shape = origCar.shape;
		pointTowards = origCar.pointTowards;
		lastDirection = origCar.lastDirection;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}
	
	public Point2D getLoc() {
		return loc;
	}
	
	public void setLoc(Point2D loc) {
		this.loc = loc;
		AffineTransform at = new AffineTransform(); 
		at.translate(loc.getX(), loc.getY());
		polygon.transform(at);
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public void setPointTowards(Point2D pointTowards) {
		this.pointTowards = pointTowards;
	}
	
	public Point2D getPointTowards() {
		return pointTowards;
	}
	
	public void render(Graphics2D g2) {
		g2.draw(polygon);
	}
	
	public void move(double distance) {
		boolean keepMoving = true;
		double offset = distance;
		
		while (keepMoving) {
			Branch.FindPointResults findPointResults = branch.findNextPoint(shape, loc, offset, pointTowards);
			// transform car body
			AffineTransform at = new AffineTransform(); 
			// 4. translate to new location
			at.translate(findPointResults.newPt.getX(), findPointResults.newPt.getY());
			// 3. rotate to new position
			at.rotate(findPointResults.direction);
			// 2. rotate back to original position
			at.rotate(-lastDirection);
			// 1. send back to origin
			at.translate(-loc.getX(), -loc.getY());
			polygon.transform(at);
			loc.setLocation(findPointResults.newPt);
			lastDirection = findPointResults.direction;

			if (findPointResults.overrun > 0.00001) {
				// at the end of a shape, pointTowards is the current location
				offset = findPointResults.overrun;
				
				// see if there is a junction at the current location
				Junction junction = SceneryManager.getInstance().getJunctionAtPoint(pointTowards);
				Junction.BranchInfo branchInfo = null;
				if (junction != null) {
					//System.out.println("found junction");
					branchInfo = junction.getNextBranch(shape);
				}
				if (branchInfo != null) {
					//System.out.println("found branch from junction");
					branch = branchInfo.branch;
					shape = branchInfo.shape;
					pointTowards = Geometry.getOppositePoint(shape, pointTowards);
				} else {
					Object[] shapeInfo = branch.getNextShapeAtPoint(shape, pointTowards);
					if (shapeInfo != null) {
						//System.out.println("found next shape in current branch");
						shape = (Shape) shapeInfo[0];
						pointTowards = (Point2D) shapeInfo[1];
					} else {
						boolean foundNextShape = false;
						// branches are for efficiency, now scan all shapes for the next one
						for (Branch b : SceneryManager.getInstance().getBranches()) {
							List<Shape> shapes = b.findShapesAtPoint(findPointResults.newPt, Geometry.POINT_ERROR);
							for (Shape s : shapes) {
								if (s != shape) {
									shape = s;
									branch = b;
									pointTowards = Geometry.getOppositePoint(shape, findPointResults.newPt);
									foundNextShape = true;
									break;
								}
							}
							if (foundNextShape) break;
						}
						if (!foundNextShape) {
							// no place to go. probably should stop train
							keepMoving = false;
						}
					}
				}
			} else {
				break;
			}
		}
	}
	
	public void changeDirection() {
		pointTowards = Geometry.getOppositePoint(shape, pointTowards);
	}

	@Override
	public Object clone() {
		try {
			RailCar clone = (RailCar) super.clone();
			// more may be needed, but these two are getting altered during mouse drag
			clone.polygon = (Path2D) polygon.clone();
			clone.loc = (Point2D) loc.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void transform(AffineTransform at) {
		polygon.transform(at);
		at.transform(loc, loc);
	}

	// Shape methods - delegate to polygon
	
	@Override
	public Rectangle getBounds() {
		return polygon.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return polygon.getBounds2D();
	}

	@Override
	public boolean contains(Point2D p) {
		return polygon.contains(p);
	}

	@Override
	public boolean contains(double x, double y) {
		return polygon.contains(x, y);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return polygon.intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return polygon.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return polygon.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return polygon.contains(r);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return polygon.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return polygon.getPathIterator(at, flatness);
	}

}
