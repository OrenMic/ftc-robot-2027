package miscar.motorIOs;

import edu.wpi.first.wpilibj.motorcontrol.VictorSP;
import miscar.configs.motors.Phoenix5Config;

public class MotorIOVictor extends EmptyMotor {

  public VictorSP motor;

  public MotorIOVictor(Phoenix5Config config) {
    super(config.motorPort);
    motor = new VictorSP(config.motorPort);

    motor.setInverted(config.getInverted());
  }

  @Override
  public void updateInputs(MotorIOInputsAutoLogged inputs) {
    inputs.appliedVolts = motor.getVoltage();
    inputs.connected = motor.isAlive();

    super.updateInputs(inputs);
  }

  @Override
  public void setPower(double power) {
    motor.set(power);
  }

  @Override
  public void setVoltage(double output) {
    motor.setVoltage(output);
  }

  @Override
  public void setTargetPosition(double pose) {
    // not supported
    super.setTargetPosition(pose);
  }

  public void setTargetMotorAngularVelocity(double velocity) {
    // not supported
    super.setTargetMotorAngularVelocity(velocity);
  }
}
