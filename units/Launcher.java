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

public class Launcher extends Unit {

	public Launcher(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(35, rc.getTeam().opponent());
		MapLocation myLocation = rc.getLocation();

		if (enemiesAttackable.length > 0) {
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(selectTarget(enemiesAttackable)));
			int[] offsets = {0,1,-1,2,-2};
			int offsetIndex = 0;
			while (offsetIndex < 5 && !rc.canLaunch(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				rc.launchMissile(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
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
			else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_LAUNCHERS_GROUP) {				
				MapLocation target;
				boolean hasHQCommand = rc.readBroadcast(Broadcast.launcherGroupCh) == 1;
				// if headquarter says attack, read from attack channel
				if (hasHQCommand) {
					 target = Broadcast.readLocation(rc, Broadcast.launcherAttackLocationChs);
				} else {
					 target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
				}
				rc.setIndicatorString(0, target.toString());
				navigation.moveToDestination(target, false);
	
				
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				MapLocation myHQ = rc.senseHQLocation();
				navigation.moveToDestination(myHQ, true);
			} else {
				navigation.moveToDestination(enemyHQ, true);
			}
		}
	}

}
