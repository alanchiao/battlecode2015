package team158.com;

import team158.utils.Hashing;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class GroupController {
	
	// high-level game information
	private RobotController rc;
	
	// two group management code
	int ptTankA;
	int ptTankB;
	int[] tankGroupA;
	int[] tankGroupB;
	int ptDroneA;
	int ptDroneB;
	int[] droneGroupA;
	int[] droneGroupB;
	int ptSoldierA;
	int ptSoldierB;
	int[] soldierGroupA;
	int[] soldierGroupB;
	int ptLauncherA;
	int ptLauncherB;
	int[] launcherGroupA;
	int[] launcherGroupB;
	RobotType[] roboTypes;
	int[][] attackGroups;
	int[][] defenseGroups;
	int[][] broadcastChannels;
	int[][] pointers;
	
	public GroupController(RobotController rc, int strategy) {
		this.rc = rc;

		this.ptSoldierA = 0;
		this.ptSoldierB = 0;
		this.soldierGroupA = new int[256];
		this.soldierGroupB = new int[256];
		this.ptTankA = 0;
		this.ptTankB = 0;
		this.tankGroupA = new int[256];
		this.tankGroupB = new int[256];
		this.ptDroneA = 0;
		this.ptDroneB = 0;
		this.droneGroupA = new int[256];
		this.droneGroupB = new int[512];
		this.ptLauncherA = 0;
		this.ptLauncherB = 0;
		this.launcherGroupA = new int[256];
		this.launcherGroupB = new int[256];

		// roboTypes contains the robotType in a list
		this.roboTypes = new RobotType[] {RobotType.SOLDIER, RobotType.TANK, RobotType.DRONE, RobotType.LAUNCHER};
		// attackGroups and defenseGroups contains the different groups
		this.attackGroups = new int[][] {this.soldierGroupA, this.tankGroupA, this.droneGroupA, this.launcherGroupA};
		this.defenseGroups = new int[][] {this.soldierGroupB, this.tankGroupB, this.droneGroupB, this.launcherGroupB};
		this.broadcastChannels = new int[][] {{Broadcast.soldierGroupAttackCh, Broadcast.soldierGroupDefenseCh},
												{Broadcast.tankGroupAttackCh, Broadcast.tankGroupDefenseCh},
												{Broadcast.droneGroupAttackCh, Broadcast.droneGroupDefenseCh},
												{Broadcast.launcherGroupAttackCh, Broadcast.launcherGroupDefenseCh}};
		this.pointers = new int[][] {{ptSoldierA, ptSoldierB},
										{ptTankA, ptTankB},
										{ptDroneA, ptDroneB},
										{ptLauncherA, ptLauncherB}};
		
	}
	
	public int rcToIntConvert(RobotType rt) {
		if (rt == RobotType.SOLDIER) return 0;
		else if (rt == RobotType.TANK) return 1;
		else if (rt == RobotType.DRONE) return 2;
		else if (rt == RobotType.LAUNCHER) return 3;
		else return -1;
	}
	
	public void groupUnits(RobotType rt, int position) {
		//position = 0 => attack | 1 => defense
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		int robotTypeInt = rcToIntConvert(rt);
		RobotType unitType = roboTypes[robotTypeInt];
		int ID_Broadcast = broadcastChannels[robotTypeInt][position];
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == unitType) {
				// if robot is in the hashmap but not in a group
				if (Hashing.find(r.ID) == 0) {
					Hashing.put(r.ID, ID_Broadcast);
					//update the corresponding broadcasted group
					// 0 -> attack
					// 1 -> defense
					if (position == 0) {
						attackGroups[robotTypeInt][pointers[robotTypeInt][0]] = r.ID;
						pointers[robotTypeInt][1]++;
					}
					else if (position == 1) {
						defenseGroups[robotTypeInt][pointers[robotTypeInt][1]] = r.ID;
						pointers[robotTypeInt][1]++;
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
