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
		if (rc.readBroadcast(Broadcast.dronesRallyXCh) == 0) {
			MapLocation rally = myLocation;
			// Move 5 squares away
			int rallyDistance = (int)hqDistance / 6;
			for (int i = 0; i < rallyDistance; i++) {
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
			rc.broadcast(Broadcast.dronesRallyXCh, rally.x);
			rc.broadcast(Broadcast.dronesRallyYCh, rally.y);
		}
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
