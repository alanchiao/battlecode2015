package team158.units;

import team158.utils.Broadcast;
import battlecode.common.*;

public class Tank extends Unit {
	
	public Tank(RobotController newRC) {
		super(newRC);
	}
	
	@Override
	protected void actions() throws GameActionException {

		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		if (rc.isCoreReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
			if (enemies.length > 0) {
				Direction d = selectMoveDirectionMicro();
				if (d != null) {
					rc.move(d);
					return;
				}
			}
			
			RobotInfo[] attackableEnemies = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, rc.getTeam().opponent());
			if (attackableEnemies.length == 0) {
				boolean hasHQCommand = rc.readBroadcast(groupManager.groupID) == 1;
				if (groupManager.isGrouped() && hasHQCommand) {
					MapLocation target = Broadcast.readLocation(rc, Broadcast.groupTargetLocationChs);
					navigation.moveToDestination(target, false);
				}
				else {
					groupManager.spawnRallyInGroup(navigation);
				}
			}
		}	
	}
}