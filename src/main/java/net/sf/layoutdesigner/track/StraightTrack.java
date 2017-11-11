package net.sf.layoutdesigner.track;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import net.sf.layoutdesigner.util.Geometry;

public class StraightTrack extends Line2D.Double implements Track {

    private static final long serialVersionUID = 1L;
    private double z1, z2;
	
//	public StraightTrack(double x1, double y1, double x2, double y2) {
//		super(x1, y1, x2, y2);
//	}
	
	/**
	 * Create straight track segment centered on 0,0 with length
	 * @param length
	 */
	public StraightTrack(double length) {
	    super(0, 0, length, 0);
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
    public void transform(AffineTransform at) {
        Geometry.transform(this, at);
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
	public final void drawRoadbed(Graphics2D g2) {
		g2.draw(this);
	}

	@Override
	public final void drawTies(Graphics2D g2) {
		double distance = getP1().distance(getP2());
		Point2D unit = Geometry.calcUnitVector(getP1(), getP2());
		// going perpendicular - swap X and Y
		Line2D tie = new Line2D.Double(getX1() + unit.getY() * 8.0, 
				getY1() - unit.getX() * 8.0, 
				getX1() - unit.getY() * 8.0, 
				getY1() + unit.getX() * 8.0);
		//g2.draw(tie);
		AffineTransform at = AffineTransform.getTranslateInstance(unit.getX() * -5.0, unit.getY() * -5.0);
		for (double step = 0.0; step < distance; step += 5.0) {
			Geometry.transform(tie, at);
			g2.draw(tie);
		}
	}

	@Override
	public final void drawRails(Graphics2D g2) {
		Point2D unit = Geometry.calcUnitVector(getP1(), getP2());
		AffineTransform at = AffineTransform.getTranslateInstance(unit.getY() * 5.0, unit.getX() * -5.0);
		Shape rail = at.createTransformedShape(this);
		g2.draw(rail);
		at.setToTranslation(unit.getY() * -5.0, unit.getX() * 5.0);
		rail = at.createTransformedShape(this);
		g2.draw(rail);
	}
	
}
