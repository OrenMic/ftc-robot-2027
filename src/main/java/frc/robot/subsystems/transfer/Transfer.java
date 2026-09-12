package frc.robot.subsystems.transfer;

import static frc.robot.Tuners.Transfer.beltVelocityOffset;
import static frc.robot.generated.transfer.TransferConstants.beltConstants;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.util.LoggedTracer;
import lombok.Getter;
import miscar.mecsIOs.Mechanism;

public class Transfer extends SubsystemBase {
  {
    setName("Transfer");
  }
  public Mechanism belt;


  @Getter
  private TransferState state = TransferState.IDLE;

  // private Command pulseCommand;
  private String statePath = getName() + "/state";

  LoggedNetworkBoolean disintegrate = new LoggedNetworkBoolean(getName() + "/disintegrate", false);

  public Transfer(Mechanism belt) {
    this.belt = belt;

    belt.setName(getName() + "/Belt");

    belt.setRatio(beltConstants.mecRatiosConstants.config);
    belt.setMaxVelocity(beltConstants.mecMovementConstants.beltMaxVelocity);

  }

  public void setState(TransferState wantedState) {
    state = wantedState;
    if (state == TransferState.IDLE) {
      stop();

      return;
    }

    double beltVelocity = state.beltVelocity;

    if (state != TransferState.PROCESSING) {
      beltVelocity += beltVelocityOffset.getAsDouble();
    }
    belt.setTargetMecVelocity(beltVelocity);
  }

  public boolean isConnected() {
    return belt.inputs.connected;
  }

  private void stop() {
    belt.setPower(0);
  }

  public void disintegrate() {
    stop();
    belt.disintegrate();
  }

  @Override
  public void periodic() {
    belt.updateInputs();

    if (disintegrate.getAsBoolean()) {
      disintegrate();
    }

    Logger.recordOutput(getName() + "/allMotorsConnected", isConnected());
    Logger.recordOutput(statePath, state);

    LoggedTracer.record(getName());
  }
}
