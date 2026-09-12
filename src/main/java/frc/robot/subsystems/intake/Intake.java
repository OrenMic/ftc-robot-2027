package frc.robot.subsystems.intake;

import static frc.robot.generated.intake.IntakeConstants.*;
import static frc.robot.Tuners.Intake.*;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.generated.ports.Ports;
import frc.robot.subsystems.intake.IntakeStates.ExtensionState;
import frc.robot.subsystems.intake.IntakeStates.IntakeState;
import frc.robot.util.EqualsUtil;
import frc.robot.util.LoggedTracer;
import lombok.Getter;
import lombok.Setter;
import miscar.annotation.CreateConstants;
import miscar.configs.mecs.Movement;
import miscar.configs.mecs.Ratios;
import miscar.configs.motors.MotorIOConfig;
import miscar.configs.motors.SimConfig;
import miscar.configs.motors.TalonFXConfig;
import miscar.mecsIOs.Mechanism;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;
import miscar.motorIOs.MotorIOTalonFX;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;

public class Intake extends SubsystemBase {
  {
    setName("Intake");
  }

  public Mechanism intakeLeft;

  public Mechanism intakeRight;

  @CreateConstants(
      configType = {TalonFXConfig.class, Movement.class, Ratios.class, SimConfig.class})
  public Mechanism extension;

  @Getter
  private IntakeState intakeState = IntakeState.IDLE;
  private IntakeState lastIntakeState = intakeState;

  @Getter
  private ExtensionState extensionState = ExtensionState.IDLE;
  private ExtensionState lastExtensionState = extensionState;

  private boolean startPulseUp = false;
  private boolean pulseUp = !startPulseUp;
  private double pulseTimer = 0;

  @Getter
  @Setter
  private boolean pulse = false;

  double pulseStartTime = 0;

  boolean shouldPulse = true;

  boolean hasReset = false;

  LoggedNetworkBoolean disintegrateIntake =
      new LoggedNetworkBoolean(getName() + "/disintegrate/Intake", false);
  LoggedNetworkBoolean disintegrateExtension =
      new LoggedNetworkBoolean(getName() + "/disintegrate/Extension", false);

  LoggedNetworkBoolean setCoast = new LoggedNetworkBoolean(getName() + "/setCoast", false);


  public Intake(MotorIOConfig intakeConfig, Mechanism extension) {
    this.intakeLeft = Mechanism.create(intakeConfig);

    this.intakeRight = Mechanism.create(intakeConfig.withMotorPort(Ports.intakeRightMotorPort)
        .withInverted(!intakeConfig.getInverted()));

    this.extension = extension;

    extension.setRatio(extensionConstants.mecRatiosConstants.config);

    extension.setMinMovement(extensionConstants.mecMovementConstants.extensionMinMovement);
    extension.setMaxMovement(extensionConstants.mecMovementConstants.extensionMaxMovement);
    extension.setStartPose(extensionConstants.mecMovementConstants.extensionMinMovement);


    intakeLeft.setRatio(intakeConstants.mecRatiosConstants.config);
    intakeLeft.setMaxVelocity(intakeConstants.mecMovementConstants.intakeMaxVelocity);
    intakeRight.setRatio(intakeConstants.mecRatiosConstants.config);
    intakeRight.setMaxVelocity(intakeConstants.mecMovementConstants.intakeMaxVelocity);

    extension.setName(getName() + "/Extension");
    intakeLeft.setName(getName() + "/IntakeLeft");
    intakeRight.setName(getName() + "/IntakeRight");
  }

  public void setState(IntakeState wantedIntakeState, ExtensionState wantedExtensionState) {
    manageIntakingLogic(wantedIntakeState);
    manageExtensionLogic(wantedExtensionState);
  }

