package team158.com;

import team158.utils.DirectionHelper;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

// Sensor class for common methods that involve the sense
// methods

public class Sensor {
	
	private RobotController rc;
	
	public Sensor(RobotController rc) {
		this.rc = rc;
	}
	
	// check if location is safe from tower and HQ attacks
	public boolean isLocationSafe(MapLocation location) {
		return true;
		/**
		// towers
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		for (MapLocation l : enemyTowers) {
			int initDistance = location.distanceSquaredTo(l);
			if (initDistance <= 34) {
				if () {
					if (location.add(d).distanceSquaredTo(l) <= 24) {
						return false;
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
		**/
	}
}
