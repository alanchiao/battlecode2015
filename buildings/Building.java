package team158.buildings;
import java.util.Random;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import team158.Robot;

public abstract class Building extends Robot {
	protected double hqDistance = 0;
	protected MapLocation myLocation;

	public Building (RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
		enemyHQ = rc.senseEnemyHQLocation();
		myLocation = rc.getLocation();
	}

	@Override
	public void move() {
		try {
			// Compute hqDistance
			if (hqDistance == 0) {
				hqDistance = Math.sqrt(rc.senseHQLocation().distanceSquaredTo(enemyHQ));
			}
			actions();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}
}
