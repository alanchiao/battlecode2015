package battlecode2015.units.com;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode2015.units.Unit;

public class Navigation {
	// move unit steadliy towards destination
	public static void moveToDestinationPoint(RobotController rc, Unit unit) {
		Direction fastestDirection = rc.getLocation().directionTo(unit.destinationPoint);
		
	}
}
