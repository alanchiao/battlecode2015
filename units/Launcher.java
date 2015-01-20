package team158.units;

import team158.buildings.Headquarters;
import team158.com.Broadcast;
import team158.units.com.Navigation;
import team158.utils.DirectionHelper;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends Unit {
	
	public boolean isReloading;
	public boolean noSupply;

	public Launcher(RobotController newRC) {
		super(newRC);
		this.isReloading = false;
		noSupply = false;
	}

	@Override
	protected void actions() throws GameActionException {
		RobotInfo[] enemiesAttackable = rc.senseNearbyRobots(24, rc.getTeam().opponent());
		MapLocation myLocation = rc.getLocation();

		if (rc.getSupplyLevel() == 0 && myLocation.distanceSquaredTo(ownHQ) > 15) {
			if (noSupply) {
				rc.broadcast(Broadcast.requestSupplyDroneCh, rc.getID());
			}
			else {
				noSupply = true;
			}
		}
		else {
			noSupply = false;
		}
		
		if (rc.getMissileCount() >= 3) {
			isReloading = false;
		}
		// launch 3 missiles at a time, then retreat. Do not launch
		// 3 missiles if already surrounded by more than 1 missile
		RobotInfo[] nearbyAlliedUnits = rc.senseNearbyRobots(2, rc.getTeam());
		int nearbyMissileCount = 0;
		for (RobotInfo ally: nearbyAlliedUnits) {
			if (ally.type == RobotType.MISSILE) {
				nearbyMissileCount++;
			}
		}
		
		if (enemiesAttackable.length > 0 && nearbyMissileCount < 1 && !isReloading) {
			int dirint = DirectionHelper.directionToInt(myLocation.directionTo(selectTarget(enemiesAttackable)));
			int[] offsets = {0,1,-1,2,-2};
			int offsetIndex = 0;
			while (offsetIndex < 5 && !rc.canLaunch(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				rc.launchMissile(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
			while (offsetIndex < 5 && !rc.canLaunch(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				rc.launchMissile(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
			while (offsetIndex < 5 && !rc.canLaunch(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
				offsetIndex++;
			}
			if (offsetIndex < 5) {
				rc.launchMissile(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
			if (rc.getMissileCount() < 3) {
				isReloading = true;
			}
        }
		
		// Move
		if (rc.isCoreReady()) {
			// reloading - retreat ot recover missiles
			if (isReloading) {
				navigation.moveToDestination(this.ownHQ, Navigation.AVOID_ALL);
				return;
			}
			
			MapLocation target;
			int approachStrategy;
			if (Clock.getRoundNum() < Headquarters.TIME_UNTIL_LAUNCHERS_GROUP) {
				rc.setIndicatorString(0, String.valueOf(groupTracker.groupID));
				if (groupTracker.groupID == Broadcast.launcherGroupDefenseCh) {
					boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
					if (hasHQCommand) {								
						approachStrategy = 2;
						// enemyNearHQLocationChs defaults to rally location if no enemy around.
						target = Broadcast.readLocation(rc, Broadcast.enemyNearHQLocationChs);
						boolean towerAttacked = rc.readBroadcast(Broadcast.towerAttacked) == 1; 
						boolean enemyNear = rc.readBroadcast(Broadcast.enemyNearTower) == 1; 
						if (towerAttacked) {
							target = Broadcast.readLocation(rc, Broadcast.attackedTowerLocationChs);
						}
						else if (enemyNear) {
							target = Broadcast.readLocation(rc, Broadcast.enemyNearTowerLocationChs);;
						}
					}
					else {
						approachStrategy = 0;
						target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
					}
				}
				else if (groupTracker.groupID == Broadcast.launcherGroupAttackCh) {
					boolean hasHQCommand = rc.readBroadcast(groupTracker.groupID) == 1;
					if (hasHQCommand) {								
						approachStrategy = 2;
						//enemyNearHQLocationChs defaults to ownHQ location if no enemy around.
						target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
					}
					else {
						approachStrategy = 0;
						target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
					}
				}
				else {
					approachStrategy = 0;
					target = Broadcast.readLocation(rc, Broadcast.launcherRallyLocationChs);
				}
				rc.setIndicatorString(2, "[ " + target.x + ", " + target.y + " ]");
			} else {
				if (groupTracker.groupID == Broadcast.launcherGroupAttackCh) {
					target = this.enemyHQ;
					approachStrategy = 2;
				}
				else {
					target = Broadcast.readLocation(rc, Broadcast.enemyTowerTargetLocationChs);
					approachStrategy = 2;
				}
			}
			moveToLocationWithMicro(target, approachStrategy);
		}
	}

}
