package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;
import battlecode2015.utils.Broadcast;

public class Soldier extends Robot {
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
			// told by headquarters to attack
			int soldierClusterMin = 5;
			boolean toldToAttack = rc.readBroadcast(Broadcast.soldierMarchCh) == 1 ? true : false;
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
	
	// move from point a to point b
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