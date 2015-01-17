package team158.buildings;

import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
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
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
		
		if (Broadcast.isNotInitiated(rc, Broadcast.droneRallyLocationChs)) {
			MapLocation rallyLocation = myLocation;
			// Move 5 squares away
			int rallyDistance = (int)hqDistance / 6;
			for (int i = 0; i < rallyDistance; i++) {
				rallyLocation = rallyLocation.add(DirectionHelper.directions[dirint]);
			}
			Broadcast.broadcastLocation(rc, Broadcast.droneRallyLocationChs, rallyLocation);
		}
		int threshold = rc.readBroadcast(Broadcast.yieldToLaunchers) == 1 ? 450 : 125;
		if (rc.isCoreReady() && rc.getTeamOre() >= threshold && rc.readBroadcast(Broadcast.stopDroneProductionCh) != 1) {
			int offsetIndex = 0;
			while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.DRONE)) {
				offsetIndex++;
			}
			if (offsetIndex < 8) {
				rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.DRONE);
			}
		}
	}

}
