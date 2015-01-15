package team158.strategies;

import team158.buildings.Headquarters;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import team158.utils.GroupController;
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
		int numTanksG1 = 0;
		int numTanksG2 = 0;
		int numBeavers = 0;
		int numBarracks = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		int numSupplyDepots = 0;
		int numTankFactories = 0;
		
		int closestBeaver = 0;
		
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.TANK) {
				numTanks++;
				if (Hashing.find(r.ID) == Broadcast.tankGroup1Ch) {
					numTanksG1++;
				}							
				else if (Hashing.find(r.ID)  == Broadcast.tankGroup2Ch) {
					numTanksG2++;
				}			

			} else if (type == RobotType.MINER) {
				numMiners++;
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
				closestBeaver = r.ID;
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
		rc.broadcast(Broadcast.numTanksCh, numTanks);
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numBarracksCh, numBarracks);
		rc.broadcast(Broadcast.numMinerFactoriesCh, numMinerFactories);
		rc.broadcast(Broadcast.numSupplyDepotsCh, numSupplyDepots);
		rc.broadcast(Broadcast.numTankFactoriesCh, numTanks);

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
			// Broadcast to build structures
			else if (numMinerFactories == 0) {
				if (ore >= 500) {
					rc.broadcast(Broadcast.buildMinerFactoriesCh, closestBeaver);
				}
			}
			else if (numSupplyDepots == 0 && ore >= 100) {
				rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
			}
			else if (numBarracks == 0) {
				rc.broadcast(Broadcast.buildBarracksCh, closestBeaver);
				// tell closest beaver to build barracks
			}
			else if (ore >= 500 + numTankFactories * 300) {
				rc.broadcast(Broadcast.buildTankFactoriesCh, closestBeaver);
			}
			else if (numSupplyDepots < 3 && ore >= 500) {
				rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
			}

			int[] groupSize = {numTanksG1, numTanksG2};
			int[] groupCh = {Broadcast.tankGroup1Ch, Broadcast.tankGroup2Ch};
			if (numTanksG1 > 0 || numTanksG2 > 0) {
				gc.stopGroup(RobotType.TANK);
			}
			rc.setIndicatorString(1, Integer.toString(groupSize[attackGroup]));
			rc.setIndicatorString(2, Integer.toString(groupSize[defendGroup]));
			if (numTanks - groupSize[defendGroup] > 20 && groupSize[attackGroup] == 0) {
				gc.groupUnits(groupCh[attackGroup], RobotType.TANK);
				rc.broadcast(groupCh[attackGroup], 1);
			}
			else if (rc.readBroadcast(groupCh[attackGroup]) == 1 && groupSize[attackGroup] < 10) {
				rc.broadcast(groupCh[attackGroup], 0);
				attackGroup = 1 - attackGroup;
				defendGroup = 1 - defendGroup;
			}
			else if (rc.readBroadcast(groupCh[defendGroup]) == -1) {
				gc.unGroup(groupCh[defendGroup]);
			}
		}
	}	
}
