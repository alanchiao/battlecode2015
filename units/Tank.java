package team158.units;

import team158.com.Broadcast;
import battlecode.common.*;

public class Tank extends Unit {
	
	public Tank(RobotController newRC) {
		super(newRC);
	}
	
	@Override
	protected void actions() throws GameActionException {

		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared, rc.getTeam().opponent());
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		if (rc.isCoreReady()) {
			MapLocation target = null;
			rc.setIndicatorString(0, String.valueOf(groupTracker.groupID));
			if (groupTracker.isGrouped()) {
				if (groupTracker.groupID == Broadcast.tankGroupDefenseCh) {
					defensiveMove();
				}
				else if (groupTracker.groupID == Broadcast.tankGroupAttackCh) {
					attackMove();
				}
				//boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
//				if (hasHQCommand) {
//					target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
//					approachStrategy = 0;
//				}
			}
			else {
				target = groupTracker.getRallyPoint();
				boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
				boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
				if (towerAttacked) {
					target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
				}
				else if (enemyNear) {
					target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
				}	
				rc.setIndicatorString(2, "[ " + target.x + ", " + target.y + " ]");
				moveToLocationWithMicro(target, true);
			}
		}	
	}
}