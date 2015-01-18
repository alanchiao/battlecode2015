package team158.com;

import team158.utils.Broadcast;
import team158.utils.Hashing;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class GroupController {
	
	// high-level game information
	private RobotController rc;
	private int strategy;
	
	// two group management code
	int ptTankA;
	int[] tankGroupA;
	int ptTankB;
	int[] tankGroupB;
	int ptDroneA;
	int[] droneGroupA;
	int ptDroneB;
	int[] droneGroupB;
	
	public GroupController(RobotController rc, int strategy) {
		this.rc = rc;
		this.strategy = strategy;
		
		this.tankGroupA = new int[256];
		this.tankGroupB = new int[256];
		this.ptTankA = 0;
		this.ptTankB = 0;
		this.ptDroneA = 0;
		this.droneGroupA = new int[256];
		this.ptDroneB = 0;
		this.droneGroupB = new int[256];
	}

	public void groupUnits(int ID_Broadcast, RobotType rt) {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == rt) {
				if (rt == RobotType.TANK) {
					//update hashmap with (id, group id) pair;
					// if tank is in the hashmap but not in a group
					if (Hashing.find(r.ID) == 0) {
						Hashing.put(r.ID, ID_Broadcast);
						//update the corresponding broadcasted group
						if (ID_Broadcast == Broadcast.tankGroupDefenseCh) {
							tankGroupA[ptTankA] = r.ID;
							ptTankA++;
						}
						else if (ID_Broadcast == Broadcast.tankGroupAttackCh) {
							tankGroupB[ptTankB] = r.ID;
							ptTankB++;
						}
					}
				} 
				else if (rt == RobotType.DRONE) {
					//update hashmap with (id, group id) pair;
					// if tank is in the hashmap but not in a group
					if (Hashing.find(r.ID) == 0) {
						Hashing.put(r.ID, ID_Broadcast);
						//update the corresponding broadcasted group
						if (ID_Broadcast == Broadcast.droneGroupAttackCh) {
							droneGroupB[ptDroneA] = r.ID;
							ptDroneA++;
						}
						else if (ID_Broadcast == Broadcast.droneGroupDefenseCh) {
							droneGroupB[ptDroneB] = r.ID;
							ptDroneB++;
						}
					}
				}
			}
		}
		
		int broadcastCh;
		if (rt == RobotType.DRONE) {
			broadcastCh = Broadcast.groupingDronesCh;
		}
		else if (rt == RobotType.LAUNCHER) {
			broadcastCh = Broadcast.groupingLaunchersCh;
		}
		else if (rt == RobotType.TANK) {
			broadcastCh = Broadcast.groupingTanksCh;
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
	
	public void unGroup(int ID_Broadcast) {
		try {
			rc.broadcast(ID_Broadcast, -1);
			if (ID_Broadcast == Broadcast.droneGroupAttackCh) {
				int i = 0;
				while (droneGroupA[i] != 0) {
					Hashing.put(droneGroupA[i], 0);
					droneGroupA[i] = 0;
					i++;
				}
			}
			else if (ID_Broadcast == Broadcast.droneGroupDefenseCh) {
				int i = 0;
				while (droneGroupB[i] != 0) {
					Hashing.put(droneGroupB[i], 0);
					droneGroupB[i] = 0;
					i++;
				}
			}
			
			if (ID_Broadcast == Broadcast.tankGroupDefenseCh) {
				int i = 0;
				while (tankGroupA[i] != 0) {
					Hashing.put(tankGroupA[i], 0);
					tankGroupA[i] = 0;
					i++;
				}
			}
			else if (ID_Broadcast == Broadcast.tankGroupAttackCh) {
				int i = 0;
				while (tankGroupB[i] != 0) {
					Hashing.put(tankGroupB[i], 0);
					tankGroupB[i] = 0;
					i++;
				}
			}
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
}
