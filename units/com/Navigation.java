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
	
	public static void moveToDestinationPoint(RobotController rc, Unit unit) {
		if (USE_WALL_HUGGING) {
			wallHuggingToDestination(rc, unit);
		} else {
			randomizedMoveToDestination(rc, unit);
		}
	}
	
	public static void wallHuggingToDestination(RobotController rc, Unit unit) {
		try {
			Direction directDirection = rc.getLocation().directionTo(unit.destinationPoint);
			
			if(unit.isAvoidingObstacle) { // then hug wall in counterclockwise motion
				Direction dirToObstacle = rc.getLocation().directionTo(unit.monitoredObstacle);
				Direction clockwiseDirections[] = DirectionHelper.getClockwiseDirections(dirToObstacle);
				for (Direction attemptedDir: clockwiseDirections) {
					MapLocation attemptedLocation = rc.getLocation().add(attemptedDir);
					
					// if there is a unit there blocking the hug path, pause movement
					if(isMobileUnit(rc, attemptedLocation)) {
						return;
					}
					
					// move in that direction. newLocation = attemptedLocation. Handle updating logic
					if (rc.canMove(attemptedDir)) {
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
							double distanceSquared = Math.pow(rc.getLocation().x - potNextObsts[j].x, 2) + Math.pow(rc.getLocation().y - potNextObsts[j].y, 2);
							if (distanceSquared > maxDistanceSquared) {
								maxDistanceSquared = distanceSquared;
								bestObstacle = potNextObsts[j];
							}
						}
						if (bestObstacle != null) {
							unit.monitoredObstacle = bestObstacle;
						} else {
							System.out.println("NO OBSTACLE? ERROR");
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
			else if (rc.canMove(directDirection)) {
				rc.move(directDirection);
			// possibly found obstacle
			} else {
				MapLocation blockade = rc.getLocation().add(directDirection);
				// only treat as obstacle if stationary
				// otherwise things get ugly in some cases
				if (isStationaryBlock(rc, blockade) && !isBuilding(rc, blockade)) {
					unit.isAvoidingObstacle = true;
					unit.monitoredObstacle = rc.getLocation().add(directDirection);
					unit.origDirection = directDirection;
				} else {
					randomizedMoveToDestination(rc, unit);
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	public static void randomizedMoveToDestination(RobotController rc, Unit unit) {
		try {
			MapLocation myLocation = rc.getLocation();
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(unit.destinationPoint));
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			while (offsetIndex < 5 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
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
	
	// check if the location is somewhere already occupied by something stationary
	public static boolean isStationaryBlock(RobotController rc, MapLocation potentialObstacle) throws GameActionException{
		return !rc.senseTerrainTile(potentialObstacle).isTraversable() || isBuilding(rc, potentialObstacle);
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
}
