package battlecode2015;

import java.util.Random;

import battlecode.common.RobotController;

public abstract class Robot {
	protected static Random rand;
	protected static RobotController rc;

	public static void setRC(RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
	}
}
