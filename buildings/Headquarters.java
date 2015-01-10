package team158.buildings;

import battlecode.common.*;

import java.util.*;

import team158.utils.Broadcast;
import team158.utils.DirectionHelper;

public class Headquarters extends Building {
	int attackGroup = 1;
	int defendGroup = 0;
	public HashMap<Integer, Integer> groupID = new HashMap<Integer, Integer> ();
	protected void actions() throws GameActionException {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		MapLocation myLocation = rc.getLocation();
		int numSoldiers = 0;
		int numSoldiersG1 = 0;
		int numSoldiersG2 = 0;
		int numDrones = 0;
		int numBeavers = 0;
		int numBarracks = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		int numSupplyDepots = 0;
		
		int minBeaverDistance = 25; // Make sure that the closest beaver is actually close
		int closestBeaver = 0;
		
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.SOLDIER) {
				numSoldiers++;
				//check if soldier is part of some group
				if (groupID.containsKey(r.ID)) {
					if (groupID.get(r.ID) == Broadcast.soldierGroup1Ch) {
						numSoldiersG1++;
					}					
					else if (groupID.get(r.ID) == Broadcast.soldierGroup2Ch) {
						numSoldiersG2++;
					}
				}
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
				int distanceSquared = r.location.distanceSquaredTo(myLocation);
				if (distanceSquared < minBeaverDistance) {
					closestBeaver = r.ID;
					minBeaverDistance = r.location.distanceSquaredTo(myLocation);
				}
			} else if (type == RobotType.DRONE) {
				numDrones++;
			} else if (type == RobotType.BARRACKS) {
				numBarracks++;
			} else if (type == RobotType.MINER) {
				numMiners++;
			} else if (type == RobotType.MINERFACTORY) {
				numMinerFactories++;
			} else if (type == RobotType.SUPPLYDEPOT) {
				numSupplyDepots++;
			}
		}
		
		rc.broadcast(Broadcast.numBeaversCh, numBeavers);
		rc.broadcast(Broadcast.numSoldiersCh, numSoldiers);
		rc.broadcast(Broadcast.numDronesCh, numDrones);
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numBarracksCh, numBarracks);
		rc.broadcast(Broadcast.numMinerFactoriesCh, numMinerFactories);
		rc.broadcast(Broadcast.numSupplyDepotsCh, numSupplyDepots);
		
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);
			if (enemies.length > 0) {
				rc.attackLocation(enemies[0].location);
			}
		}

		if (rc.isCoreReady()) {
			double ore = rc.getTeamOre();
			// Spawn beavers
			if (numBeavers < 2) {
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2,3,-3,4};
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(rc.senseEnemyHQLocation()));
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
			else if (numMinerFactories < 2) {
				if (ore >= 500) {
					rc.broadcast(Broadcast.buildMinerFactoriesCh, closestBeaver);
				}
			}
			else if (numSupplyDepots == 0 && ore >= 100) {
				rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
			}
			else if (ore >= 300 + numBarracks * 200) {
				rc.broadcast(Broadcast.buildBarracksCh, closestBeaver);
				// tell closest beaver to build barracks
			}
			else if (numSupplyDepots < 3 && ore >= 500) {
				rc.broadcast(Broadcast.buildSupplyCh, closestBeaver);
			}

			int[] groupSize = {numSoldiersG1, numSoldiersG2};
			int[] groupCh = {Broadcast.soldierGroup1Ch, Broadcast.soldierGroup2Ch};

			if (numSoldiersG1 > 0 || numSoldiersG2 > 0) {
				stopGroup(RobotType.SOLDIER);
			}
			rc.setIndicatorString(1, Integer.toString(groupSize[attackGroup]));
			rc.setIndicatorString(2, Integer.toString(groupSize[defendGroup]));
			if (numSoldiers - groupSize[defendGroup] > 30 && groupSize[attackGroup] == 0) {
				groupUnits(groupCh[attackGroup], RobotType.SOLDIER);
				rc.broadcast(groupCh[attackGroup], 1);
			}
			else if (rc.readBroadcast(groupCh[attackGroup]) == 1 && groupSize[attackGroup] < 15) {
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
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.SOLDIER) {
				//update hashmap with (id, group id) pair;
				if (!groupID.containsKey(r.ID)) {
					groupID.put(r.ID, ID_Broadcast);
				}
			}
		}
		int broadcastCh;
		if (rt == RobotType.SOLDIER) {
			broadcastCh = Broadcast.groupingSoldiersCh;
		}
		else if (rt == RobotType.DRONE) {
			broadcastCh = Broadcast.groupingDronesCh;
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
		if (rt == RobotType.SOLDIER) {
			broadcastCh = Broadcast.groupingSoldiersCh;
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
			for (Iterator<Map.Entry<Integer, Integer>> i = groupID.entrySet().iterator(); i.hasNext();) {
			    if (i.next().getValue() == ID_Broadcast) {
			        i.remove();
			    }
			}
		}
		catch (GameActionException e) {
			return;
		}
	}
}