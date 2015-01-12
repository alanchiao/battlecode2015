package team158.buildings;

import team158.utils.DirectionHelper;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class Helipad extends Building {

	@Override
	protected void actions() throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
		
		if (rc.isCoreReady() && rc.getTeamOre() >= 125) {
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
