package team158.units;

import team158.utils.Broadcast;
import battlecode.common.*;

public class Soldier extends Unit {
	
	public Soldier(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {

		// Note: Soldier attacks every turn (AD=1) if an enemy is in range. This is desired behavior.
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.SOLDIER.attackRadiusSquared, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
				return;
			}
        }

		if (rc.isCoreReady()) {
			MapLocation target;
			boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
			// just always moveToDestination target?
			if (groupTracker.isGrouped() && hasHQCommand) {
				target = Broadcast.readLocation(rc, Broadcast.groupTargetLocationChs);
				navigation.moveToDestination(target, false);
			}
			else {
				target = groupTracker.getRallyPoint();
			}
			moveToLocationWithMicro(target, 0);
		}	
	}
}