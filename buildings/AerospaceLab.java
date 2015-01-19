package team158.buildings;

import team158.utils.Broadcast;
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
		MapLocation[] myTowers = rc.senseTowerLocations();
		int numTowersRemaining = myTowers.length;
		int[] distances = new int[numTowersRemaining];
		for (int i = 0; i < numTowersRemaining; i++) {
			distances[i] = myLocation.distanceSquaredTo(myTowers[i]);
		}
		
		int maxDistance = 0;
		MapLocation targetTower = null;
		
		for (int i = 0; i < numTowersRemaining; i++) {		
			if (myLocation.directionTo(myTowers[i]).equals(myLocation.directionTo(enemyHQ))) {
				if (distances[i] > maxDistance) {
					maxDistance = distances[i];
					targetTower = myTowers[i];
				}
			}
		}
		
		if (targetTower != null) {
			Broadcast.broadcastLocation(rc, Broadcast.launcherAttackLocationChs, targetTower);
		}
		rc.setIndicatorString(0, String.valueOf(targetTower));

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
			this.greedySpawn(RobotType.LAUNCHER);
		}
	}

}
