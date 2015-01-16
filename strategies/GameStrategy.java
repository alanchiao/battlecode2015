package team158.strategies;

import team158.buildings.Headquarters;
import team158.com.GroupController;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class GameStrategy {
	
	RobotController rc;
	GroupController gc;
	Headquarters hq;
	
	public GameStrategy(RobotController rc, GroupController groupController, Headquarters hq) {
		this.rc = rc;
		this.gc = groupController;
		this.hq = hq;
	}
	
	public abstract void executeStrategy() throws GameActionException;
}
