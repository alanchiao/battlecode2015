package team158.units;

import team158.buildings.Headquarters;
import team158.com.Broadcast;
import team158.units.com.Navigation;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends Unit {

	public Drone(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		// Determine if opponent is using tanks/launchers and assess threat
		if (prevHealth - rc.getHealth() >= 20) {
			int threat = rc.readBroadcast(Broadcast.enemyThreatCh);
			rc.broadcast(Broadcast.enemyThreatCh, threat + 1);
		}

		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, rc.getTeam().opponent());
		rc.setIndicatorString(0, Integer.toString(enemiesAttackable.length));
		
		
		// run away from tanks / missiles / and launchers
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
		for (RobotInfo enemy: enemies) {
			if (enemy.type == RobotType.LAUNCHER || enemy.type == RobotType.TANK || enemy.type == RobotType.MISSILE) {
				this.navigation.moveToDestination(this.ownHQ, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
				return;
			}	
		}
		
		if (rc.isWeaponReady()) {
			if (enemiesAttackable.length > 0) {
				rc.attackLocation(selectTarget(enemiesAttackable));
			}
        }
		
		// Move
		if (rc.isCoreReady()) {
			//default target location
			int approachStrategy;
			MapLocation target;
			if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				if (rc.getSupplyLevel() == 0) {
					approachStrategy = 0;
					target = ownHQ;
				}
				if (groupTracker.groupID == Broadcast.droneGroupAttackCh) {
					target = enemyHQ;
					approachStrategy = 1;
				}
				else { // groupTracker.groupID == Broadcast.droneGroupDefenseCh
					target = selectDefensiveTarget();
					approachStrategy = 1;
				}
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_FULL_ATTACK) {
				target = this.ownHQ;
				approachStrategy = 1;
			} else {
				target = this.enemyHQ;
				approachStrategy = 2;
			}
			
			rc.setIndicatorString(2, target.toString());
			moveToLocationWithMicro(target, approachStrategy);
		}
	}
	
	protected MapLocation selectDefensiveTarget() {
		try {
//			boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
			boolean enemyNearTower = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
			boolean enemyNearHQ = rc.readBroadcast(Broadcast.enemyNearHQCh) == 1;
//			if (towerAttacked) {
//				return Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
//			}
			if (enemyNearHQ) {
				return Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
			}
			else if (enemyNearTower) {
				return Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);
			}
			else {
				return Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
			}
		}
		catch (GameActionException e) {
			return null;
		}
	}
//	protected void droneRushMicro(MapLocation target) {
//		try {
//			RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.DRONE.sensorRadiusSquared, rc.getTeam().opponent());
//			MapLocation myLocation = rc.getLocation();
//			if (enemies.length == 0) {
//				rc.move(myLocation.directionTo(target));
//			}
//			else if (enemies.length == 1) {
//				RobotInfo enemy = enemies[0];
//				enemy.ID 
//				if (enemy.weaponDelay > 2) {
//					moveToLocationWithMicro(enemy.location, 1);
//				}
//				else {
//					moveToLocationWithMicro(enemy.location, 0);
//				}
//			}
//			else {
//				for (RobotInfo r : enemies) {
//					rc.move();
//				}
//			}
//		}
//		catch (Exception e) {
//			return;
//		}
//	}
}
