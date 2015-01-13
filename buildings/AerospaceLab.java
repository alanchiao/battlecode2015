package team158.buildings;

import team158.utils.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class AerospaceLab extends Building {

	public AerospaceLab(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
//		
//		if (rc.readBroadcast(Broadcast.launcherRallyXCh) == 0) {
//			MapLocation rally = myLocation;
//			// Move 5 squares away
//			int rallyDistance = (int)hqDistance / 4;
//			//RobotType.LAUNCHER.
//			for (int i = 0; i < rallyDistance; i++) {
//				rally = rally.add(DirectionHelper.directions[dirint]);
//			}
//
//			int xLoc = rc.readBroadcast(Broadcast.launcherTargetLocationXCh);
//			int yLoc = rc.readBroadcast(Broadcast.launcherTargetLocationYCh);
//			rally = new MapLocation(xLoc,yLoc);
//			rc.broadcast(Broadcast.launcherRallyXCh, xLoc);
//			rc.broadcast(Broadcast.launcherRallyYCh, yLoc);
//			rc.setIndicatorString(0,String.valueOf(rally));
//		}
		if (rc.isCoreReady() && rc.getTeamOre() >= 400) {
			int offsetIndex = 0;
			while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.LAUNCHER)) {
				offsetIndex++;
			}
			if (offsetIndex < 8) {
				rc.spawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.LAUNCHER);
			}
		}
	}

}
