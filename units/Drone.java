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
	RobotType followingType;

	public Drone(RobotController newRC) {
		super(newRC);
	}

	@Override
	protected void actions() throws GameActionException {
		if (!autoSupplyTransfer) {
			MapLocation myLocation = rc.getLocation();
			if (gotSupply) {
				if (myLocation.distanceSquaredTo(followingLocation) <= 15) {
					RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
					if (friendlyRobots.length > 0) {
						for (RobotInfo r : friendlyRobots) {
							if (r.type == followingType) {
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
						if (r.type == followingType && distance < minDistance) {
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
					computeStuff();
					navigation.moveToDestination(followingLocation, Navigation.AVOID_ALL);
				}
			}
			else {
				if (rc.getSupplyLevel() > (followingType == RobotType.LAUNCHER ? 15000 : 3000)) {
					gotSupply = true;
				}
				else if (rc.isCoreReady() && myLocation.distanceSquaredTo(ownHQ) > 15) {
					computeStuff();
					navigation.moveToDestination(ownHQ, Navigation.AVOID_ALL);
				}
			}
			return;
		}
		followingID = rc.readBroadcast(Broadcast.requestSupplyDroneCh);
		if (followingID != 0) {
			rc.broadcast(Broadcast.requestSupplyDroneCh, 0);
			if (rc.canSenseRobot(followingID)) {
				RobotInfo robot = rc.senseRobot(followingID);
				followingLocation = robot.location;
				followingType = robot.type;
				autoSupplyTransfer = false;
				gotSupply = false;
			}
		}
		// Move
		else if (rc.isCoreReady()) {
			computeStuff();
			navigation.moveToDestination(ownHQ, Navigation.AVOID_ALL);
		}
	}
}
