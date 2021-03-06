/*	Dane Jarvie
 * 	UPI: djar004
 * 	ID: 2521969
 *
 *	======================================================================
 *	AnimationPanel.java : Moves shapes around on the screen according to different paths.
 *	It is the main drawing area where shapes are added and manipulated.
 *	It also contains a popup menu to clear all shapes.
 *	======================================================================
 */

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import sun.audio.*;

public class AnimationPanel extends JComponent implements Runnable {
	private Thread animationThread = null;	// the thread for animation
	
	public ArrayList<MovingShape> shapeArray; //Declaring the MovingShape arraylist to store the shapes.
	
	private int currentShapeType, 	// the current shape type
		currentPath, 				// the current path type
		currentWidth = 50,			// the current width of a shape
		currentHeight = 20;			// the current height of a shape
	private float currentPenWidth = 1;		// the current pen width of a shape
	private Color currentBorderColor = Color.blue;  // the current border colour of a shape
	private int delay = 30; 		// the current animation speed
	JPopupMenu popup;				// popup menu
	
	public Random randInt;			//For a random number generator.
	
	public Image panelImage;		// for the Animation Panel background image.
	
	// ************************************************************************************
	/*
	 * Variables and methods pertaining to the playing of audio clips.  Clips are played whenever
	 * a new MovingHomer object is created and when the background of the AnimationPanel is
	 * toggled to OnePunchMan by the user clicking the mysteryButton.
	 */
	
	public Mixer mixer;
	public Clip currentClip, tempClip, opClip;
	public ArrayList<String> clipNameArray;
	public ArrayList<Clip> clipArray;
	
	/**
	 * A method used to create a clip object from a .wav audio file.
	 * @param filename	A String representing the file name of the audio clip
	 * @return tempClip	The created Clip object
	 */
	public Clip setupClip(String filename){
		Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		mixer = AudioSystem.getMixer(mixInfos[0]); // 0 for Primary Sound Driver, try 1 for Speakers (Realtek HDA)
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		try { tempClip = (Clip)mixer.getLine(dataInfo); }
		catch(LineUnavailableException lue) { lue.printStackTrace(); }
		try {
				URL soundURL = A2.class.getResource(filename);
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
				tempClip.open(audioStream);
		}
		catch (LineUnavailableException lue) { lue.printStackTrace(); }
		catch (UnsupportedAudioFileException uafe) { uafe.printStackTrace(); }
		catch (IOException ioe) { ioe.printStackTrace(); }
		return tempClip;
	}
	
