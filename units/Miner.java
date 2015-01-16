package team158.units;

import team158.utils.*;
import battlecode.common.*;

public class Miner extends Unit {
	
	public double ORE_THRESHOLD_TO_MOVE = 2;
	
	boolean isLeaderMiner;
	double lastBroadcastedOreCount;
	private Direction prevDirection = null;
	
	public Miner(RobotController newRC) {
		super(newRC);
		isLeaderMiner = false;
		lastBroadcastedOreCount = 0;
	}
	
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
				MapLocation nearbyLocations[] = MapLocation.getAllMapLocationsWithinRadiusSq(myLocation, 16);
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
				
				/**
				if (currentIdealAverage <= 2) {
					ORE_THRESHOLD_TO_MOVE = 0.5;
				} else if (currentIdealAverage >= 20) {
					ORE_THRESHOLD_TO_MOVE = 10;
				}
				**/
				
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
				// - decision to move to nice patch of ore. Once gets closer, its average will increase
				// until it gets enough ore again.
				// - only half a third of miners do the swarm
				// - only do the swarm if the average is also higher than your ore on your patch
				} else if (currentIdealAverage > averageOre * 2 && currentIdealAverage > myOre * 1.2 && rc.getID() % 3 == 0) {
					navigation.moveToDestination(betterLocation, true);
				} else if (myOre <= ORE_THRESHOLD_TO_MOVE) {
					// looks around for an ore concentration that is bigger than its current location by a certain fraction
					// moves continuously in that direction until it reaches a non-zero ore area
					int nextDirectionIndex;
					if (prevDirection == null) {
						nextDirectionIndex = rand.nextInt(8);
					}
					else {
						nextDirectionIndex = DirectionHelper.directionToInt(prevDirection);
					}
					int offsetIndex = 0;
					while (offsetIndex < 8) {
						int candidateDirection = (nextDirectionIndex + offsets[offsetIndex]+8)%8;
						if (possibleMovesAvoidingEnemies[candidateDirection]) {
							rc.move(DirectionHelper.directions[candidateDirection]);
							this.prevDirection = DirectionHelper.directions[candidateDirection];
							break;
						}
						offsetIndex++;
					}
				} else {
					rc.mine();
				}
			}
		}
	}
}