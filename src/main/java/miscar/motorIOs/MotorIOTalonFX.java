package miscar.motorIOs;

import static miscar.util.PhoenixUtil.tryUntilOk;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.Slot1Configs;
import com.ctre.phoenix6.configs.Slot2Configs;
import com.ctre.phoenix6.controls.ControlRequest;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.DynamicMotionMagicVoltage;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicExpoVoltage;
import com.ctre.phoenix6.controls.MotionMagicVelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.MotionMagicVelocityVoltage;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.TorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;
import miscar.configs.motors.TalonFXConfig;
import miscar.mecsIOs.Mechanism;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;
import miscar.util.PhoenixControlTypes;

/** A generic class for a FalconFX motorIO */
public class MotorIOTalonFX extends EmptyMotor {

  /** The TalonFX motor */
  public final TalonFX motor;

  /** Whether of not to enable FOC */
  public final boolean enableFOC;

  /** Debouncer for if the motorIO's motor disconnects */
  private final Debouncer motorConnectedDebounce = new Debouncer(0.5);

  /** The motorIO's motor's position in rotations status signal */
  private final StatusSignal<Angle> motorPositionRotations;
  /**
   * The motorIO's motor's velocity in rotations per sec status signal
   */
  private final StatusSignal<AngularVelocity> motorVelocityRotationsPerSec;
  /** The motorIO's motor's applied volts status signal */
  private final StatusSignal<Voltage> motorAppliedVolts;
  /** The motorIO's motor's current status signal */
  private final StatusSignal<Current> motorCurrent;
  /** The motorIO's motor's temp status signal */
  private final StatusSignal<Temperature> motorTemp;

  // the closed loop pose control requests
  MotionMagicVoltage motionMagicVoltageRequest = new MotionMagicVoltage(0);
  MotionMagicExpoVoltage motionMagicExpoVoltageRequest = new MotionMagicExpoVoltage(0);
  DynamicMotionMagicVoltage motionMagicDynamicVoltageRequest =
      new DynamicMotionMagicVoltage(0, 0, 0);
  PositionTorqueCurrentFOC positionTorqueCurrentFOCRequest = new PositionTorqueCurrentFOC(0);
  PositionVoltage positionVoltageRequest = new PositionVoltage(0);

  // the closed loop velocity control requests
  MotionMagicVelocityVoltage motionMagicVelocityVoltageRequest = new MotionMagicVelocityVoltage(0);
  MotionMagicVelocityTorqueCurrentFOC motionMagicVelocityTorqueCurrentFOCRequest =
      new MotionMagicVelocityTorqueCurrentFOC(0);
  VelocityTorqueCurrentFOC velocityTorqueCurrentFOCRequest = new VelocityTorqueCurrentFOC(0);
  VelocityVoltage velocityVoltageRequest = new VelocityVoltage(0);

  // The open loop control requests
  DutyCycleOut dutyCycleOutRequest = new DutyCycleOut(0);
  VoltageOut voltageOutRequest = new VoltageOut(0);
  TorqueCurrentFOC torqueCurrentFOCRequest = new TorqueCurrentFOC(0);
  Follower followerRequest = new Follower(-1, MotorAlignmentValue.Aligned);

  public TalonFXConfig config;

  int slot = 0;

  /**
   * A constructor for a new generic motorIO utilizing a TalonFX motor
   *
   * @param config - the config for the motorIO, see
   *        {@link TalonFXConfig} for more
   */
  public MotorIOTalonFX(TalonFXConfig config) {
    super(config.motorPort);

    TalonFXConfig newConfig = config.clone();
    // Links the motorIO to the motor on the robot
    motor = new TalonFX(newConfig.getMotorPort(), newConfig.getCANBus());
    this.config = newConfig;

    // A new motor config
    var motorConfig = newConfig.motorConfig;

    // This will make sure that you don't accidentally change the motor
    // control type to be something other then rotations
    motorConfig.Feedback.SensorToMechanismRatio = 1;
    motorConfig.Feedback.RotorToSensorRatio = 1;

    // Burns the motor config into the motorIO's motor
    tryUntilOk(5, () -> motor.getConfigurator().apply(motorConfig, 0.25), newConfig.motorPort);
    // Resets the motorIO's motor's position to 0
    tryUntilOk(5, () -> motor.setPosition(0.0, 0.25), newConfig.motorPort);

    // Links the status signals
    motorPositionRotations = motor.getRotorPosition();
    motorVelocityRotationsPerSec = motor.getVelocity();
    motorAppliedVolts = motor.getMotorVoltage();
    motorCurrent = motor.getSupplyCurrent();
    motorTemp = motor.getDeviceTemp();

    // Enable only used status signals in order to reduce load on the
    // CANBus
    BaseStatusSignal.setUpdateFrequencyForAll(50.0,
        motorPositionRotations,
        motorVelocityRotationsPerSec,
        motorAppliedVolts,
        motorCurrent,
        motorTemp);

    // Disable the other status signals to farther reduce load on the
    // CANBus
    motor.optimizeBusUtilization();

    // Save the use FOC setting;
    enableFOC = config.enableFOC;
  }