	/**
	 * A method that builds an array of clip objects.
	 */
	public void buildClipArray() {
		clipNameArray = new ArrayList<String>();
		clipArray = new ArrayList<Clip>();
		clipNameArray.add("doh2.wav");
		clipNameArray.add("crap.wav");
		clipNameArray.add("boring.wav");
		
		for (String fName : clipNameArray){
			clipArray.add(setupClip(fName));
			
		}
	}
	//************************************************************************************** 
	 
	
	/** Constructor of the AnimationPanel
		*/
	public AnimationPanel() {
		
		shapeArray = new ArrayList<MovingShape>(); //Creating the MovingShape arraylist.
		
		randInt = new Random();
		panelImage = new ImageIcon("src\\Clouds.jpg").getImage(); // The default background image.
		opClip = setupClip("onePunch.wav"); // The audio clip for the OnePunchMan background.
		buildClipArray(); // The array of audio clips to use when creating new MovingHomer objects.
		
		
		popup = new JPopupMenu(); //create the popup menu
		makePopupMenu();

		// add the mouse event to handle popup menu and create new shape
		addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
			public void mouseClicked( MouseEvent e ) {
				if (animationThread != null) {	// if the animation has started, then
					boolean found = false;
					
					/*
					 * A for loop that checks each shape in the shapeArray to see if it was clicked, if so,
					 * it toggles its selected status.
					 */
					for (int i =0; i < shapeArray.size(); i++){
						if ( shapeArray.get(i).contains( e.getPoint()) ) { // if the mousepoint is within a shape, then set the shape to be selected/deselected
							found = true;
							shapeArray.get(i).setSelected( ! shapeArray.get(i).isSelected() );
						}
					}
					if (! found) createNewShape(e.getX(), e.getY()); // if the mousepoint is not within a shape, then create a new one according to the mouse position
				}
			}
		});
	}

	/** create a new shape
	 * @param x 	the x-coordinate of the mouse position
	 * @param y	the y-coordinate of the mouse position
	 */
	protected void createNewShape(int x, int y) {
		
		// get the margin of the frame
		Insets insets = getInsets();
		int marginWidth = getWidth() - insets.left - insets.right;
		int marginHeight = getHeight() - insets.top - insets.bottom;
		
		// create a new shape dependent on all current properties and the mouse position
		MovingShape a;
		switch (currentShapeType) {
			case 0: { //rectangle
				a = new MovingRectangle(x, y, currentPenWidth, currentWidth, currentHeight, marginWidth, marginHeight, currentBorderColor,currentPath);
				shapeArray.add(a);
				break;
			}
			case 1: { //square
				int side_len = Math.min(currentWidth, currentHeight);
				a =  new MovingSquare(x, y, currentPenWidth, side_len, side_len, marginWidth, marginHeight, currentBorderColor,currentPath);
				shapeArray.add(a);
				break;
			}
			case 2: { //plus sign
				a = new MovingPlus(x, y, currentPenWidth, currentWidth, currentHeight, marginWidth, marginHeight, currentBorderColor,currentPath);
				shapeArray.add(a);
				break;
			}
			case 3: { //grumpy cat
				int side_len = Math.min(currentWidth, currentHeight);
				a = new MovingGrumpyCat(x, y, currentPenWidth, side_len, side_len, marginWidth, marginHeight, currentBorderColor,currentPath);
				shapeArray.add(a);
				break;
			}
			case 4: { //rotating square
				int side_len = Math.min(currentWidth, currentHeight);
				a = new MovingRotatingSquare(x, y, currentPenWidth, side_len, side_len, marginWidth, marginHeight, currentBorderColor,currentPath);
				shapeArray.add(a);
				break;
			}
			case 5: { //Homer
				int side_len = Math.min(currentWidth, currentHeight);
				a = new MovingHomer(x, y, currentPenWidth, side_len, side_len, marginWidth, marginHeight, currentBorderColor,currentPath);
				shapeArray.add(a);
				
				int clipIndex = randInt.nextInt(clipArray.size()); //Generate a random index for a clip to play
				currentClip = clipArray.get(clipIndex); // Get the clip at the random index
				currentClip.setFramePosition(currentClip.getFrameLength()); // Set the frameposition to the end of the first loop
				currentClip.loop(1); // play clip once
				break;
			}
		
		}
		
	}

	/** set the current shape type
	 * @param s	the new shape type
	 */
	public void setCurrentShapeType(int s) {
		currentShapeType = s;
	}

	/** set the current path type and the path type for all currently selected shapes
	 * @param t	the new path type
	 */
	public void setCurrentPathType(int t) {
		currentPath = t;
		
	/*
	 * A for loop that checks each shape in the array to see if it is selected and if so, sets its
	 * path to the current path.
	 */
		for (int i = 0; i < shapeArray.size(); i++){
			if ( shapeArray.get(i).isSelected()) {
				shapeArray.get(i).setPath(currentPath);
			}
		}
	}

	/** set the current width and the width for all currently selected shapes
	 * @param w	the new width value
	 */
	public void setCurrentWidth(int w) {
		currentWidth = w;
		
	/*
	 * A for loop that checks each shape in the array to see if it is selected and if so, sets its
	 * width to the current width.
	 */
		for (int i = 0; i < shapeArray.size(); i++){
			if ( shapeArray.get(i).isSelected()) {
				shapeArray.get(i).setWidth(currentWidth);
			}
		}
	}

	/** set the current height and the height for all currently selected shapes
	 * @param h	the new height value
	 */
	public void setCurrentHeight(int h) {
		currentHeight = h;
		
	/*
	 * A for loop that checks each shape in the array to see if it is selected and if so, sets its
	 * height to the current height.
	 */
		for (int i = 0; i < shapeArray.size(); i++){
			if ( shapeArray.get(i).isSelected()) {
				shapeArray.get(i).setHeight(currentHeight);
			}
		}
	}
	
	public void setCurrentPenWidth(float pw) {
		currentPenWidth = pw;
		
	/*
	 * A for loop that checks each shape in the array to see if it is selected and if so, sets its
	 * pen width to the currentPenWidth.
	 */
		for (int i = 0; i < shapeArray.size(); i++){
			if ( shapeArray.get(i).isSelected()) {
				shapeArray.get(i).setPenWidth(currentPenWidth);
			}
		}
	}

	/** set the current border colour and the border colour for all currently selected shapes
	 * @param bc	the new border colour value
	 */
	public void setCurrentBorderColor(Color bc) {
		currentBorderColor = bc;
	
		/*
		 * A for loop that checks each shape in the array to see if it is selected and if so, sets its
		 * border colour to the current border colour.
		 */
		for (int i = 0; i < shapeArray.size(); i++){
			if ( shapeArray.get(i).isSelected()) {
				shapeArray.get(i).setBorderColor(currentBorderColor);
			}
		}
	}
		

	/** get the current width
	 * @return currentWidth - the width value
	 */
	public int getCurrentWidth() {
		return currentWidth;
	}

	/** get the current height
	 * @return currentHeight - the height value
	 */
	public int getCurrentHeight() {
		return currentHeight;
	}
	
	/**
	 * get the current pen width
	 * @return currentPenWidth
	 */
	public float getCurrentPenWidth() {
		return currentPenWidth;
	}
	
 	/** remove all shapes from our vector
	 */
	public void clearAllShapes() {
		shapeArray = new ArrayList<MovingShape>();
	}

	/**	update the painting area
	 * @param g	the graphics control
	 */
	public void update(Graphics g){
		paint(g);
	}

	/**	move and paint all shapes within the animation area
	 * @param g	the Graphics control
	 */
	public void paintComponent(Graphics g) {
		
		g.drawImage(panelImage, 0, 0, this.getWidth(), this.getHeight(), null);
	/*
	 * A for loop that calls the move() and draw() methods for each moving shape in the
	 * shapeArray.
	 */
		for (int i = 0; i < shapeArray.size(); i++){
			shapeArray.get(i).move();
			shapeArray.get(i).draw(g);
		}
	}

	/** reset the margin size of all shapes from our vector
	 */
	public void resetMarginSize() {
		Insets insets = getInsets();
		int marginWidth = getWidth() - insets.left - insets.right;
		int marginHeight = getHeight() - insets.top - insets.bottom ;
		
		
	/*
	 * A for loop that sets the new margins for each shape in the shapeArray.
	 */
		for (int i = 0; i < shapeArray.size(); i++){
			shapeArray.get(i).setMarginSize(marginWidth, marginHeight);
		}
	}


	// you don't need to make any changes after this line
//***************************************************************************************
	/** create the popup menu for our animation program
	 */
	protected void makePopupMenu() {
		JMenuItem menuItem;
	 // clear all
		menuItem = new JMenuItem("Clear All");
		menuItem.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearAllShapes();
			}
		});
		popup.add(menuItem);
	 }

	/** change the speed of the animation
	 * @param newValue 	the speed of the animation in ms
	 */
	public void adjustSpeed(int newValue) {
		if (animationThread != null) {
			stop();
			delay = newValue;
			start();
		}
	}

	/**	When the "start" button is pressed, start the thread
	 */
	public void start() {
		animationThread = new Thread(this);
		animationThread.start();
	}

	/**	When the "stop" button is pressed, stop the thread
	 */
	public void stop() {
		if (animationThread != null) {
			animationThread = null;
		}
	}

	/** run the animation
	 */
	public void run() {
		Thread myThread = Thread.currentThread();
		while(animationThread==myThread) {
			repaint();
			pause(delay);
		}
	}

	/** Sleep for the specified amount of time
	 */
	private void pause(int milliseconds) {
		try {
			Thread.sleep((long)milliseconds);
		} catch(InterruptedException ie) {}
	}
}
