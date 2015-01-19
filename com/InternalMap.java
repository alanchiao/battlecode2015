package team158.com;

import team158.utils.Broadcast;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class InternalMap {
	
	// purposes of internal map
	
	// navigation: consistently know where walls are
	// navigation: consistently know where to avoid for enemy towers
	// 
	
	private RobotController rc;
	
	private MapLocation hqLocation;
	
	private int[][] internalMap; // 1 if has wall, 0 if it doesn't
	private final static int internalMapWidth = 120;
	private final static int internalMapHeight = 120;
	private int quadrantNumber; // 1 = NE, 2 = NW, 3 = SW, 4 = SE
	
	public InternalMap(RobotController rc, MapLocation hqLocation, MapLocation enemyHQ) {
		this.hqLocation = hqLocation;
		this.rc = rc;
		this.internalMap = new int[internalMapHeight][internalMapWidth];
		
		// select quadrant to read from
		if (enemyHQ.y > hqLocation.y) {
			if (enemyHQ.x > hqLocation.x) {
				quadrantNumber = 1;
			} else {
				quadrantNumber = 2;
			}
		} else {
			if (enemyHQ.x > hqLocation.x) {
				quadrantNumber = 4;
			} else {
				quadrantNumber = 3;
			}
		}
		System.out.println(Integer.toString(quadrantNumber));
	}
			
	public void printMapRepresentation() {
		System.out.println("MAP REPRESENTATION");
		for (int y = internalMapHeight - 1; y >= 0; y--) {
			for (int x = internalMapWidth - 1; x >= 0; x--) {
				System.out.print(Integer.toString(internalMap[x][y]));
			}
			System.out.println("");
		}
	}
	
	public void readMap() throws GameActionException {
		if (quadrantNumber == 1) {
			for (int x = 0; x < internalMapWidth; x++) {
				for (int y = 0; y < internalMapHeight; y++) {
					internalMap[x][y] = readLocation(new MapLocation(hqLocation.x + x, hqLocation.y + y));
				}
			}
		} else if (quadrantNumber == 2) {
			for (int x = 0; x < internalMapWidth; x++) {
				for (int y = 0; y < internalMapHeight; y++) {
					internalMap[internalMapWidth - 1 - x][y] = readLocation(new MapLocation(hqLocation.x - x, hqLocation.y + y));
				}
			}
		} else if (quadrantNumber == 3) {
			for (int x = 0; x < internalMapWidth; x++) {
				for (int y = 0; y < internalMapHeight; y++) {
					internalMap[internalMapWidth - 1 - x][internalMapHeight - 1 - y] = readLocation(new MapLocation(hqLocation.x - x, hqLocation.y - y));
				}
			}
		} else {
			for (int x = 0; x < internalMapWidth; x++) {
				for (int y = 0; y < internalMapHeight; y++) {
					internalMap[x][internalMapHeight - 1 - y] = readLocation(new MapLocation(hqLocation.x + x, hqLocation.y - y));
				}
			}
		}
	}
	
	public int readLocation(MapLocation location) throws GameActionException {
		int dx = location.x - hqLocation.x;
		int dy = location.y - hqLocation.y;
		return rc.readBroadcast(Broadcast.internalMapHQCh + dy * 259 + dx);
	}
	
	public void broadcastLocation(MapLocation location, int hasWall) throws GameActionException {
		int dx = location.x - hqLocation.x;
		int dy = location.y - hqLocation.y;
		rc.broadcast(Broadcast.internalMapHQCh + dy * 259 + dx, hasWall);
	}
}
