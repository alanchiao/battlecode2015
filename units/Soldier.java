package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;

public class Soldier extends Robot {
	public static void move() {
		try {
            if (rc.isWeaponReady()) {
            	RobotInfo[] enemies = rc.senseNearbyRobots(
					rc.getType().attackRadiusSquared,
					rc.getTeam().opponent()
				);
				if (enemies.length > 0) {
					rc.attackLocation(enemies[0].location);
				}
			}
			if (rc.isCoreReady()) {
				int fate = rand.nextInt(1000);
				int intendedDirection;
				if (fate < 800) {
					intendedDirection = rand.nextInt(8);
				} else {
					intendedDirection = DirectionHelper.directionToInt(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
				}
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2};
				while (offsetIndex < 5 && !rc.canMove(DirectionHelper.directions[(intendedDirection+offsets[offsetIndex]+8)%8])) {
					offsetIndex++;
				}
				if (offsetIndex < 5) {
					rc.move(DirectionHelper.directions[(intendedDirection+offsets[offsetIndex]+8)%8]);
				}
			}
        } catch (Exception e) {
			System.out.println("Soldier Exception");
			e.printStackTrace();
        }
	}
}