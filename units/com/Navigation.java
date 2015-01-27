package team158.units.com;

import java.util.Random;

import team158.units.Unit;
import team158.utils.DirectionHelper;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Navigation {
	
	public final boolean USE_WALL_HUGGING = true;
	public final int MAX_TOWERS_IN_RANGE = 3; // 3 towers that can attack you at once in some maps we win on
	
	// information needed from Unit
	public RobotController rc;
	public Unit unit;
	public Random rand;
	public MapLocation enemyHQ;
	
	// states
	public boolean isAvoidingObstacle; // whether in state of avoiding obstacle

	// what institutes as an obstacle
	public int avoidLevel;
	public int avoidEnemyMinRange;
	public final static int AVOID_NOTHING = 0;
	public final static int AVOID_ENEMY_ATTACK_BUILDINGS = 1;
	public final static int AVOID_ALL = 2;
	
	// obstacle monitoring information
	public MapLocation destination; // desired point to reach
	public MapLocation origLocation; // original location where you encountered obstacle
	public MapLocation monitoredObstacle; // obstacle tile to move relative to
	public boolean isRotateRight; // turn right or left relative to obstacle
	public boolean isLastObstacleUnit;
	public int timeSinceLastMove;
	
	// precomputation per turn
	public boolean[] possibleMovesAvoidingEnemies;
	
	public Navigation(Unit unit) {
		this.unit = unit;
		this.rc = unit.rc;
		this.rand = unit.rand;
		this.enemyHQ = unit.enemyHQ;
		this.timeSinceLastMove = 0;
		
		isAvoidingObstacle = false;
		avoidLevel = 0;
		destination = null;
		monitoredObstacle = null;
		possibleMovesAvoidingEnemies = null;
	}
	
	
	// main high-level navigational method
	//
	// avoid level: 0 - avoid nothing
	// 				1 - avoid towers and HQ
	//				2 - avoid all enemies with range >= 5
	public void moveToDestination(MapLocation nextDestination, int avoidLevel) {
		if (monitoredObstacle != null) {
			rc.setIndicatorDot(monitoredObstacle, 0, 0, 0);
		}
		
		// optimization: stop avoiding current obstacle if destination changes
		if (!nextDestination.equals(destination)) { // then no longer obstacle
			stopObstacleTracking();
		}
		
		// reached destination
		if (nextDestination.equals(rc.getLocation())) {
			stopObstacleTracking();
			return;
		}
		
		destination = nextDestination;
		this.avoidLevel = avoidLevel;
		this.possibleMovesAvoidingEnemies = null;
		timeSinceLastMove++;
		/**
		if (timeSinceLastMove >= 6) {
			isRotateRight = !isRotateRight;
			timeSinceLastMove = 0;
		}
		**/
		if (USE_WALL_HUGGING) {
			wallHuggingToDestination();
		} else {
			greedyMoveToDestination();
		}
	}
	
	// wall hugging!
	public void wallHuggingToDestination() {
		try {
			MapLocation myLocation = rc.getLocation();
			
			Direction directDirection = myLocation.directionTo(destination);
			MapLocation directLocation = myLocation.add(directDirection);
			if (isAvoidingObstacle) { // then hug wall in counterclockwise motion
				
				// done with obstacle given this condition:
				// 1. can move in direction of destination and
				// 2. closer to destination than before when we first hit the obstacle
				if(rc.canMove(directDirection) && rc.getLocation().distanceSquaredTo(destination) < origLocation.distanceSquaredTo(destination)) {
					stopObstacleTracking();
					wallHuggingToDestination();
					return;
				}
				
				if(!isObstacle(monitoredObstacle, directDirection)) {
					stopObstacleTracking();
					wallHuggingToDestination();
					return;
				}
				
				// wall hugging - consistently hug by rotating right or left until overcome
				// condition above.
				//
				// look at direction to obstacle and rotate one direction until you can move
				// move in that new direction. Any obstacles you see as you rotate the direction
				// become the new "monitored obstacle" to hug.
				Direction dirToObstacle = rc.getLocation().directionTo(monitoredObstacle);
				Direction attemptedDir = dirToObstacle;
				for (int i = 0; i < 4; i++) { // max of 4 before starting to go away
					if (isRotateRight) {
						attemptedDir = attemptedDir.rotateRight();
					} else {
						attemptedDir = attemptedDir.rotateLeft();
					}
					MapLocation attemptedLocation = myLocation.add(attemptedDir);
					
					if (isObstacle(attemptedLocation, attemptedDir)) {
						monitoredObstacle = attemptedLocation;
						if (isMobileUnit(attemptedLocation)) {
							isLastObstacleUnit = true;
						} else {
							isLastObstacleUnit = false;
						}
					} else {
						// if blocked by a mobile unit, go the other direction (to prevent
						if (isPassable(attemptedDir)) {
							if (rc.canMove(attemptedDir)) {
								timeSinceLastMove = 0;
								rc.move(attemptedDir);
							}
							return;
						}
					}
				}
			}
			// not in state of avoiding obstacle
			else if (isPassable(directDirection)) { // then free to move!
				timeSinceLastMove = 0;
				rc.move(directDirection);
			// possibly found obstacle
			} else {
				if (isObstacle(directLocation, directDirection)) { // then start hugging
					startObstacleTracking(directLocation, directDirection);
					if (isMobileUnit(directLocation)) {
						isLastObstacleUnit = true;
					} else {
						isLastObstacleUnit = false;
					}
					wallHuggingToDestination();
					return;
				} else { // otherwise, greedy move since just a building
					greedyMoveToDestination();
				}
				
				/** else { // otherwise, greedy move since bugging gets scary with moving obstacles
					// greedyMoveToDestination();
					isLastObstacleUnit = true;
					int index = 0;
					while(!rc.canMove(directDirection) && index <= 7) {
						if (isRotateRight) {
							directDirection = directDirection.rotateRight();
						} else {
							directDirection = directDirection.rotateLeft();
						}
						index++;
					}
					
					if (index <= 7) {
						rc.move(directDirection);
					}
				} **/
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	// greedy movement
	public void greedyMoveToDestination() {
		try {
			MapLocation myLocation = rc.getLocation();
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(destination));
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			Direction direction = DirectionHelper.directions[dirint];
			while (offsetIndex < 5 && (!rc.canMove(direction) || isObstacle(rc.getLocation().add(direction), direction))) {
				offsetIndex++;
				if (offsetIndex < 5) {
					direction = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
				}	
			}
			Direction moveDirection = null;
			if (offsetIndex < 5) {
				moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
			}
			if (moveDirection != null && myLocation.add(moveDirection).distanceSquaredTo(destination) <= myLocation.distanceSquaredTo(destination)) {
				if (rc.canMove(moveDirection)) {
					timeSinceLastMove = 0;
					rc.move(moveDirection);
				}
			} 
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods
	public void stopObstacleTracking() {
		isAvoidingObstacle = false;
		monitoredObstacle = null;
	}
	
	public void startObstacleTracking(MapLocation obstacle, Direction collisionDirection) throws GameActionException {
		isAvoidingObstacle = true;
		monitoredObstacle = obstacle;
		origLocation = rc.getLocation();
		
		if (isLastObstacleUnit) { // then do not update direction
			return;
		}
		
		// turn in direction that brings it closer to destination, unless
		MapLocation locationIfTurnRight = origLocation.add(collisionDirection.rotateRight());
		MapLocation locationIfTurnLeft = origLocation.add(collisionDirection.rotateLeft());
		
		int distanceIfTurnRight = locationIfTurnRight.distanceSquaredTo(destination);
		int distanceIfTurnLeft = locationIfTurnLeft.distanceSquaredTo(destination);
		if (distanceIfTurnRight < distanceIfTurnLeft) {
			isRotateRight = true;
		} else {
			isRotateRight = false;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// Location analysis methods: to be potentially used outside this class
	
	// treat as passable or not
	public boolean isPassable(Direction movementDirection) {
		if (this.avoidLevel == AVOID_ENEMY_ATTACK_BUILDINGS) {
			if (unit.safeSpots[DirectionHelper.directionToInt(movementDirection)] == 0) {
				return false;
			}
		} else if (this.avoidLevel == AVOID_ALL) {
			if (unit.safeSpots[DirectionHelper.directionToInt(movementDirection)] != 2) {
				return false;
			}
		}	
		return rc.canMove(movementDirection); 
	}
	
	// treat as obstacle or not to wall hug along
	public boolean isObstacle (MapLocation location, Direction movementDirection) throws GameActionException {
		if (this.avoidLevel == AVOID_ENEMY_ATTACK_BUILDINGS) {
			if (unit.safeSpots[DirectionHelper.directionToInt(movementDirection)] == 0) {
				return true;
			}
		}
		else if (this.avoidLevel == AVOID_ALL) {
			if (unit.safeSpots[DirectionHelper.directionToInt(movementDirection)] != 2) {
				return true;
			}
		}
		return isStationaryBlock(location) || isMobileUnit(location);
	}
	
	// check if the location is somewhere a bot cannot go more or less for the entire game
	public boolean isStationaryBlock(MapLocation potentialObstacle) throws GameActionException{
		if (rc.getType() == RobotType.DRONE) {
			return false;
		} else {
			return !rc.senseTerrainTile(potentialObstacle).isTraversable();
		}
	}
	
	// check if there is a building at a location
	public boolean isBuilding(MapLocation potentialBuilding) throws GameActionException{
		RobotInfo robot = rc.senseRobotAtLocation(potentialBuilding);
		if (robot == null) {
			return false;
		}
		RobotType type = robot.type;
		return type.isBuilding;
	}
	
	// check if is unit that moves often
	public boolean isMobileUnit(MapLocation potentialUnit) throws GameActionException{
		RobotInfo robot = rc.senseRobotAtLocation(potentialUnit);
		if (robot == null) {
			return false;
		}
		RobotType type = robot.type;
		return type == RobotType.BASHER ||
			   type == RobotType.BEAVER ||
			   type == RobotType.COMMANDER ||
			   type == RobotType.DRONE ||
			   type == RobotType.SOLDIER ||
			   type == RobotType.LAUNCHER ||
			   type == RobotType.TANK ||
			   type == RobotType.MINER;
	}
}
