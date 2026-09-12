package frc.robot.subsystems.superStructure;

import static frc.robot.RobotContainer.*;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.subsystems.intake.IntakeStates.ExtensionState;
import frc.robot.subsystems.intake.IntakeStates.IntakeState;
import frc.robot.subsystems.shooter.shootingModel.DataTypes.SweetSpot;
import frc.robot.subsystems.shooter.shootingModel.SweetSpots;
import frc.robot.util.LoggedTracer;
import lombok.experimental.ExtensionMethod;
import miscar.util.GenericHIDUtill;
import org.littletonrobotics.junction.Logger;

@ExtensionMethod(GenericHIDUtill.class)
public class ButtonsCommand {

  SuperStructure superStructure;

  public ButtonsCommand(SuperStructure superStructure) {
    this.superStructure = superStructure;
  }

  private static class Actions {

    public static boolean IDLE;

    public static boolean resetPose;

    public static boolean clearShooter;
    public static boolean resetSweetspot;

    public static boolean CHANGE_AUTOMATION_LEVEL;

    public static boolean SHOOT;
    public static boolean PULSE;

    public static boolean intakeCurrentlyPressed;
    public static boolean intakeWasPressed;
    public static boolean intakeWasRelesed;
    public static boolean OUTTAKE;
    public static boolean CLOSE_INTAKE;
    public static boolean CLOSE_INTAKE_WASPRESSED = false;

    private static boolean wasIntaking = false;
    private static Timer intakeTimer = new Timer();
    private static double timeToIntakeDepot = 0.32;
    public static AutomationLevel automationLevel = AutomationLevel.ASSISTED;

    public static SuperStructureState wantedState = SuperStructureState.IDLE;

    public static void updateButtons() {
      IDLE = driver.getCrossButton();

      resetPose = driver.povUp();

      CHANGE_AUTOMATION_LEVEL = driver.getSquareButtonPressed();

      SHOOT = driver.getR1ButtonPressed();

      intakeWasPressed = driver.getR2ButtonPressed();
      intakeWasRelesed = driver.getR2ButtonReleased();
      intakeCurrentlyPressed = driver.getR2Button();
      OUTTAKE = driver.getL2ButtonPressed();
      CLOSE_INTAKE = driver.povDown();
      clearShooter = driver.povLeft();
      resetSweetspot = driver.povRight();

      PULSE = driver.getL1ButtonPressed();
    }


    private static void updateWantedStateByButtons() {
      changeAutomationLevel();

      changeWantedStateByAutoLevel();
    }

