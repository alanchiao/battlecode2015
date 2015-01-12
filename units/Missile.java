package team158.units;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import team158.Robot;

public class Missile extends Robot {

	public int targetID;
	
	public Missile (RobotController newRC) {
		rc = newRC;
		targetID = 0;
		
		RobotInfo enemies[] = rc.senseNearbyRobots(25, rc.getTeam().opponent());
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
			RobotInfo enemies[] = rc.senseNearbyRobots(25, rc.getTeam().opponent());
			for (RobotInfo enemy: enemies) {
				if (enemy.ID == targetID) {
					Direction dir = rc.getLocation().directionTo(enemy.location);
					int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemy.location);
					if (distanceToEnemy <= 2) {
						rc.explode();
					}
					else if(rc.canMove(dir)) {
						rc.move(dir);
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
