package battlecode2015.utils;

import battlecode.common.Direction;

public class DirectionHelper {
	public static Direction[] directions = {
		Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST
	};
	
	public static int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}
	
	// get directions going clockwise starting from one direction clockwise
	// from initDir
	public static Direction[] getClockwiseDirections(Direction initDir) {
		Direction clockwiseDirections[] = new Direction[8];
		for(int i = 1; i <= 7; i++) {
			Direction nextDir = DirectionHelper.directions[(directionToInt(initDir) + i) % 8];
			clockwiseDirections[i - 1] = nextDir;
		}
		return clockwiseDirections;
	}
}
