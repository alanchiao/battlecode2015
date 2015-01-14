package team158.units.com;

import team158.utils.Broadcast;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Group {
	
	public static final int UNGROUPED = -1;
	public static final int RETREATING = 0;
	
	public RobotController rc;
	
	public int groupID;
	
	public Group (RobotController rc) {
		this.rc = rc;
		groupID = Group.UNGROUPED;
	}
	
	public boolean isGrouped() {
		return groupID > 0;
	}
	
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	
	public void unGroup() {
		this.groupID = Group.UNGROUPED;
	}
	
	
	
	// rallying to location that spawning buildings
	// tell units to go to
	public void spawnRallyInGroup(Navigation navigation) {
		MapLocation target;
		try {
			switch(rc.getType()) {
				case DRONE:		target = Broadcast.readLocation(rc, Broadcast.droneRallyLocationChs);
								break;
				case SOLDIER:	target = Broadcast.readLocation(rc, Broadcast.soldierRallyLocationChs);
								break;
				case TANK:		target = Broadcast.readLocation(rc, Broadcast.tankRallyLocationChs);
								break;
				case LAUNCHER:	target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
								break;
				default:		// should never happen
								target = null;
								assert(false);
								break;				
			} 
			// TODO: more robust way of determining when rally point has been reached
//				if (target.distanceSquaredTo(rc.getLocation()) <= 24) {
//					rc.broadcast(groupID, -1);
//					groupID = -1;
//				}
			navigation.moveToDestination(target, false);
		}  catch (GameActionException e) {
			return;
		}
	}

}
