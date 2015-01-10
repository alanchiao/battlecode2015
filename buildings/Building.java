package team158.buildings;
import team158.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Clock;

public abstract class Building extends Robot {
	protected double hqDistance = 0;
	public void move() {
		try {
			int cost=0;
			// Compute hqDistance
			if (hqDistance == 0) {
				hqDistance = Math.sqrt(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()));
			}
			int mySupply = (int) rc.getSupplyLevel();
			if (mySupply > 0) {
				//122 bytecode
				RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
				
//				if (rc.getType() == RobotType.HQ) {
//					System.out.println("building code before = " + Clock.getBytecodeNum());
//				}
				int distanceFactor = (int) hqDistance;
				for (RobotInfo r : friendlyRobots) {
//					if (rc.getType() == RobotType.HQ) {
//						cost = Clock.getBytecodeNum();
//					}
					if (r.type == RobotType.MINER) {
						if (r.supplyLevel < r.type.supplyUpkeep * 20 * distanceFactor) {
							rc.transferSupplies(r.type.supplyUpkeep * 30 * distanceFactor, r.location);
							break;
						}
					}
					else if (r.type == RobotType.TOWER) {
						if (r.supplyLevel < mySupply) {
							rc.transferSupplies(mySupply / 2, r.location);
							break;
						}
					}
					else if (r.type == RobotType.BEAVER) {
						if (r.supplyLevel < r.type.supplyUpkeep * 6 * distanceFactor) {
							rc.transferSupplies(r.type.supplyUpkeep * 10 * distanceFactor, r.location);
							break;
						}
					}
					else if (r.supplyLevel < r.type.supplyUpkeep * 10 * distanceFactor) {
						rc.transferSupplies(Math.max(r.type.supplyUpkeep * 15 * distanceFactor, mySupply / 4), r.location);
						break;
					}
//					if (rc.getType() == RobotType.HQ) {
//						System.out.println(r.type+ " = " + (Clock.getBytecodeNum() - cost));
//					}
				}
			}
//			if (rc.getType() == RobotType.HQ) {
//				System.out.println("building code after = " + Clock.getBytecodeNum());
//			}
			actions();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}
}
