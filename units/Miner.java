package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.utils.*;

public class Miner extends Unit {
	protected void actions() throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);

		if (rc.isWeaponReady()) {
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }
		
		if (rc.isCoreReady()) {
			// HQ has given command for this particular beaver to build a miner factory
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
//				if (rc.canMove(dir)) {
//					possibleDirection = dir;
//				}
			}
			if (rc.senseOre(myLocation) == 0) {
				rc.move(bestDirection);
			}
//			else {
//				rc.move(possibleDirection);
//			}
			else {
				rc.mine();
			}
		}
	}
}