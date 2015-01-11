package team158;

import team158.buildings.Barracks;
import team158.buildings.Headquarters;
import team158.buildings.Helipad;
import team158.buildings.MinerFactory;
import team158.buildings.Tower;
import team158.units.Beaver;
import team158.units.Drone;
import team158.units.Miner;
import team158.units.Soldier;
import battlecode.common.*;

public class RobotPlayer {
	private static RobotController rc;
	private static Headquarters headquarters;
	private static Tower tower;
	private static Soldier soldier;
	private static Beaver beaver;
	private static Drone drone;
	private static Barracks barracks;
	private static Miner miner;
	private static MinerFactory minerfactory;
	private static Helipad helipad;
	
	public static void run(RobotController r) {
		rc = r;
		if (rc.getType() == RobotType.HQ) {
			headquarters = new Headquarters();
			headquarters.setRC(rc);
		}
        if (rc.getType() == RobotType.TOWER) {
        	tower = new Tower();
        	tower.setRC(rc);
        }
        if (rc.getType() == RobotType.SOLDIER) {
        	soldier = new Soldier();
            soldier.setRC(rc);
        }
		if (rc.getType() == RobotType.BEAVER) {
			beaver = new Beaver();
			beaver.setRC(rc);
		}
		if (rc.getType() == RobotType.DRONE) {
			drone = new Drone();
			drone.setRC(rc);
		}
        if (rc.getType() == RobotType.BARRACKS) {
        	barracks = new Barracks();
			barracks.setRC(rc);
		}
        if (rc.getType() == RobotType.MINER) {
        	miner = new Miner();
			miner.setRC(rc);
		}
        if (rc.getType() == RobotType.MINERFACTORY) {
        	minerfactory = new MinerFactory();
			minerfactory.setRC(rc);
		}
        if (rc.getType() == RobotType.HELIPAD) {
        	helipad = new Helipad();
        	helipad.setRC(rc);
        }

		while(true) {
            try {
                rc.setIndicatorString(0, "I am a " + rc.getType());
            } catch (Exception e) {
                System.out.println("Unexpected exception");
                e.printStackTrace();
            }

			if (rc.getType() == RobotType.HQ) {
				headquarters.move();
			}
            if (rc.getType() == RobotType.TOWER) {
            	tower.move();
            }
            if (rc.getType() == RobotType.SOLDIER) {
                soldier.move();
            }
			if (rc.getType() == RobotType.BEAVER) {
				beaver.move();
			}
			if (rc.getType() == RobotType.DRONE) {
				drone.move();
			}
            if (rc.getType() == RobotType.BARRACKS) {
				barracks.move();
			}
            if (rc.getType() == RobotType.MINER) {
				miner.move();
			}
            if (rc.getType() == RobotType.MINERFACTORY) {
				minerfactory.move();
			}
            if (rc.getType() == RobotType.HELIPAD) {
				helipad.move();
			}
			
			rc.yield();
		}
	}
}