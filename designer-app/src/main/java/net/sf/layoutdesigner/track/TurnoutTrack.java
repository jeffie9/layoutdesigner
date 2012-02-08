package net.sf.layoutdesigner.track;

import java.awt.geom.AffineTransform;


public class TurnoutTrack extends AbstractTrack {

	private AbstractTrack[] tracks;
	
	@Override
	protected void transform(AffineTransform at) {
		for (int i = 0; i < tracks.length; i++) {
			tracks[i].transform(at);
		}
	}

}
