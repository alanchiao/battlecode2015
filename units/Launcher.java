package team158.units;

import team158.buildings.Headquarters;
import team158.com.Broadcast;
import team158.units.com.Navigation;
import team158.utils.DirectionHelper;
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
		RobotInfo[] nearbyAlliedUnits = rc.senseNearbyRobots(2, rc.getTeam());
		int nearbyMissileCount = 0;
		for (RobotInfo ally: nearbyAlliedUnits) {
			if (ally.type == RobotType.MISSILE) {
				nearbyMissileCount++;
			}
		}
		
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		boolean isTargetTowerOrHq = false;
		for (MapLocation enemyTower: enemyTowers) {
			if (enemyTower.equals(this.navigation.destination)) {
				isTargetTowerOrHq = true;
			}
		}
		
		if (enemyHQ.equals(this.navigation.destination)) {
			isTargetTowerOrHq = true;
		}
		
		if (isTargetTowerOrHq && myLocation.distanceSquaredTo(this.navigation.destination) <= 36) {
			Direction directionToTarget = myLocation.directionTo(this.navigation.destination);
			if (rc.canLaunch(directionToTarget)) {
				rc.launchMissile(directionToTarget);
			}
			if (rc.getMissileCount() < 3) {
				isReloading = true;
			}
		} else if (enemiesAttackable.length > 0 && !isReloading) {
			MapLocation target =  selectTarget(enemiesAttackable);
			RobotType targetType = rc.senseRobotAtLocation(target).type;
			Direction dirToEnemy = myLocation.directionTo(target);
			
			int missileDensity;
			if (targetType == RobotType.MISSILE || targetType == RobotType.LAUNCHER) {
				missileDensity = 1;
			} else {
				missileDensity = 2;
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
			if (rc.canLaunch(dirToFire) && nearbyAllyMissiles < missileDensity) {
				rc.launchMissile(dirToFire);
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
        }
		
		// Move
		if (rc.isCoreReady()) {
			// reloading - retreat ot recover missiles
			if (isReloading) {
				navigation.moveToDestination(this.ownHQ, Navigation.AVOID_ALL);
				return;
			}
			
			MapLocation target;
			int approachStrategy;
			if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_LAUNCHERS_GROUP) {
				if (groupTracker.groupID == Broadcast.launcherGroupDefenseCh) {
					boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
					if (hasHQCommand) {								
						approachStrategy = 2;
						// enemyNearHQLocationChs defaults to rally location if no enemy around.
						target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
						boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
						boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
						if (towerAttacked) {
							target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
						}
						else if (enemyNear) {
							target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
						}
					}
					else {
						approachStrategy = 0;
						target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
					}
				}
				else if (groupTracker.groupID == Broadcast.launcherGroupAttackCh) {
					boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
					if (hasHQCommand) {								
						approachStrategy = 2;
						//enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
						target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
					}
					else {
						approachStrategy = 0;
						target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
					}
				}
				else {
					approachStrategy = 0;
					target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
				}
				
			} else {
				if (groupTracker.groupID == Broadcast.launcherGroupAttackCh) {
					target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
					approachStrategy = 2;
				}
				else {
					target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);
					approachStrategy = 2;
				}
			}
			moveToLocationWithMicro(target, approachStrategy);
		}
	}

}
