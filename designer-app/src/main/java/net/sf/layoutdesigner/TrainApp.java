/*
 * $Id$
 * 
 * Copyright � 2010 Jeff Eltgroth.
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
package net.sf.layoutdesigner;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.layoutdesigner.scenery.Branch;
import net.sf.layoutdesigner.scenery.SceneryManager;
import net.sf.layoutdesigner.view.LayoutEditPanel;
import net.sf.layoutdesigner.view.RunPanel;



@SuppressWarnings("serial")
public class TrainApp {
    JPanel cards; //a panel that uses CardLayout
    final static String FRAME_TEXT = "Train v 0.1";
    final static String RUNPANEL = "Run View";
    final static String LAYOUTEDITPANEL = "Layout Edit View";
    
    LayoutEditPanel layoutEditPanel;
    RunPanel runPanel;
    
    public ActionListener cardSwitchAction;
	public ActionListener toolMenuAction;
	public Action fileOpenAction;
	public Action fileSaveAction;
	public Action fileSaveAsAction;
	public Action selectAllAction;
	public Action pasteAction;
	public Action deleteAction;
	public ActionListener runMenuAction;

	private File activeFile;
	
	public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
    	        UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI(); 
            }
        });
    }
	
	public TrainApp() {
		activeFile = null;
		
        //Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());
        
		// Create panels for card layout
        layoutEditPanel = new LayoutEditPanel();
        runPanel = new RunPanel();
        cards.add(runPanel, RUNPANEL);
        cards.add(layoutEditPanel, LAYOUTEDITPANEL);

        // Common action for Window menu
        cardSwitchAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        CardLayout cl = (CardLayout)(cards.getLayout());
		        cl.show(cards, (String)e.getActionCommand());
			}
        };
        
        toolMenuAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Does nothing now");
			}};
        
		fileOpenAction = new AbstractAction("Open", null, "Load an existing layout",
				KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(cards);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					activeFile = fc.getSelectedFile();
					System.out.println("Loading file: " + activeFile.getName() + ".");
					layoutEditPanel.deselectAllShapes();
					SceneryManager.getInstance().readFromFile(activeFile);
					cards.repaint();
					setFileInTitle(activeFile.getName());
				}
			}};
				
		fileSaveAction = new AbstractAction("Save", null, "Save current layout",
				KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}};
			
		fileSaveAsAction = new AbstractAction("Save As...", null, "Save current layout to a new file",
				0, null) {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFileAs();
			}};
				
        selectAllAction = new AbstractAction("Select All", null, "Select all objects",
        		0, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutEditPanel.selectAllShapes();
			}};
		
		pasteAction = new AbstractAction("Paste", null, "Paste objects from clipboard",
				0, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutEditPanel.paste();
			}};
        
		deleteAction = new AbstractAction("Delete", null, "Delete selected objects",
				0, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutEditPanel.delete();
			}};

		runMenuAction = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ("Start".equals(e.getActionCommand())) {
					runPanel.startFrameLoop(100);
				} else if ("Stop".equals(e.getActionCommand())) {
					runPanel.stopFrameLoop();
				}
			}};

	}
	
	public void saveFile() {
		if (activeFile == null) {
			saveFileAs();
		} else {
			SceneryManager.getInstance().writeToFile(activeFile);
		}
	}

	public void saveFileAs() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(cards);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			activeFile = fc.getSelectedFile();
			setFileInTitle(activeFile.getName());
			System.out.println("Saving as: " + activeFile.getName() + ".");
			SceneryManager.getInstance().writeToFile(activeFile);
		}
	}
	
	public void setFileInTitle(String filename) {
		Frame f = getTopFrame();
		f.setTitle(FRAME_TEXT + " - [" + filename + "]");
	}

    public JMenuBar createMenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        // TODO can set menu text to something different than command
        
        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription(
                "Commands to open and save files");
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = new JMenuItem("New", KeyEvent.VK_N);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Create a new layout");
        menu.add(menuItem);

        menuItem = new JMenuItem(fileOpenAction);
        menu.add(menuItem);

        menuItem = new JMenuItem(fileSaveAction);
        menu.add(menuItem);

        menuItem = new JMenuItem(fileSaveAsAction);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Exit the application");
        menu.add(menuItem);
        
        //Build Edit menu in the menu bar.
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.getAccessibleContext().setAccessibleDescription(
                "Menu for editing layout");
        menuBar.add(menu);
        
        menuItem = new JMenuItem("Cut");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Cut selected objects");
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Copy");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Copy selected objects");
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Paste");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Paste objects from clipboard");
        menuItem.setAction(pasteAction);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem(deleteAction);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Select All");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Select all objects");
        menuItem.setAction(selectAllAction);
        menu.add(menuItem);
        
        //Build Tools menu in the menu bar.
        menu = new JMenu("Layout");
        menu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(menu);
        
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Select");
        rbMenuItem.addActionListener(toolMenuAction);
        menu.add(rbMenuItem);
        group.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Move");
        rbMenuItem.setText("Move");
        rbMenuItem.setActionCommand("Move");
        rbMenuItem.addActionListener(toolMenuAction);
        menu.add(rbMenuItem);
        group.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Rotate");
        rbMenuItem.addActionListener(toolMenuAction);
        menu.add(rbMenuItem);
        group.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Size");
        rbMenuItem.addActionListener(toolMenuAction);
        menu.add(rbMenuItem);
        group.add(rbMenuItem);
        
        rbMenuItem = new JRadioButtonMenuItem("Inspector");
        rbMenuItem.addActionListener(toolMenuAction);
        menu.add(rbMenuItem);
        group.add(rbMenuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Demo Layout");
        menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SceneryManager.getInstance().createTestScenery();
			}});
        menu.add(menuItem);
        
        //Build Run menu in the menu bar.
        menu = new JMenu("Run");
        menu.setMnemonic(KeyEvent.VK_R);
        menuBar.add(menu);
        
        menuItem = new JMenuItem("Start");
        menuItem.addActionListener(runMenuAction);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Stop");
        menuItem.addActionListener(runMenuAction);
        menu.add(menuItem);
        
        //Build Window menu in the menu bar.
        menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(menu);
        
        menuItem = new JMenuItem(RUNPANEL);
        menuItem.addActionListener(cardSwitchAction);
        menu.add(menuItem);

        menuItem = new JMenuItem(LAYOUTEDITPANEL);
        menuItem.addActionListener(cardSwitchAction);
        menu.add(menuItem);

        return menuBar;
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame("Train v 0.1");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        TrainApp app = new TrainApp();
		SceneryManager.getInstance().createTestScenery();
        f.setJMenuBar(app.createMenuBar());
        f.add(app.cards);
        
        // Get the size of the default screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        f.setSize(new Dimension((int)(dim.getWidth() * 2.0/3.0), (int)(dim.getHeight() * 2.0/3.0)));
        //f.setSize(new Dimension(1055,750));
        f.setVisible(true);
    } 
    
	public static Frame getTopFrame() {
		Frame[] frames = Frame.getFrames();
		for (int i = 0; i < frames.length; i++) {
			if (frames[i].getFocusOwner() != null) {
				return frames[i];
			}
		}
		if (frames.length > 0) {
			return frames[0];
		}
		return null;
	}

    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TrainApp.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

	abstract class AbstractAction extends javax.swing.AbstractAction {
		public AbstractAction(String text, ImageIcon icon,
                String desc, Integer mnemonic, KeyStroke accelerator) {
	        super(text, icon);
	        putValue(SHORT_DESCRIPTION, desc);
	        putValue(MNEMONIC_KEY, mnemonic);
	        putValue(ACCELERATOR_KEY, accelerator);
	    }
	}

}
