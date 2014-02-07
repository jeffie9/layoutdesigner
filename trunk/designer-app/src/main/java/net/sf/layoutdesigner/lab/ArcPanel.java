package net.sf.layoutdesigner.lab;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.swing.border.LineBorder;

public class ArcPanel extends JPanel implements DocumentListener {
    private static final Log log = LogFactory.getLog(ArcPanel.class);
            
    private JTextField txtX;
    private JTextField txtY;
    private JTextField txtWidth;
    private JTextField txtStart;
    private JTextField txtExtent;
    private JTextField txtHeight;
    private JPanel canvas;

    /**
     * Create the panel.
     */
    public ArcPanel() {
        setLayout(new BorderLayout(0, 0));
        
        canvas = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                log.debug("paintComponent");
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.addRenderingHints(new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON));
                g2.scale(1.0, 1.0);
                AffineTransform saveTransform = g2.getTransform();
                AffineTransform at = AffineTransform.getTranslateInstance(getWidth()/2, getHeight()/2);
                g2.setTransform(at);

                // create the Arc
                double x, y, w, h;
                double start, extent;
                try {
                    x = Double.parseDouble(txtX.getText());
                    y = Double.parseDouble(txtY.getText());
                    w = Double.parseDouble(txtWidth.getText());
                    h = Double.parseDouble(txtHeight.getText());
                    start = Double.parseDouble(txtStart.getText());
                    extent = Double.parseDouble(txtExtent.getText());
                } catch (NumberFormatException e) {
                    // probably blank input or bad, ignore this track
                    log.error("Bad arc input", e);
                    return;
                }
                //double xoffset = radius + radius / 2.0 * Math.sqrt(2.0);
                //double yoffset = radius - radius / 2.0 * Math.sqrt(2.0);
                Arc2D arc = new Arc2D.Double(x, y, w, h, start, extent, Arc2D.OPEN);
                //Arc2D arc = new Arc2D.Double(center.getX() - radius, center.getY() - radius,
                //        radius * 2.0, radius * 2.0, 0.0, 0.0, Arc2D.OPEN);
                //arc.setAngles(start, end);

                // draw it
                Color saveColor = g2.getColor();
                Stroke saveStroke = g2.getStroke();

                //Color color = new Color(192, 192, 192);  // silver-ish
                Color color = new Color(0, 0, 0);
                g2.setColor(color);
                Stroke stroke = new BasicStroke(1.0f, 
                        BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_MITER);
                g2.setStroke(stroke);
                
                g2.draw(arc);
                
                // let's see the bounding rectangle
                g2.draw(arc.getBounds());

                // restore graphics
                g2.setStroke(saveStroke);
                g2.setColor(saveColor);

                // restore the coordinates
                g2.setTransform(saveTransform);
            }
        };
        canvas.setBackground(Color.WHITE);
        canvas.setBorder(new LineBorder(new Color(0, 0, 0)));
        add(canvas, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        add(panel, BorderLayout.SOUTH);
        panel.setLayout(new GridLayout(3, 0, 0, 0));
        
        JLabel lblX = new JLabel("X");
        panel.add(lblX);
        
        txtX = new JTextField();
        txtX.getDocument().addDocumentListener(this);
        txtX.setText("20.0");
        panel.add(txtX);
        txtX.setColumns(10);
        
        JLabel lblY = new JLabel("Y");
        panel.add(lblY);
        
        txtY = new JTextField();
        txtY.getDocument().addDocumentListener(this);
        txtY.setText("20.0");
        panel.add(txtY);
        txtY.setColumns(10);
        
        JLabel lblH = new JLabel("Height");
        panel.add(lblH);
        
        txtHeight = new JTextField();
        txtHeight.getDocument().addDocumentListener(this);
        txtHeight.setText("20.0");
        panel.add(txtHeight);
        txtHeight.setColumns(10);
        
        JLabel lblW = new JLabel("Width");
        panel.add(lblW);
        
        txtWidth = new JTextField();
        txtWidth.getDocument().addDocumentListener(this);
        txtWidth.setText("20.0");
        panel.add(txtWidth);
        txtWidth.setColumns(10);
        
        JLabel lblStart = new JLabel("Start");
        panel.add(lblStart);
        
        txtStart = new JTextField();
        txtStart.getDocument().addDocumentListener(this);
        txtStart.setText("0");
        panel.add(txtStart);
        txtStart.setColumns(10);
        
        JLabel lblExtent = new JLabel("Extent");
        panel.add(lblExtent);
        
        txtExtent = new JTextField();
        txtExtent.getDocument().addDocumentListener(this);
        txtExtent.setText("15.0");
        panel.add(txtExtent);
        txtExtent.setColumns(10);

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        canvas.repaint();
        log.debug("insertUpdate");
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        canvas.repaint();
        log.debug("removeUpdate");
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        canvas.repaint();
        log.debug("changedUpdate");
    }

}
