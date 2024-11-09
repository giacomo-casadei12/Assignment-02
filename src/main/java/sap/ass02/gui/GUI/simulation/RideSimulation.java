package sap.ass02.gui.GUI.simulation;

import sap.ass02.gui.GUI.EBikeApp;

import javax.swing.*;

/**
 * Simulation of a Ride.
 */
public class RideSimulation extends Thread {

	private static final int DEFAULT_POSITION_VALUE = 200;
	private static final int DEFAULT_TIME_TO_CHANGE_DIRECTION = 500;
	private final int bikeId;


    private P2d loc;
	private V2d direction = new V2d(1,0);

    private final EBikeApp app;
	private volatile boolean stopped;

	/**
	 * Instantiates a new Ride simulation.
	 *
	 * @param bikeId the bike id of the chosen bike
	 * @param bikeX  the starting x coordinate of the bike
	 * @param bikeY  the starting y coordinate of the bike
	 * @param app    the EBikeApp
	 */
	public RideSimulation(int bikeId, int bikeX, int bikeY, EBikeApp app) {
		this.bikeId = bikeId;
		this.loc = new P2d(bikeX, bikeY);
        this.app = app;
		stopped = false;
	}

	@Override
	public void run() {
        double speed = 3;

		var lastTimeChangedDir = System.currentTimeMillis();
		
		while (!stopped) {
			/* update pos */
			this.loc = this.loc.sum(this.direction.mul(speed));
			updatePosition();

			/* change dir randomly */

			changeDirection(lastTimeChangedDir);

			this.app.requestUpdateRide(this.bikeId, (int) Math.round(this.loc.x()), (int) Math.round(this.loc.y())).onComplete(x -> {
				if (x.result() != null) {
					if (x.result().first() <= 0 || x.result().second() <= 0) {
						this.stopped = true;
						interrupt();
						JOptionPane.showMessageDialog(app, "Credit emptied or dead battery", "End ride", JOptionPane.WARNING_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(app, "Failed to update ride", "Fail", JOptionPane.ERROR_MESSAGE);
				}
			});

			try {
				Thread.sleep(2000);
			} catch (Exception ignored) {}
			
		}
		this.app.requestEndRide(this.bikeId).onComplete(x -> {
			if (!x.result()) {
				JOptionPane.showMessageDialog(app, "Failed to stop ride", "Fail", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	private void updatePosition() {
		if (this.loc.x() > DEFAULT_POSITION_VALUE || this.loc.x() < -DEFAULT_POSITION_VALUE) {
			this.direction = new V2d(-this.direction.x(), this.direction.y());
			if (this.loc.x() > DEFAULT_POSITION_VALUE) {
				this.loc = new P2d(DEFAULT_POSITION_VALUE, this.loc.y());
			} else {
				this.loc = new P2d(-DEFAULT_POSITION_VALUE, this.loc.y());
			}
		}
		if (this.loc.y() > DEFAULT_POSITION_VALUE || this.loc.y() < -DEFAULT_POSITION_VALUE) {
			this.direction = new V2d(this.direction.x(), -this.direction.y());
			if (this.loc.y() > DEFAULT_POSITION_VALUE) {
				this.loc = new P2d(this.loc.x(), DEFAULT_POSITION_VALUE);
			} else {
				this.loc = new P2d(this.loc.x(), -DEFAULT_POSITION_VALUE);
			}
		}
	}

	private void changeDirection(long lastTimeChangedDir) {
		var elapsedTimeSinceLastChangeDir = System.currentTimeMillis() - lastTimeChangedDir;
		if (elapsedTimeSinceLastChangeDir > DEFAULT_TIME_TO_CHANGE_DIRECTION) {
			double angle = Math.random()*60 - 30;
			this.direction = this.direction.rotate(angle);
}
	}

	/**
	 * Stop the simulation.
	 */
	public void stopSimulation() {
		stopped = true;
		interrupt();
	}

}
