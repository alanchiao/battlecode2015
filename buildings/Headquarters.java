package team158.buildings;

import battlecode.common.*;

import java.util.*;

import team158.utils.Broadcast;
import team158.utils.DirectionHelper;

public class Headquarters extends Building {
	int attackGroup = 1;
	int defendGroup = 0;
	//public HashSet<Integer> groupID = new HashSet<Integer>();
	//public HashMap<Integer, Integer> groupID = new HashMap <Integer, Integer> ();
	public HashMap<Integer, HashSet<Integer>> groupID = new HashMap<Integer, HashSet<Integer>> ();
	protected void actions() throws GameActionException {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		rc.setIndicatorString(1, Integer.toString(myRobots.length));
		MapLocation myLocation = rc.getLocation();
		int numSoldiers = 0;
		int numSoldiersG1 = 0;
		int numSoldiersG2 = 0;
		int numBashers = 0;
		int numBashers701 = 0;
		int numBeavers = 0;
		int numBarracks = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		
		int minBeaverDistance = 25; // Make sure that the closest beaver is actually close
		int closestBeaver = 0;
		
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.SOLDIER) {
				numSoldiers++;
				//check if soldier is part of some group
//				if (groupID.containsKey(r.ID)) {
//					if (groupID.get(r.ID) == Broadcast.soldierGroup1Ch) {
//						numSoldiersG1++;
//					}					
//					else if (groupID.get(r.ID) == Broadcast.soldierGroup2Ch) {
//						numSoldiersG2++;
//					}
//				}
				if(groupID.containsKey(Broadcast.soldierGroup1Ch)){
					if(groupID.get(Broadcast.soldierGroup1Ch).contains(r.ID)) {
						numSoldiersG1++;
					}				
				}
				if(groupID.containsKey(Broadcast.soldierGroup2Ch)){
					if(groupID.get(Broadcast.soldierGroup2Ch).contains(r.ID)) {
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
			} else if (type == RobotType.BASHER) {
				numBashers++;
			} else if (type == RobotType.BARRACKS) {
				numBarracks++;
			} else if (type == RobotType.MINER) {
				numMiners++;
			} else if (type == RobotType.MINERFACTORY) {
				numMinerFactories++;
			}
		}
		
		rc.broadcast(Broadcast.numBeaversCh, numBeavers);
		rc.broadcast(Broadcast.numSoldiersCh, numSoldiers);
		rc.broadcast(Broadcast.numBashersCh, numBashers);
		rc.broadcast(Broadcast.numBarracksCh, numBarracks);
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numMinerFactoriesCh, numMinerFactories);
		
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
				int dirint = rand.nextInt(8);
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
			else if (ore >= 300 * numBarracks) {
				rc.broadcast(Broadcast.buildBarracksCh, closestBeaver);
				// tell closest beaver to build barracks
			}

			//Only update soldiers if current group is dead
//			if (numSoldiers700 == 0 && numSoldiers > 30) { 
//				groupUnits(Broadcast.soldierGroupCh, RobotType.SOLDIER);
//				rc.broadcast(Broadcast.soldierGroupCh, 1);
//			}
//			else if (numSoldiers700 <15) {
//				unGroup(RobotType.SOLDIER);
//				rc.broadcast(Broadcast.soldierGroupCh, 0);
//				
//			}
//			else { // stops grouping of soldiers until other group soldiers are dead
//				stopGroup(RobotType.SOLDIER);
//			}
			int[] groupSize = {numSoldiersG1, numSoldiersG2};
			int[] groupCh = {Broadcast.soldierGroup1Ch, Broadcast.soldierGroup2Ch};
			if (numSoldiersG1 == 0 && numSoldiersG2==0 && numSoldiers >30) {
				groupUnits(Broadcast.soldierGroup1Ch, RobotType.SOLDIER);
			}

			
			if (groupSize[attackGroup]>30) {
				System.out.println(groupCh[attackGroup] + "  " +groupSize[attackGroup]);
				rc.broadcast(groupCh[attackGroup], 1);
				groupUnits(groupCh[defendGroup], RobotType.SOLDIER);
			}
			else if (rc.readBroadcast(groupCh[attackGroup])==1 && groupSize[attackGroup] < 20) {
				System.out.println(groupSize[attackGroup]);
				System.out.println(groupCh[attackGroup]);
//				if (groupID.containsKey(groupCh[attackGroup])) {
//					groupID.get(groupCh[attackGroup]).clear();
//				}
				groupUnits(groupCh[defendGroup], RobotType.SOLDIER);
				rc.broadcast(groupCh[attackGroup], 0);
				int temp = attackGroup;
				attackGroup = defendGroup;
				defendGroup = temp;
			} else  {
				groupUnits(groupCh[attackGroup], RobotType.SOLDIER);
			}
				//			if (numBashers701 > 50) {
//				System.out.println(numBashers701);
//				rc.broadcast(701, 1);
//			} else if (numBashers701 <= 50) {
//				//System.out.println(numBashers701);
//				rc.broadcast(701, 0);
//				groupUnits(701, RobotType.BASHER);
//			}
		}
	}
	
	public void groupUnits(int ID_Broadcast, RobotType rt) {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.SOLDIER) {
				//update hashmap with (id, group id) pair;
				if (groupID.containsKey(ID_Broadcast)) {
					groupID.get(ID_Broadcast).add(r.ID);
				}
				else {
					HashSet<Integer> n = new HashSet<Integer> ();
					n.add(r.ID);
					groupID.put(ID_Broadcast,n);
				}
			}
//			if (type == RobotType.BASHER) {
//				//System.out.println(r.ID);
//				groupID.add(r.ID);
//			}
		}
		int broadcastCh;
		if (rt == RobotType.SOLDIER) {
			broadcastCh = Broadcast.groupingSoldiersCh;
		}
		else if (rt == RobotType.DRONE) {
			broadcastCh = Broadcast.groupingDronesCh;
		}
		else if (rt == RobotType.BASHER) {
			broadcastCh = Broadcast.groupingBashersCh;
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
		else if (rt == RobotType.BASHER) {
			broadcastCh = Broadcast.groupingBashersCh;
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
}