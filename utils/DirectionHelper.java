package team158.utils;

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
		Direction clockwiseDirections[] = new Direction[7];
		int directionInt = directionToInt(initDir);
		int index = 0;
		
		// directions from initDir to end of directions array
		for(int i = directionInt + 1; i <= 7; i++) {
			Direction nextDir = DirectionHelper.directions[i];
			clockwiseDirections[index] = nextDir;
			index++;
		}
		
		// directions from start of directions array to initDir
		for (int i = 0; i < directionInt; i++) {
			Direction nextDir = DirectionHelper.directions[i];
			clockwiseDirections[index] = nextDir;
			index++;
		}
		return clockwiseDirections;
	}
	
	// get directions going counter-clockwise starting from one direction counter-clockwise
	// from initDir
	public static Direction[] getCounterClockwiseDirections(Direction initDir) {
		Direction counterClockwiseDirections[] = new Direction[7];
		int directionInt = directionToInt(initDir);
		int index = 0;
		// directions from initDir to start of directions array
		for(int i = directionInt - 1; i >= 0; i--) {
			Direction nextDir = DirectionHelper.directions[i];
			counterClockwiseDirections[index] = nextDir;
			index++;
		}
		// directions from end of directions array to initDir
		for (int i = 7; i > directionInt; i--) {
			Direction nextDir = DirectionHelper.directions[i];
			counterClockwiseDirections[index] = nextDir;
			index++;
		}
		return counterClockwiseDirections;
	}
}
