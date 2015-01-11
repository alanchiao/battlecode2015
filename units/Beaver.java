package team158.units;
import team158.units.com.Navigation;
import team158.utils.*;
import battlecode.common.*;

public class Beaver extends Unit {
	private final int[] offsets = {0,1,-1,2,-2,3,-3,4};
	private void tryBuildInDirection(int dirint, RobotType robotType) throws GameActionException {
		int offsetIndex = 0;
		while (offsetIndex < 8 && !rc.canBuild(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], robotType)) {
			offsetIndex++;
		}
		Direction buildDirection = null;
		if (offsetIndex < 8) {
			buildDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
		}
		if (buildDirection != null) {
			rc.build(buildDirection, robotType);
		}
		else {
			rc.disintegrate();
		}
	}
	
	@Override
	protected void actions() throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		if (enemies.length > 0 && rc.isWeaponReady()) { 
			if (rc.isWeaponReady()) {
				rc.attackLocation(enemies[0].location);
			}
		}
		
		if (rc.isCoreReady()) {
			if (rc.readBroadcast(Broadcast.buildHelipadsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildHelipadsCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseEnemyHQLocation().directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.HELIPAD);
			}
			else if (rc.readBroadcast(Broadcast.buildTankFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildTankFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseEnemyHQLocation().directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.TANKFACTORY);
			}
			// HQ has given command to build a supply depot
			else if (rc.readBroadcast(Broadcast.buildSupplyCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildSupplyCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseEnemyHQLocation().directionTo(rc.senseHQLocation()));
				tryBuildInDirection(dirint, RobotType.SUPPLYDEPOT);
			}
			// HQ has given command to build a miner factory
			else if (rc.readBroadcast(Broadcast.buildMinerFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildMinerFactoriesCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseHQLocation().directionTo(myLocation));
				tryBuildInDirection(dirint, RobotType.MINERFACTORY);
			}
			// HQ has given command to build a barracks
			else if (rc.readBroadcast(Broadcast.buildBarracksCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildBarracksCh, 0);
				int dirint = DirectionHelper.directionToInt(rc.senseHQLocation().directionTo(rc.senseEnemyHQLocation()));
				tryBuildInDirection(dirint, RobotType.BARRACKS);
			}
			else {
				double currentOre = rc.senseOre(myLocation);
				double maxOre = -2;
				Direction bestDirection = null;
				boolean[] avoidMoves = Navigation.moveDirectionsAvoidingAttack(rc, rc.senseNearbyRobots(24, rc.getTeam().opponent()), 5);
				// looks around for an ore concentration that is bigger than its current location by a certain fraction
				for (Direction dir: DirectionHelper.directions) {
					MapLocation possibleLocation = myLocation.add(dir);
					if (possibleLocation.distanceSquaredTo(rc.senseHQLocation()) < 8) {
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