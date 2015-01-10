package team158.units;
import team158.Robot;
import team158.units.com.Navigation;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public abstract class Unit extends Robot {
	// navigation information
	public boolean isAvoidingObstacle = false; // whether in state of avoiding obstacle
	public MapLocation destinationPoint; // desired point to reach
	public Direction origDirection = null; // original direction of collision of robot into obstacle
	public MapLocation monitoredObstacle; // obstacle tile to move relative to
	
	// grouping information
	public int groupID = -1;
	/**
	 * groupID:
	 * -1 = ungrouped
	 * 0 = retreating back
	 * >0 = grouped  
	 */
	
	public void move() {
		try {
			// Transfer supply stage
			int mySupply = (int) rc.getSupplyLevel();
			RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
			if (friendlyRobots.length > 0) {
				if (rc.getHealth() < 9) {
					
					RobotInfo bestFriend = null;
					double maxHealth = 0;
					for (RobotInfo r : friendlyRobots) {
						if (r.health > maxHealth) {
							maxHealth = r.health;
							bestFriend = r;
						}
					}
					if (maxHealth > 8) {
						rc.transferSupplies(mySupply, bestFriend.location);
					}
				}
				else if (mySupply > rc.getType().supplyUpkeep * 100) {
					for (RobotInfo r : friendlyRobots) {
						if (r.supplyLevel < r.type.supplyUpkeep * 50) {
							rc.transferSupplies(Math.min(mySupply / 2, r.type.supplyUpkeep * 200), r.location);
							break;
						}
					}
				}
			}
			
			// Grouping stage
			if (groupID == -1) {
				int broadcastCh = -1;
				if (rc.getType() == RobotType.SOLDIER) {
					broadcastCh = Broadcast.groupingSoldiersCh;
				}
				else if (rc.getType() == RobotType.DRONE) {
					broadcastCh = Broadcast.groupingDronesCh;
				}
				if (broadcastCh != -1) {
					int group = rc.readBroadcast(broadcastCh);
					if (group > 0) {
						groupID = group;
					}
				}
			}
			else {
				if (rc.readBroadcast(groupID) == -1) {
					groupID = -1;
				}
			}
			// Unit-specific actions
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
			if (1 / r.health > maxPriority && r.type.attackPower > 0) {
				maxPriority = 1 / r.health;
				target = r.location;
			}
		}
		if (target != null) {
			return target;
		}
		else {
			return enemies[0].location;
		}
	}
	
	protected Direction selectMoveDirectionMicro() {
		MapLocation myLocation = rc.getLocation();
		int myRange = rc.getType().attackRadiusSquared;
		RobotInfo[] enemies = rc.senseNearbyRobots(15, rc.getTeam().opponent());
		int[] damages = new int[9]; // 9th slot for current position
		int[] enemyInRange = new int[8];
		if (enemies.length < 5) { // Only do computation if it won't take too long
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
				if (myLocation.distanceSquaredTo(r.location) <= r.type.attackRadiusSquared) {
					damages[8] += r.type.attackPower / r.type.attackDelay;
				}
			}
		}
		int bestDirection = 8;
		int bestDamage = 999999;
		for (int i = 0; i < 8; i++) {
			if (rc.canMove(DirectionHelper.directions[i]) && damages[i] <= bestDamage && enemyInRange[i] > 0) {
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
	
	public void moveByGroup() {
		try {
			boolean toldToAttack = rc.readBroadcast(groupID) == 1;
			MapLocation target;
			if (toldToAttack) {
				target = rc.senseEnemyHQLocation();
			}
			else {
				int xLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
				int yLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
				target = new MapLocation(xLoc, yLoc);
				// TODO: more robust way of determining when rally point has been reached
				if (target.distanceSquaredTo(rc.getLocation()) <= 24) {
					rc.broadcast(groupID, -1);
					groupID = -1;
				}
			}
			// optimization. stop trying to traverse an obstacle once destination changes
			if (this.destinationPoint != null &&  (this.destinationPoint.x != target.x || this.destinationPoint.y != target.y)) { // then no longer obstacle
				this.isAvoidingObstacle = false;
			}
			this.destinationPoint = target;
			Navigation.moveToDestinationPoint(rc, this);

		} 
		catch (GameActionException e) {
			return;
		}
	}

}
