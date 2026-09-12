package miscar.configs.motors;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.motorIOs.MotorIOSim;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;

/** A generic motorIO sim config */
@ConstantsName(constantsName = "simMotorConfig")
public class SimConfig extends MotorIOConfig {
  /** The gear box of the generic motorIO */
  public final DCMotorUtil gearBox;
  /** The PID controller for the generic motorIO */
  public final PIDControllerUtil PIDController;
  /** The moment of Inertia of the generic motorIO */
  public final double MOI;

  /**
   * Constructs a new config for a generic motorIO sim
   *
   * @param gearBox - The sim gear box, see {@link DCMotor} for more
   * @param PIDController - The PID controller for the motor
   * @param MOI - The MOI of the motor
   * @param motorToMecRatio - The ratio between the motor and the mec
   *        moment
   */
  @ConstantsConstructor
  public SimConfig(DCMotor gearBox, PIDController PIDController, double MOI) {
    super();
    this.gearBox = DCMotorUtil.from(gearBox);
    this.PIDController = PIDControllerUtil.from(PIDController);
    this.MOI = MOI;
  }

  public SimConfig(SimConfig config) {
    super();
    this.gearBox = DCMotorUtil.from(config.gearBox.clone());
    this.PIDController = PIDControllerUtil.from(config.PIDController.clone());
    this.MOI = config.MOI;
    this.withInverted(config.inverted).withMotorPort(config.motorPort);
  }

  @Override
  public MotorIOSim buildMotor() {
    return new MotorIOSim(this);
  }

  @Override
  public SimConfig clone() {
    SimConfig newConfig = new SimConfig(this);
    return newConfig;
  }
}
