package team158.com;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Broadcast {
	
	public static final int EARLY_GAME = -10;
	public static final int MID_GAME = -9;
	public static final int LATE_GAME = -8;

	// Channels for number of robots
	public static final int numBeaversCh = 0;
	public static final int numSoldiersCh = 1;
	public static final int numDronesCh = 2;
	public static final int numMinersCh = 3;
	public static final int numLaunchersCh = 4;
	public static final int numAerospaceLabsCh = 5;
	public static final int numBarracksCh = 6;
	public static final int numMinerFactoriesCh = 7;
	public static final int numSupplyDepotsCh = 8;
	public static final int numHelipadsCh = 9;
	
	// Channels for signaling
	public static final int requestSupplyFromHQCh = 10;
	public static final int scoutEnemyHQCh = 11;
	public static final int requestSupplyDroneCh = 12;
	public static final int towerAttacked = 13;
	public static final int enemyNearTower = 14;
	public static final int gameStageCh = 15; // EARLY_GAME, EARLY_MID_GAME, or MID_GAME.
	public static final int soldierTowerTarget1ExistsCh = 16;
	public static final int soldierTowerTarget2ExistsCh = 17;

	// Misc
	public static final int minerOreX1000Ch = 18;
	public static final int minersProducedCh = 19;
	
	// Channels for grouping
	public static final int groupingSoldiersCh = 20;
	public static final int groupingLaunchersCh = 22;
	public static final int soldierGroup1Ch = 23;
	public static final int soldierGroup2Ch = 24;
	public static final int launcherGroupAttackCh = 27;
	public static final int launcherGroupDefenseCh = 28;

	// Location channels - each requires two consecutive channels
	public static final int soldierRallyLocationChs = 30;
	public static final int droneRallyLocationChs = 32;
	public static final int launcherRallyLocationChs = 34;
	public static final int launcherAttackLocationChs = 36;
	public static final int soldierHarassLocationChs = 38;
	// target commanded by the HQ
	public static final int soldierTowerTargetLocation1Chs = 40;
	public static final int soldierTowerTargetLocation2Chs = 42;
	public static final int enemyTowerTargetLocationChs = 44;
	public static final int enemyNearHQLocationChs = 46;
	// target commanded by the towers
	public static final int attackedTowerLocationChs = 48;
	public static final int enemyNearTowerLocationChs = 50;

	public static final int enemyHQLocation = 52;
	
	// Channels 8000 -  65121 on for internal map representation (remember 9999 is used somewhere)
	public static final int internalMapHQCh = 36880;

	// check if channel has not been initialized yet
	public static boolean isNotInitialized(RobotController rc, int channelStart) throws GameActionException {
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
}
