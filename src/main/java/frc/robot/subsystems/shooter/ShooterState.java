package frc.robot.subsystems.shooter;

import java.util.function.Supplier;
import frc.robot.subsystems.shooter.shootingModel.DataTypes.SweetSpot;
import frc.robot.subsystems.shooter.shootingModel.SweetSpots;

public enum ShooterState {
  SHOOTING(SweetSpots.hubSweetSpots::InterpolateOptimalVelocities), //
  DELIVERY(SweetSpots.deliverySweetSpots::InterpolateOptimalVelocities), //
  MANUAL_SHOOTING(() -> SweetSpots.tower), //
  MANUAL_DELIVERY(() -> SweetSpots.defaultDelivery), //
  HOLDING_VELOCITY(() -> SweetSpot.kZero), //
  IDLE(() -> SweetSpot.kZero);

  public final Supplier<SweetSpot> sweetSpot;

  private ShooterState(Supplier<SweetSpot> sweetSpot) {
    this.sweetSpot = sweetSpot;
  }
}
