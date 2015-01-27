package team158.units.beaver;

import team158.com.Broadcast;
import team158.units.com.Navigation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Builder {
	
	public final static int NUMBER_BUILDINGS_MAX = 50;
	
	// necessary information from Beaver
	private RobotController rc;
	public Navigation navigation;
	private MapLocation hqLocation;
	
	// safe areas to build
	public MapLocation[] safeLocations;
	
	// variables stored to track command
	// to building a particular building
	public boolean isNavigating;
	public RobotType buildingType;
	public MapLocation buildingLocation;
	public int expectedCount;
	
	public Builder(RobotController rc, MapLocation hqLocation, Navigation navigation) {
		this.rc = rc;
		this.navigation = navigation;
		this.hqLocation = hqLocation;
		
		this.safeLocations = new MapLocation[NUMBER_BUILDINGS_MAX];
		updateSafeBuildLocations();
		
		this.isNavigating = false;
		this.buildingType = null;
	}
	
	// choose to building a building type
	public void buildBuilding(RobotType buildingType, int expectedCount) throws GameActionException {
		this.isNavigating = true;
		this.expectedCount = expectedCount;
		this.buildingType = buildingType;
		for (int i = 0; i < NUMBER_BUILDINGS_MAX; i++) {
			MapLocation attemptedLocation = this.safeLocations[i];
			boolean isBuildable = !navigation.isBuilding(attemptedLocation);
			if (isBuildable) {
				this.buildingLocation = attemptedLocation;
				this.continueNavigating();
				return;
			}
		}
		this.isNavigating = false;
	}
	
	
	// continue building the current building
	public void continueNavigating() throws GameActionException {
		rc.setIndicatorString(1, this.buildingLocation.toString() + " " +  Integer.toString(this.expectedCount) + " " + this.buildingType.toString());

		boolean isOnBuildingLocation = rc.getLocation().equals(this.buildingLocation);
		if (isOnBuildingLocation) {
			navigation.moveToDestination(this.buildingLocation.add(Direction.NORTH), Navigation.AVOID_ALL);
			return;
		}

		if (this.expectedCount != countBuildings(this.buildingType)) {
			this.isNavigating = false;
			return;
		}
		
		boolean isNextToBuildingLocation = rc.getLocation().distanceSquaredTo(this.buildingLocation) <= 2;
		if (isNextToBuildingLocation) {
			Direction dirToBuildingLocation = rc.getLocation().directionTo(this.buildingLocation);
			if (rc.canBuild(dirToBuildingLocation, this.buildingType)) {
				rc.build(dirToBuildingLocation, this.buildingType);
				this.isNavigating = false;
			}
		} else {
			navigation.moveToDestination(this.buildingLocation, Navigation.AVOID_ALL);
		}
	}
	
	// available build locations are in spiral around HQ
	//
	// TODO avoid any spots that are next to too many VOID
	public void updateSafeBuildLocations() {
		int dx = 1;
		int dy = 0;
		int segmentLength = 1;
		
		int x = hqLocation.x;
		int y = hqLocation.y;
		int segmentPassed = 0;
		
		int safeLocationCount = 0;
		
		for (int k = 0; k < NUMBER_BUILDINGS_MAX * 4; k++) {
			///////////////////////////////////
			// Spiraling logic
			
	        // make a step in direction (dx, dy) relative to current position (x, y)
	        x += dx;
	        y += dy;
	        segmentPassed++;
	       
	        if (segmentPassed == segmentLength) { // then done with current segment. rotate.
	            segmentPassed = 0;

	            // rotate directions
	            int temp = dx;
	            dx = -dy;
	            dy = temp;

	            // progress to larger segments
	            if (dy == 0) {
	                segmentLength++;
	            }
	        }
	        
	        /////////////////////////////////////
	        // Adding safe locations : skip every other tile
	        
	        MapLocation potentialSafeLocation = new MapLocation(x, y);
 			if (k % 2 == 1 && rc.senseTerrainTile(potentialSafeLocation).isTraversable() ) {	        
		        safeLocations[safeLocationCount] = potentialSafeLocation;
		        safeLocationCount++;
 			}
 			
 			if (safeLocationCount == NUMBER_BUILDINGS_MAX) {
 				return;
 			}
	    }
	}
	
	public int countBuildings(RobotType buildingType) throws GameActionException {
		switch(buildingType) {
		case MINERFACTORY: return rc.readBroadcast(Broadcast.numMinerFactoriesCh);
		case SUPPLYDEPOT: return rc.readBroadcast(Broadcast.numSupplyDepotsCh);
		case AEROSPACELAB: return rc.readBroadcast(Broadcast.numAerospaceLabsCh);
		case HELIPAD: return rc.readBroadcast(Broadcast.numHelipadsCh);
		case BARRACKS: return rc.readBroadcast(Broadcast.numBarracksCh);
		default: return 0;
		}
	}
}
