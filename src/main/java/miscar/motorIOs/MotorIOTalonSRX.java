package miscar.motorIOs;

import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import miscar.configs.motors.Phoenix5Config;
import miscar.mecsIOs.features.ModeOnDisable.NeutralMode;

public class MotorIOTalonSRX extends EmptyMotor {
  public final TalonSRX motor;
  private Faults faults = new Faults();

  public MotorIOTalonSRX(Phoenix5Config config) {
    super(config.motorPort);
    motor = new TalonSRX(config.motorPort);
    motor.setInverted(config.getInverted());
    motor.configureSlot(config.slotConfig);
    motor.setNeutralMode(
        config.neutralMode == NeutralMode.COAST ? com.ctre.phoenix.motorcontrol.NeutralMode.Coast
            : com.ctre.phoenix.motorcontrol.NeutralMode.Brake);

    motor.setSelectedSensorPosition(0);
  }

  public void updateInputs(MotorIOInputsAutoLogged inputs) {
    motor.getFaults(faults);
    inputs.appliedVolts = motor.getMotorOutputVoltage();
    inputs.connected = !faults.hasAnyFault();
    inputs.positionRotations = motor.getSelectedSensorPosition();
    inputs.currentAmps = -1;
    inputs.velocityRotationsPerMinute = motor.getSelectedSensorVelocity();
    inputs.temperature = motor.getTemperature();

    super.updateInputs(inputs);
  }

  @Override
  public void setPower(double power) {
    motor.set(TalonSRXControlMode.PercentOutput, power);
  }

  @Override
  public void setVoltage(double output) {
    // not supported
  }

  @Override
  public void setTargetPosition(double pose) {
    motor.set(TalonSRXControlMode.Position, pose);

    super.setTargetPosition(pose);
  }

  public void setTargetMotorAngularVelocity(double velocity) {
    motor.set(TalonSRXControlMode.Velocity, velocity);

    super.setTargetMotorAngularVelocity(velocity);
  }
}
