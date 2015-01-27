package team158.buildings;

import team158.com.Broadcast;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Helipad extends Building {

	private int supplyRequestTurn;
	private int supplyRequestID;
	public Helipad(RobotController newRC) {
		super(newRC);
		supplyRequestTurn = 3000;
		supplyRequestID = 0;
	}

	@Override
	protected void actions() throws GameActionException {
		
		int newID = rc.readBroadcast(Broadcast.requestSupplyDroneCh);
		if (newID == 0) {
			supplyRequestTurn = 3000;
			supplyRequestID = 0;
		}
		else if (supplyRequestID != 0) {
			if (newID != supplyRequestID) {
				supplyRequestTurn = Clock.getRoundNum();
				supplyRequestID = newID;
			}
		}
		else {
			supplyRequestTurn = Clock.getRoundNum();
			supplyRequestID = newID;
		}
		int numDrones = rc.readBroadcast(Broadcast.numDronesCh);
		if (rc.isCoreReady() && (Clock.getRoundNum() - supplyRequestTurn >= 5 && numDrones <3) && rc.getTeamOre() >= 125) {
			this.greedySpawn(RobotType.DRONE);
		}
	}
}
