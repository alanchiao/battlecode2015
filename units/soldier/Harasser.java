package team158.units.soldier;

import team158.units.Unit;
import team158.units.com.Navigation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Harasser {

	//////////////////////////////////////////////
	// Operational variables
	private RobotController rc;
	private Unit unit;
	
	///////////////////////////////////////////////
	// Harassing Variables
	
	public final static boolean IS_ATTACKING = true;
	
	// harassing drone states
	public int state;
	public final static int RETREAT_STATE = 0;
	public final static int SEARCH_STATE = 1;
	public final static int FOLLOW_STATE = 2;
	public final static int ATTACK_STATE = 3;
	
	// SEARCH_STATE variables
	public MapLocation searchDestinationOne;
	public MapLocation searchDestinationTwo;
	public MapLocation currentSearchDestination;
	public final static int SEARCH_RADIUS = 10;
	public boolean switchedSearchDestination = false;
	public int timeSinceLastSwitch = 0;
	
	// FOLLOW_STATE/ATTACK_STATE variables
	public int targetID;
	public final static int NO_TARGET = -1;

	public Harasser(RobotController rc, Unit unit) {
		this.rc = rc;
		this.unit = unit;
		
		this.targetID = NO_TARGET;
		
		if (IS_ATTACKING) {
			this.state = SEARCH_STATE;
			initializeSearchDestinations();
		} else {
			this.state = SEARCH_STATE;
		}
		
		try {
			MapLocation safeSearchDestination = Broadcast.readLocation(rc, Broadcast.soldierHarassLocationChs);
			if (!safeSearchDestination.equals(new MapLocation(0, 0)) && !safeSearchDestination.equals(this.currentSearchDestination)) {
				this.currentSearchDestination = safeSearchDestination;
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}

	public void harass() throws GameActionException {
		
		// keep track of destination switching
	
		
		if (this.state == RETREAT_STATE) {
			rc.setIndicatorString(0, Integer.toString(this.state));
			
			if (rc.isCoreReady()) {
				if (shouldRetreat()) {
					unit.navigation.moveToDestination(unit.ownHQ, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
					// switch searching areas to avoid initial enemy
					if (!switchedSearchDestination) {
						if (this.currentSearchDestination.equals(this.searchDestinationOne)) {
							this.currentSearchDestination = this.searchDestinationTwo;
						} else {
							this.currentSearchDestination = this.searchDestinationOne;
						}
						switchedSearchDestination = true;
						Broadcast.broadcastLocation(rc, Broadcast.soldierHarassLocationChs, this.currentSearchDestination);
					}
				} else {			
					this.state = SEARCH_STATE;
					switchedSearchDestination = false;
					timeSinceLastSwitch = 0;
					harass();
				}
				return;
			}
		}  else if (this.state == SEARCH_STATE) { // then randomly move around searching for miner/beaver while avoiding attack
			rc.setIndicatorString(0, Integer.toString(this.state));
			
			if (shouldRetreat()) {
				this.state = RETREAT_STATE;
				harass();
				return;
			} 
			
			if (rc.isWeaponReady()) {
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
				if (enemies.length > 0) { 
					rc.attackLocation(Unit.selectTarget(enemies));
				}
			}
			
			if (rc.isCoreReady()) {
				// move back and forward between two search destinations
				if (rc.getLocation().distanceSquaredTo(currentSearchDestination) <= SEARCH_RADIUS * SEARCH_RADIUS / 8) { // close enough. switch search area.
					if (this.currentSearchDestination.equals(this.searchDestinationOne)) {
						this.currentSearchDestination = this.searchDestinationTwo;
					} else {
						this.currentSearchDestination = this.searchDestinationOne;
					}
				}
			
				unit.navigation.moveToDestination(this.currentSearchDestination, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
				
				// pick a unit to follow
				RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
				for (RobotInfo nearbyEnemy: nearbyEnemies) {
					if (nearbyEnemy.type == RobotType.MINER || nearbyEnemy.type == RobotType.BEAVER) {
						this.targetID = nearbyEnemy.ID;
						this.state = FOLLOW_STATE;
						break;
					} 
				}
			}
		} else if (this.state == FOLLOW_STATE) { // then continue following target
			if (shouldRetreat()) {
				this.state = RETREAT_STATE;
				harass();
				return;
			}
			
			if (rc.isCoreReady()) {
				if (rc.canSenseRobot(targetID)) { // attack
					this.state = ATTACK_STATE;
					harass();
				} else { // have to search for another unit again
					this.state = SEARCH_STATE;
					harass();
				}
			}
		} else if (this.state == ATTACK_STATE) { // try to attack target
			rc.setIndicatorString(0, Integer.toString(this.state));
			if (shouldRetreat()) {
				this.state = RETREAT_STATE;
				harass();
				return;
			}
			
			// make sure can still attack target
			if (!rc.canSenseRobot(this.targetID)) { // then need to go back to searching
				this.state = SEARCH_STATE;
				harass();
				return;
			}
			
			if (rc.isWeaponReady()) {
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
				if (enemies.length > 0) { 
					rc.attackLocation(Unit.selectTarget(enemies));
				}
			}
			
			if (rc.isCoreReady()) {
				RobotInfo targetEnemy = rc.senseRobot(this.targetID);
				unit.navigation.greedyMoveToDestination(targetEnemy.location, Navigation.AVOID_ENEMY_ATTACK_BUILDINGS);
			} 
		}
	}
	
	// search destinations are two points such that the
	// line from our HQ to the enemy HQ is perpendicular to the 
	// line passing through the two points and the enemy HQ
	public void initializeSearchDestinations() {
		Direction dirToEnemyBase = unit.ownHQ.directionTo(unit.enemyHQ);
		MapLocation nearEnemyHQLocation = new MapLocation((unit.ownHQ.x + 3*unit.enemyHQ.x)/4, (unit.ownHQ.y + 3*unit.enemyHQ.y)/4);
		Direction perpDirectionOne = dirToEnemyBase.rotateRight().rotateRight();
		Direction perpDirectionTwo = dirToEnemyBase.rotateLeft().rotateLeft();
		
		this.searchDestinationOne = nearEnemyHQLocation.add(perpDirectionOne, SEARCH_RADIUS);
		this.searchDestinationTwo = nearEnemyHQLocation.add(perpDirectionTwo, SEARCH_RADIUS);
		this.currentSearchDestination = searchDestinationOne;
	}
	
	public boolean shouldRetreat() {
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
		double enemyPower = 0;
		double allyPower = 0;
		for (int i = nearbyEnemies.length; --i >= 0;) {
			if (nearbyEnemies[i].type == RobotType.LAUNCHER || nearbyEnemies[i].type == RobotType.MISSILE || nearbyEnemies[i].type == RobotType.COMMANDER) {
				enemyPower += 2.5;
			} else if (nearbyEnemies[i].type == RobotType.BEAVER || nearbyEnemies[i].type == RobotType.MINER) {
				enemyPower += 0.32;
			} else if (nearbyEnemies[i].type == RobotType.SOLDIER){
				enemyPower += 1.25;
			}
		} 
		
		for (int i = nearbyAllies.length; --i >= 0;) {
			if (nearbyAllies[i].type == RobotType.SOLDIER) {
				allyPower += 1;
			}
		} 
		allyPower += 1; // self
		/**
		for (int i = nearbyEnemies.length; --i >= 0;) {	
			if (nearbyEnemies[i].type != RobotType.BEAVER && nearbyEnemies[i].type != RobotType.MINER) {
				rc.setIndicatorString(2, "Should retreat: " + Boolean.toString(true));
				return true;
			} 
		}
		**/
		
		/**
		rc.setIndicatorString(2, "Should retreat: " + Boolean.toString(false));
		return false;
		**/
		rc.setIndicatorString(1, "Enemy power: " + Double.toString(enemyPower) + ", Ally power: " + Double.toString(allyPower));
		if (enemyPower - allyPower  >= 0) {
			rc.setIndicatorString(2, "Should retreat: " + Boolean.toString(true));
			return true;
		} else {
			rc.setIndicatorString(2, "Should retreat: " + Boolean.toString(false));
			return false;
		} 
	}
}
