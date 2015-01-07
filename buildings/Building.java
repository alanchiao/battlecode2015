package battlecode2015.buildings;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode2015.Robot;

public abstract class Building extends Robot {
	public void move() {
		try {
			int mySupply = (int) rc.getSupplyLevel();
			if (mySupply > 0) {
				RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
				for (RobotInfo r : friendlyRobots) {
					// BARRACKS currently cannot transfer supply
					if (r.type.supplyUpkeep == 0 && r.type != RobotType.BARRACKS && r.supplyLevel < mySupply) {
						rc.transferSupplies(mySupply / 2, r.location);
					}
					else if (r.supplyLevel < r.type.supplyUpkeep * 50) {
						rc.transferSupplies(Math.min(mySupply / 2, r.type.supplyUpkeep * 500), r.location);
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
