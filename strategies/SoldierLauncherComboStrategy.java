package team158.strategies;

import team158.buildings.Headquarters;
import team158.com.Broadcast;
import team158.utils.DirectionHelper;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.MapLocation;

public class SoldierLauncherComboStrategy extends GameStrategy {

	private int pathDifficulty;

	public final static int ATTACK_GROUP = 0;
	public final static int DEFENSE_GROUP = 1;
	MapLocation enemyHQ;
	
	private int scoutMiner;
	
	public SoldierLauncherComboStrategy(RobotController rc, Headquarters hq) {
		super(rc, hq);

		this.pathDifficulty = 0;
		this.scoutMiner = 0;
		this.enemyHQ = rc.senseEnemyHQLocation();
	}
	
	public void executeStrategy() throws GameActionException {
		RobotInfo[] myRobots = rc.senseNearbyRobots(999999, rc.getTeam());
		int numBeavers = 0;
		int numMiners = 0;
		int numMinerFactories = 0;
		int numSupplyDepots = 0;
		int numDrones = 0;
		int numLaunchers = 0;
		int numSoldiers = 0;
		int numHelipads = 0;
		int numAerospaceLabs = 0;
		int numBarracks = 0;
		for (RobotInfo r : myRobots) {
			RobotType type = r.type;
			if (type == RobotType.MINER) {
				numMiners++;
				// one time broadcast for scouting
				if (scoutMiner == 0) {
					scoutMiner = r.ID;
					rc.broadcast(Broadcast.scoutEnemyHQCh, scoutMiner);
				}
			} else if (type == RobotType.DRONE) {
				numDrones++;
			} else if (type == RobotType.BEAVER) {
				numBeavers++;
			} else if (type == RobotType.SOLDIER) {
				numSoldiers++;
			} else if (type == RobotType.MINERFACTORY) {
				numMinerFactories++;
			} else if (type == RobotType.SUPPLYDEPOT) {
				numSupplyDepots++;
			} else if (type == RobotType.HELIPAD) {
				numHelipads++;
			} else if (type == RobotType.AEROSPACELAB) {
				numAerospaceLabs++;
			} else if (type == RobotType.LAUNCHER) {
				numLaunchers++;
			} else if (type == RobotType.BARRACKS) {
				numBarracks++;
			}
		}
		
		rc.broadcast(Broadcast.numMinerFactoriesCh, numMinerFactories);
		rc.broadcast(Broadcast.numSupplyDepotsCh, numSupplyDepots);
		rc.broadcast(Broadcast.numAerospaceLabsCh, numAerospaceLabs);
		rc.broadcast(Broadcast.numHelipadsCh, numHelipads);
		rc.broadcast(Broadcast.numMinersCh, numMiners);
		rc.broadcast(Broadcast.numDronesCh, numDrones);
		rc.broadcast(Broadcast.numLaunchersCh, numLaunchers);
		rc.broadcast(Broadcast.numBeaversCh, numBeavers);
		rc.broadcast(Broadcast.numBarracksCh, numBarracks);
		rc.broadcast(Broadcast.numSoldiersCh, numSoldiers);

		if (pathDifficulty == 0) {
			int possibleDifficulty = rc.readBroadcast(Broadcast.scoutEnemyHQCh);
			if (possibleDifficulty != scoutMiner) {
				pathDifficulty = possibleDifficulty;
				// switch to early_mid condition 3 : map too large
				// TODO: more consistent when our scout dies / doesn't reach destination
				selectInitialStage(pathDifficulty);
			}
		}

		if (rc.isCoreReady()) {
			double ore = rc.getTeamOre();
			if (numBeavers == 0 || (numBeavers < 2 && numMinerFactories >= 1)) {
				int offsetIndex = 0;
				int[] offsets = {0,1,-1,2,-2,3,-3,4};
				int dirint = DirectionHelper.directionToInt(Direction.EAST);
				while (offsetIndex < 8 && !rc.canSpawn(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8], RobotType.BEAVER)) {
					offsetIndex++;
				}
				Direction buildDirection = null;
				if (offsetIndex < 8) {
					buildDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
				}
				if (buildDirection != null && ore >= 100) {
					rc.spawn(buildDirection, RobotType.BEAVER);
				}
			}
		}

		int gameStage = rc.readBroadcast(Broadcast.gameStageCh);
		if (gameStage == Broadcast.MID_GAME && rc.readBroadcast(Broadcast.soldierTowerTargetExistsCh) == 1) {
			RobotInfo[] allies = rc.senseNearbyRobots(Broadcast.readLocation(rc, Broadcast.soldierTowerTargetLocationChs), 52, rc.getTeam());
			if (allies.length >= 11) {
				int effectiveSoldiers = 0;
				for (RobotInfo ally : allies) {
					if (ally.supplyLevel > 0 && ally.type == RobotType.SOLDIER) {
						effectiveSoldiers += 1;
					}
				}
				if (effectiveSoldiers >= 12) {
					rc.broadcast(Broadcast.soldierAttackCh, 1);
				}
			}
		}
	}
	
	public void selectInitialStage(int pathDifficulty) throws GameActionException {
		// start as early_mid condition 1 : stage too large
		if (hq.distanceBetweenHQ >= 4000 ||pathDifficulty >= 200) {
			rc.broadcast(Broadcast.gameStageCh, Broadcast.MID_GAME);
			return;
		}
		// start as early_mid condition 2 : too many resources in safe zones
		MapLocation[] nearbySquares = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), 100);
		int totalOre = 0;
		for (MapLocation square: nearbySquares) {
			totalOre += rc.senseOre(square);
		}
		if (totalOre >= 1500) {
			rc.broadcast(Broadcast.gameStageCh, Broadcast.MID_GAME);
			return;
		}
	}
}