  @Override
  public void updateInputs(MotorIOInputsAutoLogged inputs) {
    // Refresh all signals

    var motorStatus = BaseStatusSignal.refreshAll(motorPositionRotations,
        motorVelocityRotationsPerSec,
        motorAppliedVolts,
        motorCurrent,
        motorTemp);

    // Update the inputs
    inputs.connected = motorConnectedDebounce.calculate(motorStatus.isOK());
    inputs.temperature = motorTemp.getValueAsDouble();
    inputs.positionRotations = motorPositionRotations.getValueAsDouble();
    inputs.velocityRotationsPerMinute = motorVelocityRotationsPerSec.getValueAsDouble() * 60;
    inputs.appliedVolts = motorAppliedVolts.getValueAsDouble();
    inputs.currentAmps = motorCurrent.getValueAsDouble();

    super.updateInputs(inputs);
  }

  @Override
  public void setTargetPosition(double poseRotations) {
    super.setTargetPosition(poseRotations);
    motor.setControl(getPoseControlRequest(poseRotations, 0));
  }

  public void setControl(ControlRequest request) {
    motor.setControl(request);
  }

  public ControlRequest getPoseControlRequest(double poseRotations, double feedForward) {
    return getPoseControlRequest(poseRotations, feedForward, config.poseControlType);
  }

  public ControlRequest getPoseControlRequest(double poseRotations, double feedForward,
      PhoenixControlTypes.PoseControl controlType) {
    return switch (controlType) {
      case MotionMagicExpoVoltage -> motionMagicExpoVoltageRequest.withPosition(poseRotations)
          .withEnableFOC(enableFOC).withFeedForward(feedForward).withSlot(slot);
      case MotionMagicVoltage -> motionMagicVoltageRequest.withPosition(poseRotations)
          .withEnableFOC(enableFOC).withFeedForward(feedForward).withSlot(slot);
      case PositionVoltage -> positionVoltageRequest.withPosition(poseRotations)
          .withEnableFOC(enableFOC).withFeedForward(feedForward).withSlot(slot);
      case PositionTorqueCurrentFOC -> positionTorqueCurrentFOCRequest.withPosition(poseRotations)
          .withFeedForward(feedForward).withSlot(slot);
    };
  }

  public void setTargetPosition(double poseRotations, double velocity, double acceleration) {
    super.setTargetPosition(poseRotations);
    motionMagicDynamicVoltageRequest.Velocity = velocity;
    motionMagicDynamicVoltageRequest.Acceleration = acceleration;

    motor.setControl(motionMagicDynamicVoltageRequest.withPosition(poseRotations)
        .withEnableFOC(enableFOC).withSlot(slot));
  }

  // public double feedForward = 0;

  @Override
  public void setTargetMotorAngularVelocity(double angularVelocityRPM) {
    super.setTargetMotorAngularVelocity(angularVelocityRPM);

    angularVelocityRPM /= 60;
    motor.setControl(switch (config.velocityControlType) {
      case MotionMagicVelocityVoltage -> motionMagicVelocityVoltageRequest
          .withVelocity(angularVelocityRPM).withEnableFOC(enableFOC).withSlot(slot);
      case MotionMagicVelocityTorqueCurrentFOC -> motionMagicVelocityTorqueCurrentFOCRequest
          .withVelocity(angularVelocityRPM);
      case VelocityVoltage -> velocityVoltageRequest.withVelocity(angularVelocityRPM).withSlot(slot)
          // .withFeedForward(feedForward)
          .withEnableFOC(enableFOC);
      case VelocityTorqueCurrentFOC -> velocityTorqueCurrentFOCRequest
          .withVelocity(angularVelocityRPM).withSlot(slot);
    });
  }

  @Override
  public void setPower(double power) {
    motor.setControl(switch (config.openLoopControlType) {
      case Regular -> dutyCycleOutRequest.withOutput(power).withEnableFOC(enableFOC);
      // the power is in the range -1 to 1
      // but we need -12 to 12
      // so we multiply by 12
      case TorqueCurrentFOC -> torqueCurrentFOCRequest.withOutput(power * 12);
    });
  }

  @Override
  public void setVoltage(double output) {
    motor.setControl(switch (config.openLoopControlType) {
      case Regular -> voltageOutRequest.withOutput(output).withEnableFOC(enableFOC);
      case TorqueCurrentFOC -> torqueCurrentFOCRequest.withOutput(output);
    });
  }

  @Override
  public TalonFXConfig getMotorConfig() {
    return config;
  }

  @Override
  public void setNaturalMode(NeutralMode neutralMode) {
    motor.setNeutralMode(
        neutralMode == NeutralMode.BREAK ? NeutralModeValue.Brake : NeutralModeValue.Coast);
  }

  public void setPid(Slot0Configs config) {
    tryUntilOk(5, () -> motor.getConfigurator().apply(config, 0.25), this.config.motorPort);
  }

  public void setPid(Slot1Configs config) {
    motor.getConfigurator().apply(config);
  }

  public void setPid(Slot2Configs config) {
    motor.getConfigurator().apply(config);
  }

  public void setMotionMagic(MotionMagicConfigs config) {
    motor.getConfigurator().apply(config);
  }

  public void setFollower(Mechanism leader, double frequency) {
    followerRequest.UpdateFreqHz = frequency;
    setFollower(leader);
  }

  public void setFollower(Mechanism leader) {
    boolean followerInverted = config.getInverted();
    boolean leaderInverted = leader.motorIODelegation.getMotorConfig().getInverted();

    MotorAlignmentValue allignedValue =
        followerInverted != leaderInverted ? MotorAlignmentValue.Opposed
            : MotorAlignmentValue.Aligned;

    followerRequest.LeaderID = leader.motorIODelegation.getMotorConfig().motorPort;
    followerRequest.MotorAlignment = allignedValue;
    motor.setControl(followerRequest);
  }

  @Override
  public void setClosedLoopSlot(int slot) {
    this.slot = slot;
  }
}
