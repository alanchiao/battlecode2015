package team158.units;

import team158.com.Broadcast;
import team158.utils.DirectionHelper;
import team158.units.com.Navigation;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Launcher extends Unit {
	
	public boolean isReloading;
	public boolean noSupply;
	public boolean broadcasted;
	public boolean notTooLate;
	
	public Direction runAwayDirection;
	public boolean shouldStay;

	public Launcher(RobotController newRC) {
		super(newRC);
		noSupply = false;
		broadcasted = false;
		MapLocation closestTower;
		try {
			closestTower = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
			int distance = rc.getLocation().distanceSquaredTo(closestTower);
			if (distanceBetweenHQ < distance) {
				distance = (int)distanceBetweenHQ; 
			}
			notTooLate = Clock.getRoundNum() < (rc.getRoundLimit() - 100 - Math.sqrt(distance)*4);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		
		this.runAwayDirection = null;
	}

	@Override
	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(26, rc.getTeam().opponent());
		
		MapLocation myLocation = rc.getLocation();

		if (rc.getSupplyLevel() == 0 && myLocation.distanceSquaredTo(ownHQ) > 15) {
			if (noSupply) {
				if (rc.readBroadcast(Broadcast.requestSupplyDroneCh) == 0) {
					broadcasted = true;
					rc.broadcast(Broadcast.requestSupplyDroneCh, rc.getID());
				}
			}
			else {
				noSupply = true;
			}
		}
		else {
			noSupply = false;
			if (broadcasted) {
				if (rc.readBroadcast(Broadcast.requestSupplyDroneCh) == rc.getID()) {
					rc.broadcast(Broadcast.requestSupplyDroneCh, 0);
				}
				broadcasted = false;
			}
		}
		
		if (enemiesAttackable.length > 0) {
			MapLocation target =  selectTarget(enemiesAttackable);
			RobotType targetType = rc.senseRobotAtLocation(target).type;
			Direction dirToEnemy = myLocation.directionTo(target);
			
			int missileDensity;
			int missilesFired = 0;
			if (targetType == RobotType.MISSILE || targetType == RobotType.LAUNCHER) {
				missileDensity = 0;
			} else {
				missileDensity = 1;
			}
			
			Direction dirToFire = dirToEnemy;
			MapLocation locationToFire = myLocation.add(dirToFire);
			int nearbyAllyMissiles = 0;
			RobotInfo[] nearbyPotentialAllyMissiles = rc.senseNearbyRobots(locationToFire, 2, rc.getTeam());
			for (RobotInfo r: nearbyPotentialAllyMissiles) {
				if (r.type == RobotType.MISSILE) {
					nearbyAllyMissiles++;
				}
			}
			if (rc.canLaunch(dirToFire) && nearbyAllyMissiles <= missileDensity) {
				rc.launchMissile(dirToFire);
				missilesFired++;
			}
			dirToFire = dirToEnemy.rotateLeft();
			locationToFire = myLocation.add(dirToFire);
			nearbyAllyMissiles = 0;
			nearbyPotentialAllyMissiles = rc.senseNearbyRobots(locationToFire, 2, rc.getTeam());
			for (RobotInfo r: nearbyPotentialAllyMissiles) {
				if (r.type == RobotType.MISSILE) {
					nearbyAllyMissiles++;
				}
			}
			if (rc.canLaunch(dirToFire) && nearbyAllyMissiles + missilesFired <= missileDensity) {
				rc.launchMissile(dirToFire);
				missilesFired++;
			}
			
			
			dirToFire = dirToEnemy.rotateRight();
			locationToFire = myLocation.add(dirToFire);
			nearbyAllyMissiles = 0;
			nearbyPotentialAllyMissiles = rc.senseNearbyRobots(locationToFire, 2, rc.getTeam());
			for (RobotInfo r: nearbyPotentialAllyMissiles) {
				if (r.type == RobotType.MISSILE) {
					nearbyAllyMissiles++;
				}
			}
			if (rc.canLaunch(dirToFire) && nearbyAllyMissiles + missilesFired <= missileDensity) {
				rc.launchMissile(dirToFire);
			}

		} else { 
			MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
			boolean isNextToTowerOrHQ = false;
			MapLocation target = null;
			for (MapLocation enemyTower: enemyTowers) {
				if (enemyTower.distanceSquaredTo(myLocation) <= 36) {
					isNextToTowerOrHQ = true;
					target = enemyTower;
				}
			}
			
			if (enemyHQ.distanceSquaredTo(myLocation) <= 36) {
				isNextToTowerOrHQ = true;
				target = enemyHQ;
			}
			
			if (isNextToTowerOrHQ) {
				Direction directionToTarget = myLocation.directionTo(target);
				if (rc.canLaunch(directionToTarget)) {
					rc.launchMissile(directionToTarget);
				} 
			} 
        }
		
		// sense nearby missile - run away
		RobotInfo[] potentialEnemyMissiles = rc.senseNearbyRobots(49, rc.getTeam().opponent());
		for (RobotInfo potentialMissile: potentialEnemyMissiles) {
			if (potentialMissile.type == RobotType.MISSILE || potentialMissile.type == RobotType.LAUNCHER) {
				shouldStay = true;
				break;
			}
		}
		
		RobotInfo[] potentialEnemyMissiles2 = rc.senseNearbyRobots(8, rc.getTeam().opponent());
		for (RobotInfo potentialMissile: potentialEnemyMissiles2) {
			if (potentialMissile.type == RobotType.MISSILE || potentialMissile.type == RobotType.LAUNCHER) {
				runAwayDirection = potentialMissile.location.directionTo(myLocation);
				break;
			}
		}
		
		// Move
		if (rc.isCoreReady()) {
			computeStuff();

			if (runAwayDirection != null) {
				if (rc.canMove(runAwayDirection)) {
					rc.move(runAwayDirection);
					runAwayDirection = null;
					return;
				}
				
				if (rc.canMove(runAwayDirection.rotateLeft())) {
					rc.move(runAwayDirection.rotateLeft());
					runAwayDirection = null;
					return;
				}
				
				if (rc.canMove(runAwayDirection.rotateRight())) {
					rc.move(runAwayDirection.rotateRight());
					runAwayDirection = null;
					return;
				}	
				return;
			} else if (shouldStay) {
				shouldStay = false;
				return;
			}
			
		
			MapLocation target = null;
			if (rc.readBroadcast(Broadcast.enemyNearHQ) == 1) {
				target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
				if (myLocation.distanceSquaredTo(target) > 65) {
					target = null;
				}
			}

			if (rc.readBroadcast(Broadcast.enemyNearTower) == 1) {
				target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);
				if (myLocation.distanceSquaredTo(target) > 65) {
					target = null;
				}
			}
			if (target != null) {
				launcherMoveWithMicro(target);
				return;
			}

			if (notTooLate) {
				target = null;
				MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
				if (enemyTowers.length != 0) {
					int minDistance = 9999;
					for (MapLocation tower : enemyTowers) {
						int currentDistance = myLocation.distanceSquaredTo(tower);
						if (currentDistance < minDistance) {
							target = tower;
							minDistance = currentDistance;
						}
					}
					if (enemyTowers.length < 5 && minDistance > 65) {
						target = null;
					}
				}
				if (target == null) {
					target = enemyHQ;
				}
				launcherMoveWithMicro(target);
			}
			else {
				launcherMoveWithMicro(Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs));
			}
		}
	}

	protected void launcherMoveWithMicro(MapLocation target) throws GameActionException {
		Team opponentTeam = rc.getTeam().opponent();
		RobotInfo[] enemies = rc.senseNearbyRobots(26, opponentTeam);
		// no enemies in range
		if (enemies.length == 0) { // then move towards destination safely
			if (target != null) {
				navigation.moveToDestination(target, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
			}
			return;
		}

		MapLocation myLocation = rc.getLocation();

		if (safeSpots[8] == 2) {
			// Try to move to edge of sight range
			RobotInfo[] dangerousEnemies = rc.senseNearbyRobots(15, opponentTeam);
			if (dangerousEnemies.length > 0) {
				int minDistance = 9999;
				MapLocation closestEnemy = null;
				for (int i = 0; i < dangerousEnemies.length; i++) {
					if (dangerousEnemies[i].type != RobotType.MISSILE && !dangerousEnemies[i].type.isBuilding) {
						int distance = myLocation.distanceSquaredTo(dangerousEnemies[i].location);
						if (distance < minDistance) {
							closestEnemy = dangerousEnemies[i].location;
							minDistance = distance;
						}
					}
				}
				if (closestEnemy != null) {
					Direction moveDirection = closestEnemy.directionTo(myLocation);
					for (int i = 0; i < 3; i++) {
						if (myLocation.add(moveDirection).distanceSquaredTo(closestEnemy) <= 24 &&
								damages[DirectionHelper.directionToInt(moveDirection)] <= damages[8]) {
							rc.move(moveDirection);
						}
						else if (i == 0) {
							moveDirection = moveDirection.rotateLeft();
						}
						else if (i == 1) {
							moveDirection = moveDirection.rotateRight().rotateRight();
						}
					}
				}
			}
		}
		// Take less damage
		else {
			int bestDirection = 8;
			double bestDamage = 999999;
			for (int i = 0; i < 8; i++) {
				if (rc.canMove(DirectionHelper.directions[i]) && damages[i] + i%2 <= bestDamage) {
					bestDirection = i;
					bestDamage = damages[i] + i%2;
				}
			}
			if (bestDamage < damages[8]) {
				navigation.stopObstacleTracking();
				rc.move(DirectionHelper.directions[bestDirection]);
			}
			else if (bestDamage == damages[8]) {
				navigation.moveToDestination(ownHQ, Navigation.AVOID_NOTHING);
			}
		}
	}
}
