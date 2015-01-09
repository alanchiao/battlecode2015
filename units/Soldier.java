package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.units.com.Navigation;
import battlecode2015.utils.DirectionHelper;
import battlecode2015.utils.Broadcast;

public class Soldier extends Unit {
	protected void actions() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);

		if (rc.isWeaponReady()) {
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		if (rc.isCoreReady()) {
			if (enemies.length > 0) {
				Direction d = selectMoveDirectionMicro();
				if (d != null) {
					rc.move(d);
				}
			}
			else {
				MapLocation target;
				if (groupID != -1) {
					moveByGroup();
				}
				else {
					int xLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
					int yLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
					target = new MapLocation(xLoc, yLoc);
					this.destinationPoint = target;
					Navigation.moveToDestinationPoint(rc, this);
				}
			}
		}	
	}
	//////////////////////////////
	// Detection methods
	
	// count number of friend units next to you
	public int countNearbyFriendlyUnits() {
		RobotInfo[] allies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam()
			);
		return allies.length;
	}
}