/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jacuzzifruit.mobile;

import java.io.IOException;
import javax.microedition.lcdui.*;

/**
 * @author alex
 */
public class ImageView extends Canvas {

	private String imagePath;

	/**
	 * constructor
	 */
	public ImageView() {
	}

	/**
	 * paint
	 */
	public void paint(Graphics g) {
		try {
			g.setColor(10, 10, 10);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(scaleImage(Image.createImage(imagePath)), getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.VCENTER);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private Image scaleImage(Image image) {
		int sourceWidth = image.getWidth();
		int sourceHeight = image.getHeight();

		int maxWidth = getWidth(), maxHeight = getHeight();

		int newWidth = sourceWidth, newHeight = sourceHeight;

		if(sourceWidth > maxWidth) {
			newWidth = maxWidth;
			newHeight = newWidth * sourceHeight / sourceWidth;
		}
		if(newHeight > maxHeight) {
			newHeight = maxHeight;
			newWidth = newHeight * sourceWidth / sourceHeight;
		}

		Image thumb = Image.createImage(newWidth, newHeight);
		Graphics g = thumb.getGraphics();

		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				g.setClip(x, y, 1, 1);
				int dx = x * sourceWidth / newWidth;
				int dy = y * sourceHeight / newHeight;
				g.drawImage(image, x - dx, y - dy,
						Graphics.LEFT | Graphics.TOP);
			}
		}

		Image immutableThumb = Image.createImage(thumb);

		return immutableThumb;
	}

	/**
	 * Called when a key is pressed.
	 */
	protected void keyPressed(int keyCode) {
	}

	/**
	 * Called when a key is released.
	 */
	protected void keyReleased(int keyCode) {
	}

	/**
	 * Called when a key is repeated (held down).
	 */
	protected void keyRepeated(int keyCode) {
		keyPressed(keyCode);
	}

	/**
	 * Called when the pointer is dragged.
	 */
	protected void pointerDragged(int x, int y) {
	}

	/**
	 * Called when the pointer is pressed.
	 */
	protected void pointerPressed(int x, int y) {
	}

	/**
	 * Called when the pointer is released.
	 */
	protected void pointerReleased(int x, int y) {
	}

	void setImagePath(String string) {
		imagePath = string;
		repaint();
	}
}
