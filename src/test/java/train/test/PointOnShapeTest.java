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
package train.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.layoutdesigner.util.Geometry;


@SuppressWarnings("serial")
public class PointOnShapeTest extends JPanel implements MouseListener, MouseMotionListener {
    private Shape shape;
    private Point2D pointOnShape;
	
    public PointOnShapeTest() {
		super();
		shape = new Arc2D.Double(90.0, 90.0, 100.0, 100.0, 45.0, 90.0, Arc2D.OPEN);
		//shape = new Line2D.Double(90.0, 90.0, 150.0, 100.0);
		pointOnShape = Geometry.findPointOnShapeNearPoint(shape, new Point2D.Double(0.0, 0.0));
		addMouseMotionListener(this);
		addMouseListener(this);
	}
    
	@Override
	public void paint(Graphics g) {
		super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.draw(shape);
        
        g2.setColor(Color.RED);
        Ellipse2D ellipse = new Ellipse2D.Double(pointOnShape.getX() - 5.0, pointOnShape.getY() - 5.0, 10.0, 10.0);
        g2.fill(ellipse);
    }
	
	public static void main(String s[]) {
        JFrame f = new JFrame("Graphics Test");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        JPanel panel = new PointOnShapeTest();
        f.getContentPane().add(panel, BorderLayout.CENTER);
        f.pack();
        f.setSize(new Dimension(320,200));
        f.setVisible(true);
    }

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		pointOnShape = Geometry.findPointOnShapeNearPoint(shape, e.getPoint());
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


}
