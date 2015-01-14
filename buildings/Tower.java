package team158.buildings;

import team158.units.Unit;
import battlecode.common.*;

public class Tower extends Building {

	public Tower(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);
			if (enemies.length > 0) {
				rc.attackLocation(Unit.selectTarget(enemies));
			}
		}
	}
}