  private void manageExtensionLogic(ExtensionState wantedExtensionState) {
    extensionState = wantedExtensionState;

    if ((extensionState != ExtensionState.EXTENDED && extensionState != ExtensionState.RETRACTED)
        || (lastIntakeState != intakeState) || lastExtensionState != extensionState) {
      hasReset = false;
    }

    switch (extensionState) {
      case IDLE:
        stopExtension();
        break;

      case PULSING:
        pulseFast();
        // pulse();
        // openLoopPulse();
        break;

      case RETRACTED:
        retract();
        break;
      case EXTENDED:
        extend();
        break;

      default:

        extension.setTargetPosition(extensionState.extension + extensionOffset.getAsDouble());
        break;
    }
  }

  private void manageIntakingLogic(IntakeState wantedIntakeState) {
    intakeState = wantedIntakeState;
    if (intakeState == IntakeState.IDLE) {
      stopIntake();
      return;
    }
    setIntakeVoltage(intakeState.voltage + intakeVelocityOffset.getAsDouble());
    // setIntakeVelocity(intakeState.velocity +
    // intakeVelocityOffset.getAsDouble());
  }



  private void stopIntake() {
    setIntakePower(0);
  }

  private void stopExtension() {
    extension.setPower(0);
  }

  private void keepExtensionPose() {
    extension.setTargetPosition(extension.getMecPosition());
  }


  private void openLoopPulse() {
    if (extensionState != ExtensionState.PULSING) {
      return;
    }

    if (lastExtensionState != extensionState) {
      pulseStartTime = Logger.getTimestamp() / 1_000_000.0;
    }

    if (Logger.getTimestamp() / 1_000_000.0 - pulseStartTime < pulseDelay.getAsDouble() && !pulse) {
      return;
    }

    if (extension.getMecPosition() < extensionConstants.mecMovementConstants.extensionMinMovement
        + extensionsStopOffset.getAsDouble()) {
      keepExtensionPose();
      return;
    }
    extension.setPower(-extensionPulsePower.getAsDouble());
  }

  private void pulseFast() {
    if (extensionState != ExtensionState.PULSING || !pulse) {
      return;
    }

    boolean enoughTimePassed = Logger.getTimestamp() * 1e-6 - pulseTimer > pulseDelay.getAsDouble();

    if (enoughTimePassed) {
      pulseUp = !pulseUp;
      pulseTimer = Logger.getTimestamp() * 1e-6;
    }

    hasReset = false;
    if (pulseUp) {
      // retract();
      extension.setTargetPosition(extensionState.extension);
      Logger.recordOutput(getName() + "/pulseFast", "retract");
    } else {
      extend();
      Logger.recordOutput(getName() + "/pulseFast", "extend");
    }
  }

  private void pulse() {
    if (extensionState != ExtensionState.PULSING) {
      return;
    }

    if (lastExtensionState != extensionState) {
      pulseStartTime = Logger.getTimestamp() / 1_000_000.0;
    }

    if (Logger.getTimestamp() / 1_000_000.0 - pulseStartTime < pulseDelay.getAsDouble() && !pulse) {
      return;
    }

    double middle = extensionConstants.mecMovementConstants.extensionMaxMovement / 2.0;
    double openPulsePose = middle + pulseExtensionOffset.getAsDouble();
    double closePulsePose = middle - pulseExtensionOffset.getAsDouble();

    boolean enoughTimePassed = Logger.getTimestamp() * 1e-6 - pulseTimer > pulseDelay.getAsDouble();

    if (enoughTimePassed) {
      pulseUp = !pulseUp;
      pulseTimer = Logger.getTimestamp() * 1e-6;
    }

    extension.setTargetPosition(pulseUp ? closePulsePose : openPulsePose);
  }

  private void retract() {
    if (!hasReset) {
      if (MathUtil.isNear(extensionState.extension, extension.getMecPosition(), 0.01)) {
        extension.setCurrentPose(extensionState.extension);
        hasReset = true;
      } else if (Math.abs(extension.inputs.currentAmps) >= 30) {
        extension.setCurrentPose(extension.getMecPosition() + 0.05);
        hasReset = true;
      }
    }
    extension.setTargetPosition(extensionState.extension);
  }

