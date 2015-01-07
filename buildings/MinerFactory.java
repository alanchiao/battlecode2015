package battlecode2015.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import battlecode2015.utils.Broadcast;
import battlecode2015.utils.DirectionHelper;

public class MinerFactory extends Building {

	protected void actions() throws GameActionException {
		int numMiners = rc.readBroadcast(Broadcast.numMinersCh);
		int miners = rc.readBroadcast(Broadcast.minersProducedCh);
		int ore = rc.readBroadcast(Broadcast.minerOreCh);
		double myOre = rc.getTeamOre();
		if (rc.isCoreReady() && myOre >= 50 && (numMiners < 10 || (ore / miners > 50 && numMiners < hqDistance / 2))) {
			int[] offsets = {0,1,-1,2,-2,3,-3,4};
			int offsetIndex = 0;
			int dirint = rand.nextInt(8);
			while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.MINER)) {
				offsetIndex++;
			}
			if (offsetIndex < 8) {
				rc.broadcast(Broadcast.minersProducedCh, miners + 1);
				rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.MINER);
			}
		}
	}

}
