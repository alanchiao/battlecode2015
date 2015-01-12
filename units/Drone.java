package team158.units;

import team158.buildings.Headquarters;
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
				navigation.stopObstacleTracking();
				if (d != null) {
					rc.move(d);
				}
			}
			else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				System.out.println(groupID);
				if (groupID == Broadcast.droneGroup2Ch) {
					int towerX = rc.readBroadcast(Broadcast.groupingTargetLocationXCh);
					int towerY = rc.readBroadcast(Broadcast.groupingTargetLocationYCh);
					//System.out.println(towerX + " " + towerY);
					MapLocation target = new MapLocation(towerX,towerY);
					rc.setIndicatorString(0,String.valueOf(towerX + " " + towerY));
					moveToTargetByGroup(target);
				}
				else {
					rc.setIndicatorString(0,String.valueOf(enemyHQ.x + " " + enemyHQ.y));
					navigation.moveToDestination(enemyHQ, true);
				}
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_FULL_ATTACK) {
				MapLocation myHQ = rc.senseHQLocation();
				navigation.moveToDestination(myHQ, true);
			} else {
				navigation.moveToDestination(enemyHQ, false);
			}
		}
	}
}