  private void extend() {
    extension.setPower(!hasReset ? 0.595 : 0);
    if (extension.inputs.currentAmps > resetAmps.getAsDouble()
        && extension.getMecVelocity() < resetSpeed.getAsDouble()
        && extension.getMecVelocity() >= 0) {
      extension.setCurrentPose(IntakeStates.ExtensionState.EXTENDED.extension);
      hasReset = true;
    }

  }

  public boolean isConnected() {
    return intakeLeft.inputs.connected && intakeRight.inputs.connected
        && extension.inputs.connected;
  }

  private void setIntakeVoltage(double voltage) {
    if (EqualsUtil.epsilonEquals(voltage, 0)) {
      setIntakePower(0);
    } else {
      intakeRight.motorIODelegation.setVoltage(voltage);
      intakeLeft.motorIODelegation.setVoltage(voltage);
    }
  }

  // private void setIntakeVelocity(double velocity) {
  // if (EqualsUtil.epsilonEquals(velocity, 0)) {
  // setIntakePower(0);
  // } else {
  // setIntakeClosedLoopSlot(velocity < 1350 ? 1 : 0);
  // leaderIntake.setTargetMecVelocity(velocity);
  // // intakeLeft.setTargetMecVelocity(velocity);
  // // intakeRight.setTargetMecVelocity(velocity);
  // }
  // }

  // private void setIntakeClosedLoopSlot(int slot) {
  // if (intakeLeft.motorIODelegation instanceof MotorIOTalonFX motor) {
  // motor.setClosedLoopSlot(slot);
  // }

  // if (intakeRight.motorIODelegation instanceof MotorIOTalonFX motor)
  // {
  // motor.setClosedLoopSlot(slot);
  // }
  // }

  private void setExtentionClosedLoopSlot(int slot) {
    if (extension.motorIODelegation instanceof MotorIOTalonFX motor) {
      motor.setClosedLoopSlot(slot);
    }
  }

  private void setIntakePower(double power) {
    intakeRight.setPower(power);
    intakeLeft.setPower(power);
  }

  public void setNaturalMode(NeutralMode neutralMode) {
    if (intakeLeft.motorIODelegation instanceof MotorIOTalonFX motor) {
      motor.setNaturalMode(neutralMode);
    }
    if (intakeRight.motorIODelegation instanceof MotorIOTalonFX motor) {
      motor.setNaturalMode(neutralMode);
    }
  }

  public void setExtensionNaturalMode(NeutralMode neutralMode) {
    if (extension.motorIODelegation instanceof MotorIOTalonFX motor) {
      motor.setNaturalMode(neutralMode);
    }
  }

  public void disintegrate() {
    disintegrateIntake();
    disintegrateExtension();
  }

  public void disintegrateIntake() {
    stopIntake();

    intakeLeft.disintegrate();
    intakeRight.disintegrate();
  }

  public void disintegrateExtension() {
    stopExtension();

    extension.disintegrate();
  }

  @Override
  public void periodic() {
    extension.updateInputs();
    intakeLeft.updateInputs();
    intakeRight.updateInputs();

    lastExtensionState = extensionState;
    lastIntakeState = intakeState;

    if (disintegrateIntake.getAsBoolean()) {
      disintegrateIntake();
    }
    if (disintegrateExtension.getAsBoolean()) {
      disintegrateExtension();
    }

    if (setCoast.getAsBoolean()) {
      setExtensionNaturalMode(NeutralMode.COAST);
      setCoast.set(false);
    }

    Logger.recordOutput(getName() + "/hasReset", hasReset ? "HasReset-true" : "HasReset-false");
    Logger.recordOutput(getName() + "/pulse", pulse ? "Pulse-true" : "Pulse-false");
    Logger.recordOutput(getName() + "/intake state", intakeState);
    Logger.recordOutput(getName() + "/extension state", extensionState);
    Logger.recordOutput(getName() + "/allMotorsConnected", isConnected());

    LoggedTracer.record(getName());
  }
}
