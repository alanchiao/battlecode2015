package battlecode2015.buildings;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode2015.Robot;
import battlecode2015.utils.Broadcast;
import battlecode2015.utils.DirectionHelper;

public class Barracks extends Robot {
	protected void actions() throws GameActionException {
        // get information broadcasted by the HQ
		int numSoldiers = rc.readBroadcast(1);

		MapLocation myLocation = rc.getLocation();
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = DirectionHelper.directionToInt(myLocation.directionTo(rc.senseEnemyHQLocation()));
		if (rc.readBroadcast(Broadcast.soldierRallyCh) == 0) {
			MapLocation rally = myLocation;
			// Move 5 squares away
			for (int i = 0; i < 5; i++) {
				int offsetIndex = 0;
				while (offsetIndex < 8) {
					MapLocation candidate = rally.add(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
					if (rc.senseTerrainTile(candidate).isTraversable()) {
						rally = candidate;
						break;
					}
					offsetIndex++;
				}
			}
			rc.broadcast(Broadcast.soldierRallyCh, rally.x * 256 + rally.y);
		}
		if (rc.isCoreReady() && rc.getTeamOre() >= 60 && numSoldiers < 20) {
			int offsetIndex = 0;
			while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.SOLDIER)) {
				offsetIndex++;
			}
			if (offsetIndex < 8) {
				rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.SOLDIER);
			}
		}
	}
}
