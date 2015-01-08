package battlecode2015.units;

import battlecode.common.*;
import battlecode2015.units.com.Navigation;
import battlecode2015.utils.DirectionHelper;
import battlecode2015.utils.Broadcast;

public class Soldier extends Unit {
	protected void actions() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam().opponent()
			);

		if (rc.isWeaponReady()) {
			if (enemies.length > 0) {
				rc.attackLocation(selectTarget(enemies));
			}
        }

		if (rc.isCoreReady()) {
			if (enemies.length > 0) {
				Direction d = selectMoveDirectionMicro();
				if (d != null) {
					rc.move(d);
				}
			}
			else {
				MapLocation target;
				if (groupID != -1) {
					int xLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
					int yLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
					target = new MapLocation(xLoc, yLoc);
					moveByGroup(target);
				}
				else {
					boolean toldToAttack = rc.readBroadcast(Broadcast.soldierMarchCh) == 1;
		
					if (toldToAttack) {
						target = rc.senseEnemyHQLocation();
					}
					else {
						int xLoc = rc.readBroadcast(Broadcast.soldierRallyXCh);
						int yLoc = rc.readBroadcast(Broadcast.soldierRallyYCh);
						target = new MapLocation(xLoc, yLoc);
					}
					this.destinationPoint = target;
					Navigation.moveToDestinationPoint(rc, this);
					/**
					* READD THIS BLOCK and remove call to Navigation.moveToDestinationPoint to restore
					int dirint = DirectionHelper.directionToInt(rc.getLocation().directionTo(target));
					
					int offsetIndex = 0;
					int[] offsets = {0,1,-1,2,-2};
					while (offsetIndex < 5 && !rc.canMove(DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8])) {
						offsetIndex++;
					}
					Direction moveDirection = null;
					if (offsetIndex < 5) {
						moveDirection = DirectionHelper.directions[(dirint+offsets[offsetIndex]+8)%8];
					}
					if (moveDirection != null) {
						rc.move(moveDirection);
					}
					**/
				}
			}
		}	
	}
	//////////////////////////////
	// Detection methods
	
	// count number of friend units next to you
	public int countNearbyFriendlyUnits() {
		RobotInfo[] allies = rc.senseNearbyRobots(
				rc.getType().attackRadiusSquared,
				rc.getTeam()
			);
		return allies.length;
	}
}