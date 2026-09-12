package frc.robot.subsystems.shooter;

import static frc.robot.generated.shooterMec.ShooterMecConstants.*;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import frc.robot.generated.ports.Ports;
import frc.robot.subsystems.shooter.shootingModel.DataTypes.SweetSpot;
import frc.robot.util.LoggedTracer;
import frc.robot.util.LoggedTunableNumber;
import miscar.annotation.ConstantsUser;
import miscar.annotation.CreateConstants;
import miscar.configs.mecs.Movement;
import miscar.configs.mecs.Ratios;
import miscar.configs.mecs.Tolerance;
import miscar.configs.motors.MotorIOConfig;
import miscar.configs.motors.SimConfig;
import miscar.configs.motors.TalonFXConfig;
import miscar.mecsIOs.Mechanism;
import org.littletonrobotics.junction.Logger;

@ConstantsUser
public class ShooterMec {
  @CreateConstants(configType = {TalonFXConfig.class, Ratios.class, Movement.class, Tolerance.class,
      SimConfig.class})
  public Mechanism front;

  @CreateConstants(configType = {TalonFXConfig.class, Ratios.class, Movement.class, Tolerance.class,
      SimConfig.class})
  public Mechanism backRight;

  public Mechanism backLeft;

  @CreateConstants(configType = {TalonFXConfig.class, Movement.class, Ratios.class, SimConfig.class,
      Tolerance.class})
  public Mechanism indexer;

  public SweetSpot currantSweetSpot = new SweetSpot(0, 0);
  public boolean activateIndexer = false;

  LoggedTunableNumber flyWheelReadyDebouncerTime =
      new LoggedTunableNumber("Shooter/FlyWheelReadyDebouncerTime", 0.2, false);

  private Debouncer flyWheelReadyDebouncer =
      new Debouncer(flyWheelReadyDebouncerTime.getAsDouble(), DebounceType.kBoth);

  public ShooterMec(Mechanism front, MotorIOConfig backConfig, Mechanism indexer) {
    this.front = front;
    this.backRight = Mechanism.create(backConfig);

    backConfig.withMotorPort(Ports.backLeftMotorPort).withInverted(!backConfig.getInverted());

    this.backLeft = Mechanism.create(backConfig);
    this.indexer = indexer;


    front.setName("Shooter/Front");
    backRight.setName("Shooter/BackRight");
    backLeft.setName("Shooter/BackLeft");
    indexer.setName("Shooter/Indexer");

    front.setRatio(frontConstants.mecRatiosConstants.config);
    backRight.setRatio(backRightConstants.mecRatiosConstants.config);
    backLeft.setRatio(backRightConstants.mecRatiosConstants.config);
    indexer.setRatio(indexerConstants.mecRatiosConstants.config);

    front.setMaxVelocity(frontConstants.mecMovementConstants.frontMaxVelocity);
    backRight.setMaxVelocity(backRightConstants.mecMovementConstants.backRightMaxVelocity);
    backLeft.setMaxVelocity(backRightConstants.mecMovementConstants.backRightMaxVelocity);
    indexer.setMaxVelocity(indexerConstants.mecMovementConstants.indexerMaxVelocity);

    front.setVelocityTolerance(frontConstants.mecToleranceConstants.frontAllowedMecVelocityError);
    backRight.setVelocityTolerance(
        backRightConstants.mecToleranceConstants.backRightAllowedMecVelocityError);
    backLeft.setVelocityTolerance(
        backRightConstants.mecToleranceConstants.backRightAllowedMecVelocityError);
    indexer.setVelocityTolerance(
        indexerConstants.mecToleranceConstants.indexerAllowedMecVelocityError);
  }

  public void setSweetSpot(SweetSpot sweetSpot, boolean activateIndexer) {

    currantSweetSpot = sweetSpot;
    Logger.recordOutput("Shooter/currantSweetSpot", currantSweetSpot);
    setMecVelocityWithStop(backRight, currantSweetSpot.backVelocity());
    setMecVelocityWithStop(backLeft, currantSweetSpot.backVelocity());
    setMecVelocityWithStop(front, currantSweetSpot.frontVelocity());
    setMecVelocityWithStop(indexer, activateIndexer ? currantSweetSpot.indexerVelocity() : 0);
    this.activateIndexer = activateIndexer;

  }

  public boolean isConnected() {
    return front.inputs.connected && backRight.inputs.connected && backLeft.inputs.connected
        && indexer.inputs.connected;
  }

  // this function is so you can put the motors at zero and the pid wont
  // effect it
  public void setMecVelocityWithStop(Mechanism mechanism, double targetVelocity) {

    if (Math.abs(targetVelocity) <= 100) {
      mechanism.targetVelocity = 0;
      mechanism.setPower(0);
    } else {
      mechanism.setTargetMecVelocity(targetVelocity);
    }
  }

  public boolean isReadyToShoot() {
    return isFlyWheelReadyToShoot()
        && (activateIndexer && indexer.mecInVelocity(currantSweetSpot.indexerVelocity()));
  }


  public boolean isFlyWheelReadyToShoot() {

    return flyWheelReadyDebouncer.calculate(backRight.mecInVelocity() && backLeft.mecInVelocity()
        && front.mecInVelocity() && !isFlyWheelMecVelocityZero());
  }

  private boolean isFlyWheelMecVelocityZero() {

    return backLeft.targetVelocity == 0 && backRight.targetVelocity == 0
        && front.targetVelocity == 0;
  }

  public void stop() {
    currantSweetSpot = SweetSpot.kZero;
    activateIndexer = false;
    backLeft.setPower(0);
    backRight.setPower(0);
    front.setPower(0);
    indexer.setPower(0);
  }

  public void disintegrate() {
    stop();
    front.disintegrate();
    backRight.disintegrate();
    backLeft.disintegrate();
    indexer.disintegrate();
  }

  public void updateInputs() {
    front.updateInputs();
    backRight.updateInputs();
    backLeft.updateInputs();
    indexer.updateInputs();

    flyWheelReadyDebouncer.setDebounceTime(flyWheelReadyDebouncerTime.getAsDouble());
    LoggedTracer.record("ShooterMec");
  }
}
