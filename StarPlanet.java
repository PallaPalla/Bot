import java.awt.Point;
import java.awt.event.KeyEvent;

public class StarPlanet {
	
	private RobotManager r;
	private static final int TIMEOUT = 20000, MAX_CARDS = 16;
	private static final int[] INGAME_BOUNDS = {350, 500, 600, 750};
	
	public static final int FIVE_WINS = 0, TEN_GAMES = 1, ONE_WIN = 2, FIVE_GAMES = 3, ONE_GAME = 4, ALL_QUESTS = 5;
	
	public StarPlanet(RobotManager r){
		this.r = r;
	}
	
	public void logout() throws RobotManager.RobotPausedException{
		r.keyClick(KeyEvent.VK_ESCAPE);
		r.delay(200);
		r.keyClick(KeyEvent.VK_UP);
		r.delay(200);
		r.keyClick(KeyEvent.VK_ENTER);
	}
	
	public void playUNO(long timeLimit, String username, String password, String pin, int endCondition) throws RobotManager.RobotPausedException {
		long time = Long.MAX_VALUE;
		String lastMap = "";
		String name = username.split("@")[0];
		int games = 0;
		timeLimit += System.currentTimeMillis();
		
		while(System.currentTimeMillis() < timeLimit){
			
			if(r.getLocation("inGame") != null){
				//inGame
				time = Long.MAX_VALUE;
				lastMap = "inGame";
				Point p = r.getLocation("inGame");
				if(p == null)
					continue;
				int i;
				for(i=0; i<MAX_CARDS*3 && r.getLocation("waiting", INGAME_BOUNDS) == null && r.getLocation("next", INGAME_BOUNDS) == null; i++){
					r.click(p.x + 155 + 18*(i % MAX_CARDS), p.y + 50);
					r.delay(10);
				}
				r.delay(100);
				if(i == MAX_CARDS*3)
					r.click(p.x + 175, p.y-150);
			}
			
			else if(r.getLocation("lobby2") != null){
				//lobby 2
				time = Long.MAX_VALUE;
				if(lastMap.equals("inGame")){
					games++;
					Main.addToLog(name + " has completed " + games + " games.");
				}
				lastMap = "lobby2";
				r.click("lobby2", 60, 380);
				r.delay(1000);
				if(r.getLocation("requestGame") == null){
					r.keyClick(KeyEvent.VK_ENTER);
					r.keyClick(KeyEvent.VK_SPACE);
					r.click("lobby2", 240, 500);
					r.delay(1000);
					r.keyClick(KeyEvent.VK_DOWN);
					r.delay(500);
					r.keyClick(KeyEvent.VK_SPACE);
					r.delay(500);
				}
				if(finishQuest(endCondition)){
					r.keyClick(KeyEvent.VK_ESCAPE);
					r.delay(1000);
					/*
					r.click("lobby2", 240, 500);
					r.delay(1000);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(500);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(500);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(500);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(1000);
					r.keyClick(KeyEvent.VK_ENTER, 500);
					r.delay(8000);
					if(cashShop())
						Main.addToLog(name + " accepted his reward points in the cash shop.");
					*/
					logout();
					Main.addToLog(name + " has finished his quests. Logging off.");
					r.delay(8000);
					return;
				}
				acceptGame();
			}
			
			else if(r.getLocation("login") != null){
				//login
				time = Long.MAX_VALUE;
				lastMap = "login";
				Point p = r.getLocation("login");
				r.click(p.x+60, p.y-60);
				r.click(p.x+60, p.y-60);
				r.write(username + "\t" + password);
				r.keyClick(KeyEvent.VK_ENTER);
				r.delay(2000);
			}
			
			else if(r.getLocation("worldSelection") != null){
				//world selection
				time = Long.MAX_VALUE;
				lastMap = "worldSelection";
				r.keyClick(KeyEvent.VK_ENTER);
				r.delay(500);
				r.keyClick(KeyEvent.VK_ENTER);
				r.delay(2000);
			}
			
			else if(r.getLocation("charSelection") != null){
				//char selection
				time = Long.MAX_VALUE;
				lastMap = "charSelection";
				r.keyClick(KeyEvent.VK_ENTER);
				r.delay(500);
				r.keyClick(KeyEvent.VK_ESCAPE);
				
				//enter the pin
				for(int i=0; i<pin.length(); i++){
					r.move(20, 300);
					r.delay(200);
					r.click("pin" + pin.charAt(i));
				}
				r.move(20, 300);
				r.delay(500);
				r.click("pinOK");
				r.delay(8000);
			}
			
			else if(r.getLocation("entrance") != null){
				//entrance
				//move a bit and then enter
				time = Long.MAX_VALUE;
				if(lastMap.equals("charSelection")){
					Main.addToLog("Successfully logged on " + name);
				}
				lastMap = "entrance";
				r.delay(5000);
				r.click(20, 300);
				r.delay(500);
				r.keyClick(KeyEvent.VK_RIGHT, 500);
				r.delay(1000);
				r.keyClick(KeyEvent.VK_UP, 500);
				r.delay(1000);
				r.keyClick(KeyEvent.VK_ENTER, 3000);
				r.delay(2000);
			}
			
			else if(r.getLocation("taxi") != null){
				//taxi
				//press enter
				time = Long.MAX_VALUE;
				lastMap = "taxi";
				r.delay(2000);
				r.click(20, 300);
				r.delay(500);
				r.keyClick(KeyEvent.VK_ENTER, 200);
				r.delay(2000);
			}
			
			else if(r.getLocation("lobby1") != null){
				//lobby 1
				//walk right and talk to the bunny
				lastMap = "lobby1";
				r.delay(2000);
				r.click(20, 300);
				r.delay(500);
				time = System.currentTimeMillis();
				while(System.currentTimeMillis() - time < TIMEOUT*2){
					r.keyClick(KeyEvent.VK_RIGHT, 500);
					Point p = r.getLocation("unoSign");
					if(p != null){
						r.delay(4000);
						r.click("unoSign", 20, 400);
						break;
					}
				}
				if(System.currentTimeMillis() - time >= TIMEOUT*2)
					throw new TimeOutException();
				time = Long.MAX_VALUE;
				r.delay(500);
				acceptGame();
			}
			
			else {
				r.delay(100);
				r.click(20, 300);
				r.delay(100);
				r.keyClick(KeyEvent.VK_ENTER);
				r.delay(200);
				r.click(20, 300);
				if(time == Long.MAX_VALUE){
					//the map is not known, start the timer
					time = System.currentTimeMillis();
				}
				else if(System.currentTimeMillis() - time > TIMEOUT){
					//its been too long since we have seen a known map
					throw new TimeOutException();
				}
			}
			
		}
	}
	
