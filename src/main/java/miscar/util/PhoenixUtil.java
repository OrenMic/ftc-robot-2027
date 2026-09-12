// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package miscar.util;

import com.ctre.phoenix6.StatusCode;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import java.util.function.Supplier;

public class PhoenixUtil {

  /** Attempts to run the command until no error is produced. */
  public static void tryUntilOk(int maxAttempts, Supplier<StatusCode> command, int port,
      String... deviceIdentity) {
    // if the device does't tell us its type (motor/encoder etc) then
    // revert to motor
    String Identity = deviceIdentity.length > 0 ? deviceIdentity[0] : "Motor";
    Alert alert = new Alert(Identity + " " + port + " configuration failed ", AlertType.kError);
    boolean failed = true;
    for (int i = 0; i < maxAttempts; i++) {
      var error = command.get();
      if (error.isOK()) {
        failed = false;
        break;
      }
    }
    alert.set(failed);
  }
}
