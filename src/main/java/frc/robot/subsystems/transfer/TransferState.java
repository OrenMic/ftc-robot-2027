package frc.robot.subsystems.transfer;


public enum TransferState {
  SHOOTING(2500), //
  PROCESSING(0), //
  UNLOADING(-2500), //
  IDLE(0), CLEARING(-1000);

  public final double beltVelocity;

  private TransferState(double beltVelocity) {
    this.beltVelocity = beltVelocity;
  }
}
