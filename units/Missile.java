package team158.units;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import team158.Robot;
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
				RobotInfo alliesInAttack[] = rc.senseNearbyRobots(2, rc.getTeam());
				RobotInfo enemiesInAttack[] = rc.senseNearbyRobots(2, rc.getTeam().opponent());
				
				if (alliesInAttack.length == 0 && enemiesInAttack.length > 0) {
					rc.explode();
					return;
				} 
				if (timeUntilDeath == 1) {
					rc.disintegrate();
				}
			   RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
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
