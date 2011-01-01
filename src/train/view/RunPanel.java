/*
 * $Id$
 * 
 * Copyright © 2010 Jeff Eltgroth.
 * 
 * This file is part of Layout Designer.
 *
 * Layout Designer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Layout Designer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Layout Designer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package train.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import train.TrainApp;
import train.scenery.Branch;
import train.scenery.Junction;
import train.scenery.RailCar;
import train.scenery.SceneryManager;
import train.scenery.Train;
import train.util.Geometry;

@SuppressWarnings("serial")
public class RunPanel extends JPanel {
	public static final int FRAME_DELAY = 33;  // use 33 for 30 FPS, 1000 for slow-motion
	private Timer timer;
	private JPanel layoutPanel;
	private JPanel trainControl;
    private JPanel trainList;
	private ActionListener directionListener;
	private ChangeListener throttleListener;
	private ActionListener frameLoopListener;
	private PropertyChangeListener junctionListener;
	private MouseAdapter mouseAdapter;
	private Set<JunctionControl> junctionControls;
	
	// mouse adapter states
	enum Mode {SELECT, MOVE};

	
	// testing frames to find good frame-rate
	private int frame = 1;
	
	public RunPanel() {
		super(new BorderLayout());
		junctionControls = new HashSet<JunctionControl>();
		
		directionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchDirectionFrom(e.getActionCommand());
			}};
		
		throttleListener = new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				changeThrottle((Number) ((JSpinner)e.getSource()).getValue());
			}};
		
		junctionListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateJunctions();
			}};
			
		frameLoopListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SceneryManager.getInstance().updateScenery();
				// TODO Only repaint visible area
				layoutPanel.repaint();
			}};
		timer = new Timer(FRAME_DELAY, frameLoopListener);
		
		SceneryManager.getInstance().addPropertyChangeListener("junctions", junctionListener);
		SceneryManager.getInstance().addPropertyChangeListener("layout", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				layoutPanel.setSize(SceneryManager.getInstance().getWidth(), 
						SceneryManager.getInstance().getHeight());
				layoutPanel.setPreferredSize(new Dimension(SceneryManager.getInstance().getWidth(), 
						SceneryManager.getInstance().getHeight()));
			}});
		
		// the lower panel with controls for trains
		trainControl = new JPanel(new BorderLayout());
		trainControl.setBackground(Color.BLUE);
		
		// the left panel with user controls
		JPanel controlPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
        SpinnerModel spinnerModel = new SpinnerNumberModel(0, //initial value
                0,   //min
                100, //max
                10); //step
        JSpinner spinner = new JSpinner(spinnerModel);
        spinner.addChangeListener(throttleListener);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		controlPanel.add(spinner, c);
		
		ButtonGroup buttonGroup = new ButtonGroup();

        JToggleButton tbutton = new JToggleButton();
        tbutton.setActionCommand("Backward");
        tbutton.addActionListener(directionListener);
        ImageIcon icon = TrainApp.createImageIcon("images/Back16.gif");
        tbutton.setIcon(icon);
        tbutton.setToolTipText("Makes train go backwards");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
        controlPanel.add(tbutton, c);
        buttonGroup.add(tbutton);

        tbutton = new JToggleButton();
        tbutton.setActionCommand("Forward");
        tbutton.addActionListener(directionListener);
        icon = TrainApp.createImageIcon("images/Forward16.gif");
        tbutton.setIcon(icon);
        tbutton.setToolTipText("Makes train go forwards");
		c.gridx = 1;
		c.gridy = 1;
        controlPanel.add(tbutton, c);
        buttonGroup.add(tbutton);
        
        trainControl.add(controlPanel, BorderLayout.LINE_START);
        
        trainList = new JPanel(new GridLayout(1, 1));
        JScrollPane scrollPane = new JScrollPane(trainList);
        
        trainControl.add(scrollPane, BorderLayout.CENTER);
		
		add(trainControl, BorderLayout.PAGE_END);
		
//		layoutPanel = new JPanel(null) {
//			@Override
//			protected void paintComponent(Graphics g) {
//				super.paintComponent(g);
//		        Graphics2D g2 = (Graphics2D) g;
//		        renderScenery(g2);
//			};
//		};
		
		layoutPanel = new SceneryDisplayPanel2D();
		
		layoutPanel.setSize(SceneryManager.getInstance().getWidth(), 
				SceneryManager.getInstance().getHeight());
		layoutPanel.setPreferredSize(new Dimension(SceneryManager.getInstance().getWidth(), 
				SceneryManager.getInstance().getHeight()));
		
		scrollPane = new JScrollPane(layoutPanel);
		add(scrollPane, BorderLayout.CENTER);
		
		mouseAdapter = new MouseAdapter();
		layoutPanel.addMouseListener(mouseAdapter);
		layoutPanel.addMouseMotionListener(mouseAdapter);
		layoutPanel.addMouseWheelListener(mouseAdapter);
		
	}
	
	public void startFrameLoop(int delay) {
		timer.setInitialDelay(delay);
		timer.start();
	}
	
	public void stopFrameLoop() {
		timer.stop();
	}
	
	public void renderScenery(Graphics2D g2) {
		g2.drawString("" + frame++, 2, 15);
		if (frame > 50) frame = 1;
		
		for (Branch branch : SceneryManager.getInstance().getBranches()) {
			branch.render(g2);
		}
		for (Junction junction : SceneryManager.getInstance().getJunctions()) {
			decorateJunction(junction, g2);
		}
		for (Train train : SceneryManager.getInstance().getTrains()) {
			train.render(g2);
		}

	}
	
	private void decorateJunction(Junction junction, Graphics2D g2) {
		for (Junction.BranchInfo bi : junction.getBranches()) {
			double x = (junction.getLocation().getX() + bi.otherPoint.getX()) / 2.0;
			double y = (junction.getLocation().getY() + bi.otherPoint.getY()) / 2.0;
			String label = "" + bi.pole + ", " + (bi.active ? "A" : "X");
			g2.drawString(label, (int) x, (int) y);
		}
	}
	
	public void switchDirectionFrom(String command) {
		// TODO should probably not change direction when speed is above 0
		// TODO get selected train from trainList
		Train train = SceneryManager.getInstance().getTrains().get(0);
		if ("Forward".equals(command)) {
			train.setDirection(Train.PowerDirection.BACKWARD);
		} else if ("Backward".equals(command)) {
			train.setDirection(Train.PowerDirection.FORWARD);
		}
	}
	
	public void changeThrottle(Number value) {
		// TODO get selected train from trainList
		Train train = SceneryManager.getInstance().getTrains().get(0);
		train.setSpeed(value.intValue() / 10);
	}
	
	public void updateJunctions() {
		//System.out.println("updateJunctions");
		
		// make sure the controls are still in the scenery
		for (JunctionControl control : junctionControls) {
			boolean found = false;
			for (Junction junction : SceneryManager.getInstance().getJunctions()) {
				//System.out.println("Checking junction: " + (control.junction == junction));
				if (control.junction == junction) {
					found = true;
					break;
				}
			}			
			if (!found) {
				//System.out.println("junction missing, removing control");
				layoutPanel.remove(control);
			}				
		}		
		
		// add any new junctions
		for (Junction junction : SceneryManager.getInstance().getJunctions()) {
			boolean found = false;
			for (JunctionControl control : junctionControls) {
				if (control.junction == junction) {
					found = true;
					break;
				}
			}
			if (!found) {
				//System.out.println("adding junction control");
				JunctionControl control = new JunctionControl(junction);
				layoutPanel.add(control);
				junctionControls.add(control);
			}
		}
	}

	class MouseAdapter extends java.awt.event.MouseAdapter {
		Mode mode;
		Point2D lastMousePoint;
		Shape dragShape = null;
		Shape originalShape = null;  // to abort drag operation and restore position
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Point2D mousePoint = e.getPoint();
			lastMousePoint = mousePoint;
			for (Train train : SceneryManager.getInstance().getTrains()) {
				// TODO do not allow user to drag moving trains
				RailCar car = train.findCarAtPoint(mousePoint);
				if (car != null) {
					dragShape = car;
					originalShape = (Shape) car.clone();
					mode = Mode.MOVE;
					Rectangle rect = dragShape.getBounds();
					rect.grow(10, 10);
					layoutPanel.repaint(rect);
					break;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			Point2D mousePoint = e.getPoint();
			if (mode == Mode.MOVE) {
				// place object at destination
				if (dragShape instanceof RailCar) {
					RailCar car = (RailCar) dragShape;
					boolean foundShape = false;
					for (Branch branch : SceneryManager.getInstance().getBranches()) {
						List<Shape> shapes = branch.findShapesAtPoint(mousePoint, Geometry.CLOSE_ENOUGH);
						if (shapes.size() > 0) {
							// first will do
							Shape shape = shapes.get(0);
							Point2D shapeLocation = Geometry.findPointOnShapeNearPoint(shape, mousePoint);
							Point2D carLocation = car.getLoc();
							// TODO this isn't snapping like I hoped
							Geometry.translate(car, shapeLocation.getX() - carLocation.getX(), shapeLocation.getY() - carLocation.getY());
							car.setBranch(branch);
							car.setShape(shape);
							car.setPointTowards(Geometry.getPointsFromShape(shape)[0]);
							car.move(0.0);
							foundShape = true;
							break;
						}
					}
					if (!foundShape) {
						// abort operation
						RailCar origCar = (RailCar) originalShape;
						car.restore(origCar);
					}
					layoutPanel.repaint();
				}
				mode = Mode.SELECT;
				lastMousePoint = null;
				originalShape = null;
				dragShape = null;
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseEntered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseExited(e);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			super.mouseWheelMoved(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point2D mousePoint = e.getPoint();
			if (mode == Mode.MOVE) {
				Geometry.translate(dragShape, mousePoint.getX() - lastMousePoint.getX(), 
						mousePoint.getY() - lastMousePoint.getY());
				Rectangle rect = dragShape.getBounds();
				rect.grow(10, 10);
				layoutPanel.repaint(rect);
			}
			lastMousePoint = mousePoint;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
		
	}
}

class JunctionControl extends JComponent {
	protected Junction junction;
	private final Color[] colors = new Color[]{Color.GREEN, Color.RED}; 
	
	public JunctionControl(Junction junction) {
		super();
		this.junction = junction;
		
		setForeground(colors[0]);
		this.setPreferredSize(new Dimension(15, 15));
		Point2D location = junction.getControlLocation();
		this.setLocation((int) location.getX(), (int) location.getY());
		this.setSize(15, 15);
		
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				toggleJunction();
			};
		});
	}

	protected void toggleJunction() {
		junction.toggleJunction();
		setForeground(colors[0] == getForeground() ? colors[1] : colors[0]);
	}
	
    @Override
    protected void paintComponent(Graphics g) {
        //g.setColor(Color.black);
        Graphics2D g2 = (Graphics2D) g;
        // Get the current transform
        AffineTransform saveAT = g2.getTransform();

        int cx = this.getWidth() / 2;
        int cy = this.getHeight() / 2;
        
        Junction.BranchInfo bi = junction.getActiveBranch();
		// get direction to create indicator of live spur
        double direction = Geometry.calcDirection(junction.getLocation(), bi.otherPoint) - Math.toRadians(90.0);
        g2.rotate(direction, cx, cy);
        //g2.transform(AffineTransform.getRotateInstance(direction, cx, cy));
        
        // create the control shape
        Path2D triangle = new Path2D.Double(Path2D.WIND_EVEN_ODD, 3);
        triangle.moveTo(cx, cy);
        triangle.lineTo(cx - 5, cy + 5);
        triangle.lineTo(cx + 5, cy + 5);
        triangle.closePath();
        //Rectangle rect = new Rectangle(cx - 5, cy - 5, 10, 10);
        g2.fill(triangle);
		BasicStroke wide = new BasicStroke(2.0f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER);
		g2.setStroke(wide);
        g2.drawLine(cx, 0, cx, cy);
        
        //int x = 0;
        //int y = 0;
        //int width = this.getWidth() - 1;
        //int height = this.getHeight() - 1;
        //g2.fill(new Rectangle(x, y, width, height));
			
		// Restore original transform
		g2.setTransform(saveAT);

    }

}
