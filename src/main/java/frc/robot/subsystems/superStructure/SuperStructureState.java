package frc.robot.subsystems.superStructure;

public enum SuperStructureState {
  IDLE, //
  DISABLED, //
  AUTO_CONTROLLED, //
  INTAKING, //
  INTAKING_DEPOT, //
  OUTTAKING, //
  SHOOTING_MANUAL, //
  SHOOTING_ASSISTED, //
  DELIVERY_MANUAL, //
  DELIVERY_ASSISTED, //
  GOING_OVER_BUMP, //
  GOING_UNDER_TRENCH, //
  EXTENDED, //
  RETRACTED; //

  public boolean isShootingState() {
    return this == SHOOTING_MANUAL || this == SHOOTING_ASSISTED;
  }

  public boolean isDeliveryState() {
    return this == DELIVERY_MANUAL || this == DELIVERY_ASSISTED;
  }

  public boolean isShooterActiveState() {
    return isShootingState() || isDeliveryState();
  }
}
