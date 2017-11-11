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
import java.awt.EventQueue;
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
import net.sf.layoutdesigner.view.TrackDesignerPanel;

import java.awt.event.InputEvent;



@SuppressWarnings("serial")
public class TrainApp {
    private JFrame frame;

    
    JPanel cards; //a panel that uses CardLayout
    final static String FRAME_TEXT = "Train v 0.1";
    final static String RUNPANEL = "Run View";
    final static String LAYOUTEDITPANEL = "Layout Edit View";
    final static String TRACKDESIGNERPANEL = "Track Designer";
    
    LayoutEditPanel layoutEditPanel;
    RunPanel runPanel;
    TrackDesignerPanel trackDesignerPanel;
    
    public ActionListener cardSwitchAction;
	public ActionListener toolMenuAction;
	public Action fileOpenAction = new FileOpenAction();
	public Action fileSaveAction = new FileSaveAction();
	public Action fileSaveAsAction = new FileSaveAsAction();
	public Action selectAllAction = new SelectAllAction();
	public Action pasteAction = new PasteAction();
	public Action deleteAction = new DeleteAction();
	public ActionListener runMenuAction;

	private File activeFile;
	
	public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.put("swing.boldMetal", Boolean.FALSE);
                    TrainApp window = new TrainApp();
                    SceneryManager.getInstance().createTestScenery();
                    // Get the size of the default screen
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    window.frame.setSize(new Dimension((int)(dim.getWidth() * 2.0/3.0), (int)(dim.getHeight() * 2.0/3.0)));
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
	
	public TrainApp() {
	    initialize();
	}
	
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Train v 0.1");
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new CardLayout(0, 0));
        
        runPanel = new RunPanel();
        frame.getContentPane().add(runPanel, RUNPANEL);
        layoutEditPanel = new LayoutEditPanel();
        frame.getContentPane().add(layoutEditPanel, LAYOUTEDITPANEL);
        trackDesignerPanel = new TrackDesignerPanel();
        frame.getContentPane().add(trackDesignerPanel, TRACKDESIGNERPANEL);
        
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);
        
        JMenuItem mntmNew = new JMenuItem("New");
        mnFile.add(mntmNew);
        
        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.setAction(fileOpenAction);
        mnFile.add(mntmOpen);
        
        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.setAction(fileSaveAction);
        mnFile.add(mntmSave);
        
        JMenuItem mntmSaveAs = new JMenuItem("Save As...");
        mntmSaveAs.setAction(fileSaveAsAction);
        mnFile.add(mntmSaveAs);
        
        JMenuItem mntmExit = new JMenuItem("Exit");
        mnFile.add(mntmExit);
        
        JMenu mnEdit = new JMenu("Edit");
        menuBar.add(mnEdit);
        
        JMenuItem mntmCut = new JMenuItem("Cut");
        mnEdit.add(mntmCut);
        
        JMenuItem mntmCopy = new JMenuItem("Copy");
        mnEdit.add(mntmCopy);
        
        JMenuItem mntmPaste = new JMenuItem("Paste");
        mntmPaste.setAction(pasteAction);
        mnEdit.add(mntmPaste);
        
        JMenuItem mntmDelete = new JMenuItem("Delete");
        mntmDelete.setAction(deleteAction);
        mnEdit.add(mntmDelete);
        
        JMenuItem mntmSelectAll = new JMenuItem("Select All");
        mntmSelectAll.setAction(selectAllAction);
        mnEdit.add(mntmSelectAll);
        
        JMenu mnLayout = new JMenu("Layout");
        menuBar.add(mnLayout);
        
        JMenu mnRun = new JMenu("Run");
        menuBar.add(mnRun);
        
        JMenuItem mntmStart = new JMenuItem("Start");
        mntmStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runPanel.startFrameLoop(100);
            }
        });
        mnRun.add(mntmStart);
        
        JMenuItem mntmStop = new JMenuItem("Stop");
        mntmStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runPanel.stopFrameLoop();
            }
        });
        mnRun.add(mntmStop);
        
        JMenu mnWindow = new JMenu("Window");
        menuBar.add(mnWindow);
        
        JMenuItem mntmRunView = new JMenuItem(RUNPANEL);
        mntmRunView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(frame.getContentPane().getLayout());
                cl.show(frame.getContentPane(), RUNPANEL);
            }
        });
        mnWindow.add(mntmRunView);
        
        JMenuItem mntmLayoutEditView = new JMenuItem(LAYOUTEDITPANEL);
        mntmLayoutEditView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(frame.getContentPane().getLayout());
                cl.show(frame.getContentPane(), LAYOUTEDITPANEL);
            }
        });
        mnWindow.add(mntmLayoutEditView);
        
        JMenuItem mntmTrackDesigner = new JMenuItem("Track Designer");
        mntmTrackDesigner.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(frame.getContentPane().getLayout());
                cl.show(frame.getContentPane(), TRACKDESIGNERPANEL);
            }
        });
        mnWindow.add(mntmTrackDesigner);

                
    }

	
	private void oldConstructor() {
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
        f.getContentPane().add(app.cards);
        
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

    private class FileOpenAction extends AbstractAction {
        public FileOpenAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME, "Open");
            putValue(SHORT_DESCRIPTION, "Load an existing layout");
        }
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
        }
    }
    
    private class FileSaveAction extends AbstractAction {
        public FileSaveAction() {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
            putValue(NAME, "Save");
            putValue(SHORT_DESCRIPTION, "Save current layout");
        }
        public void actionPerformed(ActionEvent e) {
            saveFile();
        }
    }
    
    private class FileSaveAsAction extends AbstractAction {
        public FileSaveAsAction() {
            putValue(NAME, "Save As...");
            putValue(SHORT_DESCRIPTION, "Save current layout to a new file");
        }
        public void actionPerformed(ActionEvent e) {
            saveFileAs();
        }
    }
    
    private class SelectAllAction extends AbstractAction {
        public SelectAllAction() {
            putValue(NAME, "Select All");
            putValue(SHORT_DESCRIPTION, "Select all objects");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            layoutEditPanel.selectAllShapes();
        }
    }
    
    private class PasteAction extends AbstractAction { 
        public PasteAction() {
            putValue(NAME, "Paste");
            putValue(SHORT_DESCRIPTION, "Paste objects from clipboard");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK)); 
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            layoutEditPanel.paste();
        }
    }
    
    private class DeleteAction extends AbstractAction {
        public DeleteAction() {
            putValue(NAME, "Delete");
            putValue(SHORT_DESCRIPTION, "Delete selected objects");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            layoutEditPanel.delete();
        }};

}
