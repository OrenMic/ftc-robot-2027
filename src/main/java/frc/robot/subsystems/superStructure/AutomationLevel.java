package frc.robot.subsystems.superStructure;


public enum AutomationLevel {
  MANUAL, ASSISTED, AUTO;

  public AutomationLevel advance() {
    switch (this) {
      case MANUAL:
        return ASSISTED;
      case ASSISTED:
        return MANUAL;

      default:
        return MANUAL;
    }
  }
}
