package net.sf.layoutdesigner.track;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public interface Track {
	public void translate(double dx, double dy);
	public void translate(int dx, int dy);
	public void rotate(double theta, double cx, double cy);
	public void rotate(double theta, int cx, int cy);
	public void transform(AffineTransform at);
	//public void draw(Graphics2D g2);
//	public void drawDebug(Graphics2D g2);
	public void drawRoadbed(Graphics2D g2);
	public void drawTies(Graphics2D g2);
	public void drawRails(Graphics2D g2);
}
