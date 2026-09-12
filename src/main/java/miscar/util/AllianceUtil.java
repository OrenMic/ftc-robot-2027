package miscar.util;

import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import lombok.Getter;

public class AllianceUtil {
  private static AllianceUtil instance;
  @Getter
  private static final String prefrensesPath = "Alliance/isRed";
  private Alliance currntAlliance;

  public static void configureTo(Alliance alliance) {
    if (instance == null)
      instance = new AllianceUtil(alliance);
    else
      throw new RuntimeException("you tried to configure the alliance color more then one time");
  }

  public static void reloadConfiguration() {
    boolean isRedAlliance =
        Preferences.getString(prefrensesPath, Alliance.Red.name()).equals(Alliance.Red.name());
    configureTo(isRedAlliance ? Alliance.Red : Alliance.Blue);
  }

  public static boolean isRedAlliance() {
    return AllianceUtil.instance.currntAlliance == Alliance.Red;
  }

  public static Alliance getCurrntAlliance() {
    return AllianceUtil.instance.currntAlliance;
  }

  private AllianceUtil(Alliance alliance) {
    this.currntAlliance = alliance;
  }
}
