package team158.units;

import team158.utils.Broadcast;
import battlecode.common.*;

public class Soldier extends Unit {
	
	public Soldier(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {

		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		if (rc.isCoreReady()) {
			// 20 is how far away a drone can be for the soldier to have a risk of getting too close
			RobotInfo[] enemies = rc.senseNearbyRobots(20, rc.getTeam().opponent());
			if (enemies.length > 0) {
				Direction d = selectMoveDirectionMicro();
				if (d != null) {
					rc.move(d);
					return;
				}
			}
			RobotInfo[] attackableEnemies = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, rc.getTeam().opponent());
			if (attackableEnemies.length == 0) {
				boolean hasHQCommand = rc.readBroadcast(groupManager.groupID) == 1;
				// just always moveToDestination target?
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