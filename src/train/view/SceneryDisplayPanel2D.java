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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import train.scenery.Branch;
import train.scenery.Junction;
import train.scenery.SceneryManager;
import train.scenery.Train;
import train.util.Geometry;

@SuppressWarnings("serial")
public class SceneryDisplayPanel2D extends JPanel {

	public SceneryDisplayPanel2D() {
		// no layout manager
		super(null);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
		renderGround(g2);
		renderRoadbed(g2);
		renderTies(g2);
		renderRails(g2);
		
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			//branch.render(g2);
		}
		for (Train train : SceneryManager.getInstance().getTrains()) {
			train.render(g2);
		}

	}
	
	public void renderAll(Graphics2D g2) {
		
	}
	
	public void renderGround(Graphics2D g2) {
		// simple green lawn
		Color color = new Color(51, 153, 51);
		Color saveColor = g2.getColor();
		g2.setColor(color);
		g2.fill(g2.getClip());
		g2.setColor(saveColor);
	}
	
	public void renderRoadbed(Graphics2D g2) {
		Color color = new Color(96, 96, 96);  // dark grey
		Color saveColor = g2.getColor();
		g2.setColor(color);
		
		Stroke saveStroke = g2.getStroke();
		BasicStroke stroke = new BasicStroke(18.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
		g2.setStroke(stroke);
		
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			for (Shape shape : branch.getShapes()) {
				g2.draw(shape);
			}
		}

		g2.setStroke(saveStroke);
		g2.setColor(saveColor);

	}
	
	public void renderTies(Graphics2D g2) {
		Color color = new Color(102, 51, 51);  // brown
		Color saveColor = g2.getColor();
		g2.setColor(color);
		
		Stroke saveStroke = g2.getStroke();
		BasicStroke stroke = new BasicStroke(2.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
		g2.setStroke(stroke);
		
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			for (Shape shape : branch.getShapes()) {
				if (shape instanceof Line2D) {
					Line2D line = (Line2D) shape;
					double distance = line.getP1().distance(line.getP2());
					Point2D unit = Geometry.calcUnitVector(line.getP1(), line.getP2());
					// going perpendicular - swap X and Y
					Line2D tie = new Line2D.Double(line.getX1() + unit.getY() * 8.0, 
							line.getY1() - unit.getX() * 8.0, 
							line.getX1() - unit.getY() * 8.0, 
							line.getY1() + unit.getX() * 8.0);
					//g2.draw(tie);
					AffineTransform at = AffineTransform.getTranslateInstance(unit.getX() * -5.0, unit.getY() * -5.0);
					for (double step = 0.0; step < distance; step += 5.0) {
						Geometry.transform(tie, at);
						g2.draw(tie);
					}
					
				} else if (shape instanceof Arc2D) {
					Arc2D arc = (Arc2D) shape;
					Point2D[] points = Geometry.getPointsFromShape(arc);
					Point2D unit = Geometry.calcUnitVector(points[2], points[0]);
					Line2D tie = new Line2D.Double(
							points[0].getX() + unit.getX() * 8.0,
							points[0].getY() + unit.getY() * 8.0,
							points[0].getX() - unit.getX() * 8.0,
							points[0].getY() - unit.getY() * 8.0);
					//g2.draw(tie);
					double angleStep = 5.0 / (arc.getWidth() / 2.0); 
					AffineTransform at = AffineTransform.getRotateInstance(-angleStep, points[2].getX(), points[2].getY());
					angleStep = Math.toDegrees(angleStep);
					for (double step = 0; step < arc.getAngleExtent(); step += angleStep) {
						Geometry.transform(tie, at);
						g2.draw(tie);
					}
				}
				
			}
		}

		g2.setStroke(saveStroke);
		g2.setColor(saveColor);
		
	}
	
	public void renderRails(Graphics2D g2) {
		Color color = new Color(192, 192, 192);  // silver-ish
		Color saveColor = g2.getColor();
		g2.setColor(color);
		
		Stroke saveStroke = g2.getStroke();
		BasicStroke stroke = new BasicStroke(1.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
		g2.setStroke(stroke);

		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			for (Shape shape : branch.getShapes()) {
				if (shape instanceof Line2D) {
					Line2D line = (Line2D) shape;
					Point2D unit = Geometry.calcUnitVector(line.getP1(), line.getP2());
					AffineTransform at = AffineTransform.getTranslateInstance(unit.getY() * 5.0, unit.getX() * -5.0);
					Shape rail = at.createTransformedShape(line);
					g2.draw(rail);
					at.setToTranslation(unit.getY() * -5.0, unit.getX() * 5.0);
					rail = at.createTransformedShape(line);
					g2.draw(rail);
				} else if (shape instanceof Arc2D) {
					Arc2D arc = (Arc2D) shape;
					double radius = arc.getWidth() / 2.0;
					double scale = (radius - 5.0) / radius;
					AffineTransform at = AffineTransform.getTranslateInstance(arc.getCenterX(), arc.getCenterY());
					at.scale(scale, scale);
					at.translate(-arc.getCenterX(), -arc.getCenterY());
					Shape rail = at.createTransformedShape(arc);
					g2.draw(rail);
					scale = (radius + 5.0) / radius;
					at.setToTranslation(arc.getCenterX(), arc.getCenterY());
					at.scale(scale, scale);
					at.translate(-arc.getCenterX(), -arc.getCenterY());
					rail = at.createTransformedShape(arc);
					g2.draw(rail);
				}
			}
		}

		g2.setStroke(saveStroke);
		g2.setColor(saveColor);
	}
}
