package team158.units;
import team158.utils.*;
import battlecode.common.*;

public class Beaver extends Unit {
	
	int stepsUntilEnemyHQ;
	boolean stayNearHQ;
	// for building the first few buildings
	boolean needMove;
	boolean buildBuildingsClose;

	public Beaver(RobotController newRC) {
		super(newRC);
		stepsUntilEnemyHQ = 0;
		stayNearHQ = true;
		needMove = false;
		try {
			buildBuildingsClose = rc.readBroadcast(Broadcast.buildBuildingsCloseCh) == 1;
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}

	private final int[] offsets = {0,1,-1,2,-2,3,-3,4};
	private void tryBuildInDirection(int dirint, RobotType robotType, int numBuildings) throws GameActionException {
		if (!buildBuildingsClose || numBuildings >= 4) {
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
		else {
			Direction buildDirection = DirectionHelper.directions[2 * numBuildings];
			if (rc.canBuild(buildDirection, robotType)) {
				rc.build(buildDirection, robotType);
				needMove = true;
			}
			else {
				rc.mine();
			}
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
			int buildings = rc.readBroadcast(Broadcast.numBuildingsCh);
			if (needMove) {
				rc.move(DirectionHelper.directions[(3 + buildings * 2) % 8]);
				needMove = false;
			}
			else if (rc.readBroadcast(Broadcast.buildAerospaceLabsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildAerospaceLabsCh, 0);
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.AEROSPACELAB, buildings);
			}
			else if (rc.readBroadcast(Broadcast.buildHelipadsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildHelipadsCh, 0);
				int dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.HELIPAD, buildings);
			}
			else if (rc.readBroadcast(Broadcast.buildTankFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildTankFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.TANKFACTORY, buildings);
			}
			// HQ has given command to build a supply depot
			else if (rc.readBroadcast(Broadcast.buildSupplyCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildSupplyCh, 0);
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.SUPPLYDEPOT, buildings);
			}
			// HQ has given command to build a miner factory
			else if (rc.readBroadcast(Broadcast.buildMinerFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildMinerFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.MINERFACTORY, buildings);
			}
			// HQ has given command to build a barracks
			else if (rc.readBroadcast(Broadcast.buildBarracksCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildBarracksCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseHQLocation().directionTo(enemyHQ));
				tryBuildInDirection(dirint, RobotType.BARRACKS, buildings);
			}
			else if (rc.readBroadcast(Broadcast.scoutEnemyHQCh) == rc.getID()) {
				navigation.moveToDestination(enemyHQ, true);
				stepsUntilEnemyHQ++;
				// uses symmetrical properties of map. doubles distance it had to travel
				// to get there. May be delayed by enemy units, but shouldn't be much
				// since early game
				if (rc.getLocation().distanceSquaredTo(enemyHQ) <= distanceBetweenHQ/4) {
					rc.broadcast(Broadcast.scoutEnemyHQCh, stepsUntilEnemyHQ * 2);
					stayNearHQ = false;
				}
			}
			else if (!buildBuildingsClose || !stayNearHQ || buildings >= 4) {
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
			else {
				rc.mine();
			}
		}
	}
}