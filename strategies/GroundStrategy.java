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

public class GroundStrategy extends GameStrategy {

	private int attackGroup;
	private int defendGroup;
	
	public GroundStrategy(RobotController rc, GroupController gc, Headquarters hq) {
		super(rc, gc, hq);	

		this.attackGroup = 1;
		this.defendGroup = 0;
	}
	
	public void executeStrategy() throws GameActionException {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		int numTanks = 0;
		int numTanksDefense = 0;
		int numTanksAttack = 0;
		int numBeavers = 0;
		int numBarracks = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		int numSupplyDepots = 0;
		int numTankFactories = 0;
		
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.TANK) {
				numTanks++;
			} else if (type == RobotType.MINER) {
				numMiners++;
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
			} else if (type == RobotType.TANKFACTORY) {
				numTankFactories++;
			}
			else if (type == RobotType.BARRACKS) {
				numBarracks++;
			} else if (type == RobotType.MINERFACTORY) {
				numMinerFactories++;
			} else if (type == RobotType.SUPPLYDEPOT) {
				numSupplyDepots++;
			}
		}
		
		rc.broadcast(Broadcast.numBeaversCh, numBeavers);
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numBarracksCh, numBarracks);
		rc.broadcast(Broadcast.numMinerFactoriesCh, numMinerFactories);
		rc.broadcast(Broadcast.numSupplyDepotsCh, numSupplyDepots);

		if (rc.isCoreReady()) {
			double ore = rc.getTeamOre();
			// Spawn beavers
			if (numBeavers == 0) {
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2,3,-3,4};
				int dirint = DirectionHelper.directionToInt(hq.myLocation.directionTo(hq.enemyHQ));
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
		}			
	}	
}
