package battlecode2015.units;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode2015.Robot;

public abstract class Unit extends Robot {
	// stored information about reaching a destination
	// for navigation
	protected MapLocation destinationPoint;
	
	public void move() {
		try {
			int mySupply = (int) rc.getSupplyLevel();
			if (mySupply > rc.getType().supplyUpkeep * 100) {
				RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
				for (RobotInfo r : friendlyRobots) {
					if (r.supplyLevel < r.type.supplyUpkeep * 50) {
						rc.transferSupplies(Math.min(mySupply / 2, r.type.supplyUpkeep * 200), r.location);
					}
				}
			}
			actions();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}
	
	protected MapLocation selectTarget(RobotInfo[] enemies) {
		MapLocation target = null;
		double maxPriority = 0;
		for (RobotInfo r : enemies) {
			if (1 / r.health > maxPriority) {
				maxPriority = 1 / r.health;
				target = r.location;
			}
		}
		return target;
	}
}
