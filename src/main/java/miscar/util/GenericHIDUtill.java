package miscar.util;

import edu.wpi.first.wpilibj.GenericHID;

public class GenericHIDUtill {
  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently Up
   */
  public static boolean povUp(GenericHID hid) {
    return hid.getPOV() == 0;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently UpRight
   */
  public static boolean povUpRight(GenericHID hid) {
    return hid.getPOV() == 45;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently Right
   */
  public static boolean povRight(GenericHID hid) {
    return hid.getPOV() == 90;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently DownRight
   */
  public static boolean povDownRight(GenericHID hid) {
    return hid.getPOV() == 135;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently Down
   */
  public static boolean povDown(GenericHID hid) {
    return hid.getPOV() == 180;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently DownLeft
   */
  public static boolean povDownLeft(GenericHID hid) {
    return hid.getPOV() == 225;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently Left
   */
  public static boolean povLeft(GenericHID hid) {
    return hid.getPOV() == 270;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently UpLeft
   */
  public static boolean povUpLeft(GenericHID hid) {
    return hid.getPOV() == 315;
  }

  /**
   * @param hid - the hid to get the pov angle from
   * @return whether the pov is currently Center
   */
  public static boolean povCenter(GenericHID hid) {
    return hid.getPOV() == -1;
  }
}
