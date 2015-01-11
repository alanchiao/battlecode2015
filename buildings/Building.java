package team158.buildings;
import team158.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

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
	
				int distanceFactor = (int) hqDistance;
				for (RobotInfo r : friendlyRobots) {
					if (r.type == RobotType.DRONE || r.type == RobotType.SOLDIER || r.type == RobotType.TANK) {
						if (r.supplyLevel < r.type.supplyUpkeep * 10 * distanceFactor) {
							rc.setIndicatorString(0, "transferring supply to attacking unit");
							rc.transferSupplies(Math.max(r.type.supplyUpkeep * 15 * distanceFactor, mySupply / 4), r.location);
							break;
						}
					}
					else if (r.type == RobotType.MINER) {
						if (r.supplyLevel < r.type.supplyUpkeep * 20 * distanceFactor) {
							rc.setIndicatorString(0, "transferring supply to miner");
							rc.transferSupplies(r.type.supplyUpkeep * 30 * distanceFactor, r.location);
							break;
						}
					}
					else if (r.type == RobotType.TOWER) {
						if (r.supplyLevel < mySupply) {
							rc.setIndicatorString(0, "transferring supply to tower");
							rc.transferSupplies(mySupply / 2, r.location);
							break;
						}
					}
					else if (r.type == RobotType.BEAVER) {
						if (r.supplyLevel < r.type.supplyUpkeep * 6 * distanceFactor) {
							rc.setIndicatorString(0, "transferring supply to beaver");
							rc.transferSupplies(r.type.supplyUpkeep * 10 * distanceFactor, r.location);
							break;
						}
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
