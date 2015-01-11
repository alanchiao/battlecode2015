package team158.units;

import team158.units.com.Navigation;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class Drone extends Unit {

	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);

		if (rc.isWeaponReady()) {
			if (enemiesAttackable.length > 0) {
				rc.attackLocation(selectTarget(enemiesAttackable));
			}
        }
		
		RobotInfo[] enemiesSeen = rc.senseNearbyRobots(
				24,
				rc.getTeam().opponent()
			);

		// Move
		if (rc.isCoreReady()) {
			if (enemiesAttackable.length > 0) {
				Direction d = selectMoveDirectionMicro();
				if (d != null) {
					rc.move(d);
				}
			}
			else if (Clock.getRoundNum() < 1700) {
			MapLocation myLocation = rc.getLocation();
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
			boolean safeDirections[] = moveDirectionsAvoidingAttack(enemiesSeen, 10);
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			while (offsetIndex < 5 && (!rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]) || !safeDirections[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			Direction moveDirection = null;
			if (offsetIndex < 5) {
				moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
			}
			if (moveDirection != null && myLocation.add(moveDirection).distanceSquaredTo(enemyHQ) <= myLocation.distanceSquaredTo(enemyHQ)) {
				rc.move(moveDirection);
			}
			} else if (Clock.getRoundNum() < 1850) {
				MapLocation myLocation = rc.getLocation();
				MapLocation myHQ = rc.senseHQLocation();
				boolean safeDirections[] = moveDirectionsAvoidingAttack(enemiesSeen, 10);
				DirectionHelper.directionToInt(myLocation.directionTo(myHQ));
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(myHQ));
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2};
				while (offsetIndex < 5 && (!rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]) || !safeDirections[(dirint+offsets[offsetIndex]+8)%8])) {
					offsetIndex++;
				}
				Direction moveDirection = null;
				if (offsetIndex < 5) {
					moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
				}
				if (moveDirection != null && myLocation.add(moveDirection).distanceSquaredTo(myHQ) <= myLocation.distanceSquaredTo(myHQ)) {
					rc.move(moveDirection);
				}
				
			} else {
				MapLocation myLocation = rc.getLocation();
				MapLocation enemyHQ = rc.senseEnemyHQLocation();
				DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2};
				while (offsetIndex < 5 && (!rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]))) {
					offsetIndex++;
				}
				Direction moveDirection = null;
				if (offsetIndex < 5) {
					moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
				}
				if (moveDirection != null && myLocation.add(moveDirection).distanceSquaredTo(enemyHQ) <= myLocation.distanceSquaredTo(enemyHQ)) {
					rc.move(moveDirection);
				}
			}
		}
	}
}
