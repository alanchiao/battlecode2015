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
				rc.setIndicatorString(1, Integer.toString(groupID));				
				int locX = rc.readBroadcast(Broadcast.launcherTargetLocationXCh);
				int locY = rc.readBroadcast(Broadcast.launcherTargetLocationYCh);
				MapLocation target = new MapLocation(locX, locY);
				//if launcher Target Location is not set to Tower
//				if (rc.readBroadcast(Broadcast.launcherGroupCh)) {
//					
//				} else {
//					
//				}

	
//						locX = rc.readBroadcast(Broadcast.launcherRallyXCh);
////						locY = rc.readBroadcast(Broadcast.launcherRallyYCh);
//				locX = rc.readBroadcast(Broadcast.launcherTargetLocationXCh);
//				locY = rc.readBroadcast(Broadcast.launcherTargetLocationYCh);
				
				target = new MapLocation(locX, locY);
				rc.setIndicatorString(0,String.valueOf(target.x + " " + target.y));
				navigation.moveToDestination(target, false);
	

//					RobotInfo[] closeRobots = rc.senseNearbyRobots(52, rc.getTeam().opponent());
//					if (closeRobots.length > 0) {
//						MapLocation closestRobot = closeRobots[0].location;
//						int closestDistance = closestRobot.distanceSquaredTo(myLocation);
//						for (int i = 1; i < closeRobots.length; i++) {
//							int distance = closeRobots[i].location.distanceSquaredTo(myLocation);
//							if (distance < closestDistance) {
//								closestDistance = distance;
//								closestRobot = closeRobots[i].location;
//							}
//						}
//						navigation.moveToDestination(closestRobot, false);
//					}
//					else {
//						if (myLocation.distanceSquaredTo(myHQ) > 52) {
//							navigation.moveToDestination(myHQ, false);
//						}
//						else {
//							navigation.moveToDestination(myLocation.add(DirectionHelper.directions[rand.nextInt(8)]), true);
//						}
//					}
				
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				MapLocation myHQ = rc.senseHQLocation();
				navigation.moveToDestination(myHQ, true);
			} else {
				navigation.moveToDestination(enemyHQ, true);
			}
		}
	}

}
