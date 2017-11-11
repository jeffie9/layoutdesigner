package net.sf.layoutdesigner.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.layoutdesigner.track.CurveTrack;
import net.sf.layoutdesigner.track.StraightTrack;
import net.sf.layoutdesigner.track.Track;
import net.sf.layoutdesigner.util.Geometry;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.Dimension;
import java.text.MessageFormat;

public class TrackDesignerPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(TrackDesignerPanel.class);
    
    private Track[] tracks;
    private Line2D anchor;
    private TrackInputPanel inputPanel1;
    private TrackInputPanel inputPanel2;
    //private TrackInputPanel inputPanel3;
    private JPanel canvasPanel;

    /**
     * Create the panel.
     */
    public TrackDesignerPanel() {
        tracks = new Track[2];
        anchor = new Line2D.Double(130.0, 0.0, 160.0, 0.0);
        
        setLayout(new BorderLayout(0, 0));
        
        JPanel inputPanel = new JPanel();
        add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel1 = new TrackInputPanel(this);
        inputPanel2 = new TrackInputPanel(this);
        //inputPanel3 = new TrackInputPanel(this);
        inputPanel.add(inputPanel1);
        inputPanel.add(inputPanel2);
        //inputPanel.add(inputPanel3);
        
        JPanel trackPanel = new JPanel();
        add(trackPanel, BorderLayout.CENTER);
        trackPanel.setLayout(new BoxLayout(trackPanel, BoxLayout.X_AXIS));
        
        canvasPanel = new JPanel() {           
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.addRenderingHints(new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON));
                g2.scale(1.0, 1.0);
                AffineTransform saveTransform = g2.getTransform();
                AffineTransform at = AffineTransform.getTranslateInstance(getWidth()/2, getHeight()/2);
                g2.setTransform(at);
    
                drawTracks(tracks, g2);
                
                // debug
                Color saveColor = g2.getColor();
                java.awt.Stroke saveStroke = g2.getStroke();

                Color color = new Color(0, 0, 0);
                g2.setColor(color);
                java.awt.Stroke stroke = new java.awt.BasicStroke(1.0f, 
                        java.awt.BasicStroke.CAP_BUTT, 
                        java.awt.BasicStroke.JOIN_MITER);
                g2.setStroke(stroke);
                g2.draw(anchor);
                g2.setStroke(saveStroke);
                g2.setColor(saveColor);
                // end debug
    
                g2.setTransform(saveTransform);
            }
        };
        canvasPanel.setPreferredSize(new Dimension(200, 200));

        canvasPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        trackPanel.add(canvasPanel);
        
        JPanel libraryPanel = new JPanel();
        trackPanel.add(libraryPanel);

        
    }
    
    void updateTrack() {
        if (inputPanel1.panelCheck.isSelected()) {
            switch (inputPanel1.trackCombo.getSelectedIndex()) {
            case 0: // straight
                tracks[0] = createStraight(inputPanel1);
                break;
            case 1: // curve
                tracks[0] = createCurve(inputPanel1);
                break;
            }
        } else {
            tracks[0] = null;
        }

        if (inputPanel2.panelCheck.isSelected()) {
            switch (inputPanel2.trackCombo.getSelectedIndex()) {
            case 0: // straight
                tracks[1] = createStraight(inputPanel2);
                break;
            case 1: // curve
                tracks[1] = createCurve(inputPanel2);
                break;
            }
        } else {
            tracks[1] = null;
        }

        canvasPanel.repaint();

    }
    
    private Track createStraight(TrackInputPanel trackInputPanel) {
        double length = 0.0;
        try {
            length = Double.parseDouble(trackInputPanel.lengthField.getText());
        } catch (NumberFormatException e) {
            // probably blank input or bad, ignore this track
            return null;
        }
        double offset = length / 2.0 * Math.sqrt(2.0);
        StraightTrack track = new StraightTrack(length);
        String result = MessageFormat.format("<straight x1=\"{0}\" x2=\"{2}\" y1=\"{1}\" y2=\"{3}\" />", -offset, -offset, offset, offset);
        //log.info(result);
        AffineTransform at = Geometry.getSnapToTransform(anchor, track, anchor.getP1(), track.getP2());
        track.transform(at);
        return track;
    }

    private Track createCurve(TrackInputPanel trackInputPanel) {
        double radius = 0.0;
        double extent = 0.0;
        try {
            radius = Double.parseDouble(trackInputPanel.radiusField.getText());
            extent = Double.parseDouble(trackInputPanel.extentField.getText());
        } catch (NumberFormatException e) {
            // probably blank input or bad, ignore this track
            return null;
        }
        double xoffset = radius + radius / 2.0 * Math.sqrt(2.0);
        double yoffset = radius - radius / 2.0 * Math.sqrt(2.0);
        Arc2D arc = new Arc2D.Double(-xoffset, -yoffset, radius * 2.0, radius * 2.0, (90.0 - extent) / 2.0, extent, Arc2D.OPEN);
        CurveTrack track = new CurveTrack(radius, extent);
        //log.fine(track.toString());
        String result = MessageFormat.format("<curve x1=\"{0}\" x2=\"{2}\" xc=\"{4}\" y1=\"{1}\" y2=\"{3}\" yc=\"{5}\" />",
                arc.getStartPoint().getX(), arc.getStartPoint().getY(), arc.getEndPoint().getX(), arc.getEndPoint().getY(), 
                arc.getCenterX(), arc.getCenterY());
        log.debug(result);
        AffineTransform at = Geometry.getSnapToTransform(anchor, track, anchor.getP1(), track.getEndPoint());
        track.transform(at);
        return track;
    }
    
    // TODO move to another class
    public void drawTracks(Track[] tracks, Graphics2D g2) {
        // TODO add context to drive which elements get drawn
        Color saveColor = g2.getColor();
        Stroke saveStroke = g2.getStroke();

        Color color = new Color(96, 96, 96);  // dark grey
        g2.setColor(color);
        BasicStroke stroke = new BasicStroke(18.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke);
//        for (int i = 0; i < tracks.length; i++) {
//            if (tracks[i] != null) {
//                tracks[i].drawRoadbed(g2);
//            }
//        }

        color = new Color(102, 51, 51);  // brown
        g2.setColor(color);
        stroke = new BasicStroke(2.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke);
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i] != null) {
                tracks[i].drawTies(g2);
            }
        }

        color = new Color(192, 192, 192);  // silver-ish
        g2.setColor(color);
        stroke = new BasicStroke(1.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
        g2.setStroke(stroke);
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i] != null) {
                tracks[i].drawRails(g2);
            }
        }

        // restore graphics
        g2.setStroke(saveStroke);
        g2.setColor(saveColor);
    }

    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    TrackDesignerPanel panel = new TrackDesignerPanel();
                    JFrame frame = new JFrame("Track Designer");
                    frame.setBounds(100, 100, 450, 300);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.getContentPane().setLayout(new BorderLayout(0, 0));
                    frame.getContentPane().add(panel, BorderLayout.CENTER);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
