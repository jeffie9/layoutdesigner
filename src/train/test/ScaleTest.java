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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class ScaleTest {
	public static void main(String[] args) {
//		Point2D point = new Point2D.Double(1.0, 0.0);
//		AffineTransform at = new AffineTransform();
//		at.scale(1.5, 1);
//		at.transform(point, point);
//		System.out.println("scale " + at.getScaleX() + ": " + point);
//		
//		point = new Point2D.Double(10.0, 0.0);
//		at.transform(point, point);
//		System.out.println("scale " + at.getScaleX() + ": " + point);
//		
//		
//		point = new Point2D.Double(300.0, 0.0);
//		at.setToScale(303.0/300.0, 1);
//		at.transform(point, point);
//		System.out.println("scale " + at.getScaleX() + ": " + point);
		
		double value = 10.456789;
		int rounded = (int)(value / 2.0) * 2;
		System.out.println("value: " + value + " rounded: " + rounded);
		
		value = 105.456789;
		rounded = (int)(value / 2.0) * 2;
		System.out.println("value: " + value + " rounded: " + rounded);
		
		value = 106.456789;
		rounded = (int)(value / 2.0) * 2;
		System.out.println("value: " + value + " rounded: " + rounded);
		
		
	}
}
