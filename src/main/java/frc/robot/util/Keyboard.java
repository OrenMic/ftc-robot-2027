package frc.robot.util;

import edu.wpi.first.wpilibj.GenericHID;

public class Keyboard {
  private GenericHID controller;

  public Keyboard(int port) {
    controller = new GenericHID(port);
  }

  // generic button function. change as you see fit
  public boolean getButton() {
    return controller.getRawButton(13);
  }
}
