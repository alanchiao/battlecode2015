package team158.units;

import team158.units.com.Navigation;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.*;

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
					
					if (this.destinationPoint != target) { // then no longer obstacle
						this.isAvoidingObstacle = false;
					}
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