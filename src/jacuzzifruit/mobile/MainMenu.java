/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jacuzzifruit.mobile;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import org.netbeans.microedition.lcdui.SplashScreen;

/**
 * @author alex
 */
public class MainMenu extends MIDlet implements CommandListener {

	private boolean midletPaused = false;
	Display display; //display instance
	//<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
	private SplashScreen splashScreen;
	private ImageView imageView;
	private List imageList;
	private Command exitCommand;
	private Command itemCommand;
	private Command backCommand;
	private Command okCommand;
	private Command backCommand1;
	private Command screenCommand;
	private Font font;
	//</editor-fold>//GEN-END:|fields|0|
	private static final String IMG_PATH = "/jacuzzifruit/mobile/img/";
	//</editor-fold>

	/**
	 * The ImageView constructor.
	 */
	public MainMenu() {
		display = Display.getDisplay(this);
	}

	//<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
	//</editor-fold>//GEN-END:|methods|0|
	//<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
	/**
	 * Initilizes the application.
	 * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
	 */
	private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
		// write pre-initialize user code here
//GEN-LINE:|0-initialize|1|0-postInitialize
		// write post-initialize user code here
	}//GEN-BEGIN:|0-initialize|2|
	//</editor-fold>//GEN-END:|0-initialize|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
	/**
	 * Performs an action assigned to the Mobile Device - MIDlet Started point.
	 */
	public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
		// write pre-action user code here
		switchDisplayable(null, getSplashScreen());//GEN-LINE:|3-startMIDlet|1|3-postAction
		// write post-action user code here
	}//GEN-BEGIN:|3-startMIDlet|2|
	//</editor-fold>//GEN-END:|3-startMIDlet|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
	/**
	 * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
	 */
	public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
		// write pre-action user code here
		switchDisplayable(null, getImageList());//GEN-LINE:|4-resumeMIDlet|1|4-postAction
		// write post-action user code here
	}//GEN-BEGIN:|4-resumeMIDlet|2|
	//</editor-fold>//GEN-END:|4-resumeMIDlet|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
	/**
	 * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
	 * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
	 * @param nextDisplayable the Displayable to be set
	 */
	public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
		// write pre-switch user code here
		Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
		if (alert == null) {
			display.setCurrent(nextDisplayable);
		} else {
			display.setCurrent(alert, nextDisplayable);
		}//GEN-END:|5-switchDisplayable|1|5-postSwitch
		// write post-switch user code here
	}//GEN-BEGIN:|5-switchDisplayable|2|
	//</editor-fold>//GEN-END:|5-switchDisplayable|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
	/**
	 * Called by a system to indicated that a command has been invoked on a particular displayable.
	 * @param command the Command that was invoked
	 * @param displayable the Displayable where the command was invoked
	 */
	public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
		// write pre-action user code here
		if (displayable == imageList) {//GEN-BEGIN:|7-commandAction|1|31-preAction
			if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|1|31-preAction
				// write pre-action user code here
				imageListAction();//GEN-LINE:|7-commandAction|2|31-postAction
				// write post-action user code here
			} else if (command == exitCommand) {//GEN-LINE:|7-commandAction|3|34-preAction
				// write pre-action user code here
				destroyApp(false);             notifyDestroyed();//GEN-LINE:|7-commandAction|4|34-postAction
				// write post-action user code here
			} else if (command == itemCommand) {//GEN-LINE:|7-commandAction|5|36-preAction
				getImageView().setImagePath(IMG_PATH + (imageList.getString(imageList.getSelectedIndex())));
				switchDisplayable(null, getImageView());//GEN-LINE:|7-commandAction|6|36-postAction
				// write post-action user code here
			}//GEN-BEGIN:|7-commandAction|7|54-preAction
		} else if (displayable == imageView) {
			if (command == backCommand1) {//GEN-END:|7-commandAction|7|54-preAction
				// write pre-action user code here
				switchDisplayable(null, getImageList());//GEN-LINE:|7-commandAction|8|54-postAction
				// write post-action user code here
			}//GEN-BEGIN:|7-commandAction|9|16-preAction
		} else if (displayable == splashScreen) {
			if (command == SplashScreen.DISMISS_COMMAND) {//GEN-END:|7-commandAction|9|16-preAction
				// write pre-action user code here
				switchDisplayable(null, getImageList());//GEN-LINE:|7-commandAction|10|16-postAction
				// write post-action user code here
			}//GEN-BEGIN:|7-commandAction|11|7-postCommandAction
		}//GEN-END:|7-commandAction|11|7-postCommandAction
		// write post-action user code here
	}//GEN-BEGIN:|7-commandAction|12|
	//</editor-fold>//GEN-END:|7-commandAction|12|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: splashScreen ">//GEN-BEGIN:|14-getter|0|14-preInit
	/**
	 * Returns an initiliazed instance of splashScreen component.
	 * @return the initialized component instance
	 */
	public SplashScreen getSplashScreen() {
		if (splashScreen == null) {//GEN-END:|14-getter|0|14-preInit
			// write pre-init user code here
			splashScreen = new SplashScreen(getDisplay());//GEN-BEGIN:|14-getter|1|14-postInit
			splashScreen.setTitle("splashScreen");
			splashScreen.setCommandListener(this);
			splashScreen.setText("SPLAAAAAAAASSSSHHHHH");
			splashScreen.setTextFont(getFont());//GEN-END:|14-getter|1|14-postInit
			// write post-init user code here
		}//GEN-BEGIN:|14-getter|2|
		return splashScreen;
	}
	//</editor-fold>//GEN-END:|14-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: imageView ">//GEN-BEGIN:|24-getter|0|24-preInit
	/**
	 * Returns an initiliazed instance of imageView component.
	 * @return the initialized component instance
	 */
	public ImageView getImageView() {
		if (imageView == null) {//GEN-END:|24-getter|0|24-preInit
			// write pre-init user code here
			imageView = new ImageView();//GEN-BEGIN:|24-getter|1|24-postInit
			imageView.setTitle("imageView");
			imageView.addCommand(getBackCommand1());
			imageView.setCommandListener(this);//GEN-END:|24-getter|1|24-postInit
			// write post-init user code here
		}//GEN-BEGIN:|24-getter|2|
		return imageView;
	}
	//</editor-fold>//GEN-END:|24-getter|2|
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: imageList ">//GEN-BEGIN:|29-getter|0|29-preInit
	/**
	 * Returns an initiliazed instance of imageList component.
	 * @return the initialized component instance
	 */
	public List getImageList() {
		if (imageList == null) {//GEN-END:|29-getter|0|29-preInit
			// write pre-init user code here
			imageList = new List("Images", Choice.IMPLICIT);//GEN-BEGIN:|29-getter|1|29-postInit
			imageList.addCommand(getExitCommand());
			imageList.addCommand(getItemCommand());
			imageList.setCommandListener(this);
			imageList.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
			imageList.setSelectedFlags(new boolean[] {  });//GEN-END:|29-getter|1|29-postInit
			for (int i = 3; i <= 5; i++) {
				String imageName = "image" + i + ".jpg";
				imageList.append(imageName, null);
			}
		}//GEN-BEGIN:|29-getter|2|
		return imageList;
	}
	//</editor-fold>//GEN-END:|29-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Method: imageListAction ">//GEN-BEGIN:|29-action|0|29-preAction
	/**
	 * Performs an action assigned to the selected list element in the imageList component.
	 */
	public void imageListAction() {//GEN-END:|29-action|0|29-preAction
		// enter pre-action user code here
		String __selectedString = getImageList().getString(getImageList().getSelectedIndex());//GEN-LINE:|29-action|1|29-postAction
		// enter post-action user code here
	}//GEN-BEGIN:|29-action|2|
	//</editor-fold>//GEN-END:|29-action|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|33-getter|0|33-preInit
	/**
	 * Returns an initiliazed instance of exitCommand component.
	 * @return the initialized component instance
	 */
	public Command getExitCommand() {
		if (exitCommand == null) {//GEN-END:|33-getter|0|33-preInit
			// write pre-init user code here
			exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|33-getter|1|33-postInit
			// write post-init user code here
		}//GEN-BEGIN:|33-getter|2|
		return exitCommand;
	}
	//</editor-fold>//GEN-END:|33-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: itemCommand ">//GEN-BEGIN:|35-getter|0|35-preInit
	/**
	 * Returns an initiliazed instance of itemCommand component.
	 * @return the initialized component instance
	 */
	public Command getItemCommand() {
		if (itemCommand == null) {//GEN-END:|35-getter|0|35-preInit
			// write pre-init user code here
			itemCommand = new Command("Item", Command.ITEM, 0);//GEN-LINE:|35-getter|1|35-postInit
			// write post-init user code here
		}//GEN-BEGIN:|35-getter|2|
		return itemCommand;
	}
	//</editor-fold>//GEN-END:|35-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: backCommand ">//GEN-BEGIN:|39-getter|0|39-preInit
	/**
	 * Returns an initiliazed instance of backCommand component.
	 * @return the initialized component instance
	 */
	public Command getBackCommand() {
		if (backCommand == null) {//GEN-END:|39-getter|0|39-preInit
			// write pre-init user code here
			backCommand = new Command("Back", Command.BACK, 0);//GEN-LINE:|39-getter|1|39-postInit
			// write post-init user code here
		}//GEN-BEGIN:|39-getter|2|
		return backCommand;
	}
	//</editor-fold>//GEN-END:|39-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: okCommand ">//GEN-BEGIN:|49-getter|0|49-preInit
	/**
	 * Returns an initiliazed instance of okCommand component.
	 * @return the initialized component instance
	 */
	public Command getOkCommand() {
		if (okCommand == null) {//GEN-END:|49-getter|0|49-preInit
			// write pre-init user code here
			okCommand = new Command("Ok", Command.OK, 0);//GEN-LINE:|49-getter|1|49-postInit
			// write post-init user code here
		}//GEN-BEGIN:|49-getter|2|
		return okCommand;
	}
	//</editor-fold>//GEN-END:|49-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: backCommand1 ">//GEN-BEGIN:|53-getter|0|53-preInit
	/**
	 * Returns an initiliazed instance of backCommand1 component.
	 * @return the initialized component instance
	 */
	public Command getBackCommand1() {
		if (backCommand1 == null) {//GEN-END:|53-getter|0|53-preInit
			// write pre-init user code here
			backCommand1 = new Command("Back", Command.BACK, 0);//GEN-LINE:|53-getter|1|53-postInit
			// write post-init user code here
		}//GEN-BEGIN:|53-getter|2|
		return backCommand1;
	}
	//</editor-fold>//GEN-END:|53-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: screenCommand ">//GEN-BEGIN:|56-getter|0|56-preInit
	/**
	 * Returns an initiliazed instance of screenCommand component.
	 * @return the initialized component instance
	 */
	public Command getScreenCommand() {
		if (screenCommand == null) {//GEN-END:|56-getter|0|56-preInit
			// write pre-init user code here
			screenCommand = new Command("Screen", Command.SCREEN, 0);//GEN-LINE:|56-getter|1|56-postInit
			// write post-init user code here
		}//GEN-BEGIN:|56-getter|2|
		return screenCommand;
	}
	//</editor-fold>//GEN-END:|56-getter|2|

	//<editor-fold defaultstate="collapsed" desc=" Generated Getter: font ">//GEN-BEGIN:|59-getter|0|59-preInit
	/**
	 * Returns an initiliazed instance of font component.
	 * @return the initialized component instance
	 */
	public Font getFont() {
		if (font == null) {//GEN-END:|59-getter|0|59-preInit
			// write pre-init user code here
			font = Font.getFont(Font.FONT_STATIC_TEXT);//GEN-LINE:|59-getter|1|59-postInit
			// write post-init user code here
		}//GEN-BEGIN:|59-getter|2|
		return font;
	}
	//</editor-fold>//GEN-END:|59-getter|2|

	/**
	 * Returns a display instance.
	 * @return the display instance.
	 */
	public Display getDisplay() {
		return Display.getDisplay(this);
	}

	/**
	 * Exits MIDlet.
	 */
	public void exitMIDlet() {
		switchDisplayable(null, null);
		destroyApp(true);
		notifyDestroyed();
	}

	/**
	 * Called when MIDlet is started.
	 * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
	 */
	public void startApp() {
		if (midletPaused) {
			resumeMIDlet();
		} else {
			initialize();
			startMIDlet();
		}
		midletPaused = false;
	}

	/**
	 * Called when MIDlet is paused.
	 */
	public void pauseApp() {
		midletPaused = true;
	}

	/**
	 * Called to signal the MIDlet to terminate.
	 * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
	 */
	public void destroyApp(boolean unconditional) {
	}
}
