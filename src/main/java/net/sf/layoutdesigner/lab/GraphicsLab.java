package net.sf.layoutdesigner.lab;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class GraphicsLab {

    private JFrame frmGraphicsLab;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GraphicsLab window = new GraphicsLab();
                    window.frmGraphicsLab.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public GraphicsLab() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmGraphicsLab = new JFrame();
        frmGraphicsLab.setTitle("Graphics Lab");
        frmGraphicsLab.setBounds(100, 100, 450, 300);
        frmGraphicsLab.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        frmGraphicsLab.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        ArcPanel arcPanel = new ArcPanel();
        tabbedPane.addTab("Arc", null, arcPanel, null);
        
        RectPanel rectPanel = new RectPanel();
        tabbedPane.addTab("Rect", null, rectPanel, null);
        
        CurvePanel curvePanel = new CurvePanel();
        tabbedPane.addTab("Curve", curvePanel);
        
    }

}
