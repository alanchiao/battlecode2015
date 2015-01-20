package team158.com;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Broadcast {
	
	public static final int numBeaversCh = 0;
	public static final int numSoldiersCh = 1;
	public static final int numDronesCh = 2;
	public static final int numMinersCh = 3;
	public static final int numTanksCh = 4;
	public static final int numLaunchersCh = 5;

	public static final int numBuildingsCh = 100;
	public static final int numBarracksCh = 101;
	public static final int numMinerFactoriesCh = 102;
	public static final int numSupplyDepotsCh = 103;
	public static final int numTankFactoriesCh = 104;
	
	public static final int buildBarracksCh = 201;
	public static final int buildMinerFactoriesCh = 202;
	public static final int buildSupplyCh = 203;
	public static final int buildHelipadsCh = 204;
	public static final int buildTankFactoriesCh = 205;
	public static final int buildAerospaceLabsCh = 206;
	
	public static final int minerOreX1000Ch = 301;
	public static final int minersProducedCh = 302;
	
	// target commanded by unit spawning buildings
	public static final int tankRallyLocationChs = 400; // 400 - 401
	public static final int soldierRallyLocationChs = 402; // 402 - 403
	public static final int droneRallyLocationChs = 404; // 404 - 405
	public static final int launcherRallyLocationChs = 408; // 408- 409
	public static final int launcherAttackLocationChs = 410;
	// target commanded by the HQ
	public static final int enemyTowerTargetLocationChs = 412; // 410 - 411
	public static final int enemyNearHQLocationChs = 414;
	// target commanded by the Towers
	public static final int attackedTowerLocationChs = 416;
	public static final int enemyNearTowerLocationChs = 418;
	
	public static final int groupingSoldiersCh = 501;
	public static final int groupingTanksCh = 502;
	public static final int groupingDronesCh = 503;
	public static final int groupingLaunchersCh = 504;
	
	// Channels for signaling
	public static final int enemyRushCh = 600;
	public static final int enemyThreatCh = 601;
	public static final int scoutEnemyHQCh = 603;
	public static final int stopDroneProductionCh = 605;
	public static final int buildBuildingsCloseCh = 606;
	public static final int yieldToLaunchers = 607;
	public static final int towerAttacked = 608;
	public static final int enemyTowerFound = 609;
	public static final int enemyNearTower = 610;
	public static final int enemyNearHQCh = 611;
	
	// Channels 700-799 are used for grouping
	public static final int soldierGroupAttackCh = 700;
	public static final int soldierGroupDefenseCh = 701;
	public static final int tankGroupAttackCh = 702;
	public static final int tankGroupDefenseCh = 703;
	public static final int droneGroupAttackCh = 704;
	public static final int droneGroupDefenseCh = 705;
	public static final int launcherGroupAttackCh = 707;
	public static final int launcherGroupDefenseCh = 708;
	
	// Channels 800-812
	public static final int idealMiningLocation = 800;
	public static final int idealMiningOreAverage = 802;
	
	
	// Channels 8000 -  65121 on for internal map representation (remember 9999 is used somewhere)
	public static final int internalMapChs = 8000;
	public static final int internalMapHQCh = 36880;
	
	
	// check if channel has not been initiated yet
	public static boolean isNotInitiated(RobotController rc, int channelStart) throws GameActionException {
		return rc.readBroadcast(channelStart) == 0;
	}
	
	// read two contiguous channels for location information, x and y coordinates
	public static MapLocation readLocation(RobotController rc, int channelStart) throws GameActionException {
		return new MapLocation(rc.readBroadcast(channelStart), rc.readBroadcast(channelStart + 1));
	}
	
	// write to two contiguous channels for a location
	public static void broadcastLocation (RobotController rc, int channelStart, MapLocation location) throws GameActionException {
		rc.broadcast(channelStart, location.x);
		rc.broadcast(channelStart + 1, location.y);
	}
	
	// read from channel to see if this particular unit has a unique command (own ID matches channel information)
	public static boolean hasSoloCommand(RobotController rc, int channel) throws GameActionException{
		return rc.readBroadcast(channel) == rc.getID();
	}
	
	

}
