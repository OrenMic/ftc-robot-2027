package miscar.motorIOs;

import static miscar.util.PhoenixUtil.*;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.filter.Debouncer;
import miscar.configs.motors.SparkConfig;
import miscar.configs.motors.TalonFXConfig;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;

/** A generic class for a FalconFX motorIO */
public class MotorIOSpark extends EmptyMotor {

  /** The SparkMax motor */
  public final SparkMax motor;

  /** The SparkMax motor closed loop controller */
  public final SparkClosedLoopController motorClosedLoopController;

  /** The SparkMax motor encoder */
  public final RelativeEncoder motorEncoder;

  /** Debouncer for if the motorIO's motor disconnects */
  private final Debouncer motorConnectedDebounce = new Debouncer(0.5);

  SparkConfig config;

  /**
   * A constructor for a new generic motorIO utilizing a TalonFX motor
   *
   * @param config - the config for the motorIO, see
   *        {@link TalonFXConfig} for more
   */
  public MotorIOSpark(SparkConfig config) {
    super(config.motorPort);

    // Links the motorIO to the motor on the robot
    this.config = config.clone();
    motor = new SparkMax(this.config.getPort(), MotorType.kBrushless);
    motorClosedLoopController = motor.getClosedLoopController();
    motorEncoder = motor.getEncoder();

    // A new motor config
    var motorConfig = this.config.motorConfig;

    // This will make sure that you don't accidentally change the motor
    // control type to be something other then rotations
    // motorConfig.encoder.positionConversionFactor(1).velocityConversionFactor(1);

    // Burns the motor config into the motorIO's motor
    motor.configure(motorConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    // Resets the motorIO's motor's position to 0
    motorEncoder.setPosition(0);
  }

  @Override
  public void updateInputs(MotorIOInputsAutoLogged inputs) {
    // Update the inputs
    inputs.connected = motorConnectedDebounce.calculate(!motor.getFaults().can);
    inputs.positionRotations = motorEncoder.getPosition();
    inputs.velocityRotationsPerMinute = motorEncoder.getVelocity();
    inputs.appliedVolts = motor.getBusVoltage();
    inputs.currentAmps = motor.getOutputCurrent();
    inputs.temperature = motor.getMotorTemperature();

    super.updateInputs(inputs);
  }

  @Override
  public void setTargetPosition(double poseRotations) {
    super.setTargetPosition(poseRotations);

    motorClosedLoopController.setReference(poseRotations, ControlType.kMAXMotionPositionControl);
  }

  @Override
  public void setTargetMotorAngularVelocity(double angularVelocityRPM) {
    super.setTargetMotorAngularVelocity(angularVelocityRPM);

    motorClosedLoopController.setReference(angularVelocityRPM,
        ControlType.kMAXMotionVelocityControl);
  }

  @Override
  public void setPower(double power) {
    motor.set(power);
  }

  @Override
  public void setVoltage(double output) {
    motor.setVoltage(output);;
  }

  @Override
  public SparkConfig getMotorConfig() {
    return config;
  }

  @Override
  public void setNaturalMode(NeutralMode neutralMode) {
    config.motorConfig
        .idleMode(neutralMode == NeutralMode.BREAK ? IdleMode.kBrake : IdleMode.kCoast);

    motor.configure(config.motorConfig,
        ResetMode.kResetSafeParameters,
        PersistMode.kNoPersistParameters);
  }
}
