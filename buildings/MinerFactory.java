package battlecode2015.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import battlecode2015.utils.Broadcast;
import battlecode2015.utils.DirectionHelper;

public class MinerFactory extends Building {

	protected void actions() throws GameActionException {
		int numMiners = rc.readBroadcast(Broadcast.numMinersCh);
		if (rc.isCoreReady() && rc.getTeamOre() >= 50 && numMiners < 20) {
			int[] offsets = {0,1,-1,2,-2,3,-3,4};
			int offsetIndex = 0;
			int dirint = rand.nextInt(8);
			while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.MINER)) {
				offsetIndex++;
			}
			if (offsetIndex < 8) {
				rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.MINER);
			}
		}
	}

}
