package team158.units;

import team158.units.com.Navigation;
import team158.utils.Broadcast;
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
				if (groupID > 0) {
					moveByGroup();
				}
				else {
					int targetXLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
					int targetYLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
					target = new MapLocation(targetXLoc, targetYLoc);
					Navigation.moveToDestination(rc, this, target);
				}
			}
		}	
	}
}