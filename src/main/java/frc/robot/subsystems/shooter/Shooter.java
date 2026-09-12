package frc.robot.subsystems.shooter;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.shooter.shootingModel.DataTypes.SweetSpot;
import frc.robot.util.LoggedTracer;
import frc.robot.util.LoggedTunableNumber;
import lombok.Getter;
import miscar.configs.motors.MotorIOConfig;
import miscar.mecsIOs.Mechanism;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;

public class Shooter extends SubsystemBase {
  {
    setName("Shooter");
  }

  ShooterMec shooterMec;

  @Getter
  private ShooterState state = ShooterState.IDLE;

  LoggedNetworkBoolean disintegrate = new LoggedNetworkBoolean(getName() + "/disintegrate", false);


  public LoggedTunableNumber backVelocityOffset =
      new LoggedTunableNumber(getName() + "/VelocityOffsets/back", 0, true);
  public LoggedTunableNumber frontVelocityOffset =
      new LoggedTunableNumber(getName() + "/VelocityOffsets/front", 0, true);
  public LoggedTunableNumber indexerVelocityOffset =
      new LoggedTunableNumber(getName() + "/VelocityOffsets/indexer", 0, false);

  public Shooter(Mechanism front, MotorIOConfig backConfig, Mechanism indexer) {
    shooterMec = new ShooterMec(front, backConfig, indexer);
  }

  public void setStateIdle() {
    setState(ShooterState.IDLE, false);
  }

  public void setState(ShooterState wantedState, boolean activeIndexer) {
    state = wantedState;
    if (wantedState == ShooterState.IDLE) {
      shooterMec.stop();
      return;
    }

    if (wantedState == ShooterState.HOLDING_VELOCITY) {
      shooterMec.setSweetSpot(shooterMec.currantSweetSpot, false);
      return;
    }

    SweetSpot sweetSpot = wantedState.sweetSpot.get();
    if (backVelocityOffset.getAsDouble() != 0 || frontVelocityOffset.getAsDouble() != 0) {
      if (indexerVelocityOffset.getAsDouble() != 0) {
        sweetSpot = new SweetSpot(backVelocityOffset.getAsDouble(),
            frontVelocityOffset.getAsDouble(), indexerVelocityOffset.getAsDouble());
      } else {
        sweetSpot =
            new SweetSpot(backVelocityOffset.getAsDouble(), frontVelocityOffset.getAsDouble());
      }
    }

    shooterMec.setSweetSpot(sweetSpot, activeIndexer);
  }


  public boolean isConnected() {
    return shooterMec.isConnected();
  }

  public boolean isReadyToShoot() {
    return shooterMec.isReadyToShoot();
  }

  public boolean isFlyWheelReadyToShoot() {
    return shooterMec.isFlyWheelReadyToShoot();
  }

  public void disintegrate() {
    shooterMec.disintegrate();
  }

  public boolean isIndexerActive() {
    return shooterMec.activateIndexer;
  }

  @Override
  public void periodic() {
    shooterMec.updateInputs();
    if (disintegrate.getAsBoolean()) {
      disintegrate();
    }

    Logger.recordOutput(getName() + "/isFlyWheelReadyToShoot",
        isFlyWheelReadyToShoot() ? "FlyWheel-true" : "FlyWheel-false");
    Logger.recordOutput(getName() + "/isIndexerActive",
        isIndexerActive() ? "IndexerActive-true" : "IndexerActive-false");
    Logger.recordOutput(getName() + "/isReadyToShoot",
        isReadyToShoot() ? "Ready-true" : "Ready-false");
    Logger.recordOutput(getName() + "/state", state);
    Logger.recordOutput(getName() + "/allMotorsConnected", isConnected());


    LoggedTracer.record(getName());
  }

}
