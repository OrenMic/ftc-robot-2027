package frc.robot.subsystems.superStructure;

import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveState;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeStates.ExtensionState;
import frc.robot.subsystems.intake.IntakeStates.IntakeState;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterState;
import frc.robot.subsystems.shooter.util.ShootingUtil;
import frc.robot.subsystems.shooter.shootingModel.SweetSpots;
import frc.robot.subsystems.transfer.Transfer;
import frc.robot.subsystems.transfer.TransferState;
import frc.robot.util.GeomUtil;
import frc.robot.util.LoggedTracer;
import frc.robot.util.LoggedTunableNumber;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(GeomUtil.class)
public class SuperStructure extends SubsystemBase {
  {
    setName("SuperStructure");
  }
  @Setter
  public boolean overrideIntake = false;
  @Getter
  public ExtensionState overrideExtensionState = ExtensionState.EXTENDED;
  @Getter
  public IntakeState overrideIntakeState = IntakeState.INTAKING;

  public boolean clearShooter = false;

  public Drive drive;
  public Shooter shooter;
  public Transfer transfer;
  public Intake intake;

  @Getter
  private SuperStructureState state = SuperStructureState.RETRACTED;

  private String stateLogPath = getName() + "/state";
  private String distanceToHubLogPath = getName() + "/distance/hub";
  private String disToDeliveryLineLogPath = getName() + "/distance/deliveryLine";
  private String allMotorsConnectedLogPath = getName() + "/all motors connected";

  private LoggedTunableNumber shootingAngleOffset =
      new LoggedTunableNumber(getName() + "/shootingAngleOffset", 0, false);

  private LoggedTunableNumber shootingSpeedCap =
      new LoggedTunableNumber(getName() + "/shootingSpeedCap", 1, false);
  private LoggedTunableNumber deliverySpeedCap =
      new LoggedTunableNumber(getName() + "/deliverySpeedCap", 2.5, false);

  private LoggedNetworkBoolean useStayAtVel =
      new LoggedNetworkBoolean(getName() + "/useStayAtVel", false);

  @Setter
  @Getter
  private boolean lockState = false;

  private ButtonsCommand buttonsCommand;

  public SuperStructure(Drive drive, Shooter shooter, Transfer transfer, Intake intake) {
    this.drive = drive;
    this.shooter = shooter;
    this.transfer = transfer;
    this.intake = intake;
    buttonsCommand = new ButtonsCommand(this);
  }

  @Override
  public void periodic() {
    buttonsCommand.periodic();

    switch (state) {
      case IDLE:
        IDLE();
        break;
      case DISABLED:
        DISABLED();
        break;
      case AUTO_CONTROLLED:
        AUTO_CONTROLLED();
        break;
      case INTAKING:
        INTAKING();
        break;

      case INTAKING_DEPOT:
        INTAKING_DEPOT();
        break;

      case OUTTAKING:
        OUTTAKING();
        break;
      case SHOOTING_MANUAL:
        SHOOTING_MANUAL();
        break;
      case SHOOTING_ASSISTED:
        SHOOTING_ASSISTED();
        break;
      case EXTENDED:
        EXTENDED();
        break;
      case RETRACTED:
        RETRACTED();
        break;
      default:
        state = SuperStructureState.IDLE;
        break;
    }

    if (!state.isShootingState()) {
      drive.setSpeedCap(5);
    }

    Logger.recordOutput(allMotorsConnectedLogPath, allMotorsConnected());
    Logger.recordOutput(getName() + "/overrides/IntakeState", overrideIntakeState);
    Logger.recordOutput(getName() + "/overrides/ExtensionState", overrideExtensionState);
    Logger.recordOutput(getName() + "/overrides/active", overrideIntake);
    Logger.recordOutput(stateLogPath, state);
    double hubDistance = SweetSpots.hubSweetSpots.distanceSupplier.getAsDouble();
    Logger.recordOutput(distanceToHubLogPath,
        hubDistance < 100 ? hubDistance : ShootingUtil.getDistanceToHub());
    LoggedTracer.record(getName());
  }

  private boolean allMotorsConnected() {
    return drive.isConnected() && shooter.isConnected() && transfer.isConnected()
        && intake.isConnected();
  }

  public void IDLE() {
    drive.setState(DriveState.IDLE);
    // use default state logic to by pass the stay at vel logic and make
    // sure everything is idle
    shooter.setStateIdle();
    transfer.setState(TransferState.IDLE);
    setHomeIntakeState();
  }

  private void EXTENDED() {
    drive.setState(DriveState.FREE_DRIVE);
    setShooterStateIdle();
    transfer.setState(TransferState.IDLE);
    setIntakeState(IntakeState.IDLE, ExtensionState.EXTENDED);
  }

