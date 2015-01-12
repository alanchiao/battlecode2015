package team158.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import team158.Robot;

public class Missile extends Robot {

	public Missile (RobotController newRC) {
		rc = newRC;
	}

	@Override
	public void move() {
		try {
			if (rc.getSupplyLevel() > 0) {
				System.out.println("Missiles shouldn't have supply");
			}
			rc.explode();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}

	@Override
	protected void actions() throws GameActionException {
	}

}
