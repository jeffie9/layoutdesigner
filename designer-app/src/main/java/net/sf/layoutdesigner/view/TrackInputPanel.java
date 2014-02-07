package net.sf.layoutdesigner.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TrackInputPanel extends JPanel implements DocumentListener, ActionListener {
    private TrackDesignerPanel parent;
    JCheckBox panelCheck;
    JComboBox trackCombo;
    JTextField lengthField;
    JTextField radiusField;
    JTextField extentField;
    private JLabel lengthLabel;
    private JLabel radiusLabel;
    private JLabel extentLabel;

    /**
     * Create the panel.
     */
    public TrackInputPanel() {
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

    public TrackInputPanel(TrackDesignerPanel parent) {
        this();
        this.parent = parent;
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        //log.fine("got action event: " + evt);
        if (evt.getSource() == panelCheck) {
            //log.fine("track selected: " + panelCheck.isSelected());
            trackCombo.setEnabled(panelCheck.isSelected());
            lengthLabel.setEnabled(panelCheck.isSelected());
            lengthField.setEnabled(panelCheck.isSelected());
            radiusLabel.setEnabled(panelCheck.isSelected());
            radiusField.setEnabled(panelCheck.isSelected());
            extentLabel.setEnabled(panelCheck.isSelected());
            extentField.setEnabled(panelCheck.isSelected());
        } else if (evt.getSource() == trackCombo) {
            //log.fine("track type: " + trackCombo.getSelectedItem());
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
        
        parent.updateTrack();
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        //log.fine("got change event: " + evt);
        parent.updateTrack();
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        //log.fine("got insert event: " + evt);
        parent.updateTrack();
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        //log.fine("got remove event: " + evt);
        parent.updateTrack();
    }

}
