package team158.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team158.utils.DirectionHelper;

public class TankFactory extends Building {
	
	public TankFactory(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		if (rc.isCoreReady() && rc.getTeamOre() >= RobotType.TANK.oreCost) {
			this.greedySpawn(RobotType.TANK);
		}
	}
}
