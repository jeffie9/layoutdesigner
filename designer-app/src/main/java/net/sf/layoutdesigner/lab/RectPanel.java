package net.sf.layoutdesigner.lab;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RectPanel extends JPanel implements DocumentListener {
    private static final Log log = LogFactory.getLog(RectPanel.class);
    private JTextField txtX;
    private JTextField txtY;
    private JTextField txtW;
    private JTextField txtH;
    private JPanel canvasPanel;

    /**
     * Create the panel.
     */
    public RectPanel() {
        setLayout(new BorderLayout(0, 0));
        
        canvasPanel = new JPanel(){
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
                // put origin at center of canvas
                AffineTransform at = AffineTransform.getTranslateInstance(getWidth()/2, getHeight()/2);
                g2.setTransform(at);

                // create the Rectangle
                double x, y, w, h;
                try {
                    x = Double.parseDouble(txtX.getText());
                    y = Double.parseDouble(txtY.getText());
                    w = Double.parseDouble(txtW.getText());
                    h = Double.parseDouble(txtH.getText());
                } catch (NumberFormatException e) {
                    // probably blank input or bad, ignore this track
                    log.error("Bad rectangle input", e);
                    return;
                }

                Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);

                // draw it
                Color saveColor = g2.getColor();
                Stroke saveStroke = g2.getStroke();

                Color color = new Color(0, 0, 0);
                g2.setColor(color);
                Stroke stroke = new BasicStroke(1.0f, 
                        BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_MITER);
                g2.setStroke(stroke);
                
                g2.draw(rect);
                
                // restore graphics
                g2.setStroke(saveStroke);
                g2.setColor(saveColor);

                // restore the coordinates
                g2.setTransform(saveTransform);
                
            }
        };
        add(canvasPanel, BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel();
        add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setLayout(new GridLayout(2, 0, 0, 0));
        
        JLabel lblX = new JLabel("X");
        inputPanel.add(lblX);
        
        txtX = new JTextField();
        txtX.getDocument().addDocumentListener(this);
        txtX.setText("20.0");
        inputPanel.add(txtX);
        txtX.setColumns(10);
        
        JLabel lblY = new JLabel("Y");
        inputPanel.add(lblY);
        
        txtY = new JTextField();
        txtY.getDocument().addDocumentListener(this);
        txtY.setText("20.0");
        inputPanel.add(txtY);
        txtY.setColumns(10);
        
        JLabel lblW = new JLabel("W");
        inputPanel.add(lblW);
        
        txtW = new JTextField();
        txtW.getDocument().addDocumentListener(this);
        txtW.setText("20.0");
        inputPanel.add(txtW);
        txtW.setColumns(10);
        
        JLabel lblH = new JLabel("H");
        inputPanel.add(lblH);
        
        txtH = new JTextField();
        txtH.getDocument().addDocumentListener(this);
        txtH.setText("20.0");
        inputPanel.add(txtH);
        txtH.setColumns(10);

    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        canvasPanel.repaint();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        canvasPanel.repaint();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        canvasPanel.repaint();
    }

}
