package battlecode2015.buildings;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode2015.Robot;

public abstract class Building extends Robot {
	protected double hqDistance = 0;
	public void move() {
		try {
			// Compute hqDistance
			if (hqDistance == 0) {
				hqDistance = Math.sqrt(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()));
			}
			int mySupply = (int) rc.getSupplyLevel();
			if (mySupply > 0) {
				RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
				for (RobotInfo r : friendlyRobots) {
					if (r.type == RobotType.TOWER && r.supplyLevel < mySupply) {
						rc.transferSupplies(mySupply / 2, r.location);
					}
					else if (r.supplyLevel < r.type.supplyUpkeep * 10 * (int)hqDistance) {
						rc.transferSupplies(r.type.supplyUpkeep * 15 * (int)hqDistance, r.location);
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
}
