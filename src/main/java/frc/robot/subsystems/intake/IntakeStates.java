package frc.robot.subsystems.intake;

import static frc.robot.generated.intake.IntakeConstants.extensionConstants;

public class IntakeStates {


  public static enum IntakeState {
    INTAKING(3500, 7.552), INTAKING_DEPOT(1888.8888888, 4.224), HOLDING(500, 1.3), OUTTAKING(-2000,
        -4.608), PULSING(1000, 2.3), IDLE(0, 0);

    public final double velocity;
    public final double voltage;

    IntakeState(double velocity, double voltage) {
      this.velocity = velocity;
      this.voltage = voltage;
    }
  }

  public static enum ExtensionState {
    RETRACTED(extensionConstants.mecMovementConstants.extensionMinMovement), EXTENDED(
        extensionConstants.mecMovementConstants.extensionMaxMovement), EXTENDED_DEPOT(
            extensionConstants.mecMovementConstants.extensionMaxMovement - 0.1), PULSING(
                0), IDLE(0);

    public final double extension;

    ExtensionState(double extension) {
      this.extension = extension;
    }

  }
}
