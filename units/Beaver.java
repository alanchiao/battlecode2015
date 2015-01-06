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
				for (Direction dir: DirectionHelper.directions) {
					if(rc.senseOre(rc.getLocation().add(dir)) > rc.senseOre(rc.getLocation()) && rc.canMove(dir)) {
						rc.move(dir);
					}
				}
				rc.mine();
			
				int fate = rand.nextInt(1000);
				if (fate < 8 && rc.getTeamOre() >= 300) {
					int offsetIndex = 0;
					int[] offsets = {0,1,-1,2,-2,3,-3,4};
					int dirint = rand.nextInt(8);
					while (offsetIndex < 8 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
						offsetIndex++;
					}
					if (offsetIndex < 8) {
						rc.build(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.BARRACKS);
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