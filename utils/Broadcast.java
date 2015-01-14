package team158.utils;

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
	
	public static final int minerOreX100Ch = 301;
	public static final int minersProducedCh = 302;
	
	// commanded by unit spawning buildings
	public static final int tankRallyLocationChs = 400; // 400 - 401
	public static final int soldierRallyLocationChs = 402; // 402 - 403
	public static final int droneRallyLocationChs = 404; // 404 - 405
	public static final int launcherRallyLocationChs = 408; // 408- 409
	// commanded by the HQ
	public static final int groupTargetLocationChs = 410; // 410 - 411
	public static final int launcherAttackLocationChs = 412;
	
	public static final int groupingSoldiersCh = 501;
	public static final int groupingTanksCh = 502;
	public static final int groupingDronesCh = 503;
	public static final int groupingLaunchersCh = 504;
	
	// Channels for signaling
	public static final int enemyRushCh = 600;
	public static final int enemyThreatCh = 601;
	public static final int scoutEnemyHQCh = 603;
	public static final int slowMinerProductionCh = 604;
	public static final int stopDroneProductionCh = 605;
	public static final int buildBuildingsCloseCh = 606;
	public static final int yieldToLaunchers = 607;
	
	// Channels 700-799 are used for grouping
	public static final int soldierGroup1Ch = 700;
	public static final int soldierGroup2Ch = 701;
	public static final int tankGroup1Ch = 702;
	public static final int tankGroup2Ch = 703;
	public static final int droneGroup1Ch = 704;
	public static final int droneGroup2Ch = 705;
	public static final int launcherGroupCh = 706;
	
	// read two contiguous channels for location information, x and y coordinates
	public static MapLocation readLocation(RobotController rc, int channelStart) throws GameActionException {
		return new MapLocation(rc.readBroadcast(channelStart), rc.readBroadcast(channelStart + 1));
	}
	
	// write to two contiguous channels for a location
	public static void broadcastLocation (RobotController rc, MapLocation location, int channelStart) throws GameActionException {
		rc.broadcast(channelStart, location.x);
		rc.broadcast(channelStart + 1, location.y);
	}
	
	// check if channel has not been initiated yet
	public static boolean isNotInitiated(RobotController rc, int channelStart) throws GameActionException {
		return rc.readBroadcast(channelStart) == 0;
	}

}
