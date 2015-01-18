package team158.units;
import java.util.Random;

import team158.Robot;
import team158.com.InternalMap;
import team158.units.com.GroupTracker;
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
import battlecode.common.TerrainTile;

public abstract class Unit extends Robot {
		
	protected Navigation navigation;
	protected GroupTracker groupTracker;
	protected InternalMap internalMap;
	protected double prevHealth;
	
	public Unit (RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
		ownHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();	
		distanceBetweenHQ = ownHQ.distanceSquaredTo(enemyHQ);
		prevHealth = 0;
		navigation = new Navigation(rc, rand);
		groupTracker = new GroupTracker(rc);
	}

	@Override
	public void move() {
		try {
			// get information about surrounding walls and broadcast
			/** internal map
			MapLocation locations[] = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 2);
			for (MapLocation location: locations) {
				if (rc.senseTerrainTile(location) == TerrainTile.VOID) {
					internalMap.broadcastLocation(location, 1);
				}
			} **/
			
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
			if (groupTracker.groupID == GroupTracker.UNGROUPED) {
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
						groupTracker.setGroupID(newGroupID);
					}
				}
			}
			else {
				if (rc.readBroadcast(groupTracker.groupID) == -1) {
					groupTracker.unGroup();
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

	public static int rangeConverter(int range) {
		switch (range) {
		case 2: return 8;
		case 5: return 13;
		case 10: return 20;
		case 15: return 24; // doesn't work perfectly for 4^2 + 3^2 case
		default: return 0;
		}
	}

	public void defensiveMove() {
		try {
			boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
			if (hasHQCommand) {
				//enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
				Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);
				MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
				rc.setIndicatorString(1, String.valueOf(rc.readBroadcast(Broadcast.towerAttacked)));
				boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
				boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
				if (towerAttacked) {
					target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
				}
				else if (enemyNear) {
					target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
				}
//				else if (enemyAround) {
//					target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);
//				}			
				rc.setIndicatorString(2, "[ " + target.x + ", " + target.y + " ]");
				int approachStrategy = 0;
				moveToLocationWithMicro(target, approachStrategy);
			}
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}

	}
	public void attackMove() {
		try {
			boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
			//System.out.println(hasHQCommand);
			if (hasHQCommand) {
				//enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
				MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
				rc.setIndicatorString(2, "[ " + target.x + ", " + target.y + " ]");
				int approachStrategy = 1;
				moveToLocationWithMicro(target, approachStrategy);
			}
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}
	public static MapLocation selectTarget(RobotInfo[] enemies) {
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
	
	// Only use this method if the unit cannot attack.
	// approachStrategy 0 - do not approach 1 - approach units with same range 2 - charge
	protected void moveToLocationWithMicro(MapLocation target, int approachStrategy) throws GameActionException {
		Team opponent = rc.getTeam().opponent();
		// 25 covers the edge case with tanks if friendly units have vision
		RobotInfo[] enemies = rc.senseNearbyRobots(25, opponent);
		if (enemies.length == 0) {
			if (target != null) {
				navigation.moveToDestination(target, true);
			}
			return;
		}
		MapLocation myLocation = rc.getLocation();
		// set range arbitrarily if robot is a launcher
		int myRange = rc.getType() != RobotType.LAUNCHER ? rc.getType().attackRadiusSquared : 35;

		if (navigation.isAvoidingAttack(enemies, 0, myLocation)) {
			if (approachStrategy == 2) {
				navigation.moveToDestination(target, false);
				return;
			}
			// Check if almost in range of an enemy
			for (RobotInfo r : enemies) {
				if (myLocation.distanceSquaredTo(r.location) <= rangeConverter(r.type.attackRadiusSquared)) {
					if (approachStrategy == 0) {
						return;
					}
					else { // approachStrategy == 1
						if (r.type.attackRadiusSquared == myRange && navigation.isAvoidingAttack(null, myRange, r.location)) {
							navigation.moveToDestination(r.location, false);
							return;
						}
					}
				}
			}
			
			// Get almost in range of an enemy, or get closer (?) to the enemies
			Direction backupMove = null;
			MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
			for (Direction d : DirectionHelper.directions) {
				boolean good = false;
				boolean reasonable = false;
				MapLocation potentialLocation = myLocation.add(d);
				for (RobotInfo r : enemies) {
					int potentialLocationDistance = potentialLocation.distanceSquaredTo(r.location);
					if (potentialLocationDistance <= r.type.attackRadiusSquared) {
						break;
					}
					if (potentialLocationDistance <= rangeConverter(r.type.attackRadiusSquared)) {
						good = true;
					}
					else {
						reasonable = true;
					}
				}
				// Factor in HQ
				if (enemyTowers.length <= 1) {
					if (potentialLocation.distanceSquaredTo(enemyHQ) <= 24) {
						continue;
					}
				}
				else if (enemyTowers.length <= 4) {
					if (potentialLocation.distanceSquaredTo(enemyHQ) <= 35) {
						continue;
					}
				}
				else if (potentialLocation.add(potentialLocation.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
					continue;
				}
				// Factor in towers
				for (MapLocation tower : enemyTowers) {
					if (potentialLocation.distanceSquaredTo(tower) <= 24) {
						good = false;
						reasonable = false;
						break;
					}
				}

				// Decide on move
				if (rc.canMove(d)) {
					if (good) {
						navigation.stopObstacleTracking();
						rc.move(d);
						return;
					}
					else if (reasonable) {
						backupMove = d;
					}
				}
			}
			if (backupMove != null) {
				navigation.stopObstacleTracking();
				rc.move(backupMove);
			}
		}
		// Take less damage
		else {
			int[] damages = new int[9]; // 9th slot for current position
			
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
				}
				if (myLocation.distanceSquaredTo(r.location) <= radiusSquared) {
					damages[8] += r.type.attackPower / Math.max(r.type.attackDelay, 1);
				}
			}
			
			int bestDirection = 8;
			int bestDamage = 999999;
			for (int i = 0; i < 8; i++) {
				if (rc.canMove(DirectionHelper.directions[i]) && damages[i] <= bestDamage) {
					bestDirection = i;
					bestDamage = damages[i];
				}
			}
			if (bestDamage < damages[8]) {
				navigation.stopObstacleTracking();
				rc.move(DirectionHelper.directions[bestDirection]);
			}
		}
	}	
}
