package team158.strategies;

import team158.buildings.Headquarters;
import team158.com.Broadcast;
import team158.com.GroupController;
import team158.utils.DirectionHelper;
import team158.utils.Hashing;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class MinerTest extends GameStrategy {
	
	public MinerTest(RobotController rc, GroupController gc, Headquarters hq) {
		super(rc, gc, hq);
	}
	

	public void executeStrategy() throws GameActionException {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		int numBeavers = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		int numSupplyDepots = 0;
		
		RobotInfo beaver = null;
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.MINER) {
				numMiners++;
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
				beaver = r;
			} else if (type == RobotType.MINERFACTORY) {
				numMinerFactories++;
			} else if (type == RobotType.SUPPLYDEPOT) {
				numSupplyDepots++;
			}
		}
		if (rc.isCoreReady()) {
			double ore = rc.getTeamOre();
			if (numBeavers == 0) {
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2,3,-3,4};
				int dirint = DirectionHelper.directionToInt(Direction.EAST);
				while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.BEAVER)) {
					offsetIndex++;
				}
				Direction buildDirection = null;
				if (offsetIndex < 8) {
					buildDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
				}
				if (buildDirection != null && ore >= 100) {
					rc.spawn(buildDirection, RobotType.BEAVER);
				}
			}
			else if (numMinerFactories == 0) {
				if (ore >= 500) {
					rc.broadcast(Broadcast.buildMinerFactoriesCh, beaver.ID);
				}
			}
			else if (numSupplyDepots == 0 && ore >= 100) {
				rc.broadcast(Broadcast.buildSupplyCh, beaver.ID);
			}
			else if (numSupplyDepots < 3 && ore >= 750) {
				rc.broadcast(Broadcast.buildSupplyCh, beaver.ID);
			}
		}
	}
}
