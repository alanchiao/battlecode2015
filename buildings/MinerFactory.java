package team158.buildings;

import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class MinerFactory extends Building {

	public MinerFactory(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		int numMiners = rc.readBroadcast(Broadcast.numMinersCh);
		int miners = rc.readBroadcast(Broadcast.minersProducedCh);
		int oreX100 = rc.readBroadcast(Broadcast.minerOreX100Ch);
		boolean slowDownMining = rc.readBroadcast(Broadcast.slowMinerProductionCh) == 1;
		double myOre = rc.getTeamOre();
		if (rc.isCoreReady() && myOre >= 50 && ((numMiners < 10 && !slowDownMining)
				|| (oreX100 / miners > 5000 && numMiners < hqDistance / 2))) {
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
