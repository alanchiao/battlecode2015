package team158.units;
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

public abstract class Unit extends Robot {
		
	protected Navigation navigation;
	protected GroupTracker groupTracker;
	protected InternalMap internalMap;
	protected double prevHealth;
	protected boolean autoSupplyTransfer;
	
	public Unit (RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
		
		ownHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();	
		distanceBetweenHQ = ownHQ.distanceSquaredTo(enemyHQ);
		
		prevHealth = 0;
		autoSupplyTransfer = true;
	
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
				int approachStrategy = 0;
				if (towerAttacked) {
					target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
					approachStrategy = 2;
				}
				else if (enemyNear) {
					target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
				}
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
			if (hasHQCommand) {
				//enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
				MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
				int approachStrategy = 2;
				moveToLocationWithMicro(target, approachStrategy);
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
	
	// Only use this method if the unit cannot attack.
	// approachStrategy 0 - do not approach 
	//			        1 - approach units with same range 
	//					2 - charge
	
	protected void moveToLocationWithMicro(MapLocation target, int approachStrategy) throws GameActionException {
		Team opponentTeam = rc.getTeam().opponent();
		// 25 covers the edge case with tanks if friendly units have vision
		RobotInfo[] enemies = rc.senseNearbyRobots(25, opponentTeam);
		// no enemies in range
		if (enemies.length == 0) { // then move towards destination safely
			if (target != null) {
				if (approachStrategy == 2 && rc.getLocation().distanceSquaredTo(target) <= 52) {
					navigation.moveToDestination(target, Navigation.AVOID_NOTHING);
				}
				else {
					navigation.moveToDestination(target, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
				}
			} else {
				System.out.println("NO TARGET");
			}
			return;
		}
		MapLocation myLocation = rc.getLocation();
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		// set range arbitrarily if robot is a launcher
		int myAttackRange = rc.getType() == RobotType.LAUNCHER ? 35 : rc.getType().attackRadiusSquared;
		if (navigation.isOutsideEnemyAttackRange(enemies, 0, myLocation)) {
			if (approachStrategy == 2) { // then move closer
				navigation.moveToDestination(target, Navigation.AVOID_NOTHING);
				return;
			}
			// Check if almost in range of an enemy
			for (RobotInfo r : enemies) {
				if (myLocation.distanceSquaredTo(r.location) <= rangeConverter(r.type.attackRadiusSquared)) {
					if (approachStrategy == 0) {
						return;
					}
					else if (rc.getWeaponDelay() <= 1) { // approachStrategy == 1
						if (r.type.attackRadiusSquared == myAttackRange && navigation.isOutsideEnemyAttackRange(null, myAttackRange, r.location)) {
							navigation.moveToDestination(r.location, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
							return;
						}
					}
				}
			}
			
			// Get almost in range of an enemy, or get closer (?) to the enemies
			boolean[] reasonableMoves = new boolean[8]; // use instead of good moves
			for (Direction d : DirectionHelper.directions) {
				MapLocation potentialLocation = myLocation.add(d);		
				// if cannot move in direction, do not consider
				if (!rc.canMove(d)) {
					continue;
				}
				
				// avoid moves that let the HQ hit you
				if (enemyTowers.length <= 1) {
					if (potentialLocation.distanceSquaredTo(enemyHQ) <= 24) {
						continue;
					}
				} else if (enemyTowers.length <= 4) {
					if (potentialLocation.distanceSquaredTo(enemyHQ) <= 35) {
						continue;
					}
				} else if (potentialLocation.add(potentialLocation.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
					continue;
				}
				
				// avoid moves that allow towers to attack you
				boolean doesAvoidTower = true;
				for (MapLocation tower : enemyTowers) {
					if (potentialLocation.distanceSquaredTo(tower) <= 24) {
						doesAvoidTower = false;
						break;
					}
				}
				
				if(!doesAvoidTower) {
					continue;
				}
				
				boolean isGoodMove = false;
				boolean isReasonableMove = true;
				
				// sort of strange heuristic?
				for (RobotInfo r : enemies) {
					int potentialLocationDistance = potentialLocation.distanceSquaredTo(r.location);
					if (potentialLocationDistance <= r.type.attackRadiusSquared) {
						isGoodMove = false;
						isReasonableMove = false;
						break;
					}
					if (potentialLocationDistance <= rangeConverter(r.type.attackRadiusSquared)) {
						isGoodMove = true;
					}
				}

				// Decide on move
				if (isGoodMove) {
					navigation.stopObstacleTracking();
					rc.move(d);
					return;
				} else if (isReasonableMove) {
					reasonableMoves[DirectionHelper.directionToInt(d)] = true;
				}
			}
			
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
		}
		// Take less damage
		else {
			if (approachStrategy == 2) { // approach enemy units
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
			}
			int[] damages = new int[9]; // 9th slot for current position
			boolean[] inAttackRange = new boolean[8];
			
			int initDistance = myLocation.distanceSquaredTo(enemyHQ);

			for (MapLocation tower : enemyTowers) {
				int towerDistance = myLocation.distanceSquaredTo(tower);
				if (towerDistance <= 34) {
					for (int i = 0; i < 8; i++) {
						int newDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(tower);
						if (newDistance <= 24) {
							damages[i] += 8;
						}
						if (newDistance <= myAttackRange) {
							inAttackRange[i] = true;
						}
					}
				}
			}
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
			for (RobotInfo r : enemies) {
				int radiusSquared = r.type != RobotType.MISSILE ? r.type.attackRadiusSquared : 5;
				for (int i = 0; i < 8; i++) {
					int newLocationDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(r.location);
					if (newLocationDistance <= radiusSquared) {
						damages[i] += r.type.attackPower / Math.max(r.type.attackDelay, 1);
					}
					if (newLocationDistance <= myAttackRange) {
						inAttackRange[i] = true;
					}
				}
				if (myLocation.distanceSquaredTo(r.location) <= radiusSquared) {
					damages[8] += r.type.attackPower / Math.max(r.type.attackDelay, 1);
				}
			}
			
			int bestDirection = 8;
			int bestDamage = 999999;
			for (int i = 0; i < 8; i++) {
				if (rc.canMove(DirectionHelper.directions[i]) &&
						(approachStrategy != 2 || inAttackRange[i]) && damages[i] <= bestDamage) {
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
