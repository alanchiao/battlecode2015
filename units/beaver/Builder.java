package team158.units.beaver;

import team158.units.com.Navigation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Builder {
	
	public final static int NUMBER_BUILDINGS_MAX = 30;
	
	// necessary information from Beaver
	private RobotController rc;
	public Navigation navigation;
	private MapLocation hqLocation;
	
	// safe areas to build
	public MapLocation[] safeLocations;
	
	// variables stored to track command
	// to building a particular building
	public boolean isBuilding;
	public RobotType buildingType;
	public MapLocation buildingLocation;
	
	public Builder(RobotController rc, MapLocation hqLocation, Navigation navigation) {
		this.rc = rc;
		this.navigation = navigation;
		this.hqLocation = hqLocation;
		
		this.safeLocations = new MapLocation[NUMBER_BUILDINGS_MAX];
		updateSafeBuildLocations();
		
		this.isBuilding = false;
		this.buildingType = null;
	}
	
	// choose to building a building type
	public void buildBuilding(RobotType buildingType) throws GameActionException {
		this.isBuilding = true;
		this.buildingType = buildingType;
		for (int i = 0; i < NUMBER_BUILDINGS_MAX; i++) {
			MapLocation attemptedLocation = this.safeLocations[i];
			boolean isBuildable = !navigation.isBuilding(attemptedLocation);
			if (isBuildable) {
				this.buildingLocation = attemptedLocation;
				break;
			}
		}
		this.continueBuilding();
	}
	
	
	// continue building the current building
	public void continueBuilding() throws GameActionException {
		Direction dirToBuildingLocation = rc.getLocation().directionTo(this.buildingLocation);
		boolean isNextToBuildingLocation = rc.getLocation().distanceSquaredTo(this.buildingLocation) <= 2;
		if (isNextToBuildingLocation && rc.canBuild(dirToBuildingLocation, this.buildingType)) {
			rc.build(dirToBuildingLocation, this.buildingType);
			this.isBuilding = false;
		} else {
			navigation.moveToDestination(this.buildingLocation.add(Direction.WEST), true);
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
		
		for (int k = 0; k < NUMBER_BUILDINGS_MAX * 2; k++) {
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
	    }
	}
}
