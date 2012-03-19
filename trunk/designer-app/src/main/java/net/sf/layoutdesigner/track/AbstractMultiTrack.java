package net.sf.layoutdesigner.track;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public abstract class AbstractMultiTrack extends AbstractTrack {

	protected AbstractTrack[] tracks;
	
	@Override
	protected void transform(AffineTransform at) {
		for (int i = 0; i < tracks.length; i++) {
			tracks[i].transform(at);
		}
	}

	@Override
	protected void drawRoadbed(Graphics2D g2) {
		for (int i = 0; i < tracks.length; i++) {
			tracks[i].drawRoadbed(g2);
		}		
	}
	
	@Override
	protected void drawTies(Graphics2D g2) {
		for (int i = 0; i < tracks.length; i++) {
			// TODO logic for ties at frog end
			tracks[i].drawTies(g2);
		}		
	}
	
	@Override
	protected void drawRails(Graphics2D g2) {
		for (int i = 0; i < tracks.length; i++) {
			// TODO logic for rails at frog end
			tracks[i].drawRails(g2);
		}		
	}
}
