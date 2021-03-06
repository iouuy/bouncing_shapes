/*	Dane Jarvie
 * 	UPI: djar004
 * 	ID: 2521969
 * 
 * ======================================================================
 *	MovingImage.java : An extension of MovingSquare and the superclass for all
 *	image based objects, ie MovingGrumpyCat and MovingHomer.  I have extended
 *	MovingSquare here to keep the aspect ratio of these two sqaure images the same.
 *	A better implementation would be to over-ride the setWidth and setHeight
 *	methods to allow for rectangular images but still maintaining aspect ratio
 *	when re-sizing.
 *	======================================================================
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class MovingImage extends MovingSquare{
	
	protected Image img;
	
	public MovingImage(){
		super();
	}
	
	public MovingImage(int x, int y, float pw, int w, int h,  int mw, int mh, Color bc, int pathType) {
		super(x ,y , pw, w, h ,mw ,mh ,bc, pathType);
	}
	
	public boolean contains(Point mousePt) {
		return (p.x <= mousePt.x && mousePt.x <= (p.x + width + 1)	&&	p.y <= mousePt.y && mousePt.y <= (p.y + height + 1));
	}

	/** draw the image with a width and height equal to max(currentWidth, currentHeight)
	 *	If it is selected, draw the handles
	 *	@param g	the Graphics control
	 */
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(penWidth));
		g2d.setPaint(borderColor);
		g2d.drawImage(img, p.x, p.y, side_len, side_len, null);
		drawHandles(g);
	}

}
