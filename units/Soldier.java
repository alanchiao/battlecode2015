package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;
import battlecode2015.utils.Broadcast;

public class Soldier extends Unit {
	protected void actions() throws GameActionException {
        if (rc.isWeaponReady()) {
        	RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);
			if (enemies.length > 0) {
				rc.attackLocation(enemies[0].location);
			}
        }

		if (rc.isCoreReady()) {
			Direction intendedDir;
			// told by headquarters to attack with enough power
			boolean toldToAttack = rc.readBroadcast(Broadcast.soldierMarchCh) == 1 ? true : false;
			
			// only attack if soldiers are near enough allied units
			int soldierClusterMin = 5;
			boolean enoughFriendlyUnits = countNearbyFriendlyUnits() > soldierClusterMin ? true: false;
			if (toldToAttack && enoughFriendlyUnits) {
				int intendedDirIndex = DirectionHelper.directionToInt(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
				intendedDir = DirectionHelper.directions[intendedDirIndex];
				if (rc.canMove(intendedDir)) {
					rc.move(intendedDir);
				}
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