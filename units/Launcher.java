package team158.units;

import team158.buildings.Headquarters;
import team158.utils.DirectionHelper;
import team158.utils.Broadcast;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Unit {
	
	public boolean isReloading;

	public Launcher(RobotController newRC) {
		super(newRC);
		this.isReloading = false;
	}

	@Override
	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(24, rc.getTeam().opponent());
		MapLocation myLocation = rc.getLocation();
		
		if (rc.getMissileCount() >= 3) {
			isReloading = false;
		}
		// launch 3 missiles at a time, then retreat. Do not launch
		// 3 missiles if already surrounded by more than 1 missile
		RobotInfo[] nearbyAlliedUnits = rc.senseNearbyRobots(2, rc.getTeam());
		int nearbyMissileCount = 0;
		for (RobotInfo ally: nearbyAlliedUnits) {
			if (ally.type == RobotType.MISSILE) {
				nearbyMissileCount++;
			}
		}
		
		if (enemiesAttackable.length > 0 && nearbyMissileCount < 1 && !isReloading) {
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(selectTarget(enemiesAttackable)));
			int[] offsets = {0,1,-1,2,-2};
			int offsetIndex = 0;
			while (offsetIndex < 5 && !rc.canLaunch(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				rc.launchMissile(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
			while (offsetIndex < 5 && !rc.canLaunch(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				rc.launchMissile(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
			while (offsetIndex < 5 && !rc.canLaunch(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				rc.launchMissile(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
			if (rc.getMissileCount() < 3) {
				isReloading = true;
			}
        }
		
		// Move
		if (rc.isCoreReady()) {
			// reloading - retreat ot recover missiles
			if (isReloading) {
				navigation.moveToDestination(this.ownHQ, true);
				return;
			}
			
			if (enemiesAttackable.length > 0) {
				Direction d = selectMoveDirectionMicro();
				navigation.stopObstacleTracking();
				if (d != null) {
					rc.move(d);
				}
			}
			else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_LAUNCHERS_GROUP) {
				// if headquarter says attack, read from attack channel, otherwise
				// rally normally
				MapLocation target;
				boolean hasHQCommand = rc.readBroadcast(Broadcast.launcherGroupCh) == 1;
				if (hasHQCommand) {
					 target = Broadcast.readLocation(rc, Broadcast.launcherAttackLocationChs);
				} else {
					 target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
				}
				navigation.moveToDestination(target, false);
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				navigation.moveToDestination(this.ownHQ, true);
			} else {
				navigation.moveToDestination(this.enemyHQ, true);
			}
		}
	}

}
