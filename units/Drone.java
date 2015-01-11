package team158.units;

import team158.units.com.Navigation;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends Unit {

	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());

		if (rc.isWeaponReady()) {
			if (enemiesAttackable.length > 0) {
				rc.attackLocation(selectTarget(enemiesAttackable));
			}
        }
		
		// Move
		if (rc.isCoreReady()) {
			if (enemiesAttackable.length > 0) {
				Direction d = selectMoveDirectionMicro();
				Navigation.stopObstacleTracking(this);
				if (d != null) {
					rc.move(d);
				}
			}
			else if (Clock.getRoundNum() < 1700) {
				MapLocation enemyHQ = rc.senseEnemyHQLocation();
				Navigation.moveToDestination(rc, this, enemyHQ, true);
			} else if (Clock.getRoundNum() < 1850) {
				MapLocation myHQ = rc.senseHQLocation();
				Navigation.moveToDestination(rc, this, myHQ, true);
			} else {
				MapLocation enemyHQ = rc.senseEnemyHQLocation();
				Navigation.moveToDestination(rc, this, enemyHQ, false);
			}
		}
	}
}
