package battlecode2015.buildings;

import battlecode.common.*;
import battlecode2015.Robot;

public class Tower extends Building {
	protected void actions() throws GameActionException {
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);
			if (enemies.length > 0) {
				rc.attackLocation(enemies[0].location);
			}
		}
	}
}