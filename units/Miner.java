package team158.units;

import team158.utils.*;
import battlecode.common.*;

public class Miner extends Unit {
	
	public Miner(RobotController newRC) {
		super(newRC);
	}

	private Direction prevDirection = null;
	
	@Override
	protected void actions() throws GameActionException {
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		MapLocation myLocation = rc.getLocation();
		double myOre = rc.senseOre(myLocation);
		
		if (rc.isCoreReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
			boolean[] possibleMovesAvoidingEnemies = navigation.moveDirectionsAvoidingAttack(enemies, 5);
			for (int i = 0; i < 8; i++) {
				if (!rc.canMove(DirectionHelper.directions[i])) {
					possibleMovesAvoidingEnemies[i] = false;
				}
			}

			if (!possibleMovesAvoidingEnemies[8]) {
				int dirint;
				if (enemies.length == 0) { // need to avoid hq which is not in enemies
					dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(myLocation));
				}
				else {
					dirint = DirectionHelper.directionToInt(enemies[0].location.directionTo(myLocation));
				}
				int offsetIndex = 0;
				while (offsetIndex < 8 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
					offsetIndex++;
				}
				if (offsetIndex < 8) {
					rc.move(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
				}
			}
			else if (myOre >= 10) {
				int ore = rc.readBroadcast(Broadcast.minerOreX100Ch);
				rc.broadcast(Broadcast.minerOreX100Ch, ore + (int)(100 * Math.max(myOre / 4, 0.2)));
				rc.mine();
			}
			else {
				double maxOre = 0;
				Direction bestDirection = null;
				// looks around for an ore concentration that is bigger than its current location by a certain fraction
				for (Direction dir: DirectionHelper.directions) {
					double possibleOre = rc.senseOre(myLocation.add(dir));
					if (possibleOre > maxOre && possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(dir)]) {
						maxOre = possibleOre;
						bestDirection = dir;
					}
				}

				if (maxOre >= 10 || (myOre == 0 && bestDirection != null) || (myOre <= 2.5 && maxOre >= 5)) {
					rc.move(bestDirection);
					prevDirection = null;
				}
				else if (myOre == 0) {
					int dirint;
					if (prevDirection == null) {
						dirint = rand.nextInt(8);
					}
					else {
						dirint = DirectionHelper.directionToInt(prevDirection);
					}
					int offsetIndex = 0;
					while (offsetIndex < 8) {
						int candidateDirection = (dirint+offsets[offsetIndex]+8)%8;
						if (possibleMovesAvoidingEnemies[candidateDirection]) {
							rc.move(DirectionHelper.directions[candidateDirection]);
							prevDirection = DirectionHelper.directions[candidateDirection];
							break;
						}
						offsetIndex++;
					}
				}
				else {
					int ore = rc.readBroadcast(Broadcast.minerOreX100Ch);
					rc.broadcast(Broadcast.minerOreX100Ch, ore + (int)(100 * Math.max(myOre / 4, 0.2)));
					rc.mine();
				}
			}
		}
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(5, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }
	}
}