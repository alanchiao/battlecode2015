package team158.buildings;

import battlecode.common.*;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import team158.utils.Hashing;

public class Headquarters extends Building {

	int[] groupID;
	int ptA;
	int[] groupA;
	int ptB;
	int[] groupB;
	
	public final static int TIME_UNTIL_LAUNCHERS_GROUP = 1500;
	public final static int TIME_UNTIL_COLLECT_SUPPLY = 1650;
	public final static int TIME_UNTIL_FULL_ATTACK = 1800;

	private int attackGroup;
	private int defendGroup;
	
	private int numTowers;
	private boolean towerDied;
	MapLocation targetTower;

	private int strategy;
	private boolean enemyRush;
	private boolean enemyThreat;
	private int pathDifficulty;
	
	int closestBeaver;
	int scoutBeaver;
	
	public Headquarters(RobotController newRC) {
		super(newRC);
		groupID = new int[7919];
		groupA = new int[512];
		groupB = new int[512];
		ptA = 0;
		ptB = 0;
		
		attackGroup = 1;
		defendGroup = 0;
		
		numTowers = 7;
		towerDied = false;
		
		strategy = 2;
		enemyRush = false;
		enemyThreat = false;
		pathDifficulty = 0;
		
		closestBeaver = 0;
		scoutBeaver = 0;
	}
	
	@Override
	protected void actions() throws GameActionException {	
		//check if attackable tower exists and broadcasts location
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		//MapLocation myLocation = rc.getLocation();
		int numTowersRemaining = enemyTowers.length;
		
		if (numTowersRemaining != numTowers) {
			towerDied = true;
			numTowers = numTowersRemaining;
		}

		if (towerDied && numTowers > 0) {
			//reset tower died status
			towerDied = false;
			int[] distances = new int[numTowersRemaining];
			for (int i = 0; i < numTowersRemaining; i++) {
				distances[i] = myLocation.distanceSquaredTo(enemyTowers[i]);
			}
			
			boolean towerExists = false;
			int count = 0;
			while (count < numTowersRemaining) {
				int minDistance = 999999;
				int targetTowerIndex = 0;
				for (int i = 0; i < numTowersRemaining; i++) {
					if (distances[i] < minDistance) {
						minDistance = distances[i];
						targetTower = enemyTowers[i];
						targetTowerIndex = i;
					}
				}
				int numNearbyTowers = 0;
				for (int j = 0; j < numTowersRemaining; j++) {
					if (targetTowerIndex != j && targetTower.distanceSquaredTo(enemyTowers[j]) <= 24) {
						numNearbyTowers++;
					}
				}
				if (numNearbyTowers <= 3) {
					towerExists = true;
					break;
				}
				else {
					distances[targetTowerIndex] = 999999;
					count++;
				}
			}
			if (!towerExists) {
				if (numTowersRemaining != 6) {
					targetTower = enemyHQ;
				}
				else {
					targetTower = null;
				}
			}
		}

		if (targetTower != null) {
			rc.broadcast(Broadcast.groupingTargetLocationXCh, targetTower.x);
			rc.broadcast(Broadcast.groupingTargetLocationYCh, targetTower.y);
		}
		
		int mySupply = (int) rc.getSupplyLevel();
		RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());

