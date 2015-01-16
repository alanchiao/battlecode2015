package team158.units;

import team158.utils.*;
import battlecode.common.*;

public class Miner extends Unit {
	
	boolean isLeaderMiner;
	double lastBroadcastedOreCount;
	
	public Miner(RobotController newRC) {
		super(newRC);
		isLeaderMiner = false;
		lastBroadcastedOreCount = 0;
	}

	private Direction prevDirection = null;
	
	@Override
	protected void actions() throws GameActionException {
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		MapLocation myLocation = rc.getLocation();
		double myOre = rc.senseOre(myLocation);
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(5, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }
		
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
			} else {
				// calculate average ore in 16 squared units
				int numLocations = 0;
				int sumOre = 0;
				MapLocation nearbyLocations[] = myLocation.getAllMapLocationsWithinRadiusSq(myLocation, 16);
				for (MapLocation location: nearbyLocations) {
					numLocations++;
					sumOre += rc.senseOre(location);
				}
				int averageOre = sumOre/numLocations;
				int currentIdealAverage = rc.readBroadcast(Broadcast.idealMiningOreAverage);
				MapLocation betterLocation = Broadcast.readLocation(rc, Broadcast.idealMiningLocation);
				
				rc.setIndicatorString(0, Integer.toString(averageOre));
				rc.setIndicatorString(1, Integer.toString(currentIdealAverage));
				rc.setIndicatorString(2, Boolean.toString(isLeaderMiner));
				
				if (isLeaderMiner) {
					if (lastBroadcastedOreCount == currentIdealAverage) {
						if (currentIdealAverage > averageOre) {
							rc.broadcast(Broadcast.idealMiningOreAverage, averageOre);
							lastBroadcastedOreCount = averageOre;
						}
					} else {
						isLeaderMiner = false;
					}
				}
	
				if (averageOre > currentIdealAverage) {
					Broadcast.broadcastLocation(rc, Broadcast.idealMiningLocation, myLocation);
					rc.broadcast(Broadcast.idealMiningOreAverage, averageOre);
					lastBroadcastedOreCount = averageOre;
					this.isLeaderMiner = true;
					rc.mine();
				// decision to move to nice patch of ore. Once gets closer, its average will increase
				// until it gets enough ore again
				} else if (currentIdealAverage > averageOre * 2.5) {
					navigation.moveToDestination(betterLocation, true);
				} else if (myOre <= 2) {
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
					if (bestDirection != null) {
						rc.move(bestDirection);
					}
				} else {
					rc.mine();
				}
			}
			/**
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
			**/
		}
	}
}