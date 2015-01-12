package team158.buildings;

import battlecode.common.*;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import team158.utils.Hashing;

public class Headquarters extends Building {

	int[] groupID;
	int[] groupA;
	int[] groupB;
	
	public final static int TIME_UNTIL_COLLECT_SUPPLY = 1650; // in round #'s
	public final static int TIME_UNTIL_FULL_ATTACK = 1800;

	private int attackGroup;
	private int defendGroup;
	
	// 0 - undecided, 1 - ground, 2 - air
	private int strategy;
	private boolean enemyRush;
	private boolean enemyThreat;
	
	int closestBeaver;
	int scoutBeaver;
	
	public Headquarters(RobotController newRC) {
		super(newRC);
		groupID = new int[7919];
		groupA = new int[200];
		groupB = new int[200];
		attackGroup = 1;
		defendGroup = 0;
		strategy = 2;
		
		enemyRush = false;
		enemyThreat = false;
	}
	
	@Override
	protected void actions() throws GameActionException {

		int mySupply = (int) rc.getSupplyLevel();
		RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());

		if (Clock.getRoundNum() < TIME_UNTIL_COLLECT_SUPPLY) {
			int distanceFactor = (int) hqDistance;
			for (RobotInfo r : friendlyRobots) {
				if (r.type == RobotType.DRONE || r.type == RobotType.SOLDIER || r.type == RobotType.TANK) {
					if (r.supplyLevel < r.type.supplyUpkeep * 10 * distanceFactor) {
						rc.setIndicatorString(0, "transferring supply to attacking unit");
						rc.transferSupplies(Math.max(r.type.supplyUpkeep * 15 * distanceFactor, mySupply / 4), r.location);
						break;
					}
				}
				else if (r.type == RobotType.MINER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 20 * distanceFactor) {
						rc.setIndicatorString(0, "transferring supply to miner");
						rc.transferSupplies(r.type.supplyUpkeep * 30 * distanceFactor, r.location);
						break;
					}
				}
				else if (r.type == RobotType.BEAVER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 6 * distanceFactor) {
						rc.setIndicatorString(0, "transferring supply to beaver");
						rc.transferSupplies(r.type.supplyUpkeep * 10 * distanceFactor, r.location);
						break;
					}
				}
			}
		}
		else {
			for (RobotInfo r : friendlyRobots) {
				if (r.type == RobotType.DRONE || r.type == RobotType.SOLDIER || r.type == RobotType.TANK) {
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
		int numAerospaceLabs = 0;
		
		int closestBeaver = 0;
		
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.MINER) {
				numMiners++;
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
				if (closestBeaver == 0 && numBeavers == 2) {
					closestBeaver = r.ID;
				}
				if (scoutBeaver == 0 && numBeavers == 1) {
					scoutBeaver = r.ID;
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
		
		if (!enemyRush && Clock.getRoundNum() < 300) {
			RobotInfo[] enemyRobots = rc.senseNearbyRobots(99, rc.getTeam().opponent());
			int enemyDrones = 0;
			for (RobotInfo r : enemyRobots) {
				if (r.type == RobotType.DRONE) {
					enemyDrones++;
					if (enemyDrones == 2) {
						enemyRush = true;
						return;
					}
				}
			}
		}

		if (rc.isCoreReady()) {
			double ore = rc.getTeamOre();
			if (numBeavers <= 1) {
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
				if (enemyThreat) { // Build launchers
					rc.broadcast(Broadcast.L2DX100Ch, 10000);
					if (numAerospaceLabs == 0) {
						if (ore >= 500) {
							rc.broadcast(Broadcast.buildAerospaceLabsCh, closestBeaver);
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
				else if (enemyRush) { // Build one launcher
					rc.broadcast(Broadcast.L2DX100Ch, 50);
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
			
			if (scoutBeaver != 0 && rc.readBroadcast(Broadcast.scoutEnemyHQCh) != -1) {
				rc.broadcast(Broadcast.scoutEnemyHQCh, scoutBeaver);
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
		int i = 0;
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.TANK) {
				//update hashmap with (id, group id) pair;
				// if tank is in the hashmap but not in a group
				if (Hashing.find(groupID, ID_Broadcast) == 0) {
					Hashing.put(groupID, r.ID, ID_Broadcast);
					//update the corresponding broadcasted group
					if (ID_Broadcast == Broadcast.tankGroup1Ch) {
						groupA[i] = r.ID;
						i++;
					}
					else if (ID_Broadcast == Broadcast.tankGroup2Ch) {
						groupB[i] = r.ID;
						i++;
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