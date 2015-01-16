package team158.buildings;

import battlecode.common.*;
import team158.com.GroupController;
import team158.strategies.AerialStrategy;
import team158.strategies.GameStrategy;
import team158.strategies.GroundStrategy;
import team158.units.Unit;
import team158.utils.Broadcast;

public class Headquarters extends Building {
	
	public final static int TIME_UNTIL_LAUNCHERS_GROUP = 1500;
	public final static int TIME_UNTIL_COLLECT_SUPPLY = 1650;
	public final static int TIME_UNTIL_FULL_ATTACK = 1800;
	
	private int strategy;
	private GroupController gc;
	private GameStrategy gameStrategy;
	
	// number of enemy towers left
	private int enemyTowersRemaining;
	
	public Headquarters(RobotController newRC) {
		super(newRC);
		this.strategy = 1;
		this.gc = new GroupController(rc, strategy);
		if (this.strategy == 1) {
			gameStrategy = new GroundStrategy(rc, gc, this);
		} else {
			gameStrategy = new AerialStrategy(rc, gc, this);
		}
		
		enemyTowersRemaining = 7;
	}
	
	@Override
	protected void actions() throws GameActionException {	
		broadcastVulnerableEnemyTowerAttack();
		
		RobotInfo closestEnemy = findClosestEnemy(100);
		MapLocation closestEnemyLocation;
		if (closestEnemy == null) {
			closestEnemyLocation = myLocation;
		} else {
			closestEnemyLocation = closestEnemy.location;
		}
		rc.setIndicatorString(0, String.valueOf(closestEnemyLocation));
		Broadcast.broadcastLocation(rc, closestEnemyLocation, Broadcast.launcherRallyLocationChs);
		
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
				else if (r.type == RobotType.DRONE) {
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
				if (r.type == RobotType.DRONE || r.type == RobotType.LAUNCHER) {
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

		this.gameStrategy.executeStrategy();
	}
	
	
	
	// Broadcasts to groups about a vulnerable tower for us to attack
	// 
	// A vulnerable tower is one where that 
	protected void broadcastVulnerableEnemyTowerAttack() throws GameActionException {
		MapLocation targetTower = null;
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		int enemyTowersRemaining = enemyTowers.length;
		
		if (this.enemyTowersRemaining != enemyTowersRemaining) {
			this.enemyTowersRemaining = enemyTowersRemaining;
			if (enemyTowersRemaining > 0) {
				//reset tower died status
				int[] distToEnemyTowers = new int[enemyTowersRemaining];
				for (int i = 0; i < enemyTowersRemaining; i++) {
					distToEnemyTowers[i] = this.myLocation.distanceSquaredTo(enemyTowers[i]);
				}
				
				boolean towerExists = false;
				int count = 0;
				while (count < enemyTowersRemaining) {
					int minDistance = 999999;
					int targetTowerIndex = 0;
					for (int i = 0; i < enemyTowersRemaining; i++) {
						if (distToEnemyTowers[i] < minDistance) {
							minDistance = distToEnemyTowers[i];
							targetTower = enemyTowers[i];
							targetTowerIndex = i;
						}
					}
					int numNearbyTowers = 0;
					for (int j = 0; j < enemyTowersRemaining; j++) {
						if (targetTowerIndex != j && targetTower.distanceSquaredTo(enemyTowers[j]) <= 24) {
							numNearbyTowers++;
						}
					}
					if (numNearbyTowers <= 3) {
						towerExists = true;
						break;
					} else {
						distToEnemyTowers[targetTowerIndex] = 999999;
						count++;
					}
				}
				if (!towerExists) {
					if (enemyTowersRemaining != 6) {
						targetTower = enemyHQ;
					} else {
						targetTower = null;
					}
				}
			}
			else {
				targetTower = enemyHQ;
			}
		}

		if (targetTower != null) {
			Broadcast.broadcastLocation(rc, targetTower, Broadcast.groupTargetLocationChs);
		}

	}	
	
	public RobotInfo findClosestEnemy(int rangeSquared) {
		//find closest enemy target
		RobotInfo[] closeRobots = rc.senseNearbyRobots(rangeSquared, rc.getTeam().opponent());
		
		if (closeRobots.length == 0) {
			return null;
		}
		
		RobotInfo closestRobot;
		closestRobot = closeRobots[0];
		int closestDistance = closestRobot.location.distanceSquaredTo(myLocation);
		for (int i = 1; i < closeRobots.length; i++) {
			int distance = closeRobots[i].location.distanceSquaredTo(myLocation);
			if (distance < closestDistance) {
				closestDistance = distance;
				closestRobot = closeRobots[i];
			}
		}	
		return closestRobot;
	}
}