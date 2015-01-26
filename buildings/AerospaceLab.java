package team158.buildings;

import team158.com.Broadcast;
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
//
//		MapLocation[] myTowers = rc.senseTowerLocations();
//		int numTowersRemaining = myTowers.length;
//		int[] distances = new int[numTowersRemaining];
//		for (int i = 0; i < numTowersRemaining; i++) {
//			distances[i] = myLocation.distanceSquaredTo(myTowers[i]);
//		}
//		
//		int maxDistance = 0;
//		MapLocation targetTower = null;
//		
//		for (int i = 0; i < numTowersRemaining; i++) {		
//			if (myLocation.directionTo(myTowers[i]).equals(myLocation.directionTo(enemyHQ))) {
//				if (distances[i] > maxDistance) {
//					maxDistance = distances[i];
//					targetTower = myTowers[i];
//				}
//			}
//		}
//		
//		if (targetTower != null) {
//			Broadcast.broadcastLocation(rc, Broadcast.launcherAttackLocationChs, targetTower);
//		}
//		rc.setIndicatorString(0, String.valueOf(targetTower));
		
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = DirectionHelper.directionToInt(myLocation.directionTo(enemyHQ));
		if (Broadcast.isNotInitialized(rc, Broadcast.launcherRallyLocationChs)) {
			MapLocation rally = myLocation;
			// Move 5 squares away
			int rallyDistance = 5;
			for (int i = 0; i < rallyDistance; i++) {
				int offsetIndex = 0;
				while (offsetIndex < 8) {
					MapLocation candidate = rally.add(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
					if (rc.senseTerrainTile(candidate).isTraversable()) {
						rally = candidate;
						break;
					}
					offsetIndex++;
				}
			}
			Broadcast.broadcastLocation(rc, Broadcast.launcherRallyLocationChs, rally);
		}
		
		if (rc.isCoreReady() && rc.getTeamOre() >= 400) {
			this.greedySpawn(RobotType.LAUNCHER);
		}
	}

}