  private void RETRACTED() {
    drive.setState(DriveState.FREE_DRIVE);
    setShooterStateIdle();
    transfer.setState(TransferState.IDLE);
    setIntakeState(IntakeState.IDLE, ExtensionState.RETRACTED);
  }

  private void DISABLED() {
    drive.setState(DriveState.IDLE);
    // use default state logic to by pass the stay at vel logic and make
    // sure everything is idle
    shooter.setStateIdle();
    transfer.setState(TransferState.IDLE);
    intake.setState(IntakeState.IDLE, ExtensionState.IDLE);
  }

  private void AUTO_CONTROLLED() {}

  private void INTAKING() {
    drive.setState(DriveState.FREE_DRIVE);
    setShooterStateIdle();
    transfer.setState(TransferState.PROCESSING);
    setIntakeState(IntakeState.INTAKING, ExtensionState.EXTENDED);
  }

  private void INTAKING_DEPOT() {
    drive.setState(DriveState.FREE_DRIVE);
    setShooterStateIdle();
    transfer.setState(TransferState.PROCESSING);
    setIntakeState(IntakeState.INTAKING_DEPOT, ExtensionState.EXTENDED_DEPOT);
  }

  private void OUTTAKING() {
    drive.setState(DriveState.FREE_DRIVE);
    setShooterStateIdle();
    transfer.setState(TransferState.UNLOADING);
    setIntakeState(IntakeState.OUTTAKING, ExtensionState.EXTENDED);
  }

  private void SHOOTING_MANUAL() {
    drive.setState(DriveState.FREE_DRIVE);

    shooterLogic(true, ShooterState.MANUAL_SHOOTING);
  }

  private void SHOOTING_ASSISTED() {
    drive.setSpeedCap(shootingSpeedCap.getAsDouble());
    drive.setLockToAngle(ShootingUtil.getAngleToVirtualHub()
        .plus(Rotation2d.fromDegrees(shootingAngleOffset.getAsDouble())));
    drive.setState(DriveState.ANGLED);

    shootingLogic(ShootingUtil.isRobotAlinedToShoot());
  }

  public void shootingLogic(boolean canShoot) {
    shooterLogic(canShoot, ShooterState.SHOOTING);
  }

  private void shooterLogic(boolean canShoot, ShooterState shooterState) {
    // If the angle and shooter velocity are ready, start the indexer's
    // wheels
    if (canShoot && shooter.isFlyWheelReadyToShoot()) {
      setShooterState(shooterState, true);
    }
    // if not currently at angle accelerate only the fly wheels and not
    // the indexer's
    else {
      setShooterState(shooterState, false);
    }

    // If the indexer wheels are ready, start the belts
    if ((shooter.isReadyToShoot() && shooter.getState() == shooterState)
        || transfer.getState() == TransferState.SHOOTING) {

      if (clearShooter) {
        transfer.setState(TransferState.CLEARING);
      } else {
        transfer.setState(TransferState.SHOOTING);
      }
      setIntakeState(IntakeState.PULSING, ExtensionState.PULSING);
    } else {
      transfer.setState(TransferState.PROCESSING);
      // setHomeIntakeState();
    }
  }

  // from titanium
  private void CLIMBING() {}

  private void setHomeIntakeState() {
    setIntakeState(IntakeState.IDLE, getClosestHomeIntakeState());
  }

  public ExtensionState getClosestHomeIntakeState() {
    if (intake.getExtensionState() != ExtensionState.RETRACTED
        && intake.getExtensionState() != ExtensionState.PULSING)
      return ExtensionState.EXTENDED;
    else
      return ExtensionState.RETRACTED;
  }

  public void setIntakeOverride(IntakeState intakeState, ExtensionState extensionState) {
    overrideIntake = true;
    this.overrideIntakeState = intakeState;
    this.overrideExtensionState = extensionState;
  }

  public void setState(SuperStructureState state) {
    if (!lockState)
      this.state = state;
  }

  private void setShooterState(ShooterState wantedState, boolean activeIndexer) {
    if (wantedState == ShooterState.IDLE && useStayAtVel.getAsBoolean())
      wantedState = ShooterState.HOLDING_VELOCITY;
    shooter.setState(wantedState, activeIndexer);
  }

  private void setShooterStateIdle() {
    setShooterState(ShooterState.IDLE, false);
  }

  private void setIntakeState(IntakeState intakeState, ExtensionState extensionState) {
    if (overrideIntake) {
      intake.setState(overrideIntakeState, overrideExtensionState);
    } else {
      intake.setState(intakeState, extensionState);
    }
  }

}
