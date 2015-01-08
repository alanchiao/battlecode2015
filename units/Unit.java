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
						}
					}
				}
			}
			
			// Grouping stage
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
	
	public void moveByGroup(MapLocation location) {
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
			}
			this.destinationPoint = target;
			
			//Navigation.moveToDestinationPoint(rc, this);

			// Below code block also does navigation
			MapLocation myLocation = rc.getLocation();
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(target));
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2};
			while (offsetIndex < 5 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			Direction moveDirection = null;
			if (offsetIndex < 5) {
				moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
			}
			if (moveDirection != null && myLocation.add(moveDirection).distanceSquaredTo(target) <= myLocation.distanceSquaredTo(target)) {
				rc.move(moveDirection);
			}
			// End code block. Comment out in order to test navigation for group movement
		} 
		catch (GameActionException e) {
			return;
		}
	}

}
