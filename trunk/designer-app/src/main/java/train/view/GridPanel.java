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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import train.scenery.Branch;
import train.scenery.Junction;
import train.scenery.SceneryManager;

@SuppressWarnings("serial")
public class GridPanel extends JPanel {
	private static final Color fine = new Color(128, 250, 128);
	private static final Color minor = new Color(250, 128, 128);
	private static final Color major = new Color(0, 0, 40);

	Shape[] dragShape = null;
	private List<Shape> selectedShapes;
	double scale = 1.25;
	
	public GridPanel() {
		super(null);  // no layout manager
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(1000, 1000));
		//setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
	}
	
	public void setSelectedShapes(List<Shape> selectedShapes) {
		this.selectedShapes = selectedShapes;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// for anti-aliasing geometric shapes
		g2.addRenderingHints(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));

		g2.scale(scale, scale);
		
		// fine lines
		g.setColor(fine);
		drawGridLines(g, 5);
		
		// minor lines
		g.setColor(minor);
		drawGridLines(g, 10);
		
		// major lines
		g.setColor(major);
		drawGridLines(g, 100);

		// Start rendering track objects
		
		g.setColor(Color.BLACK);

		// do not render trains
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			branch.render(g2);
		}
		for (Junction junction : SceneryManager.getInstance().getJunctions()) {
			junction.render(g2);
		}

		// highlight selected tracks
		g.setColor(Color.RED);
		BasicStroke wide = new BasicStroke(4.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
		g2.setStroke(wide);
		for (Shape shape : selectedShapes) {
			g2.draw(shape);
		}
		
		// if dragging a shape, draw it
		if (dragShape != null) {
			g.setColor(Color.BLACK);
			BasicStroke dashed = new BasicStroke(1.0f, 
	                BasicStroke.CAP_BUTT, 
	                BasicStroke.JOIN_MITER,
	                10.0f,
	                new float[] {10.0f},
	                0.0f);
			g2.setStroke(dashed);
			for (int i = 0; i < dragShape.length; i++) {
				g2.draw(dragShape[i]);
			}
		}
		
		// highlight open end-points
		Point2D[] points = SceneryManager.getInstance().findOpenPoints();
		Rectangle2D rect = new Rectangle2D.Double();
		g.setColor(Color.RED);
		for (int i = 0; i < points.length; i++) {
			rect.setRect(points[i].getX() - 3.0, points[i].getY() - 3.0, 6.0, 6.0);
			g2.fill(rect);
		}
		
	}
	
	private void drawGridLines(Graphics g, int increment) {
		Rectangle clip = g.getClipBounds();

		// horizontal lines
		for (int y = (clip.y / increment) * increment; y <= clip.height + clip.y; y += increment) {
			g.drawLine(clip.x, y, clip.x + clip.width, y);
		}

		// vertical lines
		for (int x = (clip.x / increment) * increment; x <= clip.width + clip.x; x += increment) {
			g.drawLine(x, clip.y, x, clip.y + clip.height);
		}

	}

	
}
