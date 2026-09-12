package miscar.util;

import edu.wpi.first.math.system.plant.DCMotor;

public class DCMotorUtil extends DCMotor implements Cloneable {

  public DCMotorUtil(double nominalVoltageVolts, double stallTorqueNewtonMeters,
      double stallCurrentAmps, double freeCurrentAmps, double freeSpeedRadPerSec, int numMotors) {
    super(nominalVoltageVolts, stallTorqueNewtonMeters, stallCurrentAmps, freeCurrentAmps,
        freeSpeedRadPerSec, numMotors);
  }

  public static DCMotorUtil from(DCMotor otherDCMotor) {
    // We pass "1" has the number of motors because
    // otherwise it will alter the DCmotor's configs.
    return new DCMotorUtil(otherDCMotor.nominalVoltageVolts, otherDCMotor.stallTorqueNewtonMeters,
        otherDCMotor.stallCurrentAmps, otherDCMotor.freeCurrentAmps,
        otherDCMotor.freeSpeedRadPerSec, 1);
  }

  public DCMotorUtil clone() {
    // Deep copy the DCMotor, we pass "1" has the number of motors because
    // otherwise it will alter the DCmotor's configs
    DCMotorUtil newDCMotor = new DCMotorUtil(nominalVoltageVolts, stallTorqueNewtonMeters,
        stallCurrentAmps, freeCurrentAmps, freeSpeedRadPerSec, 1);
    return newDCMotor;
  }
}
