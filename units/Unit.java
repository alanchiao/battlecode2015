package battlecode2015.units;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode2015.Robot;

public abstract class Unit extends Robot{
	// stored information about reaching a destination
	// for navigation
	public MapLocation destinationPoint;
	public Direction lastDirectionMoved;
	boolean avoidingObstacle = false;
}
