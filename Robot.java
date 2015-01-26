package team158;

import java.util.Random;
import battlecode.common.*;

public abstract class Robot {
	public Random rand;
	public RobotController rc;
	public MapLocation ownHQ;
	public MapLocation enemyHQ;
	public int distanceBetweenHQ;
	
	public abstract void move();
	
	protected abstract void actions() throws GameActionException;
}
