import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class RobotManager {
	
	//member variables
	
	private Robot r;
	private String windowName;
	private int[] rect;
	private HashMap<String, BufferedImage> images;
	private Point lastPosition;
	
	
	
	
	//contructor
	
	public RobotManager(String windowName) {
		this.windowName = windowName;
		images = new HashMap<String, BufferedImage>();
		try{r = new Robot();}catch(AWTException e){}
		r.setAutoDelay(20);
		r.setAutoWaitForIdle(true);
		lastPosition = MouseInfo.getPointerInfo().getLocation();
		rect = new int[]{0, 0, 0, 0};
		// parse the files
		parse();
	}
	
	
	
	
	
	//public methods
	
	public void move(int x, int y) throws RobotPausedException{
		testPosition();
		updateRect();
		r.mouseMove(x+rect[0], y+rect[1]);
		lastPosition = MouseInfo.getPointerInfo().getLocation();
	}
	
	public void click(int x, int y) throws RobotPausedException {
		move(x, y);
		r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	public void click(String name) throws RobotPausedException {
		Point p = getLocation(name);
		if(p != null)
			click(p.x + images.get(name).getWidth()/2, p.y + images.get(name).getHeight()/2+10);
	}
	
	public void click(String name, int x, int y) throws RobotPausedException{
		Point p = getLocation(name);
		if(p != null)
			click(p.x + x, p.y + y);
	}
	
	public Point getLocation(String name, int[] bounds){
		updateRect();
		BufferedImage image = images.get(name);
		if(image == null)
			return null;
		BufferedImage screenshot = r.createScreenCapture(new Rectangle(bounds[0]+rect[0], bounds[1]+rect[1], bounds[2]-bounds[0], bounds[3]-bounds[1]));
		for(int x=0; x<screenshot.getWidth()-image.getWidth(); x++){
			search:
			for(int y=0; y<screenshot.getHeight()-image.getHeight(); y++){
				for (int i = 0; i < image.getWidth(); i++) {
					for (int j = 0; j < image.getHeight(); j++) {
						if (image.getRGB(i, j) != screenshot.getRGB(i+x, j+y))
							continue search;
					}
				}
				return new Point(x+bounds[0], y+bounds[1]);
			}
		}
		return null;
	}
	
	public Point getLocation(String name){
		updateRect();
		return getLocation(name, new int[]{0, 0, rect[2], rect[3]});
	}
	
	public boolean waitForImage(String name, int timeout){
		long time = System.currentTimeMillis();
		while(System.currentTimeMillis() - time < timeout){
			if(getLocation(name) != null)
				return true;
		}
		return false;
	}
	
	public void keyClick(int keycode) throws RobotPausedException{
		testPosition();
		r.keyPress(keycode);
		r.keyRelease(keycode);
	}
	
	public void keyClick(int keycode, long duration) throws RobotPausedException{
		testPosition();
		r.keyPress(keycode);
		delay(duration);
		r.keyRelease(keycode);
	}
	
	public void write(String s) throws RobotPausedException{
		s = s.toUpperCase();
		for(int i=0; i<s.length(); i++){
			String c = "" + s.charAt(i);
			if(c.equals("."))
				c = "PERIOD";
			else if(c.equals(","))
				c = "COMMA";
			else if(c.equals("\t"))
				c = "TAB";
			else if(c.equals("@")){
				r.keyPress(KeyEvent.VK_SHIFT);
				r.keyPress(KeyEvent.VK_2);
				r.keyRelease(KeyEvent.VK_SHIFT);
				r.keyRelease(KeyEvent.VK_2);
				continue;
			}
			try{keyClick(KeyEvent.class.getField("VK_" + c).getInt(null));}catch(Exception e){}
		}
	}

	public void pause() {
		lastPosition = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public void resume() {
		lastPosition = MouseInfo.getPointerInfo().getLocation();
	}

	public boolean isPaused() {
		return lastPosition.equals(MouseInfo.getPointerInfo().getLocation());
	}
	
	public void delay(long millis){
		try{Thread.sleep(millis);}catch(Exception e){}
	}
	
	public void screenshot(String name, int[] bounds){
		updateRect();
		try{ImageIO.write(r.createScreenCapture(new Rectangle(bounds[0]+rect[0], bounds[1]+rect[1], bounds[2]-bounds[0], bounds[3]-bounds[1])), "png", new File(name + ".png"));}
		catch(Exception e){}
	}
	
	public void screenshot(String name){
		updateRect();
		try{ImageIO.write(r.createScreenCapture(new Rectangle(rect[0], rect[1], rect[2]-rect[0], rect[3]-rect[1])), "png", new File(name + ".png"));}
		catch(Exception e){}
	}
	
	public void parse(){
		images.clear();
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".png") || name.endsWith(".PNG");
		    }
		};
		File folder = new File(System.getProperty("user.dir"));
		for(String s : folder.list(filter)){
			try{images.put(s.substring(0, s.length()-4), ImageIO.read(new File(s)));}
			catch(IOException e){}
		}
	}
	
	
	
	
	
	//private methods
	
	private void testPosition() throws RobotPausedException {
		if (!lastPosition.equals(MouseInfo.getPointerInfo().getLocation())) {
			pause();
			throw new RobotPausedException();
		}
	}

	
	
	
	
	//native libraries
	
	private interface User32 extends StdCallLibrary {
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

		HWND FindWindow(String lpClassName, String lpWindowName);

		int GetWindowRect(HWND handle, int[] rect);
	}
	
	private void updateRect(){
		HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
		if (hwnd == null) {
			throw new WindowNotFoundException();
		}
		int result = User32.INSTANCE.GetWindowRect(hwnd, rect);
		if (result == 0) {
			throw new WindowNotFoundException();
		}
	}
	
	
	
	
	//exceptions
	
	@SuppressWarnings("serial")
	public static class RobotPausedException extends Exception {}
	
	@SuppressWarnings("serial")
	public static class WindowNotFoundException extends RuntimeException {}
}
