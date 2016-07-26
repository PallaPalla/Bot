import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public class Main {
	
	private static RobotManager r;
	private static StarPlanet sp;
	private static File data;
	private static ArrayList<String[]> loginInfo;
	private static int endCondition;
	private static Calendar calendar;
	
	private static JFrame frame;
	private static JLabel title;
	private static JToggleButton button;
	private static JComboBox<String> selector;
	private static JTextArea logger;
	private static JTextArea text;
	private static JLabel credits;
	
	
	
	public static void main(String[] args) throws Exception{

		r = new RobotManager("MapleStory");
		sp = new StarPlanet(r);
		data = new File("data.txt");
		loginInfo = new ArrayList<String[]>();
		calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		
		createGUI();
		
		int i = 0;
		while(true){
			try{
				if(button.isSelected()){
					try{Thread.sleep(2000);}catch(Exception e){}
					while(i<loginInfo.size()){
						String[] login = loginInfo.get(i);
						if(login.length == 3){
							sp.playUNO(999999999, login[0], login[1], login[2], endCondition);
						}
						else{
							addToLog("Waiting for reset.");
							while(calendar.get(Calendar.HOUR_OF_DAY) > 12){
								try{Thread.sleep(60000);}catch(Exception e){}
							}
							addToLog("Reset detected, resuming the bot.");
						}
						i++;
						r.delay(5000);
					}
					i = 0;
					resetButton("Every account is done.");
				}
				else{
					try{Thread.sleep(500);}catch(Exception e){}
				}
			}
			catch(RobotManager.RobotPausedException e){
				resetButton("The bot was interrupted because the mouse was moved.");
			}
			catch(RobotManager.WindowNotFoundException e){
				resetButton("Maplestory could not be found. Make sure you have maplestory openned in windowed mode.");
			}
			catch(IllegalArgumentException e){
				resetButton("Maplestory is minimized or in full screen.");
			}
			catch(StarPlanet.TimeOutException e){
				resetButton("The bot was idle for too long. Log out and restart the bot.");
			}
		}
		
	}
	
	private static void createGUI(){
		
		// initialize the content pane
		
		JPanel contents = new JPanel(new GridBagLayout());
		GridBagConstraints c;
		final Insets INSETS = new Insets(3, 3, 3, 3);
		
		
		// get the initial data
		String initialText;
		try(Scanner scan = new Scanner(data)){
			initialText = scan.useDelimiter("\\Z").next();
			endCondition = initialText.charAt(initialText.length()-1) - '0';
			initialText = initialText.substring(0, initialText.length()-1);
		}
		catch(Exception e){
			initialText = "username\tpassword\tpin";
			endCondition = -1;
		}
		
		
		// title
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = INSETS;
		title = new JLabel("<html><div style='text-align: center;'><br>Star Planet UNO Bot<br><br></html>", SwingConstants.CENTER);
		contents.add(title, c);
		
		
		// button
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.insets = INSETS;
		c.anchor = GridBagConstraints.CENTER;
		button = new JToggleButton("Start");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(button.isSelected()){
					try{
						if(endCondition < 0){
							resetButton("Please select a quest.");
							return;
						}
						updateData();
					}
					catch(InvalidTextFormatException e2){
						resetButton("Invalid login info format. Use \"user pw pin\".");
						return;
					}
					button.setText("Stop");
					addToLog("Bot started.");
					r.delay(1000);
					r.resume();
				}
				else{
					button.setText("Start");
					addToLog("Bot terminated by the user.");
					r.pause();
				}
			}
		});
		contents.add(button, c);
		
		
		// selector
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.insets = INSETS;
		c.anchor = GridBagConstraints.CENTER;
		selector = new JComboBox<String>(new String[]{"Five wins", "Ten games", "One win", "Five games", "One game", "All quests"});
		selector.setSelectedIndex(endCondition);
		selector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        @SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)e.getSource();
		        String condition = (String)cb.getSelectedItem();
		        try{endCondition = StarPlanet.class.getField(condition.toUpperCase().replace(" ", "_")).getInt(null);}catch(Exception e2){}
			}
		});
		contents.add(selector, c);
		
		// logger
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 2;
		c.insets = INSETS;
		logger = new JTextArea("");
		logger.setEditable(false);
		JScrollPane scrollLogger = new JScrollPane(logger, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollLogger.setPreferredSize(new Dimension(400, 200));
		contents.add(scrollLogger, c);
		
		
		// login info text
		
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 2;
		c.insets = INSETS;
		text = new JTextArea(initialText);
		JScrollPane scrollText = new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollText.setPreferredSize(new Dimension(400, 200));
		contents.add(scrollText, c);
		
		
		// credits
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = INSETS;
		credits = new JLabel("<html><div style='text-align: center;'><br>By Mathieu Bolduc</html>", SwingConstants.CENTER);
		contents.add(credits, c);
		
		
		// pack the frame
		
		frame = new JFrame("Bot");
		frame.setContentPane(contents);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(frame.getSize());
		frame.setVisible(true);
		
	}
	
	private static void resetButton(String s){
		addToLog(s);
		button.setSelected(false);
		button.setText("Start");
		r.pause();
	}
	
	public static void addToLog(String s){
		logger.setText(logger.getText() + "\n" + new SimpleDateFormat("HH:mm.ss").format(new Date()) + " -  " + s);
	}
	
	private static void updateData() {
		
		//update the login info
		loginInfo.clear();
		String loginText = text.getText();
		
		for(String line : loginText.split("\n")){
			if(line.equals("#waitForReset")){
				loginInfo.add(new String[]{"waitForReset"});
			}
			else if(!line.startsWith("#")){
				String[] info = new String[3];
				int i=0;
				for(String word : line.split("[ \t]")){
					if(word != ""){
						if(i >= info.length)
							throw new InvalidTextFormatException();
						info[i] = word;
						i++;
					}
				}
				if(i == info.length)
					loginInfo.add(info);
				else if(i != info.length && i > 0)
					throw new InvalidTextFormatException();
			}
		}
		
		//update the data file
		try(PrintWriter p = new PrintWriter(data)){
			p.print(loginText + endCondition);
		}
		catch(Exception e){}
	}
	
	@SuppressWarnings("serial")
	public static class InvalidTextFormatException extends RuntimeException{}
}
