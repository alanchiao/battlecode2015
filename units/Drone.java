package team158.units;

import team158.units.com.Navigation;
import team158.utils.Broadcast;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class Drone extends Unit {

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

		// Move
		if (enemies.length > 0) {
			Direction d = selectMoveDirectionMicro();
			if (d != null) {
				rc.move(d);
			}
		}
		else {
			MapLocation target;
			if (groupID > 0) {
				moveByGroup();
			}
			else {
				int xLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
				int yLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
				target = new MapLocation(xLoc, yLoc);
				if (this.destinationPoint != null && (this.destinationPoint.x != target.x || this.destinationPoint.y != target.y)) { // then no longer obstacle
					this.isAvoidingObstacle = false;
				}
				this.destinationPoint = target;
				Navigation.moveToDestinationPoint(rc, this);
			}
		}
	}
}
