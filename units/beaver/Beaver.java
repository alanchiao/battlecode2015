package team158.units.beaver;

import team158.units.beaver.Builder;
import team158.utils.DirectionHelper;
import team158.com.Broadcast;
import team158.units.Unit;
import battlecode.common.*;

public class Beaver extends Unit {
	
	public Builder builder;
	
	public Beaver(RobotController newRC) {
		super(newRC);
		this.builder = new Builder(rc, ownHQ, navigation);
	}

	@Override
	protected void actions() throws GameActionException {
		if (rc.isCoreReady()) {
			// continue building current building
			if (builder.isNavigating) {
				builder.continueNavigating();
				return;
			} else if (!builder.isBuildingComplete()){
				return;
			}
			builder.buildingLocation = null;
			
			double ore = rc.getTeamOre();
			int numMinerFactories = rc.readBroadcast(Broadcast.numMinerFactoriesCh);
			int numHelipads = rc.readBroadcast(Broadcast.numHelipadsCh);
			int numSupplyDepots = rc.readBroadcast(Broadcast.numSupplyDepotsCh);
			int numAerospaceLabs = rc.readBroadcast(Broadcast.numAerospaceLabsCh);
			int numBarracks = rc.readBroadcast(Broadcast.numBarracksCh);
			int estimatedSupplyNeeded = rc.readBroadcast(Broadcast.numLaunchersCh) * 25;

			if (numMinerFactories == 0) {
				if (ore >= 500) {
					builder.buildBuilding(RobotType.MINERFACTORY, numMinerFactories);
				} else {
					rc.mine();
				}
			}
			else if (numBarracks == 0) {
				if (ore >= 300) {
					builder.buildBuilding(RobotType.BARRACKS, numBarracks);
				} else {
					rc.mine();
				}
			}
			else if (numSupplyDepots == 0) {
				if (ore >= 100) {
					builder.buildBuilding(RobotType.SUPPLYDEPOT, numSupplyDepots);
				} else {
					rc.mine();
				}
			}
			else if (numHelipads == 0) {
				if (ore >= 300) {
					builder.buildBuilding(RobotType.HELIPAD, numHelipads);
				} else {
					rc.mine();
				}
			}
			else {
				if (numAerospaceLabs == 0) {
					if (ore >= 500) {
						builder.buildBuilding(RobotType.AEROSPACELAB, numAerospaceLabs);
					}
				}
				else if (numSupplyDepots < 3 && ore >= 100) {
					builder.buildBuilding(RobotType.SUPPLYDEPOT, numSupplyDepots);
				}
				else if (numAerospaceLabs == 2 && numSupplyDepots < 6 && ore >= 100) {
					builder.buildBuilding(RobotType.SUPPLYDEPOT, numSupplyDepots);
				}
				else if (ore >= 700) {
					builder.buildBuilding(RobotType.AEROSPACELAB, numAerospaceLabs);
				}
				else if (estimatedSupplyNeeded > 200 + 100 * Math.pow(numSupplyDepots, 0.6)) {
					builder.buildBuilding(RobotType.SUPPLYDEPOT, numSupplyDepots);
				}
				else {
					rc.mine();
				}
			}
		}
	}
}