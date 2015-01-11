package team158.units;

import team158.units.com.Navigation;
import team158.utils.Broadcast;
import battlecode.common.*;

public class Tank extends Unit {
	
	@Override
	protected void actions() throws GameActionException {

		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		if (rc.isCoreReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.setIndicatorString(0, "enemy detected");
				Direction d = selectMoveDirectionMicro();
				if (d != null) {
					rc.move(d);
					return;
				}
			}
			RobotInfo[] attackableEnemies = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, rc.getTeam().opponent());
			if (attackableEnemies.length == 0) {
				MapLocation target;
				int xLoc = rc.readBroadcast(Broadcast.tankRallyXCh);
				int yLoc = rc.readBroadcast(Broadcast.tankRallyYCh);
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