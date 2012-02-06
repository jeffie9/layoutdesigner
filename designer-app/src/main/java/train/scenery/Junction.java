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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import train.util.Geometry;


public class Junction implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<BranchInfo> branches;
	private Point2D location;

	public Junction(Point2D location) {
		branches = new ArrayList<BranchInfo>();
		this.location = location;
		updateBranches();
	}
	
	public List<BranchInfo> getBranches() {
		return branches;
	}
	
	public Point2D getLocation() {
		return location;
	}
	
//	public void addBranch(Branch branch, int shape, int point, boolean active, boolean fixed) {
//		BranchInfo branchInfo = new BranchInfo();
//		branchInfo.branch = branch;
//		branchInfo.shapeIndex = shape;
//		branchInfo.pointIndex = point;
//		branchInfo.active = active;
//		branchInfo.fixed = fixed;
//		branches.add(branchInfo);
//	}
	
	public void updateBranches() {
		// make life easy - remove existing branches and start over
		branches.clear();
		
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			Shape[] shapes = Geometry.findShapesAtPoint(location, branch, Geometry.POINT_ERROR);
			if (shapes != null) {
				for (int i = 0; i < shapes.length; i++) {
					addBranch(branch, shapes[i], false);
				}
			}
		}
	}

	public void addBranch(Branch branch, Shape shape, boolean active) {
		
		BranchInfo branchInfo = new BranchInfo();
		branchInfo.branch = branch;
		branchInfo.shape = shape;
		branchInfo.otherPoint = Geometry.getOppositePoint(shape, location);
		branchInfo.active = active;

		if (branches.size() > 0) {
			// a junction only has two sides, so if the direction determines which side
			BranchInfo bi = branches.get(0);
			Point2D[] points = Geometry.getPointsFromShape(bi.shape);
			double existingDirection = Geometry.calcDirection(location, bi.otherPoint, points.length > 2 ? points[2] : null);			
			points = Geometry.getPointsFromShape(branchInfo.shape);
			double newDirection = Geometry.calcDirection(location, branchInfo.otherPoint, points.length > 2 ? points[2] : null); 
			if (Math.abs(existingDirection - newDirection) < 0.0001) {
				branchInfo.pole = bi.pole;
			} else {
				branchInfo.pole = bi.pole == 0 ? 1 : 0;
			}
		} else {
			// first branch gets set to 0
			branchInfo.pole = 0;
		}

		branches.add(branchInfo);
		
		
		// make sure one spur is active
		int[] poles = new int[]{0,0};
		
		for (BranchInfo bi : branches) {
			poles[bi.pole]++;
		}
		
		// set first of pair active
		for (int i = 0; i < 2; i++) {
			if (poles[i] > 1) {
				boolean first = true;
				for (BranchInfo bi : branches) {
					if (bi.pole == i) {
						bi.active = first;
						if (first) first = false;
					}
				}		
			}
		}

	}
	
	public BranchInfo getNextBranch(Shape shape) {
		BranchInfo shapeBranch = null;
		for (BranchInfo branchInfo : branches) {
			if (branchInfo.shape == shape) {
				shapeBranch = branchInfo;
				break;
			}
		}
		if (shapeBranch != null) {
			for (BranchInfo branchInfo : branches) {
				if (branchInfo.active && shapeBranch.pole != branchInfo.pole) {
					return branchInfo;
				}
			}
			
			// try again looking for a "single" inactive branch
			int branchCount = 0;
			for (BranchInfo branchInfo : branches) {
				if (shapeBranch.pole != branchInfo.pole) {
					branchCount++;
				}
			}
			if (branchCount == 1) {
				for (BranchInfo branchInfo : branches) {
					if (shapeBranch.pole != branchInfo.pole) {
						return branchInfo;
					}
				}
			}
			
//			if (shapeBranch.active) {
//				for (BranchInfo branchInfo : branches) {
//					if (branchInfo.fixed) {
//						return branchInfo;
//					}
//				}				
//			} else if (shapeBranch.fixed) {
//				for (BranchInfo branchInfo : branches) {
//					if (branchInfo.active) {
//						return branchInfo;
//					}
//				}
//			}
		}
		return null;
	}
	
	public void render(Graphics2D g2) {
		g2.setColor(Color.GREEN);
		Shape shape = new Ellipse2D.Double(location.getX() - 10.0, location.getY() - 10.0, 20.0, 20.0);
		g2.draw(shape);
		shape = new Ellipse2D.Double(location.getX() - 5.0, location.getY() - 5.0, 10.0, 10.0);
		g2.fill(shape);
	}

	public Point2D getControlLocation() {
		// TODO get more creative on control placement
		BranchInfo bi = getFixedBranch();
		if (bi == null) {
			bi = branches.get(0);  // shouldn't happen
		}
		Point2D[] points = Geometry.getPointsFromShape(bi.shape);
		double direction = Geometry.calcDirection(location, bi.otherPoint, points.length > 2 ? points[2] : null);
		direction = direction + Math.PI / 2.0;
		return new Point2D.Double(location.getX() + Math.cos(direction) * 15.0,
				location.getY() + Math.sin(direction) * 15.0);
	}
	
	public BranchInfo getActiveBranch() {
		int[] poles = new int[]{0,0};
		
		for (BranchInfo bi : branches) {
			poles[bi.pole]++;
		}
		
		for (int i = 0; i < poles.length; i++) {
			if (poles[i] > 1) {
				for (BranchInfo bi : branches) {
					if (bi.pole == i && bi.active) {
						return bi;
					}
				}		
			}
		}
		return null;
	}
	
	public BranchInfo getFixedBranch() {
		int[] poles = new int[]{0,0};
		
		for (BranchInfo bi : branches) {
			poles[bi.pole]++;
		}
		
		for (int i = 0; i < poles.length; i++) {
			if (poles[i] == 1) {
				for (BranchInfo bi : branches) {
					if (bi.pole == i) {
						return bi;
					}
				}		
			}
		}
		return null;
	}
	
	public void toggleJunction() {
		//System.out.println("Junction.toggleJunction()");
		int[] poles = new int[]{0,0};
		
		for (BranchInfo branch : branches) {
			poles[branch.pole]++;
		}
		
		// assume simple single turn-out
		int pole = poles[0] > 1 ? 0 : 1;
		for (BranchInfo branch : branches) {
			//System.out.println("pole: " + branch.pole + " active: " + branch.active);
			if (branch.pole == pole) {
				branch.active = !branch.active;
			}
		}		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		sb.append(this.getClass().getName() + " {" + NEW_LINE);
		sb.append(" Location: " + location.toString() + NEW_LINE);
		for (BranchInfo bi : branches) {
			sb.append(" Branch {" + NEW_LINE);
			sb.append("  Shape: " + bi.shape + NEW_LINE);
			sb.append("  Pole: " + bi.pole + NEW_LINE);
			sb.append("  Other: " + bi.otherPoint + NEW_LINE);
			sb.append("  Active: " + bi.active + NEW_LINE);
			sb.append(" }" + NEW_LINE);
		}
		sb.append("}");
		
		return sb.toString();
	}

	public class BranchInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		public Branch branch;
		public Shape shape;
		public Point2D otherPoint;
		//public int shapeIndex;
		//public int pointIndex;
		public boolean active;
		//public boolean fixed;
		public int pole;  // use to set opposite spurs in case more than three (could be two switches back to back at a single junction)
	}

}