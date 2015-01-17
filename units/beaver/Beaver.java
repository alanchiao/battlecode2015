package team158.units.beaver;

import team158.units.Unit;
import team158.utils.*;
import battlecode.common.*;

public class Beaver extends Unit {
	
	public Builder builder;
	
	
	public Beaver(RobotController newRC) {
		super(newRC);
		this.builder = new Builder(rc, ownHQ, navigation);
	}

	
	
	@Override
	protected void actions() throws GameActionException {
		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
			if (enemies.length > 0) { 
				rc.attackLocation(selectTarget(enemies));
			}
		}
		
		if (rc.isCoreReady()) {
			// continue building current building
			if (builder.isBuilding) {
				builder.continueBuilding();
				return;
			}
			
			if (Broadcast.hasSoloCommand(rc, Broadcast.buildAerospaceLabsCh)) {
				rc.broadcast(Broadcast.buildAerospaceLabsCh, 0);
				builder.buildBuilding(RobotType.AEROSPACELAB);
			}
			else if (Broadcast.hasSoloCommand(rc, Broadcast.buildHelipadsCh)) {
				rc.broadcast(Broadcast.buildHelipadsCh, 0);
				builder.buildBuilding(RobotType.HELIPAD);
			}
			else if (Broadcast.hasSoloCommand(rc, Broadcast.buildTankFactoriesCh)) {
				rc.broadcast(Broadcast.buildTankFactoriesCh, 0);
				builder.buildBuilding(RobotType.TANKFACTORY);
			}
			// HQ has given command to build a supply depot
			else if (Broadcast.hasSoloCommand(rc, Broadcast.buildSupplyCh)) {
				rc.broadcast(Broadcast.buildSupplyCh, 0);
				builder.buildBuilding(RobotType.SUPPLYDEPOT);
			}
			// HQ has given command to build a miner factory
			else if (Broadcast.hasSoloCommand(rc, Broadcast.buildMinerFactoriesCh)) {
				rc.broadcast(Broadcast.buildMinerFactoriesCh, 0);
				builder.buildBuilding(RobotType.MINERFACTORY);
			}
			// HQ has given command to build a barracks
			else if (Broadcast.hasSoloCommand(rc, Broadcast.buildBarracksCh)) {
				rc.broadcast(Broadcast.buildBarracksCh, 0);
				builder.buildBuilding(RobotType.BARRACKS);
			}
			else {
				MapLocation myLocation = rc.getLocation();
				double currentOre = rc.senseOre(myLocation);
				double maxOre = -2;
				Direction bestDirection = null;
				boolean[] avoidMoves = navigation.moveDirectionsAvoidingAttack(rc.senseNearbyRobots(24, rc.getTeam().opponent()), 5);
				// looks around for an ore concentration that is bigger than its current location by a certain fraction
				for (Direction dir: DirectionHelper.directions) {
					MapLocation possibleLocation = myLocation.add(dir);
					if (possibleLocation.distanceSquaredTo(ownHQ) < 15) {
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