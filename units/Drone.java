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
			if (enemiesAttackable.length > 0) {
				Direction d = selectMoveDirectionMicro();
				navigation.stopObstacleTracking();
				if (d != null) {
					rc.move(d);
				}
			}
			else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				if (groupManager.groupID == Broadcast.droneGroup2Ch) {
					boolean hasHQCommand = rc.readBroadcast(groupManager.groupID) == 1;
					if (hasHQCommand) {
						MapLocation target = Broadcast.readLocation(rc, Broadcast.groupTargetLocationChs);
						navigation.moveToDestination(target, false);
					} else {
						groupManager.spawnRallyInGroup(navigation);
					}
				}
				else {
					navigation.moveToDestination(enemyHQ, true);
				}
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_FULL_ATTACK) {
				navigation.moveToDestination(this.ownHQ, true);
			} else {
				navigation.moveToDestination(this.enemyHQ, false);
			}
		}
	}
}
