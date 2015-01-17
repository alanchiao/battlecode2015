package team158.units.beaver;

import team158.units.Unit;
import team158.utils.*;
import battlecode.common.*;

public class Beaver extends Unit {
	
	int stepsUntilEnemyHQ;
	boolean stayNearHQ;
	boolean buildBuildingsClose;
	MapLocation[] safeLocations;
	public Builder builder;

	public Beaver(RobotController newRC) {
		super(newRC);
		// for building
		this.builder = new Builder(rc, ownHQ, navigation);
		// for scouting
		stepsUntilEnemyHQ = 0;
		// for balancing the two
		stayNearHQ = true;
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
			// continue building current building
			if (builder.isBuilding) {
				builder.continueBuilding();
				return;
			}
			
			if (rc.readBroadcast(Broadcast.buildAerospaceLabsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildAerospaceLabsCh, 0);
				builder.buildBuilding(RobotType.AEROSPACELAB);
			}
			else if (rc.readBroadcast(Broadcast.buildHelipadsCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildHelipadsCh, 0);
				builder.buildBuilding(RobotType.HELIPAD);
			}
			else if (rc.readBroadcast(Broadcast.buildTankFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildTankFactoriesCh, 0);
				builder.buildBuilding(RobotType.TANKFACTORY);
			}
			// HQ has given command to build a supply depot
			else if (rc.readBroadcast(Broadcast.buildSupplyCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildSupplyCh, 0);
				builder.buildBuilding(RobotType.SUPPLYDEPOT);
			}
			// HQ has given command to build a miner factory
			else if (rc.readBroadcast(Broadcast.buildMinerFactoriesCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildMinerFactoriesCh, 0);
				builder.buildBuilding(RobotType.MINERFACTORY);
			}
			// HQ has given command to build a barracks
			else if (rc.readBroadcast(Broadcast.buildBarracksCh) == rc.getID()) {
				rc.broadcast(Broadcast.buildBarracksCh, 0);
				builder.buildBuilding(RobotType.BARRACKS);
			}
			/**
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
			else if (!stayNearHQ) { **/
			else {
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
		}
	}
}