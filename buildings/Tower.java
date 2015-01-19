package team158.buildings;

import team158.units.Unit;
import team158.utils.Broadcast;
import battlecode.common.*;

public class Tower extends Building {

	public Tower(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		//if  tower took damage, broadcast tower location
		rc.setIndicatorString(0, String.valueOf(prevHealth));
		if (prevHealth > rc.getHealth()) {		
			rc.broadcast(Broadcast.towerAttacked, 1);
			rc.setIndicatorString(1, String.valueOf(myLocation));
			Broadcast.broadcastLocation(rc, Broadcast.attackedTowerLocationChs, myLocation);
		}

		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);
			if (enemies.length > 0) {
				rc.attackLocation(Unit.selectTarget(enemies));
			}
		}
		if (rc.isCoreReady()) {
			RobotInfo closestEnemy = findClosestEnemy(100);
			MapLocation closestEnemyLocation;
			int enemyNear = 0;
			if (closestEnemy == null) {
				closestEnemyLocation = myLocation;
			} else {
				enemyNear = 1;
				closestEnemyLocation = closestEnemy.location;
			}
			rc.setIndicatorString(0, String.valueOf(closestEnemyLocation));
			rc.broadcast(Broadcast.enemyNearTower, enemyNear);
			Broadcast.broadcastLocation(rc, Broadcast.enemyNearTowerLocationChs, closestEnemyLocation);
		}
	}
}