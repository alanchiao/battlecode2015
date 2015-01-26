package team158.units;

import team158.com.Broadcast;
import team158.units.com.Navigation;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Drone extends Unit {

	int followingID;
	boolean gotSupply;
	MapLocation followingLocation;

	public Drone(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		if (!autoSupplyTransfer) { // Must transfer supply to a launcher
			MapLocation myLocation = rc.getLocation();
			if (gotSupply) {
				if (myLocation.distanceSquaredTo(followingLocation) <= 15) {
					RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
					if (friendlyRobots.length > 0) {
						for (RobotInfo r : friendlyRobots) {
							if (r.type == RobotType.LAUNCHER) {
								rc.transferSupplies((int) (rc.getSupplyLevel()), r.location);
								gotSupply = false;
								autoSupplyTransfer = true;
								return;
							}
						}
					}
					friendlyRobots = rc.senseNearbyRobots(99, rc.getTeam());
					int minDistance = 999999;
					for (RobotInfo r : friendlyRobots) {
						int distance = myLocation.distanceSquaredTo(r.location);
						if (r.type == RobotType.LAUNCHER && distance < minDistance) {
							followingLocation = r.location;
							minDistance = distance;
						}
					}
					if (minDistance == 999999) {
						autoSupplyTransfer = true;
						return;
					}
				}
				if (rc.isCoreReady()) {
					navigation.moveToDestination(followingLocation, Navigation.AVOID_ALL);
				}
			}
			else {
				if (rc.getSupplyLevel() > 6000) {
					gotSupply = true;
				}
				else if (myLocation.distanceSquaredTo(ownHQ) <= 15) {
					rc.broadcast(Broadcast.requestSupplyFromHQCh, rc.getID());
				}
				else if (rc.isCoreReady()) {
					navigation.moveToDestination(ownHQ, Navigation.AVOID_ALL);
				}
			}
			return;
		}
		followingID = rc.readBroadcast(Broadcast.requestSupplyDroneCh);
		if (followingID != 0) {
			rc.broadcast(Broadcast.requestSupplyDroneCh, 0);
			if (rc.canSenseRobot(followingID)) {
				followingLocation = rc.senseRobot(followingID).location;
				autoSupplyTransfer = false;
				gotSupply = false;
			}
		}
		// Move
		else if (rc.isCoreReady()) {
			navigation.moveToDestination(ownHQ, Navigation.AVOID_ALL);
		}
	}
}
