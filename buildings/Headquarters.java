package team158.buildings;

import battlecode.common.*;
import team158.com.GroupController;
import team158.strategies.AerialStrategy;
import team158.strategies.DroneHarassTest;
import team158.strategies.GameStrategy;
import team158.strategies.GroundStrategy;
import team158.strategies.MinerTest;
import team158.strategies.NavigationTest;
import team158.units.Unit;
import team158.utils.Broadcast;

public class Headquarters extends Building {

	public final static int GROUND_STRATEGY = 1;
	public final static int AERIAL_STRATEGY = 2;
	public final static int MINER_STRATEGY = 3;
	public final static int NAVIGATION_STRATEGY = 4;
	public final static int DRONE_HARASS_STRATEGY = 5;
	
	public final static int TIME_UNTIL_LAUNCHERS_GROUP = 1500;
	public final static int TIME_UNTIL_COLLECT_SUPPLY = 1650;
	public final static int TIME_UNTIL_FULL_ATTACK = 1800;
	private final static int ORE_WINDOW = 100;
	
	private GroupController gc;
	private GameStrategy gameStrategy;
	private int strategy;

	private MapLocation[] towerOrder; // order of which enemy towers can be defeated

	private int numTowersDefeatable;
	private int enemyTowersRemaining;

	private double[] oreMined;
	private double oreRate;
	private int orePointer;
	
	public Headquarters(RobotController newRC) {
		super(newRC);
		this.strategy = AERIAL_STRATEGY;
		this.gc = new GroupController(rc, strategy);
		
		switch(this.strategy) {
			case GROUND_STRATEGY:		gameStrategy = new GroundStrategy(rc, gc, this);
										break;
			case AERIAL_STRATEGY:		gameStrategy = new AerialStrategy(rc, gc, this);
										break;
			case MINER_STRATEGY:		gameStrategy = new MinerTest(rc, gc, this);
										break;
			case NAVIGATION_STRATEGY:	gameStrategy = new NavigationTest(rc, gc, this);
										break;
			case DRONE_HARASS_STRATEGY: gameStrategy = new DroneHarassTest(rc, gc, this);
										break;
		}
		
		towerOrder = new MapLocation[6];
		numTowersDefeatable = 0;
		enemyTowersRemaining = 7;
		
		oreMined = new double[ORE_WINDOW];
		oreRate = 5;
		orePointer = 0;
	}
	
	@Override
	protected void actions() throws GameActionException {	
		if (Clock.getRoundNum()==0) {
			towerDefeatable();
			for (int i = 0; i < numTowersDefeatable; i++) {
			}
		}
		rc.broadcast(Broadcast.idealMiningOreAverage, 0);
		broadcastVulnerableEnemyTowerAttack();
		
		RobotInfo closestEnemy = super.findClosestEnemy(100);
		MapLocation closestEnemyLocation;
		if (closestEnemy == null) {
			closestEnemyLocation = myLocation;
		} else {
			closestEnemyLocation = closestEnemy.location;
		}
		Broadcast.broadcastLocation(rc,  Broadcast.enemyNearHQLocationChs, closestEnemyLocation);
		
		int mySupply = (int) rc.getSupplyLevel();
		RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());

