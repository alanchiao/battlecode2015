package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.*;

public class Beaver extends Robot {

	protected void actions() throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		if (enemies.length >0 && rc.isWeaponReady()) { 
			if (rc.isWeaponReady()) {
				if (enemies.length == 1) {
					rc.attackLocation(enemies[0].location);	
				}
			}
			else {
				rc.move(myLocation.directionTo(enemies[0].location).opposite());	
			}
		}
		
		if (rc.isCoreReady()) {
			// HQ has given command for this particular beaver to build a barracks
			if (rc.readBroadcast(Broadcast.closetBeaverCh) == rc.getID()) {
				rc.broadcast(Broadcast.closetBeaverCh, 0);
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2,3,-3,4};
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(rc.senseHQLocation()));
				while (offsetIndex < 8 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
					offsetIndex++;
				}
				Direction buildDirection = null;
				if (offsetIndex < 8) {
					buildDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
				}
				if (buildDirection != null && rc.canBuild(buildDirection, RobotType.BARRACKS)) {
					rc.build(buildDirection, RobotType.BARRACKS);
				}
			} 
			else {
				double currentOre = rc.senseOre(myLocation);
				double maxOre = -2;
				Direction bestDirection = null;
				Direction possibleDirection = null;
				// looks around for an ore concentration that is bigger than its current location by a certain fraction
				for (Direction dir: DirectionHelper.directions) {
					double possibleOre = rc.senseOre(myLocation.add(dir));
					if (possibleOre > maxOre && rc.canMove(dir)) {
						maxOre = possibleOre;
						bestDirection = dir;
					}
					if (rc.canMove(dir)) {
						possibleDirection = dir;
					}
				}
				if (maxOre > 1.5 * currentOre) {
					rc.move(bestDirection);
				}
				else if (maxOre == -2) {
					rc.move(possibleDirection);
				}
				else {
					rc.mine();
				}
				rc.mine();
			}
		}

	}
	public int countNearbyFriendlyUnits() {
		RobotInfo[] allies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam()
			);
		return allies.length;
	}
}