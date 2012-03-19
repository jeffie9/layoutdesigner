package net.sf.layoutdesigner.track;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import net.sf.layoutdesigner.util.Geometry;

public class StraightTrack extends AbstractTrack implements Shape {

	private double x1, y1, z1, x2, y2, z2;
	
	public StraightTrack(double x1, double y1, double x2, double y2) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	/**
	 * Create straight track segment centered on 0,0 with length
	 * @param length
	 */
	public StraightTrack(double length) {
		y1 = y2 = 0;
		x1 = -length / 2.0;
		x2 = length / 2.0;
	}

	@Override
	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle2D getBounds2D() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(double x, double y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Point2D p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Rectangle2D r) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void transform(AffineTransform at) {
		double[] points = new double[]{x1, y1, x2, y2};
		at.transform(points, 0, points, 0, 4);
		x1 = points[0];
		y1 = points[1];
		x2 = points[2];
		y2 = points[3];
	}

	@Override
	protected final void drawRoadbed(Graphics2D g2) {
//		Line2D line = new Line2D.Double(x1, y1, x2, y2);
//		g2.draw(line);
	}

	@Override
	public final void drawTies(Graphics2D g2) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
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
	}

	@Override
	public final void drawRails(Graphics2D g2) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		Point2D unit = Geometry.calcUnitVector(line.getP1(), line.getP2());
		AffineTransform at = AffineTransform.getTranslateInstance(unit.getY() * 5.0, unit.getX() * -5.0);
		Shape rail = at.createTransformedShape(line);
		g2.draw(rail);
		at.setToTranslation(unit.getY() * -5.0, unit.getX() * 5.0);
		rail = at.createTransformedShape(line);
		g2.draw(rail);
	}


}
