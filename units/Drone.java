package team158.units;

import team158.buildings.Headquarters;
import team158.utils.Broadcast;
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
		if (prevHealth - rc.getHealth() >= 20 && prevHealth - rc.getHealth() != 24) {
			int threat = rc.readBroadcast(Broadcast.enemyThreatCh);
			rc.broadcast(Broadcast.enemyThreatCh, threat + 1);
		}

		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, rc.getTeam().opponent());

		if (rc.isWeaponReady()) {
			if (enemiesAttackable.length > 0) {
				rc.attackLocation(selectTarget(enemiesAttackable));
			}
        }
		
		// Move
		if (rc.isCoreReady()) {
			//default target location
			MapLocation target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
			int approachStrategy = 1;
			if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				if (groupTracker.isGrouped()) {
					if (groupTracker.groupID == Broadcast.droneGroupAttackCh) {
						boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
						if (hasHQCommand) {
							target = enemyHQ;
						} 
					}
					else {	
						boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
						if (hasHQCommand) {								
							approachStrategy = 2;
							//enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
							target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
							rc.setIndicatorString(1, String.valueOf(rc.readBroadcast(Broadcast.towerAttacked)));
							boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
							boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
							if (towerAttacked) {
								target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
							}
							else if (enemyNear) {
								target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
							}
							
						} 
					}
				} 
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_FULL_ATTACK) {
				target = this.ownHQ;
			} else {
				target = this.enemyHQ;
				approachStrategy = 2;
			}
			rc.setIndicatorString(2, target.toString());
			moveToLocationWithMicro(target, approachStrategy);
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
