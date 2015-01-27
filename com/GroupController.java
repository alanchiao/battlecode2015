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
		this.ptLauncherA = 0;
		this.ptLauncherB = 0;
		this.launcherGroupA = new int[256];
		this.launcherGroupB = new int[256];

		// roboTypes contains the robotType in a list
		this.roboTypes = new RobotType[] {RobotType.SOLDIER, RobotType.LAUNCHER};
		// attackGroups and defenseGroups contains the different groups
		this.attackGroups = new int[][] {this.soldierGroupA, this.launcherGroupA};
		this.defenseGroups = new int[][] {this.soldierGroupB, this.launcherGroupB};
		this.broadcastChannels = new int[][] {{Broadcast.soldierGroup1Ch, Broadcast.soldierGroup2Ch},
												{Broadcast.launcherGroupAttackCh, Broadcast.launcherGroupDefenseCh}};
		this.pointers = new int[][] {{ptSoldierA, ptSoldierB},
										{ptLauncherA, ptLauncherB}};
		
	}
	
	public int rcToIntConvert(RobotType rt) {
		if (rt == RobotType.SOLDIER) return 0;
		else if (rt == RobotType.LAUNCHER) return 1;
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
		if (rt == RobotType.LAUNCHER) {
			broadcastCh = Broadcast.groupingLaunchersCh;
		}
		else if (rt == RobotType.SOLDIER) {
			broadcastCh = Broadcast.groupingSoldiersCh;
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
}
