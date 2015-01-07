package battlecode2015.units.com;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;
import battlecode2015.units.Unit;
import battlecode2015.utils.DirectionHelper;

public class Navigation {
	// move unit steadily towards destination
	public static void moveToDestinationPoint(RobotController rc, Unit unit) {
		try {
			Direction fastestDirection = rc.getLocation().directionTo(unit.destinationPoint);
			// hug wall in counterclockwise motion
			if(unit.avoidingObstacle) {
				Direction dirToObstacle = rc.getLocation().directionTo(unit.lastObstacle);
				// find two possible moves that would relate to wall hugging
				
				// first way - unit to obstacle direction, rotate clockwise
				for (int i = 1; i < 7; i++) {
					int dirToObstacleValue = DirectionHelper.directionToInt(dirToObstacle);
					Direction nextDir = DirectionHelper.directions[(dirToObstacleValue + i) % 8];
					// should not be going back to old location, try other rotation direction
					if(rc.getLocation().add(nextDir) == unit.lastLocation) {
						break;
					}
					if (rc.canMove(nextDir)) {
						unit.lastLocation = rc.getLocation();
						// update obstacle square to monitor
						MapLocation potentialNextObstacle = unit.lastObstacle.add(nextDir);
						if (rc.senseTerrainTile(potentialNextObstacle) == TerrainTile.VOID) {
							unit.lastObstacle = potentialNextObstacle;
						}
						// successfully moved to hug wall
						int fastestDirectionValue = DirectionHelper.directionToInt(fastestDirection);
						int lastDirectionValue = DirectionHelper.directionToInt(unit.lastDirectionMoved);
						int nextDirValue = DirectionHelper.directionToInt(nextDir);
						
						// check if angle of direction to destination is between angle of movement of this turn and last turn
						// obstacle avoided if that is the case
						if(unit.lastDirectionMoved != null) {		
							if(lastDirectionValue >= nextDirValue) {
								if (nextDirValue <= fastestDirectionValue && fastestDirectionValue <= lastDirectionValue) {
									unit.avoidingObstacle = false;
								}
							} else {
								if (fastestDirectionValue <= lastDirectionValue || fastestDirectionValue >= nextDirValue) {
									unit.avoidingObstacle = false;
								}
							}
						}
						unit.lastDirectionMoved = nextDir;
						rc.move(nextDir);
					}
				}
				
				// second way - unit to obstacle direction, rotate counterclockwise
				for (int i = 1; i < 7; i++) {
					int dirInt = DirectionHelper.directionToInt(dirToObstacle);
					Direction nextDir = DirectionHelper.directions[(dirInt - i + 8) % 8];
					if (rc.canMove(nextDir)) {
						unit.lastLocation = rc.getLocation();
						// update obstacle square to monitor
						MapLocation potentialNextObstacle = unit.lastObstacle.add(nextDir);
						if (rc.senseTerrainTile(potentialNextObstacle) == TerrainTile.VOID) {
							unit.lastObstacle = potentialNextObstacle;
						}
						rc.move(nextDir);
						break;
					}
				}
			}
			else if(rc.canMove(fastestDirection)) {
				rc.move(fastestDirection);
			// possibly found obstacle
			} else {
				unit.avoidingObstacle = true;
				unit.lastObstacle = rc.getLocation().add(fastestDirection);
			}
		// error
		} catch (GameActionException e) {
			System.out.println("gg");
		}
	}
}