		if (Clock.getRoundNum() < TIME_UNTIL_LAUNCHERS_GROUP) {
			int distanceFactor = (int) hqDistance;
			for (RobotInfo r : friendlyRobots) {
				if (r.type == RobotType.MINER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 20 * distanceFactor) {
						rc.setIndicatorString(0, "transferring supply to miner/launcher");
						rc.transferSupplies(r.type.supplyUpkeep * 30 * distanceFactor, r.location);
						break;
					}
				}
				else if (r.type == RobotType.DRONE || r.type == RobotType.LAUNCHER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 10 * distanceFactor) {
						rc.setIndicatorString(0, r.location.toString());
						rc.transferSupplies(r.type.supplyUpkeep * 15 * distanceFactor, r.location);
						break;
					}
				}
				else if (r.type == RobotType.BEAVER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 100) {
						rc.setIndicatorString(0, Integer.toString(r.type.supplyUpkeep * 6 * distanceFactor));
						rc.transferSupplies(r.type.supplyUpkeep * 200, r.location);
						break;
					}
				}
				rc.setIndicatorString(0, "no supply transferred");
			}
		}
		else {
			for (RobotInfo r : friendlyRobots) {
				if (r.type == RobotType.DRONE || r.type == RobotType.LAUNCHER) {
					rc.transferSupplies(mySupply, r.location);
					break;
				}
			}
		}

		if (rc.isWeaponReady()) {
			int numTowers = rc.senseTowerLocations().length;
			if (numTowers >= 5) { // splash damage
				RobotInfo[] enemies = rc.senseNearbyRobots(52, rc.getTeam().opponent());
				if (enemies.length > 0) {
					RobotInfo[] directlyAttackable = rc.senseNearbyRobots(35, rc.getTeam().opponent());
					if (directlyAttackable.length > 0) {
						for (RobotInfo enemy : directlyAttackable) {
							if (enemy.type != RobotType.MISSILE) {
								rc.attackLocation(enemy.location);
								break;
							}
						}
					}
					else {
						for (RobotInfo enemy : enemies) {
							MapLocation attackLocation = enemy.location.add(enemy.location.directionTo(myLocation));
							if (attackLocation.distanceSquaredTo(myLocation) <= 35) {
								rc.attackLocation(attackLocation);
								break;
							}
						}
					}
				}
			}
			else if (numTowers >= 2) { // range 35
				RobotInfo[] enemies = rc.senseNearbyRobots(35, rc.getTeam().opponent());
				if (enemies.length > 0) {
					rc.attackLocation(enemies[0].location);
				}
			}
			else { // range 24
				RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
				if (enemies.length > 0) {
					rc.attackLocation(enemies[0].location);
				}
			}
		}

		if (strategy == 1) {
			groundGame();
		}
		else {
			aerialGame();
		}
	}
	
	protected void aerialGame() throws GameActionException {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		int numBeavers = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		int numSupplyDepots = 0;
		int numDrones = 0;
		int numLaunchers = 0;
		int numHelipads = 0;
		int numDronesG1 = 0;
		int numDronesG2 = 0;
		int numAerospaceLabs = 0;
		
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.MINER) {
				numMiners++;
			} else if (type == RobotType.DRONE) {
				numDrones++;
				if (Hashing.find(groupID, r.ID) == Broadcast.droneGroup1Ch) {
					numDronesG1++;
				}							
				else if (Hashing.find(groupID, r.ID)  == Broadcast.droneGroup2Ch) {
					numDronesG2++;
				}		
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
				if (scoutBeaver == 0 && closestBeaver != 0 && r.ID != closestBeaver) {
					scoutBeaver = r.ID;
					rc.broadcast(Broadcast.scoutEnemyHQCh, scoutBeaver);
				}
				else if (r.ID != scoutBeaver) {
					closestBeaver = r.ID;
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
			} else if (type == RobotType.DRONE) {
				numDrones++;
			}
		}
		
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numDronesCh, numDrones);
		rc.broadcast(Broadcast.numLaunchersCh, numLaunchers);
		rc.broadcast(Broadcast.numBuildingsCh, numMinerFactories + numSupplyDepots + numHelipads + numAerospaceLabs);
		
		if (!enemyRush && Clock.getRoundNum() < 500) {
			RobotInfo[] enemyRobots = rc.senseNearbyRobots(99, rc.getTeam().opponent());
			for (RobotInfo r : enemyRobots) {
				if (r.type == RobotType.DRONE) {
					enemyRush = true;
					return;
				}
			}
		}
		
		if (!enemyThreat && Clock.getRoundNum() < 1000) {
			if (rc.readBroadcast(Broadcast.enemyThreatCh) > 2) {
				enemyThreat = true;
				unGroup(Broadcast.droneGroup1Ch);
			}
		}
		
		if (pathDifficulty == 0) {
			int possibleDifficulty = rc.readBroadcast(Broadcast.scoutEnemyHQCh);
			if (possibleDifficulty != scoutBeaver) {
				pathDifficulty = possibleDifficulty;
				System.out.println(pathDifficulty);
			}
		}

		if (rc.isCoreReady()) {
			double ore = rc.getTeamOre();
			if (numBeavers == 0 || scoutBeaver == 0) {
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
			else if (numHelipads == 0) {
				if (ore >= 300) {
					rc.broadcast(Broadcast.buildHelipadsCh, closestBeaver);
				}
			}
			else if (numMinerFactories == 0) {
				if (ore >= 500) {
					rc.broadcast(Broadcast.buildMinerFactoriesCh, closestBeaver);
				}
			}
			else {
				if (enemyThreat && pathDifficulty < 70) { // Build launchers
					if (numAerospaceLabs == 0) {
						if (ore >= 500) {
							rc.broadcast(Broadcast.slowMinerProductionCh, 0);
							rc.broadcast(Broadcast.buildAerospaceLabsCh, closestBeaver);
						}
						else {
							rc.broadcast(Broadcast.slowMinerProductionCh, 1);
						}
					}
					else if (numSupplyDepots == 0 && ore >= 100) {
						rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
					}
					else if (ore >= 500 + numAerospaceLabs * 400) {
						rc.broadcast(Broadcast.buildAerospaceLabsCh, closestBeaver);
					}
					else if (numSupplyDepots < 3 && ore >= 600) {
						rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
					}
				}
				else if (enemyRush) { // Build some launchers
					if (numAerospaceLabs == 0) {
						if (ore >= 500) {
							rc.broadcast(Broadcast.buildAerospaceLabsCh, closestBeaver);
						}
					}
					else if (numSupplyDepots == 0 && ore >= 100) {
						rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
					}
					else if (ore >= 300 + numHelipads * 200) {
						rc.broadcast(Broadcast.buildHelipadsCh, closestBeaver);
						// tell closest beaver to build barracks
					}
					else if (numSupplyDepots < 3 && ore >= 500) {
						rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
					}
				}
				else {
					if (numSupplyDepots == 0 && ore >= 100) {
						rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
					}
					else if (ore >= 300 + numHelipads * 200) {
						rc.broadcast(Broadcast.buildHelipadsCh, closestBeaver);
						// tell closest beaver to build barracks
					}
					else if (numSupplyDepots < 3 && ore >= 500) {
						rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
					}
				}
			}
			
			rc.setIndicatorString(1, Integer.toString(numDronesG1));
			rc.setIndicatorString(2, Integer.toString(numDronesG2));
			
			if (!enemyThreat) {
				if (numDronesG1 < 15 || targetTower == null) {
					rc.broadcast(Broadcast.droneGroup1Ch, 1);
					groupUnits(Broadcast.droneGroup1Ch, RobotType.DRONE);
				}
				else {
					if (numDronesG2 > 20) {
						rc.broadcast(Broadcast.droneGroup2Ch, 1);
						groupUnits(Broadcast.droneGroup1Ch, RobotType.DRONE);
					}
					else if (numDronesG2 > 10 && rc.readBroadcast(Broadcast.droneGroup2Ch) == 1) {
						groupUnits(Broadcast.droneGroup1Ch, RobotType.DRONE);
					}
					else {
						rc.broadcast(Broadcast.droneGroup2Ch, 0);
						groupUnits(Broadcast.droneGroup2Ch, RobotType.DRONE);
					}
				}
			}
			else {
				if (numDronesG2 > 20 && targetTower != null) {
					rc.broadcast(Broadcast.droneGroup2Ch, 1);
				}
				groupUnits(Broadcast.droneGroup2Ch, RobotType.DRONE);
			}
		}
		
	}

	protected void groundGame() throws GameActionException {
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
				if (Hashing.find(groupID, r.ID) == Broadcast.tankGroup1Ch) {
					numTanksG1++;
				}							
				else if (Hashing.find(groupID, r.ID)  == Broadcast.tankGroup2Ch) {
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
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
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
				stopGroup(RobotType.TANK);
			}
			rc.setIndicatorString(1, Integer.toString(groupSize[attackGroup]));
			rc.setIndicatorString(2, Integer.toString(groupSize[defendGroup]));
			if (numTanks - groupSize[defendGroup] > 20 && groupSize[attackGroup] == 0) {
				groupUnits(groupCh[attackGroup], RobotType.TANK);
				rc.broadcast(groupCh[attackGroup], 1);
			}
			else if (rc.readBroadcast(groupCh[attackGroup]) == 1 && groupSize[attackGroup] < 10) {
				rc.broadcast(groupCh[attackGroup], 0);
				attackGroup = 1 - attackGroup;
				defendGroup = 1 - defendGroup;
			}
			else if (rc.readBroadcast(groupCh[defendGroup]) == -1) {
				unGroup(groupCh[defendGroup]);
			}
		}
	}
	
	public void groupUnits(int ID_Broadcast, RobotType rt) {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		if (strategy == 1) {
			for (RobotInfo r : myRobots) {
				RobotType type = r.type;
				if (type == RobotType.TANK) {
					//update hashmap with (id, group id) pair;
					// if tank is in the hashmap but not in a group
					if (Hashing.find(groupID, r.ID) == 0) {
						Hashing.put(groupID, r.ID, ID_Broadcast);
						//update the corresponding broadcasted group
					}
				} 
			}
		} 
		else if (strategy == 2) {
			for (RobotInfo r : myRobots) {
				RobotType type = r.type;
				if (type == RobotType.DRONE) {
					//update hashmap with (id, group id) pair;
					// if tank is in the hashmap but not in a group
					if (Hashing.find(groupID, r.ID) == 0) {
						Hashing.put(groupID, r.ID, ID_Broadcast);
						//update the corresponding broadcasted group
						if (ID_Broadcast == Broadcast.droneGroup1Ch) {
							groupA[ptA] = r.ID;
							ptA++;
						}
						else if (ID_Broadcast == Broadcast.droneGroup2Ch) {
							groupB[ptB] = r.ID;
							ptB++;
						}
					}
				}
			}
		}
		
		int broadcastCh;
		if (rt == RobotType.TANK) {
			broadcastCh = Broadcast.groupingTanksCh;
		}
		else if (rt == RobotType.DRONE) {
			broadcastCh = Broadcast.groupingDronesCh;
		}
		else {
			broadcastCh = 9999;
		}
		try {
			rc.broadcast(broadcastCh, ID_Broadcast);
		}
		catch (GameActionException e) {
			return;
		}
	}
	
	public void stopGroup(RobotType rt) {
		int broadcastCh;
		if (rt == RobotType.TANK) {
			broadcastCh = Broadcast.groupingTanksCh;
		}
		else if (rt == RobotType.DRONE) {
			broadcastCh = Broadcast.groupingDronesCh;
		}
		else {
			broadcastCh = 9999;
		}
		try {
			rc.broadcast(broadcastCh, 0);
		}
		catch (GameActionException e) {
			return;
		}
	}
	
	public void unGroup(int ID_Broadcast) {
		try {
			rc.broadcast(ID_Broadcast, -1);

			if (ID_Broadcast == Broadcast.tankGroup1Ch) {
				int i = 0;
				while (groupA[i] != 0) {
					Hashing.put(groupID, groupA[i], 0);
					groupA[i] = 0;
					i++;
				}
			}
			else if (ID_Broadcast == Broadcast.tankGroup2Ch) {
				int i = 0;
				while (groupB[i] != 0) {
					Hashing.put(groupID, groupB[i], 0);
					groupB[i] = 0;
					i++;
				}
			}
		}
		catch (GameActionException e) {
			return;
		}
	}
}