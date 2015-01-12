package team158.units;
import team158.utils.*;
import battlecode.common.*;

public class Beaver extends Unit {
	
	int stepsUntilEnemyHQ;
	boolean stayNearHQ;

	public Beaver(RobotController newRC) {
		super(newRC);
		stepsUntilEnemyHQ = 0;
		stayNearHQ = true;
	}

	private final int[] offsets = {0,1,-1,2,-2,3,-3,4};
	private void tryBuildInDirection(int dirint, RobotType robotType) throws GameActionException {
		int offsetIndex = 0;
		Direction buildDirection = null;
		int numBuildLocations = 0;
		while (offsetIndex < 8) {
			if (rc.canBuild(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], robotType)) {
				numBuildLocations++;
				if (buildDirection == null) {
					buildDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
				}
			}
			offsetIndex++;
		}
		if (numBuildLocations > 1) {
			rc.build(buildDirection, robotType);
		}
		// avoid getting trapped
		else if (numBuildLocations == 1) {
			rc.move(buildDirection);
		}
	}
	
	@Override
	protected void actions() throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		if (enemies.length > 0 && rc.isWeaponReady()) { 
			if (rc.isWeaponReady()) {
				rc.attackLocation(selectTarget(enemies));
			}
		}
		
		if (rc.isCoreReady()) {
			if (rc.readBroadcast(Broadcast.buildHelipadsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildHelipadsCh, 0);
				int dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.HELIPAD);
			}
			else if (rc.readBroadcast(Broadcast.buildTankFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildTankFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.TANKFACTORY);
			}
			// HQ has given command to build a supply depot
			else if (rc.readBroadcast(Broadcast.buildSupplyCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildSupplyCh, 0);
				int dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.SUPPLYDEPOT);
			}
			// HQ has given command to build a miner factory
			else if (rc.readBroadcast(Broadcast.buildMinerFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildMinerFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.MINERFACTORY);
			}
			// HQ has given command to build a barracks
			else if (rc.readBroadcast(Broadcast.buildBarracksCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildBarracksCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseHQLocation().directionTo(enemyHQ));
				tryBuildInDirection(dirint, RobotType.BARRACKS);
			}
			else if (rc.readBroadcast(Broadcast.buildAerospaceLabsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildAerospaceLabsCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseHQLocation().directionTo(enemyHQ));
				tryBuildInDirection(dirint, RobotType.AEROSPACELAB);
			}
			else if (rc.readBroadcast(Broadcast.scoutEnemyHQCh) == rc.getID()){
				navigation.moveToDestination(enemyHQ, true);
				stepsUntilEnemyHQ++;
				// uses symmetrical properties of map. doubles distance it had to travel
				// to get there. May be delayed by enemy units, but shouldn't be much
				// since early game
				if (rc.getLocation().distanceSquaredTo(enemyHQ) <= distanceBetweenHQ/4) {
					rc.broadcast(Broadcast.scoutEnemyHQCh, stepsUntilEnemyHQ * 2);
					stayNearHQ = false;
				}
			} else {
				double currentOre = rc.senseOre(myLocation);
				double maxOre = -2;
				Direction bestDirection = null;
				boolean[] avoidMoves = navigation.moveDirectionsAvoidingAttack(rc.senseNearbyRobots(24, rc.getTeam().opponent()), 5);
				// looks around for an ore concentration that is bigger than its current location by a certain fraction
				for (Direction dir: DirectionHelper.directions) {
					MapLocation possibleLocation = myLocation.add(dir);
					if (!stayNearHQ || possibleLocation.distanceSquaredTo(rc.senseHQLocation()) < 15) {
						double possibleOre = rc.senseOre(possibleLocation);
						if (possibleOre > maxOre && rc.canMove(dir) && avoidMoves[DirectionHelper.directionToInt(dir)]) {
							maxOre = possibleOre;
							bestDirection = dir;
						}
					}
				}
				if (maxOre > 1.5 * currentOre && bestDirection != null) {
					rc.move(bestDirection);
				}
				else {
					rc.mine();
				}
			}
		}
	}
}