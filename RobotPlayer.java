package battlecode2015;

import battlecode.common.*;
import battlecode2015.buildings.Barracks;
import battlecode2015.buildings.Headquarters;
import battlecode2015.buildings.Tower;
import battlecode2015.units.Beaver;
import battlecode2015.units.Soldier;

public class RobotPlayer {
	static RobotController rc;
	
	public static void run(RobotController r) {
		rc = r;
		if (rc.getType() == RobotType.HQ) {
			Headquarters.setRC(rc);
		}
        if (rc.getType() == RobotType.TOWER) {
           Tower.setRC(rc);
        }
		
        if (rc.getType() == RobotType.SOLDIER) {
            Soldier.setRC(rc);
        }
		
		if (rc.getType() == RobotType.BEAVER) {
			Beaver.setRC(rc);
		}

        if (rc.getType() == RobotType.BARRACKS) {
			Barracks.setRC(rc);
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
				Headquarters.move();
			}
			
            if (rc.getType() == RobotType.TOWER) {
               Tower.move();
            }
			
            if (rc.getType() == RobotType.SOLDIER) {
                Soldier.move();
            }
			
			if (rc.getType() == RobotType.BEAVER) {
				Beaver.move();
			}

            if (rc.getType() == RobotType.BARRACKS) {
				Barracks.move();
			}
			
			rc.yield();
		}
	}
}