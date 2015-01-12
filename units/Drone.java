package team158.units;

import team158.buildings.Headquarters;
import team158.units.com.Navigation;
import team158.utils.Broadcast;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends Unit {

	public Drone(RobotController newRC) {
		super(newRC);
	}

	@Override
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
			else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				Navigation.moveToDestination(rc, this, enemyHQ, true);
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_FULL_ATTACK) {
				MapLocation myHQ = rc.senseHQLocation();
				Navigation.moveToDestination(rc, this, myHQ, true);
			} else {
				if (groupID > 0) {		
					int towerX = rc.readBroadcast(Broadcast.groupingTargetLocationXCh);
					int towerY = rc.readBroadcast(Broadcast.groupingTargetLocationYCh);
					moveToTargetByGroup(new MapLocation(towerX,towerY));
				}
				else {
					Navigation.moveToDestination(rc, this, enemyHQ, false);
				}
			}
		}
	}
}
