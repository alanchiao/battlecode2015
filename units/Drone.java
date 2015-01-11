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
	
	public static int TIME_UNTIL_COLLECT_SUPPLY = 1700; // in round #'s
	public static int TIME_UNTIL_FULL_ATTACK = 1850;

	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, rc.getTeam().opponent());

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
			else if (Clock.getRoundNum() < TIME_UNTIL_COLLECT_SUPPLY) {
				MapLocation enemyHQ = rc.senseEnemyHQLocation();
				Navigation.moveToDestination(rc, this, enemyHQ, true);
			} else if (Clock.getRoundNum() < TIME_UNTIL_FULL_ATTACK) {
				MapLocation myHQ = rc.senseHQLocation();
				Navigation.moveToDestination(rc, this, myHQ, true);
			} else {
				MapLocation enemyHQ = rc.senseEnemyHQLocation();
				Navigation.moveToDestination(rc, this, enemyHQ, false);
			}
		}
	}
}
