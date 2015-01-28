package team158.units;

import battlecode.common.*;
import team158.com.Broadcast;
import team158.units.com.Navigation;
import team158.utils.*;

public class Miner extends Unit {
	
	// ore information propagation
	public double oreThreshold;
	boolean isLeaderMiner;
	double lastBroadcastedOreCount;
	
	// miner scouting
	private int stepsUntilEnemyHQ;
	
	public Miner(RobotController newRC) {
		super(newRC);
		
		this.oreThreshold = 2.5;
		this.isLeaderMiner = false;
		this.lastBroadcastedOreCount = 0;
		
		this.stepsUntilEnemyHQ = 0;
	}

	private Direction prevDirection = null;
	
	@Override
	protected void actions() throws GameActionException {
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		MapLocation myLocation = rc.getLocation();
		double myOre = rc.senseOre(myLocation);

		if (rc.isCoreReady()) {
			computeStuff();

			if (!minerMoveIgnoreOre(myOre < 2.5)) {
				return;
			}

			if (rc.readBroadcast(Broadcast.scoutEnemyHQCh) == rc.getID()) {
				navigation.moveToDestination(enemyHQ, Navigation.AVOID_ALL);
				stepsUntilEnemyHQ++;
				// uses symmetrical properties of map. doubles distance it had to travel
				// to get there. May be delayed by enemy units, but shouldn't be much
				// since early game
				if (rc.getLocation().distanceSquaredTo(enemyHQ) <= rc.getLocation().distanceSquaredTo(ownHQ)) {
					rc.broadcast(Broadcast.scoutEnemyHQCh, stepsUntilEnemyHQ * 2);
				}
			}
			else if (myOre >= 10) {
				int ore = rc.readBroadcast(Broadcast.minerOreX1000Ch);
				rc.broadcast(Broadcast.minerOreX1000Ch,  ore + (int)(1000 * Math.min(Math.max(myOre/4, 0.2), 2.5)));
				rc.mine();
			}
			else {
				double currentThreshold = Clock.getRoundNum() < 1000 ? oreThreshold : 0;
				double maxOre = currentThreshold;
				Direction bestDirection = null;
				// looks around for an ore concentration that is bigger than its current location by a certain fraction
				int dirI = rand.nextInt(8);
				for (int i = 0; i <= 7; i++) {
					Direction dir = DirectionHelper.directions[(dirI + i) % 8];
					double possibleOre = rc.senseOre(myLocation.add(dir));
					if (possibleOre > maxOre && rc.canMove(dir) && damages[(dirI + i) % 8] == 0) {
						maxOre = possibleOre;
						bestDirection = dir;
					}
				}

				if ((myOre <= currentThreshold && bestDirection != null) || (myOre >= 2.5 && maxOre >= myOre * 1.5)) {
					navigation.stopObstacleTracking();
					rc.move(bestDirection);
					prevDirection = null;
				}
				else if (myOre <= currentThreshold) {
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
						if (rc.canMove(DirectionHelper.directions[candidateDirection]) && damages[candidateDirection] == 0) {
							rc.move(DirectionHelper.directions[candidateDirection]);
							prevDirection = DirectionHelper.directions[candidateDirection];
							break;
						}
						offsetIndex++;
					}
				}
				else {
					int ore = rc.readBroadcast(Broadcast.minerOreX1000Ch);
					rc.broadcast(Broadcast.minerOreX1000Ch,  ore + (int)(1000 * Math.min(Math.max(myOre/4, 0.2), 2.5)));
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
	
	// enemies must have length 0
	// returns whether or not it's ok to move
	protected boolean minerMoveIgnoreOre(boolean priorityMove) throws GameActionException {
		Team opponentTeam = rc.getTeam().opponent();
		RobotInfo[] enemies = rc.senseNearbyRobots(24, opponentTeam);
		MapLocation myLocation = rc.getLocation();
		
		if (enemies.length == 0) {
			return true;
		}
		if (safeSpots[8] == 2) {
			for (RobotInfo enemy : enemies) {
				if (enemy.type == RobotType.COMMANDER) {
					if (enemy.location.distanceSquaredTo(myLocation) <= 20) {
						Direction moveDirection = enemy.location.directionTo(myLocation);
						if (damages[DirectionHelper.directionToInt(moveDirection)] <= damages[8] && rc.canMove(moveDirection)) {
							navigation.stopObstacleTracking();
							rc.move(moveDirection);
							return false;
						}
					}
				}
				else if (enemy.type != RobotType.MINER) {
					Direction moveDirection = enemy.location.directionTo(myLocation);
					if (enemy.location.add(moveDirection).distanceSquaredTo(myLocation) <= enemy.type.attackRadiusSquared) {
						if (priorityMove && rc.canMove(moveDirection)) {
							navigation.stopObstacleTracking();
							rc.move(moveDirection);
							return false;
						}
					}
				}
			}
		}
		// Take less damage
		else {
			int bestDirection = 8;
			double bestDamage = 999999;
			for (int i = 0; i < 8; i++) {
				if (rc.canMove(DirectionHelper.directions[i]) && damages[i] + i%2 <= bestDamage) {
					bestDirection = i;
					bestDamage = damages[i] + i%2;
				}
			}
			if (bestDamage < damages[8]) {
				navigation.stopObstacleTracking();
				rc.move(DirectionHelper.directions[bestDirection]);
				return false;
			}
			else if (bestDamage == damages[8]) {
				navigation.moveToDestination(ownHQ, Navigation.AVOID_NOTHING);
				return false;
			}
		}
		return true;
	}
}
