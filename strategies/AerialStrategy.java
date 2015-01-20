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

public class AerialStrategy extends GameStrategy {
	
	private boolean enemyRush;
	private boolean enemyThreat;
	private int pathDifficulty;
	private int attackGroup;
	private int defendGroup;
	
	private int builderBeaver;
	private int scoutMiner;
	
	public AerialStrategy(RobotController rc, GroupController gc, Headquarters hq) {
		super(rc, gc, hq);
		
		this.attackGroup = 1;
		this.defendGroup = 0;
		this.enemyRush = false;
		this.enemyThreat = false;
		this.pathDifficulty = 0;	
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
		int numLaunchersAttack = 0;
		int numLaunchersDefense = 0;
		int numHelipads = 0;
		int numDronesAttack = 0;
		int numDronesDefense = 0;
		int numAerospaceLabs = 0;
		double oreDepletion = 0;

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
				if (Hashing.find(r.ID) == Broadcast.launcherGroupAttackCh) {
					numLaunchersAttack++;
				}							
				else if (Hashing.find(r.ID)  == Broadcast.launcherGroupDefenseCh) {
					numLaunchersDefense++;
				}	
			}
		}
		
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numDronesCh, numDrones);
		rc.broadcast(Broadcast.numLaunchersCh, numLaunchers);
		rc.broadcast(Broadcast.numBuildingsCh, numMinerFactories + numSupplyDepots + numHelipads + numAerospaceLabs);
		
		if (!enemyRush && Clock.getRoundNum() < 600) {
			RobotInfo[] enemyRobots = rc.senseNearbyRobots(99, rc.getTeam().opponent());
			for (RobotInfo r : enemyRobots) {
				if (r.type == RobotType.DRONE) {
					enemyRush = true;
					// Tell helipads to yield
					rc.broadcast(Broadcast.stopDroneProductionCh, 1);
					break;
				}
			}
		}
		
		if (!enemyThreat && Clock.getRoundNum() < 1200) {
			if (rc.readBroadcast(Broadcast.enemyThreatCh) > 2) {
				enemyThreat = true;
				rc.broadcast(Broadcast.stopDroneProductionCh, 1);
				gc.unGroup(Broadcast.droneGroupAttackCh);
			}
		}
		
		if (pathDifficulty == 0) {
			int possibleDifficulty = rc.readBroadcast(Broadcast.scoutEnemyHQCh);
			if (possibleDifficulty != scoutMiner) {
				pathDifficulty = possibleDifficulty;
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
					rc.broadcast(Broadcast.buildMinerFactoriesCh, builderBeaver);
				}
			}
			else if (numHelipads == 0) {
				if (ore >= 300) {
					rc.broadcast(Broadcast.buildHelipadsCh, builderBeaver);
				}
			}
			else if (numSupplyDepots == 0) {
				if (ore >= 100) {
					rc.broadcast(Broadcast.buildSupplyCh, builderBeaver);
				}
			}
			else {
				if (numAerospaceLabs == 0) {
					if (ore >= 500) {
						rc.broadcast(Broadcast.stopDroneProductionCh, 0);
						rc.broadcast(Broadcast.buildAerospaceLabsCh, builderBeaver);
					}
				}
				else if (ore >= 700) {
					rc.broadcast(Broadcast.buildAerospaceLabsCh, builderBeaver);
				}
				else if (numSupplyDepots < 3 && ore >= 750) {
					rc.broadcast(Broadcast.buildSupplyCh, builderBeaver);
				}
			}
			int[] groupSize = {numLaunchersDefense, numLaunchersAttack};
			int[] groupCh = {Broadcast.launcherGroupDefenseCh, Broadcast.launcherGroupAttackCh};
			if (numLaunchersAttack > 0 || numLaunchersDefense > 0) {
				gc.stopGroup(RobotType.LAUNCHER);
			}
			
			rc.setIndicatorString(1, Integer.toString(groupSize[attackGroup]));
			rc.setIndicatorString(2, Integer.toString(groupSize[defendGroup]));

			if (numDrones > 0) {
				gc.groupUnits(RobotType.DRONE, 0);
				rc.broadcast(Broadcast.droneGroupAttackCh, 1);
			}
			rc.setIndicatorString(0, String.valueOf(pathDifficulty));
			if (Clock.getRoundNum() < 2000 - pathDifficulty*4) {
				if (enemyRush && numLaunchersDefense < 5) {
					gc.groupUnits(RobotType.LAUNCHER, 1);
					rc.broadcast(Broadcast.launcherGroupDefenseCh, 1);
				} else	if (numLaunchersAttack >= 6) {
					gc.groupUnits(RobotType.LAUNCHER, 0);
					rc.broadcast(Broadcast.launcherGroupAttackCh, 1);
				} else {
					gc.groupUnits(RobotType.LAUNCHER, 0);
					rc.broadcast(Broadcast.launcherGroupAttackCh, 0);				
				}
			}
			else {
				gc.groupUnits(RobotType.LAUNCHER, 1);
				rc.broadcast(Broadcast.launcherGroupDefenseCh, 1);
			}
//			if (numLaunchersDefense < 6) {
//				gc.groupUnits(RobotType.LAUNCHER, 1);
//				rc.broadcast(Broadcast.launcherGroupDefenseCh, 1);
//			}
//			else {
//				gc.groupUnits(RobotType.LAUNCHER, 0);
//				if (numLaunchers - numLaunchersDefense >= 8) {
//					rc.broadcast(Broadcast.launcherGroupAttackCh, 1);
//				}
//				else {
//					rc.broadcast(Broadcast.launcherGroupAttackCh, 0);
//				}
//			}		
			//if they don't build tanks and launchers
//			if (!enemyThreat) {
//				if (groupSize[defendGroup] < 10) {
//					gc.groupUnits(RobotType.DRONE, 0);
//					rc.broadcast(groupCh[defendGroup], 1);
//				}
//				else {
//					rc.broadcast(groupCh[attackGroup],1);
//					if (numDrones - groupSize[defendGroup] - groupSize[attackGroup] > 10) {
//						gc.groupUnits(RobotType.DRONE, 1);
//					}
//				}
//			}
//			//if enemy builds tanks and launchers
//			else {

//				MapLocation targetTower = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
//				if (numDronesDefense > 20 && targetTower != null) {
//					rc.broadcast(Broadcast.droneGroupDefenseCh, 1);
//				}
//				else if (numDronesDefense <= 15){
//					rc.setIndicatorString(2, String.valueOf(numDronesDefense));
//					rc.broadcast(Broadcast.droneGroupDefenseCh, 0);
//				}
//				gc.groupUnits(RobotType.DRONE, 0);
//			}
		}
		
	}
}
