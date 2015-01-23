package team158.buildings;

import team158.com.Broadcast;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Helipad extends Building {

	public Helipad(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		
		if (Broadcast.isNotInitiated(rc, Broadcast.droneRallyLocationChs)) {
			MapLocation rallyLocation = myLocation.add(myLocation.directionTo(enemyHQ), 6);
			Broadcast.broadcastLocation(rc, Broadcast.droneRallyLocationChs, rallyLocation);
		}
		int numLaunchers = rc.readBroadcast(Broadcast.numLaunchersCh);
		int numDrones = rc.readBroadcast(Broadcast.numDronesCh);
		if (rc.isCoreReady() && numLaunchers > 2 && numDrones < 2 && rc.getTeamOre() >= 125) {
			this.greedySpawn(RobotType.DRONE);
		}
	}
}
