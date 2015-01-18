package team158.units;

import team158.buildings.Headquarters;
import team158.utils.Broadcast;
import battlecode.common.Clock;
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
		// Determine if opponent is using tanks/launchers and assess threat
		if (prevHealth - rc.getHealth() >= 20 && prevHealth - rc.getHealth() != 24) {
			int threat = rc.readBroadcast(Broadcast.enemyThreatCh);
			rc.broadcast(Broadcast.enemyThreatCh, threat + 1);
		}

		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, rc.getTeam().opponent());

		if (rc.isWeaponReady()) {
			if (enemiesAttackable.length > 0) {
				rc.attackLocation(selectTarget(enemiesAttackable));
			}
        }
		
		// Move
		if (rc.isCoreReady()) {
			MapLocation target;
			if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				if (groupTracker.groupID == Broadcast.droneGroupDefenseCh) {
					boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
					if (hasHQCommand) {
						target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
						// navigation.moveToDestination(target, false);
					} else {
						target = groupTracker.getRallyPoint();
					}
				}
				else {
					target = enemyHQ;
				}
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_FULL_ATTACK) {
				target = this.ownHQ;
			} else {
				target = this.enemyHQ;
			}
			moveToLocationWithMicro(target, 1);
		}
	}
	protected void droneMicro(MapLocation target) {
		RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.DRONE.sensorRadiusSquared, rc.getTeam().opponent());
		
	}
}
