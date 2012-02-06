package train.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class ImageTest extends JPanel {

	BufferedImage img;
	
	public ImageTest() {
		setBackground(Color.GREEN);
	       try {
	           img = ImageIO.read(new File("scenery/loco01.png"));
	       } catch (IOException e) {
	       }


	}

	public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);    
        Graphics2D g2 = (Graphics2D) g;
        
        AffineTransform saveTransform = g2.getTransform();
        AffineTransform at = new AffineTransform();
        at.translate(40.0, 40.0);
        at.rotate(Math.toRadians(45.0));
        at.translate(-img.getWidth()/2, -img.getHeight()/2);
        g2.setTransform(at);
        g.drawImage(img, 0, 0, null);
        
        g2.setTransform(saveTransform);
    } 
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void createAndShowGUI() {
		System.out.println("Created GUI on EDT? "
				+ SwingUtilities.isEventDispatchThread());
		JFrame f = new JFrame("Swing Paint Demo");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new ImageTest());
		f.pack();
		f.setVisible(true);
	}

}
