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
		if (rc.isCoreReady() && rc.getTeamOre() >= 125 && rc.readBroadcast(Broadcast.stopDroneProductionCh) != 1) {
			this.greedySpawn(RobotType.DRONE);
		}
	}

}
