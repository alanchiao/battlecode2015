package team158.units.com;

import java.util.Random;

import team158.utils.DirectionHelper;
import battlecode.common.Clock;
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
	public Random rand;
	public MapLocation enemyHQ;
	
	// states
	public boolean isAvoidingObstacle; // whether in state of avoiding obstacle
	public boolean isAvoidAllAttack;
	
	// obstacle monitoring informatoin
	public MapLocation destination; // desired point to reach
	public MapLocation origLocation; // original location where you encountered obstacle
	public MapLocation monitoredObstacle; // obstacle tile to move relative to
	public boolean isRotateRight; // turn right or left relative to obstacle
	
	// precomputation per turn
	public boolean[] possibleMovesAvoidingEnemies;
	
	public Navigation(RobotController rc, Random rand, MapLocation enemyHQ) {
		this.rc = rc;
		this.rand = rand;
		this.enemyHQ = enemyHQ;
		
		isAvoidingObstacle = false;
		isAvoidAllAttack = false;
		destination = null;
		monitoredObstacle = null;
		possibleMovesAvoidingEnemies = null;
	}
	
	
	// main high-level navigational method
	public void moveToDestination(MapLocation nextDestination, boolean isAvoidAllAttack) {
		rc.setIndicatorString(0, Boolean.toString(isAvoidingObstacle));
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
		this.isAvoidAllAttack = isAvoidAllAttack;
		this.possibleMovesAvoidingEnemies = null;
		
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
					} else {
						// if blocked by a mobile unit, go the other direction (to prevent
						// blockades)
						if (isMobileUnit(attemptedLocation)) {
							isRotateRight = !isRotateRight;
							return;
						}
						// move in that direction. newLocation = attemptedLocation. Handle updating logic
						else if (isPassable(attemptedLocation, attemptedDir)) {
							if (rc.canMove(attemptedDir)) {
								rc.move(attemptedDir);
							}
							return;
						}
					}
				}
			}
			// not in state of avoiding obstacle
			else if (isPassable(directLocation, directDirection)) { // then free to move!
				rc.move(directDirection);
			// possibly found obstacle
			} else {
				if (isObstacle(directLocation, directDirection)) { // then start hugging
					startObstacleTracking(directLocation, directDirection);
				} else { // otherwise, greedy move since bugging gets scary with moving obstacles
					greedyMoveToDestination();
				}
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
	public boolean isPassable(MapLocation location, Direction movementDirection) {
		boolean isPassable = rc.canMove(movementDirection); 
		if (isAvoidAllAttack) {
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			if(!isOutsideEnemyAttackRange(enemies, 5, location)) {
				return false;
			}
		} 
		return isPassable;
	}
	
	// treat as obstacle or not to wall hug along
	public boolean isObstacle (MapLocation location, Direction movementDirection) throws GameActionException {
		if (isAvoidAllAttack) {
			if (possibleMovesAvoidingEnemies == null) {
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
				possibleMovesAvoidingEnemies = moveDirectionsAvoidingAttack(enemies, 5);
			} 
			if (!possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(movementDirection)]) {
				return true;
			}
		}
		return isStationaryBlock(location);
	}
	
	// check if unit is outside the attack range of any enemy units
	// with greater than rangeSquared range and any enemy buildings
	public boolean isOutsideEnemyAttackRange(RobotInfo[] enemies, int rangeSquared, MapLocation loc) {
		// enemies
		if (enemies != null) {
			for (RobotInfo enemy : enemies) {
				if (enemy.type.attackRadiusSquared > rangeSquared) {
					if (loc.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared) {
						return false;
					}
				}
			}
		}
		// towers
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		for (MapLocation l : enemyTowers) {
			if (loc.distanceSquaredTo(l) <= 24) {
				return false;
			}
		}
		// hq
		int initDistance = loc.distanceSquaredTo(enemyHQ);
		if (enemyTowers.length < 2) {
			if (initDistance <= 24) {
				return false;
			}
		}
		else if (enemyTowers.length > 4) {
			if (loc.add(loc.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
				return false;
			}
		}
		else if (initDistance <= 35) {
			return false;
		}
		return true;
	}

	public boolean[] moveDirectionsAvoidingAttack(RobotInfo[] enemies, int rangeSquared) {
		boolean[] possibleMovesAvoidingEnemies = {true,true,true,true,true,true,true,true,true};
		MapLocation myLocation = rc.getLocation();
		possibleMovesAvoidingEnemies[8] = isOutsideEnemyAttackRange(enemies, rangeSquared, myLocation);
		// enemies
		for (RobotInfo enemy : enemies) {
			if (enemy.type.attackRadiusSquared > rangeSquared) {
				for (Direction d : DirectionHelper.directions) {
					if (myLocation.add(d).distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared) {
						possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(d)] = false;
					}
				}
			}
		}
		// towers
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		for (MapLocation l : enemyTowers) {
			int initDistance = myLocation.distanceSquaredTo(l);
			if (initDistance <= 34) {
				for (Direction d : DirectionHelper.directions) {
					if (myLocation.add(d).distanceSquaredTo(l) <= 24) {
						possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(d)] = false;
					}
				}
			}
		}
		// hq
		int initDistance = myLocation.distanceSquaredTo(enemyHQ);
		if (enemyTowers.length < 2) {
			if (initDistance <= 34) {
				for (Direction d : DirectionHelper.directions) {
					if (myLocation.add(d).distanceSquaredTo(enemyHQ) <= 24) {
						possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(d)] = false;
					}
				}
			}
		}
		else if (enemyTowers.length < 5) {
			if (initDistance <= 52) {
				for (Direction d : DirectionHelper.directions) {
					if (myLocation.add(d).distanceSquaredTo(enemyHQ) <= 35) {
						possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(d)] = false;
					}
				}
			}
		}
		else {
			if (initDistance <= 74) {
				for (Direction d : DirectionHelper.directions) {
					MapLocation newLocation = myLocation.add(d);
					if (newLocation.distanceSquaredTo(enemyHQ) <= 35) {
						possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(d)] = false;
					}
					else if (newLocation.add(newLocation.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
						possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(d)] = false;
					}
				}
			}
		}
		return possibleMovesAvoidingEnemies;
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
