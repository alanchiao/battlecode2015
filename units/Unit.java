package battlecode2015.units;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode2015.Robot;
import battlecode2015.units.com.Navigation;
import battlecode2015.utils.Broadcast;
import battlecode2015.utils.DirectionHelper;

public abstract class Unit extends Robot {
	// stored information about reaching a destination
	// for navigation
	public int groupID = -1;
	public MapLocation destinationPoint; // desired point to reach
	public boolean isAvoidingObstacle = false; // whether in state of avoiding obstacle
	public MapLocation lastObstacle; // obstacle tile to move relative to
	public MapLocation lastLocation; // unit's location in previous step
	public Direction lastDirectionMoved = null;
	public Direction origDirection = null; // original direction of collision of robot into obstacle
	
	
	public void move() {
		try {
			int mySupply = (int) rc.getSupplyLevel();
			if (mySupply > rc.getType().supplyUpkeep * 100) {
				RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
				for (RobotInfo r : friendlyRobots) {
					if (r.supplyLevel < r.type.supplyUpkeep * 50) {
						rc.transferSupplies(Math.min(mySupply / 2, r.type.supplyUpkeep * 200), r.location);
					}
				}
			}
			int broadcastCh = -1;
			if (rc.getType() == RobotType.SOLDIER) {
				broadcastCh = Broadcast.groupingSoldiersCh;
			}
			else if (rc.getType() == RobotType.DRONE) {
				broadcastCh = Broadcast.groupingDronesCh;
			}
			else if (rc.getType() == RobotType.BASHER) {
				broadcastCh = Broadcast.groupingBashersCh;
			}
			if (broadcastCh != -1 && rc.readBroadcast(700) !=1) {
				groupID = rc.readBroadcast(broadcastCh);
			}
			actions();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}

	protected MapLocation selectTarget(RobotInfo[] enemies) {
		MapLocation target = null;
		double maxPriority = 0;
		for (RobotInfo r : enemies) {
			if (1 / r.health > maxPriority) {
				maxPriority = 1 / r.health;
				target = r.location;
			}
		}
		return target;
	}
	
	protected Direction selectMoveDirectionMicro() {
		MapLocation myLocation = rc.getLocation();
		int myRange = rc.getType().attackRadiusSquared;
		RobotInfo[] enemies = rc.senseNearbyRobots(15, rc.getTeam().opponent());
		int[] damages = new int[9]; // 9th slot for current position
		int[] enemyInRange = new int[8];
		if (enemies.length < 4) { // Only do computation if it won't take too long
			for (RobotInfo r : enemies) {
				for (int i = 0; i < 8; i++) {
					int newLocationDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(r.location);
					if (newLocationDistance <= r.type.attackRadiusSquared) {
						damages[i] += r.type.attackPower / r.type.attackDelay;
					}
					if (newLocationDistance <= myRange) {
						enemyInRange[i] += 1;
					}
				}
				if (myLocation.distanceSquaredTo(r.location) <=
						r.type.attackRadiusSquared) {
					damages[8] += r.type.attackPower / r.type.attackDelay;
				}
			}
		}
		int bestDirection = 8;
		int bestDamage = 999999;
		for (int i = 0; i < 8; i++) {
			if (damages[i] <= bestDamage && enemyInRange[i] > 0) {
				bestDirection = i;
				bestDamage = damages[i];
			}
		}
		if (bestDamage < damages[8]) {
			return DirectionHelper.directions[bestDirection];
		}
		else {
			return null;
		}
	}
	
	public void moveByGroup(MapLocation location) {
		try {
			boolean toldToAttack = rc.readBroadcast(groupID) == 1;
//			System.out.println(groupID);
//			System.out.println("toldToAttack = " + toldToAttack);
			
			MapLocation target;
			if (toldToAttack) {
				target = rc.senseEnemyHQLocation();
			}
			else {
				int xLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
				int yLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
				target = new MapLocation(xLoc, yLoc);
			}
			this.destinationPoint = target;
			// TODO - should not be calling moveToDestination and doing the random moving
			// at the same time. will cause delay. choose one or the other
			Navigation.moveToDestinationPoint(rc, this);
			
			int dirint = DirectionHelper.directionToInt(rc.getLocation().directionTo(target));
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			while (offsetIndex < 5 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			Direction moveDirection = null;
			if (offsetIndex < 5) {
				moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
			}
			if (moveDirection != null) {
				rc.move(moveDirection);
			}
		} 
		catch (GameActionException e) {
			return;
		}
	}

}
