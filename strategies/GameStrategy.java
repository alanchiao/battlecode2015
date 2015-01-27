package team158.strategies;

import team158.buildings.Headquarters;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public abstract class GameStrategy {
	
	RobotController rc;
	Headquarters hq;
	
	public GameStrategy(RobotController rc, Headquarters hq) {
		this.rc = rc;
		this.hq = hq;
	}
	
	public abstract void executeStrategy() throws GameActionException;
}
