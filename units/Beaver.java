package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;

public class Beaver extends Robot {
	public static void move() {
		try {
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
			if (enemies.length >0 && rc.isWeaponReady()) { 
				if (rc.isWeaponReady()) {
					if (enemies.length == 1) {
						rc.attackLocation(enemies[0].location);	
					}
				}
				else {
					rc.move(rc.getLocation().directionTo(enemies[0].location).opposite());	
				}
			}
			if (rc.isCoreReady()) {
				int fate = rand.nextInt(1000);
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
				} 
				
				for (Direction dir: DirectionHelper.directions) {
					if(rc.senseOre(rc.getLocation().add(dir)) > rc.senseOre(rc.getLocation()) && rc.canMove(dir)) {
						rc.move(dir);
					}
				}
				rc.mine();
			}
		} catch (Exception e) {
			System.out.println("Beaver Exception");
            e.printStackTrace();
		}
	}
}