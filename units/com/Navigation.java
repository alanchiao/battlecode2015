package battlecode2015.units.com;


import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode2015.units.Unit;
import battlecode2015.utils.DirectionHelper;

public class Navigation {
	
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
		
	public static void moveToDestinationPoint(RobotController rc, Unit unit) {
		try {
			if (unit.origDirection != null) {
				rc.setIndicatorString(0, unit.origDirection.toString());
			}
			Direction directDirection = rc.getLocation().directionTo(unit.destinationPoint);
			
			// then hug wall in counterclockwise motion
			if(unit.isAvoidingObstacle) {
				Direction dirToObstacle = rc.getLocation().directionTo(unit.lastObstacle);
				Direction clockwiseDirections[] = DirectionHelper.getClockwiseDirections(dirToObstacle);
				// find two possible moves that would relate to wall hugging
				
				// first way - unit to obstacle direction, rotate clockwise
				for (Direction attemptedDir: clockwiseDirections) {
					MapLocation attemptedLocation = rc.getLocation().add(attemptedDir);
					
					// should not be going back to old location, try other rotation direction
					if(attemptedLocation == unit.lastLocation) {
						break;	
					}
					
					// if there is a unit there, do not move to prevent logic
					// from messing up
					if(isMobileUnit(rc, attemptedLocation)) {
						return;
					}
					
					// move in that direction. handle updating logic
					if (rc.canMove(attemptedDir)) {
						// search for obstacles immediately next to new location
						Direction obstacleSearch[] = {Direction.NORTH, Direction.EAST,	Direction.SOUTH, Direction.WEST};
						MapLocation potNextObsts[] = new MapLocation[4];
						int numObstacles = 0;
						for (Direction dir: obstacleSearch) {
							MapLocation potentialObstacle = attemptedLocation.add(dir);
							// yourself
							if (dir == attemptedDir.opposite()) {
								continue;
							}
							if (isStationaryBlock(rc, potentialObstacle)) {
								potNextObsts[numObstacles] = potentialObstacle;
								numObstacles++;
								rc.setIndicatorDot(potentialObstacle, 0, 0, 0);
							}
						}
						
						// obstacle to monitor next is one that is farthest away from the current location
						double maxDistanceSquared = -1;
						MapLocation bestObstacle = null;
						for (int j = 0; j < numObstacles; j++) {
							double distanceSquared = (rc.getLocation().x - potNextObsts[j].x)^2 + (rc.getLocation().y - potNextObsts[j].y)^2;
							if (distanceSquared > maxDistanceSquared) {
								bestObstacle = potNextObsts[j];
							}
						}
						if (bestObstacle != null) {
							unit.lastObstacle = bestObstacle;
						} else {
							// TODO: situation when unit is next to tile with
							// stationary thing : System.out.println("NO OBSTACLE? ERROR");
						}
						
						// check if angle of direction to destination is between angle of movement of this turn and last turn
						// obstacle avoided if that is the case
						if(attemptedDir == unit.origDirection) {
							unit.isAvoidingObstacle = false;
							unit.lastObstacle = null;
							unit.lastDirectionMoved = null;
						}
						
						unit.lastDirectionMoved = attemptedDir;
						unit.lastLocation = rc.getLocation();
						rc.move(attemptedDir);
						return;
					}
				}
			}
			else if (rc.canMove(directDirection)) {
				rc.move(directDirection);
			// possibly found obstacle
			} else {
				MapLocation blockade = rc.getLocation().add(directDirection);
				// only treat as obstacle if stationary
				// otherwise things get ugly in some cases
				if (isStationaryBlock(rc, blockade) && !isBuilding(rc, blockade)) {
					unit.isAvoidingObstacle = true;
					unit.lastObstacle = rc.getLocation().add(directDirection);
					unit.origDirection = directDirection;
				} else {
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
				}
			}
		// error
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
}
