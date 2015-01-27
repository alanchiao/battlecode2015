package team158.units;
import java.util.Random;

import team158.Robot;
import team158.com.Broadcast;
import team158.units.com.GroupTracker;
import team158.units.com.Navigation;
import team158.utils.DirectionHelper;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public abstract class Unit extends Robot {
		
	public Navigation navigation;
	protected GroupTracker groupTracker;
	protected double prevHealth;
	protected boolean autoSupplyTransfer;
	protected double[] damages;
	protected boolean[] inRange;
	public int[] safeSpots;
	
	public Unit (RobotController newRC) {
		rc = newRC;
		rand = new Random(rc.getID());
		
		ownHQ = rc.senseHQLocation();
		enemyHQ = rc.senseEnemyHQLocation();	
		distanceBetweenHQ = ownHQ.distanceSquaredTo(enemyHQ);
		
		prevHealth = 0;
		autoSupplyTransfer = true;
		
		damages = new double[9];
		inRange = new boolean[9];
		safeSpots = new int[9];
	
		navigation = new Navigation(this);
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
						rc.setIndicatorString(2, "almost dead");
						RobotInfo bestFriend = null;
						double maxHealth = 0;
						for (RobotInfo r : friendlyRobots) {
							if (r.health > maxHealth && !r.type.isBuilding) {
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
							if (rc.getType() == r.type && r.supplyLevel < r.type.supplyUpkeep * 150 && r.health > 20) {
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

	public void computeStuff() {
		MapLocation myLocation = rc.getLocation();
		int myAttackRange = rc.getType() == RobotType.LAUNCHER ? 35 : rc.getType().attackRadiusSquared;
		
		// reset
		for (int i = 0; i < 9; i++) {
			damages[i] = 0;
			inRange[i] = false;
			safeSpots[i] = 2;
		}
		
		// factor in towers
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		for (MapLocation tower : enemyTowers) {
			int towerDistance = myLocation.distanceSquaredTo(tower);
			if (towerDistance <= 34) {
				int newDistance;
				for (int i = 0; i < 8; i++) {
					newDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(tower);
					if (newDistance <= 24) {
						damages[i] += 8;
						safeSpots[i] = 0;
					}
					if (newDistance <= myAttackRange) {
						inRange[i] = true;
					}
				}
				newDistance = myLocation.distanceSquaredTo(tower);
				if (newDistance <= 24) {
					damages[8] += 8;
					safeSpots[8] = 0;
				}
				if (newDistance <= myAttackRange) {
					inRange[8] = true;
				}
			}
		}
		
		// factor in hq INRANGE IS NOT UPDATED
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
					safeSpots[8] = 0;
				}
				else if (myLocation.add(myLocation.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
					damages[8] += splashDamage;
					safeSpots[8] = 0;
				}
				for (int i = 0; i < 8; i++) {
					MapLocation newLocation = myLocation.add(DirectionHelper.directions[i]);
					if (newLocation.distanceSquaredTo(enemyHQ) <= 35) {
						damages[i] += towerDamage;
						safeSpots[i] = 0;
					}
					else if (newLocation.add(newLocation.directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 35) {
						damages[i] += splashDamage;
						safeSpots[i] = 0;
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
					safeSpots[8] = 0;
				}
				for (int i = 0; i < 8; i++) {
					if (myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(enemyHQ) <= 35) {
						damages[i] += towerDamage;
						safeSpots[i] = 0;
					}
				}
			}
			else if (initDistance <= 34) {
				for (int i = 0; i < 8; i++) {
					if (myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(enemyHQ) <= 24) {
						damages[i] += 12;
						safeSpots[i] = 0;
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
			int newLocationDistance;
			for (int i = 0; i < 8; i++) {
				newLocationDistance = myLocation.add(DirectionHelper.directions[i]).distanceSquaredTo(r.location);
				if (newLocationDistance <= radiusSquared) {
					damages[i] += canAttack ? r.type.attackPower : r.type.attackPower / Math.max(1, r.type.attackDelay);
					safeSpots[i] = 1;
				}
				if (newLocationDistance <= myAttackRange) {
					inRange[i] = true;
				}
			}
			newLocationDistance = myLocation.distanceSquaredTo(r.location);
			if (newLocationDistance <= radiusSquared) {
				damages[8] += canAttack ? r.type.attackPower : r.type.attackPower / Math.max(1, r.type.attackDelay);
				safeSpots[8] = 1;
			}
			if (newLocationDistance <= myAttackRange) {
				inRange[8] = true;
			}
		}
	}
}
