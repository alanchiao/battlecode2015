package team158;

import java.util.Random;
import battlecode.common.*;

public abstract class Robot {
	protected Random rand;
	protected RobotController rc;
	public MapLocation ownHQ;
	public MapLocation enemyHQ;
	public int distanceBetweenHQ;
	
	public abstract void move();
	
	protected abstract void actions() throws GameActionException;
}
