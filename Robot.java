package battlecode2015;

import java.util.Random;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public abstract class Robot {
	protected Random rand;
	protected RobotController rc;

	public void setRC(RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
	}
	
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
	
	protected abstract void actions() throws GameActionException;
}
