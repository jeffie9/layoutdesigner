package net.sf.layoutdesigner.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.layoutdesigner.track.CurveTrack;
import net.sf.layoutdesigner.track.StraightTrack;
import net.sf.layoutdesigner.track.Track;


@SuppressWarnings("serial")
public class TrackDesigner {
    private static final Logger log = Logger.getLogger(TrackDesigner.class.getPackage().getName());
    private JFrame frmTrackDesigner;
    private JPanel canvasPanel;
    private final JPanel libraryPanel = new JPanel();
    private final DynamicPanel[] dynamicPanels = new DynamicPanel[3];
    private Track track;

    public TrackDesigner() {
        initialize();
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmTrackDesigner = new JFrame();
        frmTrackDesigner.setTitle("Track Designer");
        frmTrackDesigner.setBounds(100, 100, 450, 300);
        frmTrackDesigner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmTrackDesigner.getContentPane().setLayout(new BorderLayout(0, 0));
        
        JPanel inputPanel = new JPanel();
        frmTrackDesigner.getContentPane().add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        for (int i = 0; i < 3; i++) {
            dynamicPanels[i] = new DynamicPanel();
            inputPanel.add(dynamicPanels[i]);
        }

        JPanel trackPanel = new JPanel();
        frmTrackDesigner.getContentPane().add(trackPanel, BorderLayout.CENTER);
        trackPanel.setLayout(new BoxLayout(trackPanel, BoxLayout.X_AXIS));
        
        canvasPanel = new JPanel(){
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

                if (track != null) {
                    //track.draw(g2);
                }

                g2.setTransform(saveTransform);
            }
        };
        canvasPanel.setPreferredSize(new Dimension(200, 200));
        canvasPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
        trackPanel.add(canvasPanel);
        trackPanel.add(libraryPanel);
    }
    
    private void updateTrack() {
        // make life easy - always create track from whole cloth
        // find number of checked panels to build Track array
        // special case while developing - taking first dynamicPanel alone
        Track[] tracks = new Track[1];
        switch (dynamicPanels[0].trackCombo.getSelectedIndex()) {
        case 0: // straight
            tracks[0] = createStraight(dynamicPanels[0]);
            break;
        case 1: // curve
            tracks[0] = createCurve(dynamicPanels[0]);
            break;
        }
        track = tracks[0];
        canvasPanel.repaint();
    }

    private Track createStraight(DynamicPanel dynamicPanel) {
        double length = 0.0;
        try {
            length = Double.parseDouble(dynamicPanel.lengthField.getText());
        } catch (NumberFormatException e) {
            // probably blank input or bad, ignore this track
            return null;
        }
        double offset = length / 2.0 * Math.sqrt(2.0);
        Track track = new StraightTrack(length);
        String result = MessageFormat.format("<straight x1=\"{0}\" x2=\"{2}\" y1=\"{1}\" y2=\"{3}\" />", -offset, -offset, offset, offset);
        log.info(result);
        return track;
    }

    private Track createCurve(DynamicPanel dynamicPanel) {
        double radius = 0.0;
        double extent = 0.0;
        try {
            radius = Double.parseDouble(dynamicPanel.radiusField.getText());
            extent = Double.parseDouble(dynamicPanel.extentField.getText());
        } catch (NumberFormatException e) {
            // probably blank input or bad, ignore this track
            return null;
        }
        double xoffset = radius + radius / 2.0 * Math.sqrt(2.0);
        double yoffset = radius - radius / 2.0 * Math.sqrt(2.0);
        Arc2D arc = new Arc2D.Double(-xoffset, -yoffset, radius * 2.0, radius * 2.0, (90.0 - extent) / 2.0, extent, Arc2D.OPEN);
        Track track = new CurveTrack(radius, (int) extent);
        log.fine(track.toString());
        String result = MessageFormat.format("<curve x1=\"{0}\" x2=\"{2}\" xc=\"{4}\" y1=\"{1}\" y2=\"{3}\" yc=\"{5}\" />",
                arc.getStartPoint().getX(), arc.getStartPoint().getY(), arc.getEndPoint().getX(), arc.getEndPoint().getY(), 
                arc.getCenterX(), arc.getCenterY());
        log.info(result);
        return track;
    }
    
    class DynamicPanel extends JPanel implements DocumentListener, ActionListener {
        JCheckBox panelCheck;
        JComboBox trackCombo;
        JTextField lengthField;
        JTextField radiusField;
        JTextField extentField;
        JLabel lengthLabel;
        JLabel radiusLabel;
        JLabel extentLabel;

        public DynamicPanel() {
            //setBorder(BorderFactory.createLineBorder(Color.black));
            ((FlowLayout) getLayout()).setAlignment(FlowLayout.LEFT);
            panelCheck = new JCheckBox();
            panelCheck.addActionListener(this);
            add(panelCheck);

            trackCombo = new JComboBox(new String[]{"Straight", "Curve"});
            trackCombo.addActionListener(this);
            trackCombo.setMaximumSize(new Dimension(50, 25));
            trackCombo.setEnabled(false);
            add(trackCombo);

            lengthField = new JTextField("100.0");
            lengthField.setColumns(15);
            lengthField.getDocument().addDocumentListener(this);
            lengthField.setMaximumSize(new Dimension(50, 25));
            lengthField.setEnabled(false);
            lengthLabel = new JLabel("Length");
            lengthLabel.setLabelFor(lengthField);
            lengthLabel.setEnabled(false);
            add(lengthLabel);
            add(lengthField);

            // these controls are initially not visible
            radiusField = new JTextField("97.5");
            radiusField.setColumns(10);
            radiusField.getDocument().addDocumentListener(this);
            radiusField.setVisible(false);
            radiusField.setEnabled(false);
            radiusLabel = new JLabel("Radius");
            radiusLabel.setLabelFor(radiusField);
            radiusLabel.setVisible(false);
            radiusField.setEnabled(false);
            add(radiusLabel);
            add(radiusField);

            extentField = new JTextField("30.0");
            extentField.setColumns(10);
            extentField.getDocument().addDocumentListener(this);
            extentField.setVisible(false);
            extentField.setEnabled(false);
            extentLabel = new JLabel("Extent");
            extentLabel.setLabelFor(extentField);
            extentLabel.setVisible(false);
            extentLabel.setEnabled(false);
            add(extentLabel);
            add(extentField);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            log.fine("got action event: " + evt);
            if (evt.getSource() == panelCheck) {
                log.fine("track selected: " + panelCheck.isSelected());
                trackCombo.setEnabled(panelCheck.isSelected());
                lengthLabel.setEnabled(panelCheck.isSelected());
                lengthField.setEnabled(panelCheck.isSelected());
                radiusLabel.setEnabled(panelCheck.isSelected());
                radiusField.setEnabled(panelCheck.isSelected());
                extentLabel.setEnabled(panelCheck.isSelected());
                extentField.setEnabled(panelCheck.isSelected());
            } else if (evt.getSource() == trackCombo) {
                log.fine("track type: " + trackCombo.getSelectedItem());
                switch (trackCombo.getSelectedIndex()) {
                case 0:  // Straight
                    lengthField.setVisible(true);
                    lengthLabel.setVisible(true);
                    radiusField.setVisible(false);
                    radiusLabel.setVisible(false);
                    extentField.setVisible(false);
                    extentLabel.setVisible(false);
                    break;
                case 1:  // Curve
                    lengthField.setVisible(false);
                    lengthLabel.setVisible(false);
                    radiusField.setVisible(true);
                    radiusLabel.setVisible(true);
                    extentField.setVisible(true);
                    extentLabel.setVisible(true);
                    break;
                }
            }
            
            updateTrack();
        }

        @Override
        public void changedUpdate(DocumentEvent evt) {
            log.fine("got change event: " + evt);
            updateTrack();
        }

        @Override
        public void insertUpdate(DocumentEvent evt) {
            log.fine("got insert event: " + evt);
            updateTrack();
        }

        @Override
        public void removeUpdate(DocumentEvent evt) {
            log.fine("got remove event: " + evt);
            updateTrack();
        }

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    TrackDesigner window = new TrackDesigner();
                    window.frmTrackDesigner.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
