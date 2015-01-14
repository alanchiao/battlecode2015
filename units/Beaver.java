package team158.units;

import team158.utils.*;
import battlecode.common.*;

public class Beaver extends Unit {
	
	int stepsUntilEnemyHQ;
	boolean stayNearHQ;
	boolean buildBuildingsClose;
	MapLocation[] safeLocations;

	public Beaver(RobotController newRC) {
		super(newRC);
		stepsUntilEnemyHQ = 0;
		stayNearHQ = true;
		int numTowers = rc.senseEnemyTowerLocations().length;
		try {
			if (numTowers <= 4) {
				buildBuildingsClose = true;
				safeLocations = new MapLocation[25];
				if (numTowers >= 2) {
					for (int i = -2; i <= 2; i++) {
						for (int j = -2; j <= 2; j++) {
							safeLocations[i + 5 * j + 12] = new MapLocation(ownHQ.x + i, ownHQ.y + j);
						}
					}
				}
				else {
					safeLocations[6] = ownHQ.add(Direction.NORTH_WEST);
					safeLocations[7] = ownHQ.add(Direction.NORTH);
					safeLocations[8] = ownHQ.add(Direction.NORTH_EAST);
					safeLocations[11] = ownHQ.add(Direction.WEST);
					safeLocations[13] = ownHQ.add(Direction.EAST);
					safeLocations[16] = ownHQ.add(Direction.SOUTH_WEST);
					safeLocations[17] = ownHQ.add(Direction.SOUTH);
					safeLocations[18] = ownHQ.add(Direction.SOUTH_EAST);
				}
				for (int i = 0; i < 25; i++) {
					if (safeLocations[i] != null &&
						(navigation.isBuilding(safeLocations[i]) || !rc.senseTerrainTile(safeLocations[i]).isTraversable())) {
						safeLocations[i] = null;
					}
				}
			}
			else {
				buildBuildingsClose = false;
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}

	private final int[] offsets = {0,1,-1,2,-2,3,-3,4};
	private void tryBuildInDirection(int dirint, RobotType robotType) throws GameActionException {
		if (!buildBuildingsClose) {
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
			MapLocation myLocation = rc.getLocation();
			int minDistance = 999999;
			MapLocation targetLocation = null;
			for (MapLocation l : safeLocations) {
				if (l != null && rc.senseRobotAtLocation(l) == null) {
					int distance = l.distanceSquaredTo(myLocation);
					if (distance < minDistance) {
						minDistance = distance;
						targetLocation = l;
					}
				}
			}
			if (targetLocation != null) {
				if (minDistance <= 2 && rc.getTeamOre() >= robotType.oreCost) {
					safeLocations[targetLocation.x - ownHQ.x + 5 * (targetLocation.y - ownHQ.y) + 12] = null;
					rc.build(myLocation.directionTo(targetLocation), robotType);
				}
				else {
					navigation.moveToDestination(targetLocation, true);
				}
			}
			else {
				// Determine if safe locations have been exhausted
				for (MapLocation l : safeLocations) {
					if (l != null) {
						return;
					}
				}
				buildBuildingsClose = false;
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
			if (rc.readBroadcast(Broadcast.buildAerospaceLabsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildAerospaceLabsCh, 0);
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(ownHQ));
				tryBuildInDirection(dirint, RobotType.AEROSPACELAB);
			}
			else if (rc.readBroadcast(Broadcast.buildHelipadsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildHelipadsCh, 0);
				int dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(ownHQ));
				tryBuildInDirection(dirint, RobotType.HELIPAD);
			}
			else if (rc.readBroadcast(Broadcast.buildTankFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildTankFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(enemyHQ.directionTo(ownHQ));
				tryBuildInDirection(dirint, RobotType.TANKFACTORY);
			}
			// HQ has given command to build a supply depot
			else if (rc.readBroadcast(Broadcast.buildSupplyCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildSupplyCh, 0);
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(ownHQ));
				tryBuildInDirection(dirint, RobotType.SUPPLYDEPOT);
			}
			// HQ has given command to build a miner factory
			else if (rc.readBroadcast(Broadcast.buildMinerFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildMinerFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(myLocation.directionTo(ownHQ));
				tryBuildInDirection(dirint, RobotType.MINERFACTORY);
			}
			// HQ has given command to build a barracks
			else if (rc.readBroadcast(Broadcast.buildBarracksCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildBarracksCh, 0);
				int dirint = DirectionHelper.directionToInt(ownHQ.directionTo(enemyHQ));
				tryBuildInDirection(dirint, RobotType.BARRACKS);
			}
			else if (rc.readBroadcast(Broadcast.scoutEnemyHQCh) == rc.getID()) {
				navigation.moveToDestination(enemyHQ, true);
				stepsUntilEnemyHQ++;
				// uses symmetrical properties of map. doubles distance it had to travel
				// to get there. May be delayed by enemy units, but shouldn't be much
				// since early game
				if (rc.getLocation().distanceSquaredTo(enemyHQ) <= rc.getLocation().distanceSquaredTo(ownHQ)) {
					rc.broadcast(Broadcast.scoutEnemyHQCh, stepsUntilEnemyHQ * 2);
					stayNearHQ = false;
				}
			}
			else if (!buildBuildingsClose || !stayNearHQ) {
				double currentOre = rc.senseOre(myLocation);
				double maxOre = -2;
				Direction bestDirection = null;
				boolean[] avoidMoves = navigation.moveDirectionsAvoidingAttack(rc.senseNearbyRobots(24, rc.getTeam().opponent()), 5);
				// looks around for an ore concentration that is bigger than its current location by a certain fraction
				for (Direction dir: DirectionHelper.directions) {
					MapLocation possibleLocation = myLocation.add(dir);
					if (!stayNearHQ || possibleLocation.distanceSquaredTo(ownHQ) < 15) {
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