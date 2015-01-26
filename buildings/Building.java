package team158.buildings;
import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import team158.Robot;
import team158.utils.DirectionHelper;

public abstract class Building extends Robot {
	protected double hqDistance;
	public MapLocation myLocation;
	protected double prevHealth;

	public Building (RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
		enemyHQ = rc.senseEnemyHQLocation();
		myLocation = rc.getLocation();
		hqDistance = Math.sqrt(rc.senseHQLocation().distanceSquaredTo(enemyHQ));
		prevHealth = 0;
	}

	@Override
	public void move() {
		try {
			actions();
			prevHealth = rc.getHealth();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}

	public RobotInfo findClosestEnemy(int rangeSquared) {
		//find closest enemy target
		RobotInfo[] closeRobots = rc.senseNearbyRobots(rangeSquared, rc.getTeam().opponent());
		
		if (closeRobots.length == 0) {
			return null;
		}
		RobotInfo closestRobot;
		closestRobot = closeRobots[0];
		int closestDistance = closestRobot.location.distanceSquaredTo(myLocation);
		for (int i = 1; i < closeRobots.length; i++) {
			int distance = closeRobots[i].location.distanceSquaredTo(myLocation);
			if (distance < closestDistance) {
				closestDistance = distance;
				closestRobot = closeRobots[i];
			}
		}	
		return closestRobot;
	}
	
	public void greedySpawn(RobotType spawnType) throws GameActionException {
		int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int offsetIndex = 0;
		while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], spawnType)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], spawnType);
		}
	}
}
