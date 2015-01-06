package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;

public class Beaver extends Robot {
	public static void move() {
		try {
			if (rc.isWeaponReady()) {
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
				if (enemies.length > 0) {
					rc.attackLocation(enemies[0].location);
				}
			}
			if (rc.isCoreReady()) {
				int fate = rand.nextInt(1000);
				// HQ has given command for this particular
				// beaver to build a barracks
				if (rc.readBroadcast(200) == rc.getID()) {
					rc.broadcast(200, 0);
					int offsetIndex = 0;
					int[] offsets = {0,1,-1,2,-2,3,-3,4};
					int dirint = DirectionHelper.directionToInt(rc.getLocation().directionTo(rc.senseHQLocation()));
					while (offsetIndex < 8 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
						offsetIndex++;
					}
					Direction buildDirection = null;
					if (offsetIndex < 8) {
						buildDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
					}
					if (buildDirection != null && rc.canBuild(buildDirection, RobotType.BARRACKS)) {
						rc.build(buildDirection, RobotType.BARRACKS);
					}
				} else if (fate < 600) {
					rc.mine();
				} else {
					int intendedDirection;
					if (fate < 900) {
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
			}
		} catch (Exception e) {
			System.out.println("Beaver Exception");
            e.printStackTrace();
		}
	}
}