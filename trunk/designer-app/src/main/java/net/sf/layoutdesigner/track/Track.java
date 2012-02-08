package net.sf.layoutdesigner.track;

public interface Track {
	public void translate(double dx, double dy);
	public void translate(int dx, int dy);
	public void rotate(double theta, double cx, double cy);
	public void rotate(double theta, int cx, int cy);
}
