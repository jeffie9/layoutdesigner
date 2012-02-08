package net.sf.layoutdesigner.track;

import java.awt.geom.AffineTransform;


public abstract class AbstractTrack implements Track {

	@Override
	public void translate(int dx, int dy) {
		translate((double) dx, (double) dy);
	}
	
	@Override
	public void rotate(double theta, int cx, int cy) {
		rotate(theta, (double) cx, (double) cy);
	}
	
	protected abstract void transform(AffineTransform at);
	
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
	

}
