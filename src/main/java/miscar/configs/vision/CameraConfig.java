package miscar.configs.vision;

import edu.wpi.first.math.geometry.Pose3d;
import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;
import miscar.configs.Config;

@ConstantsName(constantsName = "cameraConstants")
public class CameraConfig extends Config {
  public final String name;
  public final double stdDevFactors;
  public final Pose3d robotToCamera;

  @ConstantsConstructor
  public CameraConfig(String name, double stdDevFactors, Pose3d robotToCamera) {
    this.name = name;
    this.stdDevFactors = stdDevFactors;
    this.robotToCamera = robotToCamera;
  }
}
