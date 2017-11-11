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
import java.awt.geom.Point2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.border.LineBorder;

public class CurvePanel extends JPanel implements DocumentListener {
    private static final Log log = LogFactory.getLog(CurvePanel.class);
            
    private JTextField txtX1;
    private JTextField txtY1;
    private JTextField txtYC;
    private JTextField txtXC;
    private JPanel canvas;
    private JTextField txtX2;
    private JTextField txtY2;

    /**
     * Create the panel.
     */
    public CurvePanel() {
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
                double x1, y1, xc, yc, x2, y2;
                try {
                    x1 = Double.parseDouble(txtX1.getText());
                    y1 = Double.parseDouble(txtY1.getText());
                    xc = Double.parseDouble(txtXC.getText());
                    yc = Double.parseDouble(txtYC.getText());
                    //x2 = Double.parseDouble(txtX2.getText());
                    //y2 = Double.parseDouble(txtY2.getText());
                } catch (NumberFormatException e) {
                    // probably blank input or bad, ignore this track
                    log.error("Bad curve input", e);
                    return;
                }
                
                Point2D p1 = new Point2D.Double(x1, y1);
                Point2D pc = new Point2D.Double(xc, yc);
                double r = p1.distance(pc);
                
                x2 = Math.sin((Math.PI / 6.0)) * r + xc;
                y2 = -(Math.cos((Math.PI / 6.0)) * r + yc);
                
                txtX2.setText("" + x2);
                txtY2.setText("" + y2);
                Point2D p2 = new Point2D.Double(x2, y2);
                
                Arc2D arc = new Arc2D.Double(x1 - 2.0 * r, y1 - r, r * 2.0, r * 2.0, 0.0, 0.0, Arc2D.OPEN);
                arc.setAngles(p1, p2);
                
                // draw it
                Color saveColor = g2.getColor();
                Stroke saveStroke = g2.getStroke();

                Color color = new Color(0, 0, 0);
                g2.setColor(color);
                Stroke stroke = new BasicStroke(1.0f, 
                        BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_MITER);
                g2.setStroke(stroke);
                
                g2.draw(arc);
                
                // show points
                g2.drawOval((int)x1, (int)y1, 3, 3);
                g2.drawString("1", (int)x1, (int)y1);
                g2.drawOval((int)x2, (int)y2, 3, 3);
                g2.drawString("2", (int)x2, (int)y2);
                g2.drawOval((int)xc, (int)yc, 3, 3);
                g2.drawString("C", (int)xc, (int)yc);
                
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
        panel.setLayout(new GridLayout(0, 4, 0, 0));
        
        JLabel lblX1 = new JLabel("X1");
        panel.add(lblX1);
        
        txtX1 = new JTextField();
        txtX1.getDocument().addDocumentListener(this);
        txtX1.setText("100.0");
        panel.add(txtX1);
        txtX1.setColumns(10);
        
        JLabel lblY1 = new JLabel("Y1");
        panel.add(lblY1);
        
        txtY1 = new JTextField();
        txtY1.getDocument().addDocumentListener(this);
        txtY1.setText("0.0");
        panel.add(txtY1);
        txtY1.setColumns(10);
        
        JLabel lblXC = new JLabel("XC");
        panel.add(lblXC);
        
        txtXC = new JTextField();
        txtXC.getDocument().addDocumentListener(this);
        txtXC.setText("0.0");
        panel.add(txtXC);
        txtXC.setColumns(10);
        
        JLabel lblYC = new JLabel("YC");
        panel.add(lblYC);
        
        txtYC = new JTextField();
        txtYC.getDocument().addDocumentListener(this);
        txtYC.setText("0.0");
        panel.add(txtYC);
        txtYC.setColumns(10);
        
        JLabel lblX2 = new JLabel("X2");
        panel.add(lblX2);
        
        txtX2 = new JTextField();
        txtX2.setText("80.0");
        panel.add(txtX2);
        txtX2.setColumns(10);
        
        JLabel lblY2 = new JLabel("Y2");
        panel.add(lblY2);
        
        txtY2 = new JTextField();
        txtY2.setText("-30.0");
        panel.add(txtY2);
        txtY2.setColumns(10);

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
