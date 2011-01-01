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
package train.test;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.MessageFormat;

import train.util.Geometry;

public class CrossProductTest {
	public static void main(String[] args) {
		double extent = Math.toRadians(30.0);
		Point2D p1 = null;
		Point2D p2 = null;
		Point2D c = new Point2D.Double(0.0, 0.0);
		double radius = 1.0;
		double crossProduct = 0.0;
		double direction;
		MessageFormat form = new MessageFormat("A: ({0,number,#.##}, {1,number,#.##})  B: ({2,number,#.##}, {3,number,#.##})  A X B: {4,number,#.##}  Direction: {5,number,#.##}");
		
		for (int i = 0; i < 12; i++) {
			p1 = new Point2D.Double(Math.cos(extent * (double) i) * radius,
					Math.sin(extent * (double) i) * radius);
			p2 = new Point2D.Double(Math.cos(extent * (double) (i + 1)) * radius,
					Math.sin(extent * (double) (i + 1)) * radius);
			crossProduct = Geometry.crossProductZ(p1, p2, c);
			direction = Geometry.calcDirection(p1, p2, c);
			
			System.out.println(form.format(new Object[] {p1.getX(), p1.getY(), p2.getX(), p2.getY(), crossProduct, Math.toDegrees(direction)}));
			
		}

		System.out.println("\nIn reverse:\n");
		form = new MessageFormat("A: ({0,number,#.##}, {1,number,#.##})  B: ({2,number,#.##}, {3,number,#.##})  B X A: {4,number,#.##}  Direction: {5,number,#.##}");		
		
		for (int i = 0; i < 12; i++) {
			p1 = new Point2D.Double(Math.cos(extent * (double) i) * radius,
					Math.sin(extent * (double) i) * radius);
			p2 = new Point2D.Double(Math.cos(extent * (double) (i + 1)) * radius,
					Math.sin(extent * (double) (i + 1)) * radius);
			crossProduct = Geometry.crossProductZ(p2, p1, c);
			direction = Geometry.calcDirection(p2, p1, c);
			
			System.out.println(form.format(new Object[] {p1.getX(), p1.getY(), p2.getX(), p2.getY(), crossProduct, Math.toDegrees(direction)}));
			
		}
	}
	
}
