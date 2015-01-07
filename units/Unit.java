package battlecode2015.units;
import battlecode.common.*;
import battlecode2015.Robot;
import battlecode2015.utils.*;

public abstract class Unit extends Robot {
	// stored information about reaching a destination
	// for navigation
	public int groupID;
	public MapLocation destinationPoint;
	public Direction lastDirectionMoved;
	boolean avoidingObstacle = false;
	
	public void move() {
		try {
			int mySupply = (int) rc.getSupplyLevel();
			if (mySupply > rc.getType().supplyUpkeep * 100) {
				RobotInfo[] friendlyRobots = rc.senseNearbyRobots(15, rc.getTeam());
				for (RobotInfo r : friendlyRobots) {
					if (r.supplyLevel < r.type.supplyUpkeep * 50) {
						rc.transferSupplies(Math.min(mySupply / 2, r.type.supplyUpkeep * 200), r.location);
					}
				}
			}
			int broadcastCh = -1;
			if (rc.getType() == RobotType.SOLDIER) {
				broadcastCh = Broadcast.groupingSoldiersCh;
			}
			else if (rc.getType() == RobotType.DRONE) {
				broadcastCh = Broadcast.groupingDronesCh;
			}
			else if (rc.getType() == RobotType.BASHER) {
				broadcastCh = Broadcast.groupingBashersCh;
			}
			if (broadcastCh == -1) {
				int groupInfo = rc.readBroadcast(broadcastCh);
				int ID = groupInfo/100;
				int size = groupInfo %100;
				if (size > 0) {
					groupID = ID;
					rc.broadcast(broadcastCh, ID*100+size);
				}
			}
			

			
			actions();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}

	protected MapLocation selectTarget(RobotInfo[] enemies) {
		MapLocation target = null;
		double maxPriority = 0;
		for (RobotInfo r : enemies) {
			if (1 / r.health > maxPriority) {
				maxPriority = 1 / r.health;
				target = r.location;
			}
		}
		return target;
	}
	
	public void moveByGroup() {
		try {
			int loc = rc.readBroadcast(groupID);
			MapLocation target = new MapLocation(loc / 65536, loc % 65536);
			
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
		} 
		catch (GameActionException e) {
			return;
		}
	}

}
