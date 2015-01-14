package team158.units;
import java.util.Random;

import team158.Robot;
import team158.units.com.Group;
import team158.units.com.Navigation;
import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public abstract class Unit extends Robot {
		
	protected Navigation navigation;
	protected Group groupManager;
	protected double prevHealth;
	
	public Unit (RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
		ownHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();	
		distanceBetweenHQ = ownHQ.distanceSquaredTo(enemyHQ);
		prevHealth = 0;
		navigation = new Navigation(rc, rand);
		groupManager = new Group(rc);
	}

	@Override
	public void move() {
		try {
			// Transfer supply stage
			int mySupply = (int) rc.getSupplyLevel();
			RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
			if (friendlyRobots.length > 0) {
				// If predicted to die on this turn
				if (rc.getHealth() <= prevHealth / 2) {
					
					RobotInfo bestFriend = null;
					double maxHealth = 0;
					for (RobotInfo r : friendlyRobots) {
						if (r.health > maxHealth && r.type != RobotType.HQ) {
							maxHealth = r.health;
							bestFriend = r;
						}
					}
					if (maxHealth > 8) {
						rc.transferSupplies(mySupply, bestFriend.location);
					}
				}
				// Get rid of excess supply
				else if (mySupply > rc.getType().supplyUpkeep * 250) {
					for (RobotInfo r : friendlyRobots) {
						if (rc.getType() == r.type && r.supplyLevel < r.type.supplyUpkeep * 150) {
							rc.transferSupplies(mySupply - rc.getType().supplyUpkeep * 250, r.location);
							break;
						}
					}
				}
				// Give supply to robots that really need it
				else if (mySupply > rc.getType().supplyUpkeep * 100) {
					for (RobotInfo r : friendlyRobots) {
						if (rc.getType() == r.type && r.supplyLevel < r.type.supplyUpkeep * 50) {
							rc.transferSupplies((int)(mySupply - r.supplyLevel) / 2, r.location);
							break;
						}
					}
				}
			}
			
			// Grouping stage
			if (groupManager.groupID == Group.UNGROUPED) {
				int broadcastCh = -1;
				if (rc.getType() == RobotType.SOLDIER) {
					broadcastCh = Broadcast.groupingSoldiersCh;
				}
				else if (rc.getType() == RobotType.DRONE) {
					broadcastCh = Broadcast.groupingDronesCh;
				}
				else if (rc.getType() == RobotType.TANK) {
					broadcastCh = Broadcast.groupingTanksCh;
				}
				else if (rc.getType() == RobotType.LAUNCHER) {
					broadcastCh = Broadcast.groupingLaunchersCh;
				}
				if (broadcastCh != -1) {
					int newGroupID = rc.readBroadcast(broadcastCh);
					if (newGroupID > 0) {
						groupManager.setGroupID(newGroupID);
					}
				}
			}
			else {
				if (rc.readBroadcast(groupManager.groupID) == -1) {
					groupManager.unGroup();
				}
			}
			// Unit-specific actions
			actions();
			prevHealth = rc.getHealth();
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
		// set range arbitrarily if robot is a launcher
		int myRange = rc.getType() != RobotType.LAUNCHER ? rc.getType().attackRadiusSquared : 35;
		Team opponent = rc.getTeam().opponent();
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, opponent);
		
		if (enemies.length == 0) {
			return null;
		}

		RobotInfo[] attackableEnemies = rc.senseNearbyRobots(myRange, opponent);
		// Approach enemy units in range
		if (attackableEnemies.length == 0) {
			for (RobotInfo r : enemies) {
				int distance = myLocation.distanceSquaredTo(r.location);
				if (r.type.attackRadiusSquared >= distance && myRange < distance) {
					Direction enemyDirection = myLocation.directionTo(r.location);
					if (rc.canMove(enemyDirection)) {
						return enemyDirection;
					}
				}
			}
			return null;
		}
		
		// Take less damage
		if (enemies.length < 6) { // Only do computation if it won't take too long
			int[] damages = new int[9]; // 9th slot for current position
			int[] enemyInRange = new int[8];
			
			int initDistance = myLocation.distanceSquaredTo(enemyHQ);
			int enemyTowers = rc.senseEnemyTowerLocations().length;
			
			// Must have enough distance to have been missed by sight radius
			if (initDistance > rc.getType().sensorRadiusSquared && enemyTowers >= 2) {
				if (enemyTowers >= 5 && initDistance <= 74) {
					int towerDamage;
					if (enemyTowers == 6) {
						towerDamage = 240;
					}
					else {
						towerDamage = 36;
					}
					int splashDamage = towerDamage / 2;
	
					if (initDistance <= 35) {
						damages[8] += towerDamage;
					}
					else if (myLocation.add(myLocation.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
						damages[8] += splashDamage;
					}
					for (int i = 0; i < 8; i++) {
						MapLocation newLocation = myLocation.add(DirectionHelper.directions[i]);
						if (newLocation.distanceSquaredTo(enemyHQ) <= 35) {
							damages[i] += towerDamage;
						}
						else if (newLocation.add(newLocation.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
							damages[i] += splashDamage;
						}
					}
				}
				else if (initDistance <= 52) {
					int towerDamage;
					if (enemyTowers == 2) {
						towerDamage = 24;
					}
					else {
						towerDamage = 36;
					}
					if (initDistance <= 35) {
						damages[8] += towerDamage;
					}
					for (int i = 0; i < 8; i++) {
						if (myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(enemyHQ) <= 35) {
							damages[i] += towerDamage;
						}
					}
				}
			}
			for (RobotInfo r : enemies) {
				int radiusSquared = r.type != RobotType.MISSILE ? r.type.attackRadiusSquared : 5;
				for (int i = 0; i < 8; i++) {
					int newLocationDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(r.location);
					if (newLocationDistance <= radiusSquared) {
						damages[i] += r.type.attackPower / Math.max(r.type.attackDelay, 1);
					}
					if (newLocationDistance <= myRange) {
						enemyInRange[i] += 1;
					}
				}
				if (myLocation.distanceSquaredTo(r.location) <= radiusSquared) {
					damages[8] += r.type.attackPower / Math.max(r.type.attackDelay, 1);
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
		}
		return null;
	}	
}
