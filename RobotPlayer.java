package team158;

import team158.buildings.AerospaceLab;
import team158.buildings.Barracks;
import team158.buildings.Headquarters;
import team158.buildings.Helipad;
import team158.buildings.MinerFactory;
import team158.buildings.SupplyDepot;
import team158.buildings.Tower;
import team158.units.Drone;
import team158.units.Launcher;
import team158.units.Miner;
import team158.units.Missile;
import team158.units.beaver.Beaver;
import team158.units.soldier.Soldier;
import battlecode.common.*;

public class RobotPlayer {
	private static RobotController rc;
	private static Robot robot;
	
	public static void run(RobotController r) {
		rc = r;
		if (rc.getType() == RobotType.MISSILE) {
			robot = new Missile(rc);
		}
		else if (rc.getType() == RobotType.HQ) {
			robot = new Headquarters(rc);
		}
		else if (rc.getType() == RobotType.TOWER) {
        	robot = new Tower(rc);
        }
		else if (rc.getType() == RobotType.SOLDIER) {
        	robot = new Soldier(rc);
        }
		else if (rc.getType() == RobotType.BEAVER) {
			robot = new Beaver(rc);
		}
		else if (rc.getType() == RobotType.DRONE) {
			robot = new Drone(rc);
		}
		else if (rc.getType() == RobotType.BARRACKS) {
        	robot = new Barracks(rc);
		}
		else if (rc.getType() == RobotType.MINER) {
        	robot = new Miner(rc);
		}
		else if (rc.getType() == RobotType.MINERFACTORY) {
        	robot = new MinerFactory(rc);
		}
		else if (rc.getType() == RobotType.HELIPAD) {
        	robot = new Helipad(rc);
        }
		else if (rc.getType() == RobotType.SUPPLYDEPOT) {
			robot = new SupplyDepot(rc);
		}
		else if (rc.getType() == RobotType.AEROSPACELAB) {
			robot = new AerospaceLab(rc);
		}
		else if (rc.getType() == RobotType.LAUNCHER) {
			robot = new Launcher(rc);
		}
		while (true) {
			robot.move();
			rc.yield();
		}
	}
}