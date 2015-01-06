package battlecode2015.buildings;

import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;

public class Headquarters extends Robot {
	public static void move() {
		try {
			RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
			MapLocation myLocation = rc.getLocation();
			int numSoldiers = 0;
			int numBeavers = 0;
			int numBarracks = 0;
			int minBeaverDistance = 25; // Make sure that the closest beaver is actually close
			int closestBeaver = 0;
			for (RobotInfo r : myRobots) {
				RobotType type = r.type;
				if (type == RobotType.SOLDIER) {
					numSoldiers++;
				} else if (type == RobotType.BEAVER) {
					numBeavers++;
					int distanceSquared = r.location.distanceSquaredTo(myLocation);
					if (distanceSquared < minBeaverDistance) {
						closestBeaver = r.ID;
						minBeaverDistance = r.location.distanceSquaredTo(myLocation);
					}
					if (r.supplyLevel < 100 && distanceSquared <= 15) {
						rc.transferSupplies(500, r.location);
					}
				} else if (type == RobotType.BARRACKS) {
					numBarracks++;
				}
			}
			rc.broadcast(0, numBeavers);
			rc.broadcast(1, numSoldiers);
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

			if (rc.isCoreReady()) {
				double ore = rc.getTeamOre();
				if (numBeavers < 10) {
					int offsetIndex = 0;
					int[] offsets = {0,1,-1,2,-2,3,-3,4};
					int dirint = rand.nextInt(8);
					while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.BEAVER)) {
						offsetIndex++;
					}
					Direction buildDirection = null;
					if (offsetIndex < 8) {
						buildDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
					}
					if (buildDirection != null && ore >= 100) {
						rc.spawn(buildDirection, RobotType.BEAVER);
					}
				}
				else if (numBarracks < 3) {
					if (ore >= 300) {
						rc.broadcast(200, closestBeaver);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("HQ Exception");
            e.printStackTrace();
		}
	}
}