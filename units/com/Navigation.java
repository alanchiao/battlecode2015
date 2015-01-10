package team158.units.com;


import team158.units.Unit;
import team158.utils.DirectionHelper;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Navigation {
	
	public static final boolean USE_WALL_HUGGING = true;
	public static final int MAX_TOWERS_IN_RANGE = 3;
	
	public static void moveToDestinationPoint(RobotController rc, Unit unit) {
		rc.setIndicatorString(0, unit.destinationPoint.toString());
		rc.setIndicatorString(1, Boolean.toString(unit.isAvoidingObstacle));
		if (USE_WALL_HUGGING) {
			wallHuggingToDestination(rc, unit);
		} else {
			greedyMoveToDestination(rc, unit);
		}
	}
	
	// wall hugging!
	public static void wallHuggingToDestination(RobotController rc, Unit unit) {
		try {
			Direction directDirection = rc.getLocation().directionTo(unit.destinationPoint);
			
			if(unit.isAvoidingObstacle) { // then hug wall in counterclockwise motion
				Direction dirToObstacle = rc.getLocation().directionTo(unit.monitoredObstacle);
				Direction clockwiseDirections[] = DirectionHelper.getClockwiseDirections(dirToObstacle);
				for (Direction attemptedDir: clockwiseDirections) {
					MapLocation attemptedLocation = rc.getLocation().add(attemptedDir);
					
					// if there is a unit there blocking the hug path, randomize movement
					if(isMobileUnit(rc, attemptedLocation)) {
						unit.isAvoidingObstacle = false;
						greedyMoveToDestination(rc, unit);
						return;
					}
					// move in that direction. newLocation = attemptedLocation. Handle updating logic
					else if (rc.canMove(attemptedDir) && !isNearMultipleEnemyTowers(rc, rc.getLocation().add(directDirection))) {
						// search for next monitored obstacle, which is one of four directions from next location
						Direction obstacleSearch[] = {Direction.NORTH, Direction.EAST,	Direction.SOUTH, Direction.WEST};
						MapLocation potNextObsts[] = new MapLocation[4];
						int numObstacles = 0;
						for (Direction dir: obstacleSearch) {
							MapLocation potentialObstacle = attemptedLocation.add(dir);
							// ignore yourself - not obstacle
							if (dir == attemptedDir.opposite()) {
								continue;
							}
							if (isStationaryBlock(rc, potentialObstacle)) { // then is obstacle
								potNextObsts[numObstacles] = potentialObstacle;
								numObstacles++;
							}
						}
						
						// obstacle to monitor next is one that is farthest away from the current location
						double maxDistanceSquared = -1;
						MapLocation bestObstacle = null;
						for (int j = 0; j < numObstacles; j++) {
							double distanceSquared = rc.getLocation().distanceSquaredTo(potNextObsts[j]);
							if (distanceSquared > maxDistanceSquared) {
								maxDistanceSquared = distanceSquared;
								bestObstacle = potNextObsts[j];
							}
						}
						
						if (bestObstacle != null) {
							unit.monitoredObstacle = bestObstacle;
						} 
						
						// have traversed past a part of the obstacle if
						// going in the same direction again
						if(attemptedDir == unit.origDirection) {
							unit.isAvoidingObstacle = false;
							unit.monitoredObstacle = null;
							unit.origDirection = null;
						}
						
						rc.move(attemptedDir);
						return;
					}
				}
			}
			// not in state of avoiding obstacle
			else if (rc.canMove(directDirection) && !isNearMultipleEnemyTowers(rc, rc.getLocation().add(directDirection))) {
				rc.move(directDirection);
			// possibly found obstacle
			} else {
				MapLocation blockade = rc.getLocation().add(directDirection);
				if (isStationaryBlock(rc, blockade)) {
					unit.isAvoidingObstacle = true;
					unit.monitoredObstacle = rc.getLocation().add(directDirection);
					unit.origDirection = directDirection;
				} else { // otherwise, using bugging gets scary with moving obstacles
					greedyMoveToDestination(rc, unit);
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	public static void greedyMoveToDestination(RobotController rc, Unit unit) {
		try {
			MapLocation myLocation = rc.getLocation();
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(unit.destinationPoint));
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			while (offsetIndex < 5 && (!rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])
								   || isStationaryBlock(rc, rc.getLocation().add(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])))) {
				offsetIndex++;
			}
			Direction moveDirection = null;
			if (offsetIndex < 5) {
				moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
			}
			if (moveDirection != null && myLocation.add(moveDirection).distanceSquaredTo(unit.destinationPoint) <= myLocation.distanceSquaredTo(unit.destinationPoint)) {
				rc.move(moveDirection);
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	// check if the location is somewhere a bot cannot go more or less for the entire game
	public static boolean isStationaryBlock(RobotController rc, MapLocation potentialObstacle) throws GameActionException{
		return !rc.senseTerrainTile(potentialObstacle).isTraversable() || isBuilding(rc, potentialObstacle) || isNearMultipleEnemyTowers(rc, potentialObstacle);
	}
	
	// check if there is a building at a location
	public static boolean isBuilding(RobotController rc, MapLocation potentialBuilding) throws GameActionException{
		RobotInfo robot = rc.senseRobotAtLocation(potentialBuilding);
		if (robot == null) {
			return false;
		}
		RobotType type = robot.type;
		return type == RobotType.AEROSPACELAB || 
			   type == RobotType.BARRACKS ||
			   type == RobotType.HANDWASHSTATION ||
			   type == RobotType.HELIPAD ||
			   type == RobotType.HQ ||
			   type == RobotType.MINERFACTORY ||
			   type == RobotType.SUPPLYDEPOT ||
			   type == RobotType.TANKFACTORY ||
			   type == RobotType.TECHNOLOGYINSTITUTE ||
			   type == RobotType.TOWER ||
			   type == RobotType.TRAININGFIELD;
	}
	
	// check if is unit that moves often
	// MAY NEED TO EDIT
	public static boolean isMobileUnit(RobotController rc, MapLocation potentialUnit) throws GameActionException{
		RobotInfo robot = rc.senseRobotAtLocation(potentialUnit);
		if (robot == null) {
			return false;
		}
		RobotType type = robot.type;
		return type == RobotType.BASHER ||
			   type == RobotType.BEAVER ||
			   type == RobotType.COMMANDER ||
			   type == RobotType.DRONE ||
			   type == RobotType.SOLDIER;
	}
	
	// checks if location is a danger with respect to the number of towers
	// that can attack a location
	public static boolean isNearMultipleEnemyTowers(RobotController rc, MapLocation location) {
		MapLocation[] enemyTowerLocs = rc.senseEnemyTowerLocations();
		int numCloseEnemyTowers = 0;
		for (MapLocation enemyTowerLo: enemyTowerLocs) {
			if (enemyTowerLo.distanceSquaredTo(location) <= 35) {
				numCloseEnemyTowers ++;
				if (numCloseEnemyTowers > MAX_TOWERS_IN_RANGE) {
					return true;
				}
			}
		}
		return false;
	}
}
