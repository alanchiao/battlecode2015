package team158.units;
import java.util.Arrays;
import java.util.Random;

import team158.Robot;
import team158.com.Broadcast;
import team158.com.InternalMap;
import team158.units.com.GroupTracker;
import team158.units.com.Navigation;
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
	protected boolean autoSupplyTransfer;
	protected int[] damages;
	protected boolean[] inRange;
	
	public Unit (RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
		
		ownHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();	
		distanceBetweenHQ = ownHQ.distanceSquaredTo(enemyHQ);
		
		prevHealth = 0;
		autoSupplyTransfer = true;
		
		damages = new int[9];
		inRange = new boolean[8];
	
		navigation = new Navigation(rc, rand, enemyHQ);
		groupTracker = new GroupTracker(rc);
	}

	@Override
	public void move() {
		try {
			// get information about surrounding walls and broadcast
			/** internal map - do not delete yet
			MapLocation locations[] = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 2);
			for (MapLocation location: locations) {
				if (rc.senseTerrainTile(location) == TerrainTile.VOID) {
					internalMap.broadcastLocation(location, 1);
				}
			} **/
			
			// Transfer supply stage
			if (autoSupplyTransfer) {
				int mySupply = (int) rc.getSupplyLevel();
				RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
				if (friendlyRobots.length > 0) {
					// If predicted to die on this turn
					if (rc.getHealth() <= prevHealth / 2) {
						RobotInfo bestFriend = null;
						double maxHealth = 0;
						for (RobotInfo r : friendlyRobots) {
							if (r.health > maxHealth && r.type.isBuilding) {
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
							if (rc.getType() == r.type && r.supplyLevel < r.type.supplyUpkeep * 150 && r.health > 8) {
								rc.transferSupplies(mySupply - rc.getType().supplyUpkeep * 250, r.location);
								break;
							}
						}
					}
					// Give supply to robots that really need it
					else if (mySupply > rc.getType().supplyUpkeep * 100) {
						for (RobotInfo r : friendlyRobots) {
							if (rc.getType() == r.type && r.supplyLevel < r.type.supplyUpkeep * 50 && r.health > 8) {
								rc.transferSupplies((int)(mySupply - r.supplyLevel) / 2, r.location);
								break;
							}
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
				MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
				boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
				boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1;
				if (towerAttacked) {
					target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
					chargeToLocation(target);
				}
				else if (enemyNear) {
					target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);
					moveToLocationWithMicro(target, true);
				}
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
			if (hasHQCommand) {
				//enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
				MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
				chargeToLocation(target);
			}
		}
		catch (Exception e) {
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
	
	// Computes the amount of damage that could be taken before unit's next turn ONLY
	public void computePotentialDamage() {
		MapLocation myLocation = rc.getLocation();
		int myAttackRange = rc.getType() == RobotType.LAUNCHER ? 35 : rc.getType().attackRadiusSquared;
		
		// reset damages and range
		for (int i = 0; i < 8; i++) {
			damages[i] = 0;
			inRange[i] = false;
		}
		damages[8] = 0;
		
		// factor in towers
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		for (MapLocation tower : enemyTowers) {
			int towerDistance = myLocation.distanceSquaredTo(tower);
			if (towerDistance <= 34) {
				for (int i = 0; i < 8; i++) {
					int newDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(tower);
					if (newDistance <= 24) {
						damages[i] += 8;
					}
					if (newDistance <= myAttackRange) {
						inRange[i] = true;
					}
				}
				if (myLocation.distanceSquaredTo(tower) <= 24) {
					damages[8] += 8;
				}
			}
		}
		
		// factor in hq
		int initDistance = myLocation.distanceSquaredTo(enemyHQ);
		if (initDistance <= 74) {
			if (enemyTowers.length >= 5) {
				int towerDamage;
				if (enemyTowers.length == 6) {
					towerDamage = 240;
				} else {
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
			else if (enemyTowers.length >= 2 && initDistance <= 52) {
				int towerDamage;
				if (enemyTowers.length == 2) {
					towerDamage = 12;
				}
				else {
					towerDamage = 18;
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
			else if (initDistance <= 34) {
				for (int i = 0; i < 8; i++) {
					if (myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(enemyHQ) <= 24) {
						damages[i] += 12;
					}
				}
			}
		}
		
		// factor in enemies
		RobotInfo[] enemies = rc.senseNearbyRobots(25, rc.getTeam().opponent());
		for (RobotInfo r : enemies) {
			if (r.type == RobotType.TOWER) {
				continue;
			}
			int radiusSquared = r.type != RobotType.MISSILE ? r.type.attackRadiusSquared : 5;
			boolean canAttack = r.weaponDelay < (r.supplyLevel == 0 ? 1.5 : 2);
			for (int i = 0; i < 8; i++) {
				int newLocationDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(r.location);
				if (newLocationDistance <= radiusSquared && canAttack) {
					damages[i] += r.type.attackPower;
				}
				if (newLocationDistance <= myAttackRange) {
					inRange[i] = true;
				}
			}
			if (myLocation.distanceSquaredTo(r.location) <= radiusSquared && canAttack) {
				damages[8] += r.type.attackPower;
			}
		}
		rc.setIndicatorString(0, Arrays.toString(damages));
	}

	protected void chargeToLocation(MapLocation target) throws GameActionException {
		Team opponentTeam = rc.getTeam().opponent();
		// 25 covers the edge case with tanks if friendly units have vision
		RobotInfo[] enemies = rc.senseNearbyRobots(25, opponentTeam);
		if (enemies.length == 0) {
			if (target == null) {
				return;
			}
			if (rc.getLocation().distanceSquaredTo(target) <= 52) {
				navigation.moveToDestination(target, Navigation.AVOID_NOTHING);
			}
			else {
				navigation.moveToDestination(target, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
			}
			return;
		}

		MapLocation myLocation = rc.getLocation();
		if (navigation.isOutsideEnemyAttackRange(enemies, 0, myLocation)) {
			navigation.moveToDestination(target, Navigation.AVOID_NOTHING);
			return;
		}
		
		// Approach enemy
		int myAttackRange = rc.getType() == RobotType.LAUNCHER ? 35 : rc.getType().attackRadiusSquared;
		RobotInfo[] attackableRobots = rc.senseNearbyRobots(myAttackRange, opponentTeam);
		if (attackableRobots.length == 0) {
			for (RobotInfo r : enemies) {
				// approach enemies that outrange us
				if (r.type.attackRadiusSquared > myAttackRange) {
					navigation.stopObstacleTracking();
					navigation.moveToDestination(r.location, Navigation.AVOID_NOTHING);
					return;
				}
			}
			navigation.moveToDestination(target, Navigation.AVOID_NOTHING);
			return;
		}
		
		// Take less damage
		else {
			computePotentialDamage();
			
			int bestDirection = 8;
			int bestDamage = 999999;
			for (int i = 0; i < 8; i++) {
				if (rc.canMove(DirectionHelper.directions[i]) && inRange[i] && damages[i] <= bestDamage) {
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
	
	// ONLY WORKS IF SUPPLIED
	protected void moveToLocationWithMicro(MapLocation target, boolean avoidDamage) throws GameActionException {
		Team opponentTeam = rc.getTeam().opponent();
		// 25 covers the edge case with tanks if friendly units have vision
		RobotInfo[] enemies = rc.senseNearbyRobots(25, opponentTeam);
		// no enemies in range
		if (enemies.length == 0) { // then move towards destination safely
			if (target != null) {
				navigation.moveToDestination(target, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
			}
			return;
		}

		MapLocation myLocation = rc.getLocation();
		// set range arbitrarily if robot is a launcher
		int myAttackRange = rc.getType() == RobotType.LAUNCHER ? 35 : rc.getType().attackRadiusSquared;

		computePotentialDamage();

		rc.setIndicatorString(1, "in range");
		if (navigation.isOutsideEnemyAttackRange(enemies, 0, myLocation)) {
			rc.setIndicatorString(1, "out of range");
			// Check if almost in range of an enemy
			boolean almostInRange = false;
			for (RobotInfo r : enemies) {
				if (myLocation.distanceSquaredTo(r.location) <= rangeConverter(r.type.attackRadiusSquared)) {
					almostInRange = true;
					if (!avoidDamage && r.type.attackRadiusSquared == myAttackRange) {
						Direction potentialMove = myLocation.directionTo(r.location);
						if (!rc.canMove(potentialMove)) {
							continue;
						}
						int dirInt = DirectionHelper.directionToInt(potentialMove);
						// double newWeaponDelay = rc.getWeaponDelay() + (rc.senseTerrainTile(myLocation.add(potentialMove)) == TerrainTile.VOID ? 1 : rc.getType().loadingDelay - 1);
						if (damages[dirInt] < rc.getType().attackPower && rc.getWeaponDelay() < 2) {
							navigation.stopObstacleTracking();
							rc.setIndicatorString(1, "approach");
							rc.move(potentialMove);
							return;
						}
					}
				}
			}
			if (almostInRange) {
				return;
			}
			
			// Get almost in range of an enemy, or get closer to the enemies
			boolean[] reasonableMoves = new boolean[8]; // use instead of good moves
			for (int i = 0; i < 8; i++) {
				Direction d = DirectionHelper.directions[i];
				MapLocation potentialLocation = myLocation.add(d);		
				// if cannot move in direction, do not consider
				if (!rc.canMove(d) || damages[i] != 0) {
					continue;
				}
				
				reasonableMoves[i] = true;
				
				for (RobotInfo r : enemies) {
					int potentialLocationDistance = potentialLocation.distanceSquaredTo(r.location);
					double newCD = rc.getCoreDelay() + (i % 2 == 1 ? 1.4 : 1) * (rc.senseTerrainTile(myLocation.add(d)) == TerrainTile.VOID ? 2 : 1) - 1;
					// Assume enemy can move immediately
					if (potentialLocationDistance <= rangeConverter(r.type.attackRadiusSquared) && (int)newCD <=
							(int)Math.max((Math.max(r.weaponDelay - 1, 0) + r.type.loadingDelay) - 1, 0)) {
						rc.setIndicatorString(1, "newCD" + newCD);
						navigation.stopObstacleTracking();
						rc.move(d);
						return;
					}
				}
			}
			/*
			Direction dirToEnemy = rc.getLocation().directionTo(enemies[0].location);
			Direction moveDirection = dirToEnemy;
			if (reasonableMoves[DirectionHelper.directionToInt(moveDirection)]) {
				rc.move(moveDirection);
			    return;
			}
			
			moveDirection = dirToEnemy.rotateLeft();
			if (reasonableMoves[DirectionHelper.directionToInt(moveDirection)]) {
			    rc.move(moveDirection);
			    return;
			}
			
			moveDirection = dirToEnemy.rotateRight();
			if (reasonableMoves[DirectionHelper.directionToInt(moveDirection)]) {
			    rc.move(moveDirection);
			    return;
			}
			*/
		}
		// Take less damage
		else {
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