	private void acceptGame() throws RobotManager.RobotPausedException {
		int i = -1;
		Point p;
		do{
			r.delay(500);
			p = r.getLocation("requestGame");
			i++;
		}while(i < 5 && p == null);
		
		if(p == null){
			r.keyClick(KeyEvent.VK_ESCAPE);
			r.delay(500);
			r.keyClick(KeyEvent.VK_ESCAPE);
			r.delay(500);
			logout();
			return;
		}
		
		while(r.getLocation("inGame") == null){
			r.click(p.x + 20, p.y + 20);
			r.keyClick(KeyEvent.VK_ENTER);
			r.delay(1000);
		}
	}
	
	private boolean finishQuest(int condition) throws RobotManager.RobotPausedException{
		r.keyClick(KeyEvent.VK_LEFT, 1000);
		r.keyClick(KeyEvent.VK_Q);
		r.delay(2000);
		Point p = r.getLocation("quest");
		if(p == null)
			return true;
		
		boolean result = true;
		if(condition != ALL_QUESTS){
			r.click(p.x + 100, p.y + 50 + condition*22);
			r.delay(500);
			result = r.getLocation("finishQuest") != null;
		}
		else{
			for(int i=0; i<2; i++){
				r.click(p.x + 100, p.y + 50 + i*22);
				r.delay(500);
				if(r.getLocation("finishQuest") == null)
					result = false;
			}
		}
		int yIncrement = 0;
		if(result){
			for(int i=0; i<5; i++){
				r.click(p.x + 100, p.y + 50 + yIncrement*22);
				r.delay(500);
				if(r.getLocation("finishQuest") != null){
					r.click("finishQuest");
					r.delay(1000);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(500);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(500);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(500);
					r.keyClick(KeyEvent.VK_SPACE, 200);
					r.delay(2000);
				}
				else{
					yIncrement++;
				}
			}
		}
		r.keyClick(KeyEvent.VK_LEFT, 1000);
		r.keyClick(KeyEvent.VK_Q);
		r.delay(500);
		return result;
		
	}
	
	public boolean cashShop() throws RobotManager.RobotPausedException{
		r.keyClick(KeyEvent.VK_LEFT, 1000);
		r.keyClick(KeyEvent.VK_BACK_QUOTE);
		r.delay(10000);
		r.keyClick(KeyEvent.VK_ENTER, 200);
		r.delay(1000);
		r.keyClick(KeyEvent.VK_ENTER, 200);
		r.delay(1000);
		r.keyClick(KeyEvent.VK_ENTER, 200);
		r.delay(1000);
		r.keyClick(KeyEvent.VK_ENTER, 200);
		r.delay(1000);
		if(r.getLocation("cashShopExit") != null){
			r.click("cashShopExit", 5, 0);
			r.delay(8000);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("serial")
	public class TimeOutException extends RuntimeException{}
}
