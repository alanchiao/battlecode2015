package team158.buildings;

import team158.com.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Barracks extends Building {

	public Barracks(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
        // get information broadcasted by the HQ
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
		if (Broadcast.isNotInitiated(rc, Broadcast.soldierRallyLocationChs)) {
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
			Broadcast.broadcastLocation(rc, Broadcast.soldierRallyLocationChs, rally);
		}
		
		int numSoldiers = rc.readBroadcast(Broadcast.numSoldiersCh);
		if (rc.isCoreReady() && numSoldiers <= 5 && rc.getTeamOre() >= 60) {
			this.greedySpawn(RobotType.SOLDIER);
		}
	}
}
