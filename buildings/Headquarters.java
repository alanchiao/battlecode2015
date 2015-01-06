package battlecode2015.buildings;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;

public class Headquarters extends Robot {
	public static void move() {
		try {
			int fate = rand.nextInt(10000);
			RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
			int numSoldiers = 0;
			int numBashers = 0;
			int numBeavers = 0;
			int numBarracks = 0;
			for (RobotInfo r : myRobots) {
				RobotType type = r.type;
				if (type == RobotType.SOLDIER) {
					numSoldiers++;
				} else if (type == RobotType.BASHER) {
					numBashers++;
				} else if (type == RobotType.BEAVER) {
					numBeavers++;
				} else if (type == RobotType.BARRACKS) {
					numBarracks++;
				}
			}
			rc.broadcast(0, numBeavers);
			rc.broadcast(1, numSoldiers);
			rc.broadcast(2, numBashers);
			rc.broadcast(100, numBarracks);
			
			if (rc.isWeaponReady()) {
				RobotInfo[] enemies = rc.senseNearbyRobots(
					rc.getType().attackRadiusSquared,
					rc.getTeam().opponent()
				);
				if (enemies.length > 0) {
					rc.attackLocation(enemies[0].location);
				}
			}

			if (rc.isCoreReady() && rc.getTeamOre() >= 100 && fate < Math.pow(1.2,12-numBeavers)*10000) {
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2,3,-3,4};
				int dirint = rand.nextInt(8);
				while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.BEAVER)) {
					offsetIndex++;
				}
				if (offsetIndex < 8) {
					rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.BEAVER);
				}
			}
		} catch (Exception e) {
			System.out.println("HQ Exception");
            e.printStackTrace();
		}
	}
}