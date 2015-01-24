package team158.units;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import team158.Robot;
import team158.com.Broadcast;
import team158.utils.DirectionHelper;

public class Missile extends Robot {
		
	public int timeUntilDeath;
	
	public Missile (RobotController newRC) {
		rc = newRC;
		timeUntilDeath = 5;
	}

	@Override
	public void move() {
		try {
			timeUntilDeath --;
			if (rc.isCoreReady()) {
				RobotInfo enemiesInAttack[] = rc.senseNearbyRobots(2, rc.getTeam().opponent());
				boolean isNonMissileEnemyNear = false;
				for (RobotInfo enemy: enemiesInAttack) {
					if (enemy.type != RobotType.MISSILE) {
						isNonMissileEnemyNear = true;
						rc.explode();
						return;
					} 
				}
				
				RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
				RobotInfo target = null;
				for (RobotInfo enemy: enemiesInAttack) {
					if (enemy.type != RobotType.MISSILE) {
						target = enemy;
						break;
					} 
				}
				if (target != null) {
					Direction moveDirection = rc.getLocation().directionTo(target.location);
					Direction moveDirection2 = DirectionHelper.directions[(DirectionHelper.directionToInt(moveDirection) + 9) % 8];
					Direction moveDirection3 = DirectionHelper.directions[(DirectionHelper.directionToInt(moveDirection) + 6) % 8];
					if (rc.canMove(moveDirection)) {
						rc.move(moveDirection);
					} else if (rc.canMove(moveDirection2)) {
						rc.move(moveDirection2);
					} else  if (rc.canMove(moveDirection3)) {
						rc.move(moveDirection3);
					}
					if (enemiesInAttack.length >= 3) {
						rc.explode();
					}
				} else {
					Direction moveDirection = rc.getLocation().directionTo(Broadcast.readLocation(rc, Broadcast.enemyHQLocation));
					Direction moveDirection2 = DirectionHelper.directions[(DirectionHelper.directionToInt(moveDirection) + 9) % 8];
					Direction moveDirection3 = DirectionHelper.directions[(DirectionHelper.directionToInt(moveDirection) + 6) % 8];
					if (rc.canMove(moveDirection)) {
						rc.move(moveDirection);
					} else if (rc.canMove(moveDirection2)) {
						rc.move(moveDirection2);
					} else  if (rc.canMove(moveDirection3)) {
						rc.move(moveDirection3);
					}
					if (enemiesInAttack.length >= 3) {
						rc.explode();
					}
				}
			   
				if (timeUntilDeath == 1) {
					rc.disintegrate();
				}
			}
		} catch (GameActionException e) {
			System.out.println(rc.getType());
			e.printStackTrace();
		}
	}

	@Override
	protected void actions() throws GameActionException {
	}

}
