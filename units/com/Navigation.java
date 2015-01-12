package team158.units.com;

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
	
	public RobotController rc;
	
	public boolean isAvoidingObstacle; // whether in state of avoiding obstacle
	public boolean isAvoidAllAttack;

	public MapLocation destination; // desired point to reach
	public Direction origDirection; // original direction of collision of robot into obstacle
	
	public MapLocation monitoredObstacle; // obstacle tile to move relative to
	public MapLocation lastLocation;
	public int timeSinceLastMove;
	
	public Navigation(RobotController r) {
		rc = r;
		
		isAvoidingObstacle = false;
		isAvoidAllAttack = false;
		destination = null;
		origDirection = null;
		monitoredObstacle = null;
		lastLocation = null;
		timeSinceLastMove = 0;
	}
	
	public void moveToDestination(MapLocation nextDestination, boolean isAvoidAllAttack) {
		// optimization: stop avoiding current obstacle if destination changes
		if (!nextDestination.equals(destination)) { // then no longer obstacle
			stopObstacleTracking();
			lastLocation = null;
		}
		
		// let it change its behavior if it hasn't moved for some time.
		if (timeSinceLastMove >= 8) {
			lastLocation = null;
		}
		
		// new destination
		destination = nextDestination;
		if (destination.equals(rc.getLocation())) {
			return;
		}
		this.isAvoidAllAttack = isAvoidAllAttack;
		
		rc.setIndicatorString(0, destination.toString());
		rc.setIndicatorString(1, Boolean.toString(isAvoidingObstacle));
		if (lastLocation != null) {
			rc.setIndicatorString(2, lastLocation.toString());
		} else {
			rc.setIndicatorString(2, "lastLocation:null");
		}
		
		if (USE_WALL_HUGGING) {
			wallHuggingToDestination();
		} else {
			greedyMoveToDestination();
		}
	}
	
	// wall hugging!
	public void wallHuggingToDestination() {
		try {
			Direction directDirection = rc.getLocation().directionTo(destination);
			MapLocation directLocation = rc.getLocation().add(directDirection);
			
			
			if (isAvoidingObstacle) { // then hug wall in counterclockwise motion
				Direction dirToObstacle = rc.getLocation().directionTo(monitoredObstacle);
				boolean isClockwise = Math.random() < 0.5;
				Direction clockwiseDirections[];
				if (isClockwise) {
					clockwiseDirections = DirectionHelper.getClockwiseDirections(dirToObstacle);
				} else {
					clockwiseDirections = DirectionHelper.getCounterClockwiseDirections(dirToObstacle);
				}
				
				for (int i = 0; i < clockwiseDirections.length; i++) {
					Direction attemptedDir = clockwiseDirections[i];
					MapLocation attemptedLocation = rc.getLocation().add(attemptedDir);
					
					// wrong direction - go other way to stay consistent
					if (lastLocation != null && attemptedLocation.equals(lastLocation)) {
						if (isClockwise) {
							clockwiseDirections = DirectionHelper.getCounterClockwiseDirections(dirToObstacle);
						} else {
							clockwiseDirections = DirectionHelper.getClockwiseDirections(dirToObstacle);
						}	
						i = -1;
						continue;
					}
					
					// if there is a unit there blocking the hug path, move greedily
					if (isMobileUnit(attemptedLocation)) {
						stopObstacleTracking();
						greedyMoveToDestination();
						return;
					}
					// move in that direction. newLocation = attemptedLocation. Handle updating logic

					else if (isPassable(attemptedLocation, attemptedDir)) {
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
							if (isObstacle(attemptedLocation, attemptedDir)) { // then is obstacle
								potNextObsts[numObstacles] = potentialObstacle;
								numObstacles++;
								rc.setIndicatorDot(potentialObstacle, 100, 100, 100);
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
							monitoredObstacle = bestObstacle;
							rc.setIndicatorDot(monitoredObstacle, 0, 0, 0);
						} 
						
						// have traversed past a part of the obstacle if
						// going in the same direction again
						if(attemptedDir == origDirection) {
							stopObstacleTracking();
						}
						if (rc.canMove(attemptedDir)) {
							lastLocation = rc.getLocation();
							rc.move(attemptedDir);
							timeSinceLastMove = 0;
						}
						return;
					}
					
				}
				System.out.println("NO DIRECTIONS TO GO");
			}
			// not in state of avoiding obstacle

			else if (isPassable(directLocation, directDirection)) {
				lastLocation = rc.getLocation();
				rc.move(directDirection);
			// possibly found obstacle
			} else {
				if (isObstacle(directLocation, directDirection)) {
					startObstacleTracking(directLocation, directDirection);
					timeSinceLastMove++;
				} else { // otherwise, using bugging gets scary with moving obstacles
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
			if (dirint == -1) {
				System.out.println(myLocation);
				System.out.println(destination);
			}
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
					lastLocation = rc.getLocation();
					rc.move(moveDirection);
					timeSinceLastMove = 0;
				}
			} else {
				timeSinceLastMove++;
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
		origDirection = null;
	}
	
	public void startObstacleTracking(MapLocation obstacle, Direction collisionDirection) {
		isAvoidingObstacle = true;
		monitoredObstacle = obstacle;
		origDirection = collisionDirection;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// Location analysis methods
	// - 
	
	// treat as passable or not
	public boolean isPassable(MapLocation location, Direction movementDirection) {
		boolean isPassable = rc.canMove(movementDirection); 
		if (isAvoidAllAttack) {
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			if(!isAvoidingAttack(enemies, 5, location)) {
				return false;
			}
		} else {
			isPassable = isPassable && !isNearMultipleEnemyTowers(location);	
		}
		return isPassable;
	}
	
	// treat as obstacle or not to wall hug along
	public boolean isObstacle (MapLocation location, Direction movementDirection) throws GameActionException {
		if (isAvoidAllAttack) {
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			boolean[] possibleMovesAvoidingEnemies = moveDirectionsAvoidingAttack(enemies, 5);
			if (!possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(movementDirection)]) {
				return true;
			}
		}
		return isStationaryBlock(location);
	}
	
	// get list of directions you can move while avoiding all attacks
	
	// set range to infinity -> only avoid towers and HQ
	// otherwise, also avoid units with attack range greater than rangeSquared
	
	public boolean isAvoidingAttack(RobotInfo[] enemies, int rangeSquared, MapLocation loc) {
		// enemies
		for (RobotInfo enemy : enemies) {
			if (enemy.type.attackRadiusSquared > rangeSquared) {
				if (loc.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared) {
					return false;
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
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		int initDistance = loc.distanceSquaredTo(enemyHQ);
		if (enemyTowers.length < 2) {
			if (initDistance <= 24) {
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
		possibleMovesAvoidingEnemies[8] = isAvoidingAttack(enemies, rangeSquared, myLocation);
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
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
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
		else {
			if (initDistance <= 52) {
				for (Direction d : DirectionHelper.directions) {
					if (myLocation.add(d).distanceSquaredTo(enemyHQ) <= 35) {
						possibleMovesAvoidingEnemies[DirectionHelper.directionToInt(d)] = false;
					}
				}
			}
		}
		return possibleMovesAvoidingEnemies;
	}
	
	// checks if location is a danger with respect to the number of towers
	// that can attack a location
	public boolean isNearMultipleEnemyTowers(MapLocation location) {
		MapLocation[] enemyTowerLocs = rc.senseEnemyTowerLocations();
		int numCloseEnemyTowers = 0;
		for (MapLocation enemyTowerLo: enemyTowerLocs) {
			if (enemyTowerLo.distanceSquaredTo(location) <= 35) {
				numCloseEnemyTowers++;
				if (numCloseEnemyTowers > MAX_TOWERS_IN_RANGE) {
					return true;
				}
			}
		}
		return false;
	}
	
	// check if the location is somewhere a bot cannot go more or less for the entire game
	public boolean isStationaryBlock(MapLocation potentialObstacle) throws GameActionException{
		boolean isStationaryBlock;
		if (rc.getType() != RobotType.DRONE) {
			isStationaryBlock = !rc.senseTerrainTile(potentialObstacle).isTraversable();
		} else {
			isStationaryBlock = false;
		}
		if (isAvoidAllAttack) {
			return isStationaryBlock || isBuilding(potentialObstacle);
		} else {
			return isStationaryBlock || isBuilding(potentialObstacle) || isNearMultipleEnemyTowers(potentialObstacle);
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
	// MAY NEED TO EDIT
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
			   type == RobotType.SOLDIER;
	}
}
