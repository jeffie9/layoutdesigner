package net.sf.layoutdesigner.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.layoutdesigner.track.CurveTrack;
import net.sf.layoutdesigner.track.StraightTrack;
import net.sf.layoutdesigner.track.Track;


@SuppressWarnings("serial")
public class TrackDesigner extends JPanel implements ChangeListener {
	private static final Logger log = Logger.getLogger(TrackDesigner.class.getPackage().getName());
	private JTabbedPane tabPane;
	private JPanel shapePanel;
	private Track track;
	
	public TrackDesigner() {
		super(new BorderLayout());
		
		tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabPane.addTab("Straight", new StraightPanel());
		tabPane.addTab("Curve", new CurvePanel());
		tabPane.addTab("Switch", new SwitchPanel());
		tabPane.addChangeListener(this);
		
		add(tabPane, BorderLayout.PAGE_END);
		
		shapePanel = makeShapePanel();
		add(shapePanel, BorderLayout.CENTER);
		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JTabbedPane tabPane = (JTabbedPane) e.getSource();
		System.out.println("Switched to " + tabPane.getTitleAt(tabPane.getSelectedIndex()));
		shapePanel.repaint();
	}

	private JPanel makeShapePanel() {
		JPanel panel = new JPanel(){
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
					track.draw(g2);
				}
				
				g2.setTransform(saveTransform);
			}
		};
		panel.setPreferredSize(new Dimension(200, 200));
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return panel;
	}
	
	class StraightPanel extends JPanel {
		JTextField lengthField;
		JTextField resultField;
		
		public StraightPanel() {
			lengthField = new JTextField();
			lengthField.setColumns(15);
			JLabel label = new JLabel("Length");
			label.setLabelFor(lengthField);
			lengthField.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					double length = Double.parseDouble(lengthField.getText());
					double offset = length / 2.0 * Math.sqrt(2.0);
					track = new StraightTrack(length);
					shapePanel.repaint();
					String result = MessageFormat.format("<straight x1=\"{0}\" x2=\"{2}\" y1=\"{1}\" y2=\"{3}\" />", -offset, -offset, offset, offset);
					resultField.setText(result);
				}});
			add(label);
			add(lengthField);
			
			resultField = new JTextField();
			resultField.setColumns(40);
			resultField.setEditable(false);
			label = new JLabel("Result");
			label.setLabelFor(resultField);
			add(label);
			add(resultField);
		}
	}
	
	class CurvePanel extends JPanel {
		JTextField radiusField;
		JTextField extentField;
		JTextField resultField;
		
		public CurvePanel() {
			radiusField = new JTextField("97.5");
			radiusField.setColumns(10);
			extentField = new JTextField("30.0");
			extentField.setColumns(10);
			ActionListener listener = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					double radius = Double.parseDouble(radiusField.getText());
					double extent = Double.parseDouble(extentField.getText());
					double xoffset = radius + radius / 2.0 * Math.sqrt(2.0);
					double yoffset = radius - radius / 2.0 * Math.sqrt(2.0);
					Arc2D arc = new Arc2D.Double(-xoffset, -yoffset, radius * 2.0, radius * 2.0, (90.0 - extent) / 2.0, extent, Arc2D.OPEN);
					track = new CurveTrack(radius, (int) extent);
					log.fine(track.toString());
					shapePanel.repaint();
					String result = MessageFormat.format("<curve x1=\"{0}\" x2=\"{2}\" xc=\"{4}\" y1=\"{1}\" y2=\"{3}\" yc=\"{5}\" />",
							arc.getStartPoint().getX(), arc.getStartPoint().getY(), arc.getEndPoint().getX(), arc.getEndPoint().getY(), 
							arc.getCenterX(), arc.getCenterY());
					resultField.setText(result);
				}};
			
			radiusField.addActionListener(listener);
			extentField.addActionListener(listener);
			JLabel label = new JLabel("Radius");
			label.setLabelFor(radiusField);
			add(label);
			add(radiusField);
			label = new JLabel("Extent");
			label.setLabelFor(extentField);
			add(label);
			add(extentField);

			resultField = new JTextField();
			resultField.setColumns(40);
			resultField.setEditable(false);
			label = new JLabel("Result");
			label.setLabelFor(resultField);
			add(label);
			add(resultField);
		}
	}
	
	class SwitchPanel extends JPanel {
		DynamicPanel[] dynamicPanels;
		
		public SwitchPanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			dynamicPanels = new DynamicPanel[3];
			for (int i = 0; i < 3; i++) {
				dynamicPanels[i] = new DynamicPanel();
				add(dynamicPanels[i]);
			}
		}
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
			// TODO give trackCombo a listener in this scope
			add(trackCombo);
			
			lengthField = new JTextField();
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
		}

		@Override
		public void changedUpdate(DocumentEvent evt) {
			log.fine("got change event: " + evt);
		}

		@Override
		public void insertUpdate(DocumentEvent evt) {
			log.fine("got insert event: " + evt);
		}

		@Override
		public void removeUpdate(DocumentEvent evt) {
			log.fine("got remove event: " + evt);
		}
		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void createAndShowGUI() {
		JFrame f = new JFrame("Track Designer");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new TrackDesigner());
		f.pack();
		f.setVisible(true);
	}

}