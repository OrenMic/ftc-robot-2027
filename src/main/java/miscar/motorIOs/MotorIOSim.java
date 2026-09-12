package miscar.motorIOs;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import miscar.configs.motors.SimConfig;

/** A generic class for a sim motorIO */
public class MotorIOSim extends EmptyMotor {
  /** The sim motor */
  public final DCMotorSim motorSim;

  /** Whether or not the sim motor in set to closed loop control */
  private enum MotorClosedLoop {
    OPEN_LOOP, POSITION_CONTROL, VELOCITY_CONTROL
  };

  MotorClosedLoop motorClosedLoop = MotorClosedLoop.OPEN_LOOP;

  /** The sim motor's pid controller */
  private PIDController motorController;

  /** The sim motor applied volts */
  private double motorAppliedVolts = 0.0;

  SimConfig config;

  /** Constructs a new generic motorIO sim, with the inputted config */
  public MotorIOSim(SimConfig config) {
    super(config.motorPort);
    this.config = config.clone();
    motorController = this.config.PIDController.clone();
    motorSim =
        new DCMotorSim(LinearSystemId.createDCMotorSystem(this.config.gearBox, this.config.MOI, 1),
            this.config.gearBox);
  }

  @Override
  public void updateInputs(MotorIOInputsAutoLogged inputs) {
    // Update the power to the sim motorIO

    switch (motorClosedLoop) {
      case OPEN_LOOP:
        // Set the Power to the sim motorIO
        motorSim.setInputVoltage(motorAppliedVolts);

        if (Math.abs(motorAppliedVolts) < 0.05) {
          motorSim.setAngularVelocity(0);
        }

        motorController.reset();
        break;
      case POSITION_CONTROL:
        // Calc the pose error
        motorAppliedVolts = motorController.calculate(motorSim.getAngularPositionRad());
        // Set the error velocity to the sim motorIO
        motorSim.setAngularVelocity(motorAppliedVolts);

        // if (Math.abs(motorAppliedVolts) < 0.05) {
        // motorSim.setAngularVelocity(0);
        // }
        break;
      case VELOCITY_CONTROL:
        // Set the target velocity to the sim motorIO
        motorAppliedVolts =
            motorController.calculate(Units.rotationsToRadians(targetAngularVelocityRPM) / 60);
        motorSim.setAngularVelocity(Units.rotationsToRadians(targetAngularVelocityRPM) / 60);

        // motorController.reset();
        break;
      default:
        break;
    }
    motorSim.setInputVoltage(MathUtil.clamp(motorAppliedVolts, -12, 12));

    // Update the sim motorIO
    motorSim.update(0.02);

    // Update the generic motorIO inputs;
    inputs.connected = true;
    inputs.positionRotations = motorSim.getAngularPositionRotations();
    inputs.velocityRotationsPerMinute = motorSim.getAngularVelocityRPM();
    inputs.appliedVolts = motorAppliedVolts;
    inputs.currentAmps = motorSim.getCurrentDrawAmps();
    inputs.temperature = -1;

    super.updateInputs(inputs);
  }

  @Override
  public void setTargetPosition(double poseRotations) {
    super.setTargetPosition(poseRotations);
    motorClosedLoop = MotorClosedLoop.POSITION_CONTROL;
    motorController.setSetpoint(Units.rotationsToRadians(poseRotations));
  }

  @Override
  public void setTargetMotorAngularVelocity(double angularVelocityRPM) {
    super.setTargetMotorAngularVelocity(angularVelocityRPM);
    motorClosedLoop = MotorClosedLoop.VELOCITY_CONTROL;
  }

  @Override
  public void setPower(double power) {
    motorClosedLoop = MotorClosedLoop.OPEN_LOOP;
    // The power is in the range of -1 to 1
    // but the motor applied volts are in the range of -12 to 12
    // so we multiply by 12
    motorAppliedVolts = power * 12;
  }

  @Override
  public void setVoltage(double output) {
    motorClosedLoop = MotorClosedLoop.OPEN_LOOP;
    motorAppliedVolts = output;
  }

  @Override
  public SimConfig getMotorConfig() {
    return config;
  }
}
