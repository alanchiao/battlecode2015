package team158.units;

import team158.com.Broadcast;
import team158.units.com.Navigation;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Unit {
	
	public boolean isReloading;
	public boolean noSupply;
	public boolean broadcasted;

	public Launcher(RobotController newRC) {
		super(newRC);
		this.isReloading = false;
		noSupply = false;
		broadcasted = false;
	}

	@Override
	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(24, rc.getTeam().opponent());
		
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
		
		if (rc.getMissileCount() >= 3) {
			isReloading = false;
		}
		// launch 3 missiles at a time, then retreat. Do not launch
		// 3 missiles if already surrounded by more than 1 missile
		/*RobotInfo[] nearbyAlliedUnits = rc.senseNearbyRobots(2, rc.getTeam());
		int nearbyMissileCount = 0;
		for (RobotInfo ally: nearbyAlliedUnits) {
			if (ally.type == RobotType.MISSILE) {
				nearbyMissileCount++;
			}
		}*/
		
		if (enemiesAttackable.length > 0 && !isReloading) {
			MapLocation target =  selectTarget(enemiesAttackable);
			RobotType targetType = rc.senseRobotAtLocation(target).type;
			Direction dirToEnemy = myLocation.directionTo(target);
			
			int missileDensity = 1;
			
			Direction dirToFire = dirToEnemy;
			MapLocation locationToFire = myLocation.add(dirToFire);
			int nearbyAllyMissiles = 0;
			RobotInfo[] nearbyPotentialAllyMissiles;
			
			
			dirToFire = dirToEnemy.rotateLeft();
			locationToFire = myLocation.add(dirToFire);
			nearbyAllyMissiles = 0;
			nearbyPotentialAllyMissiles = rc.senseNearbyRobots(locationToFire, 2, rc.getTeam());
			for (RobotInfo r: nearbyPotentialAllyMissiles) {
				if (r.type == RobotType.MISSILE) {
					nearbyAllyMissiles++;
				}
			}
			if (rc.canLaunch(dirToFire) && nearbyAllyMissiles < missileDensity) {
				rc.launchMissile(dirToFire);
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
			if (rc.canLaunch(dirToFire) && nearbyAllyMissiles < missileDensity) {
				rc.launchMissile(dirToFire);
			}
			
			if (rc.getMissileCount() < 3) {
				isReloading = true;
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
				if (rc.getMissileCount() < 3) {
					isReloading = true;
				}
			}
        }
		
		// Move
		if (rc.isCoreReady()) {
			// reloading - retreat to recover missiles
			if (isReloading) {
				Direction enemyToUs = this.enemyHQ.directionTo(this.ownHQ);
				navigation.moveToDestination(rc.getLocation().add(enemyToUs, 4), Navigation.AVOID_ALL);
				return;
			}

			if (Clock.getRoundNum() < rc.getRoundLimit() - 500) {
				if (groupTracker.groupID == Broadcast.launcherGroupDefenseCh) {
					boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
					if (hasHQCommand) {			
						// enemyNearHQLocationChs defaults to rally location if no enemy around.
						MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
						boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
						boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
						if (towerAttacked) {
							target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
						}
						else if (enemyNear) {
							target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
						}
						chargeToLocation(target);
					}
					else {
						moveToLocationWithMicro(Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs), true);
					}
				}
				else if (groupTracker.groupID == Broadcast.launcherGroupAttackCh) {
					boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
					if (hasHQCommand) {
						// enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
						MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
						MapLocation target;
						if (enemyTowers.length != 0) {
							int minDistance = 9999;
							target = null;
							for (MapLocation tower : enemyTowers) {
								int currentDistance = myLocation.distanceSquaredTo(tower);
								if (currentDistance < minDistance) {
									target = tower;
									minDistance = currentDistance;
								}
							}
						}
						else {
							target = enemyHQ;
						}
						chargeToLocation(target);
					}
					else {
						// enemyNearHQLocationChs defaults to rally location if no enemy around.
						MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
						boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
						boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
						if (towerAttacked) {
							target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
						}
						else if (enemyNear) {
							target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
						}
						chargeToLocation(target);
					}
				}
				else {
					moveToLocationWithMicro(Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs), true);
				}
				
			}
			else {
				if (groupTracker.groupID == Broadcast.launcherGroupAttackCh) {
					chargeToLocation(Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs));
				}
				else {
					MapLocation target;
					MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
					if (enemyTowers.length != 0) {
						int minDistance = 9999;
						target = null;
						for (MapLocation tower : enemyTowers) {
							int currentDistance = myLocation.distanceSquaredTo(tower);
							if (currentDistance < minDistance) {
								target = tower;
								minDistance = currentDistance;
							}
						}
					}
					else {
						target = enemyHQ;
					}
					chargeToLocation(target);
				}
			}
		}
	}

}
