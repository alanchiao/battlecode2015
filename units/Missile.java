package team158.units;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import team158.Robot;
import team158.utils.DirectionHelper;

public class Missile extends Robot {

	public int targetID;
	public int timeUntilDeath;
	
	public Missile (RobotController newRC) {
		rc = newRC;
		targetID = 0;
		timeUntilDeath = 5;
		
		RobotInfo enemies[] = rc.senseNearbyRobots(24, rc.getTeam().opponent());
		for (RobotInfo enemy: enemies) {
			int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemy.location);
			if (distanceToEnemy >= 9) {
				targetID = enemy.ID;
				break;
			}
		}
	}

	@Override
	public void move() {
		try {
			timeUntilDeath --;
			if (rc.isCoreReady()) {
				RobotInfo enemies[] = rc.senseNearbyRobots(2, rc.getTeam().opponent());
				RobotInfo allies[] = rc.senseNearbyRobots(2, rc.getTeam());
				if (enemies.length > 0 && allies.length == 0) {
					rc.explode();
					return;
				} 
				if (timeUntilDeath == 1) {
					rc.disintegrate();
				}
				enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
				if (enemies.length > 0) {
					Direction moveDirection = rc.getLocation().directionTo(enemies[0].location);
					if (rc.canMove(moveDirection)) {
						rc.move(moveDirection);
						return;
					}
					moveDirection = DirectionHelper.directions[(DirectionHelper.directionToInt(moveDirection) + 9) % 8];
					if (rc.canMove(moveDirection)) {
						rc.move(moveDirection);
						return;
					}
					moveDirection = DirectionHelper.directions[(DirectionHelper.directionToInt(moveDirection) + 6) % 8];
					if (rc.canMove(moveDirection)) {
						rc.move(moveDirection);
						return;
					}
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
