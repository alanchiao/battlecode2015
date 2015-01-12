package team158.buildings;
import team158.Robot;

public abstract class Building extends Robot {
	protected double hqDistance = 0;

	@Override
	public void move() {
		try {
			// Compute hqDistance
			if (hqDistance == 0) {
				hqDistance = Math.sqrt(rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation()));
			}
			actions();
		}
		catch (Exception e) {
			System.out.println(rc.getType());
            e.printStackTrace();
		}
	}
}
