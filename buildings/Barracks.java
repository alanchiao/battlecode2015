package battlecode2015.buildings;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import battlecode2015.Robot;
import battlecode2015.utils.DirectionHelper;

public class Barracks extends Building {
	protected void actions() throws GameActionException {
		int fate = rand.nextInt(10000);
		
        // get information broadcasted by the HQ
		int numBeavers = rc.readBroadcast(0);
		int numSoldiers = rc.readBroadcast(1);
		int numBashers = rc.readBroadcast(2);
		
		if (rc.isCoreReady() && rc.getTeamOre() >= 60 && fate < Math.pow(1.2,15-numSoldiers-numBashers+numBeavers)*10000) {
			int offsetIndex = 0;
			int[] offsets = {0,1,-1,2,-2,3,-3,4};
			int dirint = rand.nextInt(8);
			while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.SOLDIER)) {
				offsetIndex++;
			}
			if (offsetIndex < 8) {
				rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.SOLDIER);
			}
		}
	}
}
