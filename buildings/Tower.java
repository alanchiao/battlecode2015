package team158.buildings;

import team158.com.Broadcast;
import team158.units.Unit;
import battlecode.common.*;

public class Tower extends Building {

	public Tower(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {

		if (rc.isWeaponReady()) {
			RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);
			if (enemies.length > 0) {
				rc.attackLocation(Unit.selectTarget(enemies));
			}
		}

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
		rc.setIndicatorString(1, String.valueOf(enemyNear));
		rc.broadcast(Broadcast.enemyNearTower, enemyNear);
		Broadcast.broadcastLocation(rc, Broadcast.enemyNearTowerLocationChs, closestEnemyLocation);
	}
}