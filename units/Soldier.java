package team158.units;

import team158.units.com.Navigation;
import team158.utils.Broadcast;
import battlecode.common.*;

public class Soldier extends Unit {
	protected void actions() throws GameActionException {

		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(5, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		if (rc.isCoreReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(20, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.setIndicatorString(0, "enemy detected");
				Direction d = selectMoveDirectionMicro();
				if (d != null) {
					rc.move(d);
					return;
				}
			}
			RobotInfo[] attackableEnemies = rc.senseNearbyRobots(5, rc.getTeam().opponent());
			if (attackableEnemies.length == 0) {
				MapLocation target;
				int xLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
				int yLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
				target = new MapLocation(xLoc, yLoc);
				if (groupID > 0) {
					moveToTargetByGroup(target);
				}
				else {
					Navigation.moveToDestination(rc, this, target, false);
				}
			}
		}	
	}
}