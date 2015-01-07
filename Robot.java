package battlecode2015;

import java.util.Random;
import battlecode.common.*;

public abstract class Robot {
	protected Random rand;
	protected RobotController rc;

	public void setRC(RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
	}
	
	public abstract void move();
	
	protected abstract void actions() throws GameActionException;
}
