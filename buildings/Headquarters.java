package team158.buildings;

import java.util.Arrays;

import battlecode.common.*;
import team158.com.Broadcast;
import team158.com.GroupController;
import team158.strategies.AerialStrategy;
import team158.strategies.GameStrategy;
import team158.strategies.SoldierLauncherComboStrategy;
import team158.units.Unit;

public class Headquarters extends Building {

	public final static int GROUND_STRATEGY = 1;
	public final static int AERIAL_STRATEGY = 2;
	public final static int DUAL_STRATEGY = 3;

	public final static int TIME_COLLECT_SUPPLY = 350;
	public final static int TIME_FULL_ATTACK = 200;
	//private final static int ORE_WINDOW = 100;
	
	private GroupController gc;
	private GameStrategy gameStrategy;
	private int strategy;

	private MapLocation[] towerOrder; // order of which enemy towers can be defeated

	private int numTowersInitial;
	private int enemyTowersRemaining;

	/*
	private double[] oreMined;
	private double oreRate;
	private int orePointer;
	*/
	
	public Headquarters(RobotController newRC) {
		super(newRC);
		this.strategy = DUAL_STRATEGY;
		this.gc = new GroupController(rc, strategy);
		
		switch(this.strategy) {
			case AERIAL_STRATEGY:		gameStrategy = new AerialStrategy(rc, gc, this);
										break;
			case DUAL_STRATEGY:			gameStrategy = new SoldierLauncherComboStrategy(rc, gc, this);
		}
		
		towerOrder = new MapLocation[6];
		enemyTowersRemaining = 7;

		calculateTowerOrder();
		try {
			findSoldierTowerTargets();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		try {
			Broadcast.broadcastLocation(rc, Broadcast.enemyHQLocation, enemyHQ);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		/*
		oreMined = new double[ORE_WINDOW];
		oreRate = 5;
		orePointer = 0;
		*/
	}
	
	@Override
	protected void actions() throws GameActionException {
		broadcastVulnerableEnemyTowerAttack();
		RobotInfo closestEnemy = super.findClosestEnemy((int)(hqDistance*hqDistance)/8);
		MapLocation closestEnemyLocation;
		if (closestEnemy == null) {
			// hack -- broadcast to Launcher Rally Location 
			closestEnemyLocation = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
		} else {
			closestEnemyLocation = closestEnemy.location;
		}
		//rc.setIndicatorString(2, String.valueOf(closestEnemyLocation));
		Broadcast.broadcastLocation(rc, Broadcast.enemyNearHQLocationChs, closestEnemyLocation);
		
		RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());

		int distanceFactor = (int) hqDistance;

		MapLocation loc = null;
		int supplyAmount = 0;
		int priority = 0;
		double gameFractionLeft = 1.0 - (double)Clock.getRoundNum() / rc.getRoundLimit();

		for (RobotInfo r : friendlyRobots) {
			if (r.type == RobotType.LAUNCHER) {
				if (r.supplyLevel < r.type.supplyUpkeep * 16 * distanceFactor) {
					if (priority == 0 || (r.supplyLevel == 0 && priority < 3)) {
						loc = r.location;
						supplyAmount = r.type.supplyUpkeep * 24 * distanceFactor;
						priority = 3;
					}
				}
			}
			else if (r.type == RobotType.MINER) {
				if (r.supplyLevel < r.type.supplyUpkeep * 1000 * gameFractionLeft) {
					if (priority == 0 || (r.supplyLevel == 0 && priority < 2)) {
						loc = r.location;
						supplyAmount = (int) (r.type.supplyUpkeep * 1500 * gameFractionLeft);
						priority = 2;
					}
				}
			}
			else if (r.type == RobotType.DRONE) {
				if (r.supplyLevel < 20000) {
					if (priority < 1) {
						loc = r.location;
						supplyAmount = 30000;
						priority = 1;
					}
				}
			}
			else if (r.type == RobotType.SOLDIER || r.type == RobotType.TANK) {
				if (r.supplyLevel < r.type.supplyUpkeep * 8 * distanceFactor) {
					if (priority == 0 || (r.supplyLevel == 0 && priority < 4)) {
						loc = r.location;
						supplyAmount = r.type.supplyUpkeep * 12 * distanceFactor;
						priority = 4;
					}
				}
			}
			else if (r.type == RobotType.BEAVER) {
				if (r.supplyLevel < r.type.supplyUpkeep * 100) {
					if (priority == 0 || (r.supplyLevel == 0 && priority < 2)) {
						loc = r.location;
						supplyAmount = r.type.supplyUpkeep * 200;
						priority = 2;
					}
				}
			}
		}
		if (loc != null) {
			rc.transferSupplies(supplyAmount, loc);
		}

		// Headquarters attack strategy
		if (rc.isWeaponReady()) {
			int numTowers = rc.senseTowerLocations().length;
			if (numTowers >= 5) { // splash damage
				RobotInfo[] enemies = rc.senseNearbyRobots(52, rc.getTeam().opponent());
				if (enemies.length > 0) {
					RobotInfo[] directlyAttackable = rc.senseNearbyRobots(35, rc.getTeam().opponent());
					// Greedy attack. Could potentially use selectTarget but it doesn't factor in splash.
					if (directlyAttackable.length > 0) {
						boolean attacked = false;
						for (RobotInfo enemy : directlyAttackable) {
							if (enemy.type != RobotType.MISSILE) {
								rc.attackLocation(enemy.location);
								attacked = true;
								break;
							}
						}
						if (!attacked) {
							rc.attackLocation(directlyAttackable[0].location);
						}
					}
					else {
						for (RobotInfo enemy : enemies) {
							MapLocation attackLocation = enemy.location.add(enemy.location.directionTo(myLocation));
							if (attackLocation.distanceSquaredTo(myLocation) <= 35) {
								rc.attackLocation(attackLocation);
								break;
							}
						}
					}
				}
			}
			else if (numTowers >= 2) { // range 35
				RobotInfo[] enemies = rc.senseNearbyRobots(35, rc.getTeam().opponent());
				if (enemies.length > 0) {
					rc.attackLocation(Unit.selectTarget(enemies));
				}
			}
			else { // range 24
				RobotInfo[] enemies = rc.senseNearbyRobots(24, rc.getTeam().opponent());
				if (enemies.length > 0) {
					rc.attackLocation(Unit.selectTarget(enemies));
				}
			}
		}

		/* Compute rate of ore generation
		oreMined[orePointer] = rc.readBroadcast(Broadcast.minerOreX1000Ch) / 1000.0;
		oreRate = oreRate + (oreMined[orePointer] - oreMined[(orePointer + ORE_WINDOW - 1) % ORE_WINDOW]) / ORE_WINDOW;
		orePointer = (orePointer + 1) % ORE_WINDOW;
		*/
		this.gameStrategy.executeStrategy();
	}
	
	// Broadcasts to groups about a vulnerable tower for us to attack.
	protected void broadcastVulnerableEnemyTowerAttack() throws GameActionException {
		MapLocation targetTower = null;
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		int enemyTowersRemaining = enemyTowers.length;
		if (this.enemyTowersRemaining != enemyTowersRemaining) {
			this.enemyTowersRemaining = enemyTowersRemaining;
			if (enemyTowersRemaining > 0) {
				// index of the tower targeted
				int index = numTowersInitial - enemyTowersRemaining;
				if (towerOrder[index] != null) {
					targetTower = towerOrder[index];
				}
				else {
					targetTower = ownHQ;
				}
			}
			else {
				targetTower = enemyHQ;
			}
			Broadcast.broadcastLocation(rc, Broadcast.enemyTowerTargetLocationChs, targetTower);
		}
	}

	protected void findSoldierTowerTargets() throws GameActionException {
		MapLocation targetTower1 = null;
		MapLocation targetTower2 = null;
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		double heuristicMax1 = 0;
		double heuristicMax2 = 0;
		for (MapLocation e : enemyTowers) {
			double perpendDist = computePerpendicularDistance(e);
			double heuristic = perpendDist + 2*Math.sqrt(e.distanceSquaredTo(enemyHQ));
			if (heuristic > heuristicMax1 && perpendDist > 15) {
				int numNearbyTowers = 0;
				for (MapLocation e2: enemyTowers) {
					if(e2 != e && e.distanceSquaredTo(e2) <= 24) {
						numNearbyTowers++;
					}
				}
				if (numNearbyTowers < 2) {
					if (heuristicMax2 == 0) {
						heuristicMax2 = heuristicMax1;
						targetTower2 = targetTower1;
					}
					heuristicMax1 = heuristic;
					targetTower1 = e;
				}
			}
			// check for 2nd farthest tower
			else if (heuristic > heuristicMax2 && perpendDist > 15){
				int numNearbyTowers = 0;
				for (MapLocation e2: enemyTowers) {
					if(e2 != e && e.distanceSquaredTo(e2) <= 24) {
						numNearbyTowers++;
					}
				}
				if (numNearbyTowers < 2) {
					heuristicMax2 = heuristic;
					targetTower2 = e;
				}
			}
		}
		if (targetTower1 == null) {
			rc.broadcast(Broadcast.soldierTowerTarget1Exists, 0);
		}
		else {			
			rc.broadcast(Broadcast.soldierTowerTarget1Exists, 1);
			Broadcast.broadcastLocation(rc, Broadcast.soldierTowerTargetLocation1Chs, targetTower1);
			rc.setIndicatorString(1, String.valueOf(targetTower1));
		}
		
		if (targetTower2 == null) {
			rc.broadcast(Broadcast.soldierTowerTarget2Exists, 0);
		}
		else {			
			rc.broadcast(Broadcast.soldierTowerTarget2Exists, 1);
			Broadcast.broadcastLocation(rc, Broadcast.soldierTowerTargetLocation2Chs, targetTower2);
			rc.setIndicatorString(2, String.valueOf(targetTower2));
		}
	}

	protected void calculateTowerOrder() {
		MapLocation targetTower = null;
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		numTowersInitial = enemyTowers.length;
		// keeps track of the order of the towers defeated so we don't have to recompute
		for (int j = 0; j < numTowersInitial; j++) {
			// every iteration one tower decreases so we must account for this
			int numTowersLeft = numTowersInitial - j;
			// runs the iteration on the number of towers remaining
			MapLocation[] enemyTowersLeft = new MapLocation[numTowersLeft];
			int index = 0;
			for (int i = 0; i < numTowersInitial; i++) {
				if (!inTowerOrder(enemyTowers[i], j)) {
					enemyTowersLeft[index] = enemyTowers[i];
					index++;
				} 
			}
			int[] distToEnemyTowers = new int[numTowersLeft];
			// keeps track of distance of enemy towers to own hq
			for (int i = 0; i < numTowersLeft; i++) {
				distToEnemyTowers[i] = this.myLocation.distanceSquaredTo(enemyTowersLeft[i]);
			}
			int minDistance = 999999;
			for (int i = 0; i < numTowersLeft; i++) {
				if (distToEnemyTowers[i] < minDistance) {
					minDistance = distToEnemyTowers[i];
					targetTower = enemyTowersLeft[i];
				}
			}
			towerOrder[j] = targetTower;
		}
		rc.setIndicatorString(0, Arrays.toString(towerOrder));
	}

	protected boolean inTowerOrder(MapLocation m, int numTowersDefeated) {
		for (int i = 0; i < numTowersDefeated; i++) {
			if (towerOrder[i] == m) return true;
		}
		return false;
	}

	public double computePerpendicularDistance(MapLocation candidate) {
		ownHQ = myLocation;
		double x1 = ownHQ.x;
		double x2 = enemyHQ.x;
		double y1 = ownHQ.y;
		double y2 = enemyHQ.y;
		return Math.abs((y2 - y1) * candidate.x - (x2 - x1) * candidate.y + x2 * y1 - y2 * x1) / Math.sqrt(ownHQ.distanceSquaredTo(enemyHQ));
	}
}
