package miscar.util.encoder2;

import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.Preferences;
import frc.robot.Constants;
import frc.robot.Constants.Mode;
import frc.robot.util.EqualsUtil;
import miscar.configs.encoder.EncoderConfig;
import miscar.util.AngleUnwrapper;
import miscar.util.MountedMec;
import org.littletonrobotics.junction.Logger;
// import miscar.util.encoder2.EncoderInputsAutoLogged;
import org.littletonrobotics.junction.networktables.LoggedNetworkBoolean;

public class Encoder {
  public final EncoderIO encoderDelegation;
  public final MountedMec poseCalc;
  public final AngleUnwrapper angleUnwrapper;
  private final String pathToSave;
  private final Alert disconnected;
  private final LoggedNetworkBoolean resetOffset;

  private EncoderInputsAutoLogged inputs = new EncoderInputsAutoLogged();
  private int port;

  public static Encoder create(EncoderConfig<?> config) {
    return new Encoder(config);
  }

  public static Encoder empty() {
    return new Encoder();
  }

  private Encoder() {
    encoderDelegation = new EncoderIO() {};
    encoderDelegation.updateInputs(inputs);
    angleUnwrapper = new AngleUnwrapper(() -> inputs.pose);
    poseCalc = new MountedMec(angleUnwrapper::calc, 1);

    this.port = -1;

    pathToSave = "Encoders/Offsets/" + port;
    disconnected = new Alert("Encoder " + port + " is not connected", AlertType.kError);
    resetOffset = new LoggedNetworkBoolean("Reset Encoders/id " + port, false);

    reloadOffset();
  }

  private Encoder(EncoderConfig<?> config) {
    encoderDelegation =
        Constants.currentMode == Mode.REAL ? config.buildEncoder() : new EncoderIO() {};

    // purge bad encoder values
    for (int i = 0; i == 10; i++)
      encoderDelegation.get();

    // encoderDelegation.updateInputs(inputs);
    // inputs.pose = encoderDelegation.get();
    angleUnwrapper = new AngleUnwrapper(() -> inputs.pose);
    poseCalc = new MountedMec(angleUnwrapper::calc, config.encoderToMecRatio);

    this.port = config.encoderPort;

    pathToSave = "Encoders/Offsets/" + port;
    disconnected = new Alert("Encoder " + port + " is not connected", AlertType.kError);
    resetOffset = new LoggedNetworkBoolean("Reset Encoders/id " + port, false);

    reloadOffset();
    // updateInputs();
  }

  public void reloadOffset() {
    double offset = Preferences.getDouble(pathToSave, 0);
    poseCalc.overrideOffset(offset);
  }

  public void saveOffset() {
    Preferences.setDouble(pathToSave, poseCalc.getOffset());
  }

  public void updateOffset() {
    poseCalc.updateOffset();
    saveOffset();
  }

  public double getMecPose() {
    // return 90;
    // return encoderDelegation.get();
    // return angleUnwrapper.calc();
    return poseCalc.GetRobotRelativeMecPose();
  }

  public void updateInputs() {
    encoderDelegation.updateInputs(inputs);
    inputs.offset = poseCalc.getOffset();
    Logger.processInputs("Encoders/Inputs " + port, inputs);
    if (!EqualsUtil.epsilonEquals(poseCalc.getOffset(), inputs.offset)) {
      poseCalc.overrideOffset(inputs.offset);
    }

    if (resetOffset.get()) {
      updateOffset();
      resetOffset.set(false);
    }

    disconnected.set(!inputs.connected && Constants.currentMode != Mode.SIM);
  }
}
