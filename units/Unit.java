package battlecode2015.units;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode2015.Robot;
import battlecode2015.units.com.Navigation;
import battlecode2015.utils.Broadcast;
import battlecode2015.utils.DirectionHelper;

public abstract class Unit extends Robot {
	// stored information about reaching a destination
	// for navigation
	public int groupID = -1;
	public MapLocation destinationPoint; // desired point to reach
	public boolean isAvoidingObstacle = false; // whether in state of avoiding obstacle
	public MapLocation lastObstacle; // obstacle tile to move relative to
	public MapLocation lastLocation; // unit's location in previous step
	public Direction lastDirectionMoved = null;
	public Direction origDirection = null; // original direction of collision of robot into obstacle
	
	
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
			if (broadcastCh != -1 && rc.readBroadcast(700) !=1) {
				groupID = rc.readBroadcast(broadcastCh);
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
			boolean toldToAttack = rc.readBroadcast(groupID) == 1;
//			System.out.println(groupID);
//			System.out.println("toldToAttack = " + toldToAttack);
			
			MapLocation target;
			if (toldToAttack) {
				target = rc.senseEnemyHQLocation();
			}
			else {
				int loc = rc.readBroadcast(Broadcast.soldierRallyCh);
				target = new MapLocation(loc / 65536, loc % 65536);
			}
			this.destinationPoint = target;
			Navigation.moveToDestinationPoint(rc, this);
			
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
