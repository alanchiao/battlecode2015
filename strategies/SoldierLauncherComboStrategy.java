package team158.strategies;

import team158.buildings.Headquarters;
import team158.com.Broadcast;
import team158.com.GroupController;
import team158.utils.DirectionHelper;
import team158.utils.Hashing;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.MapLocation;

public class SoldierLauncherComboStrategy extends GameStrategy {
	
	private boolean enemyRush;
	private int pathDifficulty;
	private int attackGroup;
	private int defendGroup;
	
	private int scoutMiner;
	
	public SoldierLauncherComboStrategy(RobotController rc, GroupController gc, Headquarters hq) {
		super(rc, gc, hq);
		
		this.attackGroup = 1;
		this.defendGroup = 0;
		this.enemyRush = false;
		this.pathDifficulty = 0;
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
		int numLaunchersAttack = 0;
		int numLaunchersDefense = 0;
		int numSoldiers = 0;
		int numSoldiersAttack = 0;
		int numHelipads = 0;
		int numAerospaceLabs = 0;
		int numBarracks = 0;
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
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
			} else if (type == RobotType.SOLDIER) {
				numSoldiers++;
				if (Hashing.find(r.ID) == Broadcast.soldierGroupAttackCh) {
					numSoldiersAttack++;
				}	
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
				if (Hashing.find(r.ID) == Broadcast.launcherGroupAttackCh) {
					numLaunchersAttack++;
				}							
				else if (Hashing.find(r.ID)  == Broadcast.launcherGroupDefenseCh) {
					numLaunchersDefense++;
				}	
			} else if (type == RobotType.BARRACKS) {
				numBarracks++;
			}
		}
		
		rc.broadcast(Broadcast.numMinerFactoriesCh, numMinerFactories);
		rc.broadcast(Broadcast.numSupplyDepotsCh, numSupplyDepots);
		rc.broadcast(Broadcast.numAerospaceLabsCh, numAerospaceLabs);
		rc.broadcast(Broadcast.numHelipadsCh, numHelipads);
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numDronesCh, numDrones);
		rc.broadcast(Broadcast.numLaunchersCh, numLaunchers);
		rc.broadcast(Broadcast.numBeaversCh, numBeavers);
		rc.broadcast(Broadcast.numBarracksCh, numBarracks);
		rc.broadcast(Broadcast.numSoldiersCh, numSoldiers);

		if (pathDifficulty == 0) {
			int possibleDifficulty = rc.readBroadcast(Broadcast.scoutEnemyHQCh);
			if (possibleDifficulty != scoutMiner) {
				pathDifficulty = possibleDifficulty;
			}
		}

		if (rc.isCoreReady()) {
			double ore = rc.getTeamOre();
			if (numBeavers == 0 || (numBeavers == 1 && numMinerFactories >= 1)) {
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
		}
		
		int[] groupSize = {numLaunchersDefense, numLaunchersAttack};
		if (numLaunchersAttack > 0 || numLaunchersDefense > 0) {
			gc.stopGroup(RobotType.LAUNCHER);
		}
		
//		rc.setIndicatorString(1, Integer.toString(groupSize[attackGroup]));
//		rc.setIndicatorString(2, Integer.toString(groupSize[defendGroup]));

		MapLocation closestTower = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
		MapLocation myLocation = rc.getLocation();
		int distance = myLocation.distanceSquaredTo(closestTower);
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		int hqDistance = myLocation.distanceSquaredTo(enemyHQ);
		if (hqDistance < distance) {
			distance = hqDistance; 
		}
		
		if (Clock.getRoundNum() < (rc.getRoundLimit() - 100 - Math.sqrt(distance)*4)) {
			// launcher grouping logic
			if (enemyRush && numLaunchersDefense < 3) {
				gc.groupUnits(RobotType.LAUNCHER, 1);
				rc.broadcast(Broadcast.launcherGroupDefenseCh, 1);
			} else	if (numLaunchersAttack >= 3) {
				gc.groupUnits(RobotType.LAUNCHER, 0);
				rc.broadcast(Broadcast.launcherGroupAttackCh, 1);
			} else {
				gc.groupUnits(RobotType.LAUNCHER, 0);
				rc.broadcast(Broadcast.launcherGroupAttackCh, 0);				
			}
			
			// soldier grouping logic
			if (numSoldiersAttack < 6) {
				gc.groupUnits(RobotType.SOLDIER, 0);
				rc.broadcast(Broadcast.soldierGroupAttackCh, 0);
			} else {
				gc.groupUnits(RobotType.SOLDIER, 0);
				rc.broadcast(Broadcast.soldierGroupAttackCh, 1);
			}
		}
		else {
			gc.groupUnits(RobotType.LAUNCHER, 1);
			rc.broadcast(Broadcast.launcherGroupDefenseCh, 1);
		}
	}
}
