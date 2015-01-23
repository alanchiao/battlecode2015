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

	int followingID;
	boolean gotSupply;
	MapLocation followingLocation;

	public Drone(RobotController newRC) {
		super(newRC);
		try {
			followingID = rc.readBroadcast(Broadcast.requestSupplyDroneCh);
			if (followingID != 0) {
				rc.setIndicatorString(1, "Following" + Integer.toString(followingID));
				rc.broadcast(Broadcast.requestSupplyDroneCh, 0);
				try {
					followingLocation = rc.senseRobot(followingID).location;
					autoSupplyTransfer = false;
					gotSupply = false;
				}
				catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void actions() throws GameActionException {
		if (!autoSupplyTransfer) { // Must transfer supply to a launcher
			MapLocation myLocation = rc.getLocation();
			if (gotSupply) {
				if (myLocation.distanceSquaredTo(followingLocation) <= 15) {
					RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
					if (friendlyRobots.length > 0) {
						for (RobotInfo r : friendlyRobots) {
							if (r.type == RobotType.LAUNCHER) {
								rc.transferSupplies((int) (rc.getSupplyLevel()), r.location);
								autoSupplyTransfer = true;
								return;
							}
						}
					}
					friendlyRobots = rc.senseNearbyRobots(99, rc.getTeam());
					int minDistance = 999999;
					for (RobotInfo r : friendlyRobots) {
						int distance = myLocation.distanceSquaredTo(r.location);
						if (r.type == RobotType.LAUNCHER && distance < minDistance) {
							followingLocation = r.location;
							minDistance = distance;
						}
					}
					if (minDistance == 999999) {
						autoSupplyTransfer = true;
						return;
					}
				}
				if (rc.isCoreReady()) {
					navigation.moveToDestination(followingLocation, Navigation.AVOID_ALL);
				}
			}
			else {
				if (rc.getSupplyLevel() > 6000) {
					gotSupply = true;
				}
				else if (myLocation.distanceSquaredTo(ownHQ) <= 15) {
					rc.broadcast(Broadcast.requestSupplyFromHQCh, rc.getID());
				}
				else if (rc.isCoreReady()) {
					navigation.moveToDestination(ownHQ, Navigation.AVOID_ALL);
				}
			}
			return;
		}

		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(RobotType.DRONE.attackRadiusSquared, rc.getTeam().opponent());
		
		if (rc.isWeaponReady()) {
			if (enemiesAttackable.length > 0) {
				rc.attackLocation(selectTarget(enemiesAttackable));
			}
        }
		
		// Move
		rc.setIndicatorString(1, "can't move");
		if (rc.isCoreReady()) {
			// default target location
			if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_COLLECT_SUPPLY) {
				if (rc.getSupplyLevel() == 0) {
					moveToLocationWithMicro(ownHQ, true);
				}
				else if (groupTracker.groupID == Broadcast.droneGroupAttackCh) {
					moveToLocationWithMicro(enemyHQ, false);
				}
				else { // groupTracker.groupID == Broadcast.droneGroupDefenseCh
					moveToLocationWithMicro(selectDefensiveTarget(), false);
				}
			} else if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_FULL_ATTACK) {
				moveToLocationWithMicro(ownHQ, false);
			} else {
				chargeToLocation(enemyHQ);
			}
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
