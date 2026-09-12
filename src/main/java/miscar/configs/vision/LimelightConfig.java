package miscar.configs.vision;

import edu.wpi.first.math.geometry.Pose3d;
import miscar.annotation.ConstantsConstructor;
import miscar.annotation.ConstantsName;

@ConstantsName(constantsName = "limelightConstants")
public class LimelightConfig extends CameraConfig {

  public boolean useMegaBotPose2 = false;
  public boolean useMegaBotPose1 = false;

  @ConstantsConstructor
  public LimelightConfig(String name, double stdDevFactors, Pose3d robotToCamera) {
    super(name, stdDevFactors, robotToCamera);
  }

  public LimelightConfig enableMegaBotPose1(boolean enable) {
    useMegaBotPose1 = enable;
    return this;
  }

  public LimelightConfig enableMegaBotPose2(boolean enable) {
    useMegaBotPose2 = enable;
    return this;
  }
}
