package frc.robot.subsystems.shooter.shootingModel;

import frc.robot.subsystems.drive.util.DriveUtil;
import frc.robot.subsystems.shooter.util.ShootingUtil;
import frc.robot.util.GeomUtil;
import frc.robot.util.MisCarInterpolatingTreeMap;
import frc.robot.util.MisMath;
import lombok.experimental.ExtensionMethod;
import miscar.util.BackgroundProcess;
import java.util.function.DoubleSupplier;
import org.littletonrobotics.junction.Logger;

@ExtensionMethod(GeomUtil.class)
public class ShootingInMotion {

  static String name = "ShootingInMotion";

  public static class Tof extends MisCarInterpolatingTreeMap<Double, Double> {

    DoubleSupplier distanceSupplier;

    public Tof(DoubleSupplier distanceSupplier) {
      super(MisMath::doubleInverseLerp, MisMath::lerp);

      this.distanceSupplier = distanceSupplier;
    }

    public double interpolate() {
      return super.get(distanceSupplier.getAsDouble());
    }
  }


  public ShootingInMotion() {
    BackgroundProcess.add(() -> {
      Logger.recordOutput(name + "/angleToHUb",
          DriveUtil.getAngleToPose(ShootingUtil.getVirtualHubTargetPose()));

    });
  }

  public static final Tof hubTof = new Tof(ShootingUtil::getDistanceToHub);

  static {

    // hub tof spot
    // hubTof.put(2.0, 0.98);
    // hubTof.put(3.0, 1.06);
    // hubTof.put(4.0, 1.84);
    // hubTof.put(5.0, 1.86);
    hubTof.put(1.815271, 0.8); //
    hubTof.put(2.712417, 1.2); //
    hubTof.put(3.135584, 1.2);//
    hubTof.put(3.909216, 1.59); //
    // hubTof.put(4.0942, 1.49); //
    hubTof.put(4.228813, 1.51); //
    hubTof.put(4.93482, 1.139); //
    hubTof.put(5.21491, 1.6); // 2

  }


}
