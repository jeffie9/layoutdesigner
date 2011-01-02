package train.util;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


@SuppressWarnings("serial")
public class ShapeDesigner extends JPanel implements ChangeListener {
	private JTabbedPane tabPane;
	private JPanel shapePanel;
	private List<Shape> shapes;
	
	public ShapeDesigner() {
		super(new BorderLayout());
		
		shapes = new ArrayList<Shape>();
		
		tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);
		tabPane.addTab("Straight", makeStraightPanel());
		tabPane.addTab("Curve", makeCurvePanel());
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

	
	private JPanel makeStraightPanel() {
		shapes.add(new Line2D.Double());
		return new JPanel();
	}
	
	private JPanel makeCurvePanel() {
		shapes.add(new Arc2D.Double());
		return new JPanel();
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

				g2.draw(shapes.get(tabPane.getSelectedIndex()));
				
				// draw dot in center
				Ellipse2D dot = new Ellipse2D.Double(getWidth()/2 - 5, getHeight()/2 - 5, 10, 10);
				g2.fill(dot);
			}
		};
		return panel;
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
