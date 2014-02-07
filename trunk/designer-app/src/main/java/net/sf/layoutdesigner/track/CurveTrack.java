package net.sf.layoutdesigner.track;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

import net.sf.layoutdesigner.util.Geometry;

public class CurveTrack extends Arc2D.Double implements Track {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(CurveTrack.class.getPackage().getName());
	private double z1, z2, zc;
	
    /**
     * Construct a curve from 0 degrees so the coordinates translate to
     *    start point (radius, 0)
     *    center point (0, 0)
     *    end point (sin(extent)*radius, cos(extent)*radius)
     * @param x The starting point x-coordinate
     * @param y The starting point y-coordinate
     * @param radius
     * @param extent The degrees to sweep the arc counter-clockwise
     */
	public CurveTrack(double radius, double extent) {
	    super(-radius, radius, radius * 2.0, radius * 2.0, 0.0, extent, Arc2D.OPEN);
	}
	
	public CurveTrack(double x1, double y1, double x2, double y2, double xc, double yc) {
	    Point2D start = new Point2D.Double(x1, y1);
	    Point2D end = new Point2D.Double(x2, y2);
	    Point2D center = new Point2D.Double(xc, yc);
	    double radius = start.distance(center);
	    setArcByCenter(center.getX(), center.getY(), radius, 0.0, 0.0, OPEN);
	    setAngles(start, end);
	}
	
   @Override
    public void translate(int dx, int dy) {
        translate((double) dx, (double) dy);
    }
    
    @Override
    public void rotate(double theta, int cx, int cy) {
        rotate(theta, (double) cx, (double) cy);
    }
    
    @Override
    public void translate(double dx, double dy) {
        AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
        transform(at);
    }

    @Override
    public void rotate(double theta, double cx, double cy) {
        AffineTransform at = AffineTransform.getRotateInstance(theta, cx, cy);
        transform(at);
    }

	@Override
	public Rectangle getBounds() {
	    return getBounds2D().getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
        // create a rectangle where opposite corners are the start and end points
        Rectangle2D rect = new Rectangle2D.Double(Math.min(getStartPoint().getX(), getEndPoint().getX()), 
                Math.min(getStartPoint().getY(), getEndPoint().getY()), 
                Math.abs(getEndPoint().getX() - getStartPoint().getX()), 
                Math.abs(getEndPoint().getY() - getStartPoint().getY()));
        // TODO may need to grow the rectangle a little
        return rect;
	}

	@Override
	public boolean contains(double x, double y) {
        return getBounds2D().contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return getBounds2D().contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return getBounds2D().intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return getBounds2D().intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return getBounds2D().contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return getBounds2D().contains(r);
	}

	@Override
	public void transform(AffineTransform at) {
	    Geometry.transform(this, at);
	}

	@Override
	public void drawRoadbed(Graphics2D g2) {
		g2.draw(this);
	}

	@Override
	public void drawTies(Graphics2D g2) {
		Point2D[] points = Geometry.getPointsFromShape(this);
		Point2D unit = Geometry.calcUnitVector(points[2], points[0]);
		Line2D tie = new Line2D.Double(
				points[0].getX() + unit.getX() * 8.0,
				points[0].getY() + unit.getY() * 8.0,
				points[0].getX() - unit.getX() * 8.0,
				points[0].getY() - unit.getY() * 8.0);
		//g2.draw(tie);
		double angleStep = 5.0 / (getWidth() / 2.0); 
		AffineTransform at = AffineTransform.getRotateInstance(-angleStep, points[2].getX(), points[2].getY());
		angleStep = Math.toDegrees(angleStep);
		for (double step = 0; step < getAngleExtent(); step += angleStep) {
			Geometry.transform(tie, at);
			g2.draw(tie);
		}
	}

	@Override
	public void drawRails(Graphics2D g2) {
		double radius = getWidth() / 2.0;
		double scale = (radius - 5.0) / radius;
		AffineTransform at = AffineTransform.getTranslateInstance(getCenterX(), getCenterY());
		at.scale(scale, scale);
		at.translate(-getCenterX(), -getCenterY());
		Shape rail = at.createTransformedShape(this);
		g2.draw(rail);
		scale = (radius + 5.0) / radius;
		at.setToTranslation(getCenterX(), getCenterY());
		at.scale(scale, scale);
		at.translate(-getCenterX(), -getCenterY());
		rail = at.createTransformedShape(this);
		g2.draw(rail);
	}
	
}
