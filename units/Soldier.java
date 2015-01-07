package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.utils.DirectionHelper;
import battlecode2015.utils.Broadcast;

public class Soldier extends Unit {
	protected void actions() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);

		if (rc.isWeaponReady()) {
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		// Only move if there are few enemies
		if (rc.isCoreReady() && enemies.length < 1) {
			// told by headquarters to attack
			boolean toldToAttack = rc.readBroadcast(Broadcast.soldierMarchCh) == 1 ? true : false;

			MapLocation target;
			if (toldToAttack) {
				target = rc.senseEnemyHQLocation();
			}
			else {
				int loc = rc.readBroadcast(Broadcast.soldierRallyCh);
				target = new MapLocation(loc / 65536, loc % 65536);
			}
			int dirint = DirectionHelper.directionToInt(rc.getLocation().directionTo(target));
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			while (offsetIndex < 5 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			Direction moveDirection = null;
			if (offsetIndex < 5) {
				moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
			}
			if (moveDirection != null) {
				rc.move(moveDirection);
			}
		}	
	}
	///////////////////////////////
	// Navigation methods
	
	// try to keep moving towards destination point
	public void moveToDestinationPoint() {
		Direction fastestDirection = rc.getLocation().directionTo(destinationPoint);
		
	}
	
	public void avoidObstacle(RobotController RC) {
		
	}
	
	//////////////////////////////
	// Detection methods
	
	// count number of friend units next to you
	public int countNearbyFriendlyUnits() {
		RobotInfo[] allies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam()
			);
		return allies.length;
	}
}