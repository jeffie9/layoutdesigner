package net.sf.layoutdesigner.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


@SuppressWarnings("serial")
public class ShapeDesigner extends JPanel implements ChangeListener {
	private JTabbedPane tabPane;
	private JPanel shapePanel;
	private List<Shape> shapes;
	private Shape shape;
	
	public ShapeDesigner() {
		super(new BorderLayout());
		
		shapes = new ArrayList<Shape>();
		shape = new Line2D.Double();
		
		tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabPane.addTab("Straight", new StraightPanel());
		tabPane.addTab("Curve", new CurvePanel());
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
				
				//g2.draw(shapes.get(tabPane.getSelectedIndex()));
				g2.draw(shape);
				
				// draw dot in center
				Ellipse2D dot = new Ellipse2D.Double(-5, -5, 10, 10);
				g2.fill(dot);
				
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
					shape = new Line2D.Double(-offset, -offset, offset, offset);
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
					shape = arc;
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
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void createAndShowGUI() {
		JFrame f = new JFrame("Shape Designer");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new ShapeDesigner());
		f.pack();
		f.setVisible(true);
	}

}
