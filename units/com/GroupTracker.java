package team158.units.com;

import team158.utils.Broadcast;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/***
 * Class for helping an individual unit keep track of what group it is in
 * and decide what to do in response to what group it is in.
 */
public class GroupTracker {
	
	public static final int UNGROUPED = -1;
	public static final int RETREATING = 0;
	
	public RobotController rc;
	
	public int groupID;
	
	public GroupTracker (RobotController rc) {
		this.rc = rc;
		groupID = GroupTracker.UNGROUPED;
	}
	
	public boolean isGrouped() {
		return groupID > 0;
	}
	
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	
	public void unGroup() {
		this.groupID = GroupTracker.UNGROUPED;
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
			
			navigation.moveToDestination(target, false);
		}  catch (GameActionException e) {
			return;
		}
	}

}
