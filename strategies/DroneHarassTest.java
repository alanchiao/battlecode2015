package team158.strategies;

import team158.buildings.Headquarters;
import team158.com.GroupController;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import team158.utils.Hashing;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class DroneHarassTest extends GameStrategy {
	
	private int builderBeaver;
	private int scoutMiner;
	
	public DroneHarassTest(RobotController rc, GroupController gc, Headquarters hq) {
		super(rc, gc, hq);
		
		this.builderBeaver = 0;
		this.scoutMiner = 0;
	}
	
	public void executeStrategy() throws GameActionException {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		int numBeavers = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		int numSupplyDepots = 0;
		int numDrones = 0;
		int numLaunchers = 0;
		int numHelipads = 0;
		int numDronesAttack = 0;
		int numDronesDefense = 0;
		int numAerospaceLabs = 0;
		
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.MINER) {
				numMiners++;
				// one time broadcast for scouting
				if (scoutMiner == 0) {
					scoutMiner = r.ID;
					rc.broadcast(Broadcast.scoutEnemyHQCh, scoutMiner);
				}
			} else if (type == RobotType.DRONE) {
				numDrones++;
				if (Hashing.find(r.ID) == Broadcast.droneGroupAttackCh) {
					numDronesAttack++;
				}							
				else if (Hashing.find(r.ID)  == Broadcast.droneGroupDefenseCh) {
					numDronesDefense++;
				}		
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
				builderBeaver = r.ID;
			} else if (type == RobotType.MINERFACTORY) {
				numMinerFactories++;
			} else if (type == RobotType.SUPPLYDEPOT) {
				numSupplyDepots++;
			} else if (type == RobotType.HELIPAD) {
				numHelipads++;
			} else if (type == RobotType.AEROSPACELAB) {
				numAerospaceLabs++;
			} else if (type == RobotType.LAUNCHER) {
				numLaunchers++;
			} else if (type == RobotType.DRONE) {
				numDrones++;
			}
		}
		
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numDronesCh, numDrones);
		rc.broadcast(Broadcast.numLaunchersCh, numLaunchers);
		rc.broadcast(Broadcast.numBuildingsCh, numMinerFactories + numSupplyDepots + numHelipads + numAerospaceLabs);
		

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
			} else if (numMinerFactories == 0) {
				if (ore >= 500) {
					rc.broadcast(Broadcast.buildMinerFactoriesCh, builderBeaver);
				}
			}
			else if (numHelipads == 0) {
				if (ore >= 300) {
					rc.broadcast(Broadcast.buildHelipadsCh, builderBeaver);
				}
			}
			else if (numSupplyDepots == 0 && ore >= 100) {
				rc.broadcast(Broadcast.buildSupplyCh, builderBeaver);
			}
			else {
				if (ore >= 500 + numHelipads * 200) {
					rc.broadcast(Broadcast.buildHelipadsCh, builderBeaver);
				}
				else if (numSupplyDepots < 3 && ore >= 750) {
					rc.broadcast(Broadcast.buildSupplyCh, builderBeaver);
				}
			}
		}
	}
}
