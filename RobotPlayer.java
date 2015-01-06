package battlecode2015;

import battlecode.common.*;
import battlecode2015.buildings.Barracks;
import battlecode2015.buildings.Headquarters;
import battlecode2015.buildings.Tower;
import battlecode2015.units.Beaver;
import battlecode2015.units.Soldier;

public class RobotPlayer {
	private static RobotController rc;
	private static Headquarters headquarters;
	private static Tower tower;
	private static Soldier soldier;
	private static Beaver beaver;
	private static Barracks barracks;
	
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

        if (rc.getType() == RobotType.BARRACKS) {
        	barracks = new Barracks();
			barracks.setRC(rc);
		}

		while(true) {
            try {
                rc.setIndicatorString(0, "This is an indicator string.");
                rc.setIndicatorString(1, "I am a " + rc.getType());
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

            if (rc.getType() == RobotType.BARRACKS) {
				barracks.move();
			}
			
			rc.yield();
		}
	}
}