		if (Clock.getRoundNum() < TIME_UNTIL_LAUNCHERS_GROUP) {
			int distanceFactor = (int) hqDistance;

			MapLocation loc = null;
			boolean transferred = false;
			int supplyAmount = 0;
			for (RobotInfo r : friendlyRobots) {
				if (r.type == RobotType.LAUNCHER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 16 * distanceFactor) {
						if (r.supplyLevel == 0) {
							rc.transferSupplies(r.type.supplyUpkeep * 24 * distanceFactor, r.location);
							transferred = true;
						}
						else {
							loc = r.location;
							supplyAmount = r.type.supplyUpkeep * 24 * distanceFactor;
						}
					}
				}
				else if (r.type == RobotType.MINER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 16 * distanceFactor) {
						if (r.supplyLevel == 0) {
							rc.transferSupplies(r.type.supplyUpkeep * 24 * distanceFactor, r.location);
							transferred = true;
						}
						else {
							loc = r.location;
							supplyAmount = r.type.supplyUpkeep * 24 * distanceFactor;
						}
					}
				}
				else if (r.type == RobotType.SOLDIER || r.type == RobotType.TANK || r.type == RobotType.DRONE) {
					if (r.supplyLevel < r.type.supplyUpkeep * 8 * distanceFactor) {
						if (r.supplyLevel == 0) {
							rc.transferSupplies(r.type.supplyUpkeep * 12 * distanceFactor, r.location);
							transferred = true;
						}
						else {
							loc = r.location;
							supplyAmount = r.type.supplyUpkeep * 12 * distanceFactor;
						}
					}
				}
				else if (r.type == RobotType.BEAVER) {
					if (r.supplyLevel < r.type.supplyUpkeep * 100) {
						if (r.supplyLevel == 0) {
							rc.transferSupplies(r.type.supplyUpkeep * 200, r.location);
							transferred = true;
						}
						else {
							loc = r.location;
							supplyAmount = r.type.supplyUpkeep * 200;
						}
					}
				}
			}
			if (!transferred && loc != null) {
				rc.transferSupplies(supplyAmount, loc);
			}
		}
		else {
			for (RobotInfo r : friendlyRobots) {
				if (r.type == RobotType.DRONE || r.type == RobotType.LAUNCHER || r.type == RobotType.TANK) {
					rc.transferSupplies(mySupply, r.location);
					break;
				}
			}
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
						for (RobotInfo enemy : directlyAttackable) {
							if (enemy.type != RobotType.MISSILE) {
								rc.attackLocation(enemy.location);
								break;
							}
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

		// Compute rate of ore generation
		oreMined[orePointer] = rc.readBroadcast(Broadcast.minerOreX1000Ch) / 1000.0;
		oreRate = oreRate + (oreMined[orePointer] - oreMined[(orePointer + ORE_WINDOW - 1) % ORE_WINDOW]) / ORE_WINDOW;
		orePointer = (orePointer + 1) % ORE_WINDOW;
		this.gameStrategy.executeStrategy();
	}
	
	
	
	// Broadcasts to groups about a vulnerable tower for us to attack
	// 
	// A vulnerable tower is one where that 
	//
	protected void broadcastVulnerableEnemyTowerAttack() throws GameActionException {
		MapLocation targetTower = null;
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		int enemyTowersRemaining = enemyTowers.length;
		if (this.enemyTowersRemaining != enemyTowersRemaining) {
			this.enemyTowersRemaining = enemyTowersRemaining;
			if (enemyTowersRemaining > 0) {
				//index of the tower targetted
				int index = numTowersDefeatable - enemyTowersRemaining;
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
			rc.setIndicatorString(0, String.valueOf(targetTower));
		}
	}	
	//calculates whether the towers are defeatable down to 3 using ground units. If not, we must build launchers
	protected void towerDefeatable() throws GameActionException {
		MapLocation targetTower = null;
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		int numEnemyTowers = enemyTowers.length;
		//keeps track of the order of the towers defeated so we don't have to recompute
		while (numTowersDefeatable < numEnemyTowers) {
			//every iteration one tower decreases so we must acount for this
			int numTowersLeft = numEnemyTowers - numTowersDefeatable;
			// runs the iteration on the number of towers remaining
			MapLocation[] enemyTowersLeft = new MapLocation[numTowersLeft];
			int index = 0;
			for (int i = 0; i < numEnemyTowers; i++) {
				if (!inTowerOrder(enemyTowers[i])) {
					enemyTowersLeft[index] = enemyTowers[i];
					index++;
				} 
			}
			int[] distToEnemyTowers = new int[numTowersLeft];
			// keeps track of distance of enemy towers to ourhq
			for (int i = 0; i < numTowersLeft; i++) {
				distToEnemyTowers[i] = this.myLocation.distanceSquaredTo(enemyTowersLeft[i]);
			}
			boolean towerExists = false;
			int count = 0;
			while (count < numEnemyTowers) {
				int minDistance = 999999;
				int targetTowerIndex = 0; // to keep track of which tower was chosen
				for (int i = 0; i < numTowersLeft; i++) {
					if (distToEnemyTowers[i] < minDistance) {
						minDistance = distToEnemyTowers[i];
						targetTower = enemyTowersLeft[i];
						targetTowerIndex = i;
					}
				}
				int numNearbyTowers = 0;
				for (int j = 0; j < numTowersLeft; j++) {
					if (targetTowerIndex != j && targetTower.distanceSquaredTo(enemyTowersLeft[j]) <= 24) {
						numNearbyTowers++;
					}
				}
				//valid tower target, add to towerOrder and increment the num towers defeatable
				if (numNearbyTowers <= 3) {
					towerExists = true;
					towerOrder[numTowersDefeatable] = targetTower;
					numTowersDefeatable++;
					break;
				}
				//otherwise test next target
				else {
					distToEnemyTowers[targetTowerIndex] = 999999;
					count++;
				}
			}
			// if no isolated tower exists, check if number of towers remaining is <= 3
			// if so, attack enemyHQ. otherwise, need to build launchers
			boolean buildLaunchers;
			if (!towerExists) {
				if (numTowersDefeatable >= 3) {
					buildLaunchers = true;
				}
				break;
			}
		}
	}
	
	protected boolean inTowerOrder(MapLocation m) {
		for (int i = 0; i < numTowersDefeatable; i++) {
			if (towerOrder[i] == m) return true;
		}
		return false;
	}

}