    private static void updateStateByButtons(SuperStructure superStructure) {
      SuperStructureState newState = SuperStructureState.IDLE;

      if (superStructure.getState().isShootingState() && PULSE) {
        superStructure.overrideIntake = false;
        superStructure.intake.setPulse(true);
      } else if (!superStructure.getState().isShootingState()) {
        superStructure.intake.setPulse(false);
      }
      if (clearShooter) {
        superStructure.clearShooter = true;
      } else {
        superStructure.clearShooter = false;
      }
      if (intakeCurrentlyPressed) {
        if (intakeWasPressed) {
          if (superStructure.intake.getIntakeState() == IntakeState.INTAKING) {
            wasIntaking = true;
          } else {
            wasIntaking = false;
          }
        }
        if (!intakeTimer.hasElapsed(timeToIntakeDepot)) {
          if (!superStructure.getState().isShootingState())
            newState = SuperStructureState.INTAKING;
          superStructure.setIntakeOverride(IntakeState.INTAKING, ExtensionState.EXTENDED);
        } else {
          if (!superStructure.getState().isShootingState())
            newState = SuperStructureState.INTAKING_DEPOT;
          superStructure.setIntakeOverride(IntakeState.INTAKING, ExtensionState.EXTENDED_DEPOT);
        }
      } else if (intakeWasRelesed) {
        if (wasIntaking && !intakeTimer.hasElapsed(timeToIntakeDepot)) {
          superStructure.setOverrideIntake(false);
          if (!superStructure.getState().isShootingState())
            newState = SuperStructureState.EXTENDED;
        } else {
          if (!superStructure.getState().isShootingState())
            newState = SuperStructureState.INTAKING;
          superStructure.setIntakeOverride(IntakeState.INTAKING, ExtensionState.EXTENDED);
        }
      } else {
        intakeTimer.restart();
      }

      if (OUTTAKE) {
        if (superStructure.intake.getIntakeState() != IntakeState.OUTTAKING) {
          superStructure.setIntakeOverride(IntakeState.OUTTAKING, ExtensionState.EXTENDED);
          if (!superStructure.getState().isShootingState())
            newState = SuperStructureState.OUTTAKING;
        } else {
          superStructure.setOverrideIntake(false);
          if (!superStructure.getState().isShootingState())
            newState = SuperStructureState.EXTENDED;
        }
      }

      if (SHOOT) {
        superStructure.setOverrideIntake(false);
        if (!superStructure.getState().isShootingState()) {
          newState = SuperStructureState.SHOOTING_MANUAL;
        } else {
          newState = superStructure.intake.getExtensionState() == ExtensionState.RETRACTED
              || (superStructure.intake.getExtensionState() == ExtensionState.RETRACTED
                  && superStructure.intake.isPulse()) ? SuperStructureState.RETRACTED
                      : SuperStructureState.EXTENDED;
        }
      }

      if (CLOSE_INTAKE) {
        if (!CLOSE_INTAKE_WASPRESSED) {

          if (superStructure.getOverrideExtensionState() != ExtensionState.RETRACTED)
            superStructure.setIntakeOverride(IntakeState.IDLE, ExtensionState.RETRACTED);
          else
            superStructure.setOverrideIntake(false);
        }
        CLOSE_INTAKE_WASPRESSED = true;

      } else {
        CLOSE_INTAKE_WASPRESSED = false;
      }

      if (resetSweetspot) {
        SweetSpots.hubSweetSpots.getM_map().replace(3.909216, new SweetSpot(2050, 2300));
        SweetSpots.hubSweetSpots.getM_map().put(3.8, new SweetSpot(2300, 2300));
      }

      newState = changeStateByAutoLevel(newState);
      if (newState != SuperStructureState.IDLE)
        superStructure.setState(newState);
    }

    private static void changeWantedStateByAutoLevel() {
      wantedState = changeStateByAutoLevel(wantedState);
    }

    private static SuperStructureState changeStateByAutoLevel(SuperStructureState state) {
      if (state.isShootingState()) {
        switch (automationLevel) {
          case MANUAL:
            state = SuperStructureState.SHOOTING_MANUAL;
            break;
          case ASSISTED:
            state = SuperStructureState.SHOOTING_ASSISTED;
            break;

          default:
            break;
        }
      }
      return state;
    }

    public static void changeAutomationLevel() {
      if (CHANGE_AUTOMATION_LEVEL) {
        automationLevel = automationLevel.advance();
      }
    }


    private static boolean isStateDrivable(SuperStructure superStructure) {
      return superStructure.getState() == SuperStructureState.INTAKING
          || superStructure.getState() == SuperStructureState.OUTTAKING
          || superStructure.getState() == SuperStructureState.EXTENDED
          || superStructure.getState() == SuperStructureState.RETRACTED;
    }



  }



  public void periodic() {
    Actions.updateButtons();
    resetPoseLogic();
    LoggedTracer.reset();
    if (!DriverStation.isTeleop()) {
      Actions.changeAutomationLevel();
      Actions.changeWantedStateByAutoLevel();
      Logger.recordOutput("DriveCommand/automationLevel", Actions.automationLevel);

      LoggedTracer.record("DriveCommand");
      return;
    }


    Actions.updateWantedStateByButtons();
    Actions.updateStateByButtons(superStructure);

    idleLogic();

    Logger.recordOutput("DriveCommand/automationLevel", Actions.automationLevel);
    LoggedTracer.record("DriveCommand");
  }

  private void resetPoseLogic() {
    if (Actions.resetPose) {
      superStructure.drive.restPoseByVision();
    }
  }

  private void idleLogic() {
    if (Actions.IDLE) {
      superStructure.setOverrideIntake(false);
      superStructure.setLockState(false);
      superStructure.setState(SuperStructureState.RETRACTED);
      return;
    }

    boolean isDrive = Math.abs(driver.getRightX()) > 0.2;

    boolean idleFromDrive = (isDrive && !Actions.isStateDrivable(superStructure));

    if (idleFromDrive) {
      if (superStructure.getClosestHomeIntakeState() == ExtensionState.RETRACTED)
        superStructure.setState(SuperStructureState.RETRACTED);
      else {
        superStructure.setState(SuperStructureState.EXTENDED);
      }
    }
  }
}
