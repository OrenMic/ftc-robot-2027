package tests.miscar.mecsIOs;

import static org.junit.jupiter.api.Assertions.fail;

import miscar.mecsIOs.AbstractMechanism;
import miscar.motorIOs.EmptyMotor;
import org.junit.jupiter.api.Test;

public class AbstractMechanismTest {
  @Test
  public void testSetMotorToMecRatioEdgeCases() {
    AbstractMechanism mech = new AbstractMechanism(new EmptyMotor(-1)) {};
    try {
      mech.setMotorToMecRatio(0);
      fail("You set motorToMecRatio to 0 and it didn't explode. Math is crying.");
    } catch (IllegalArgumentException e) {
    }
    try {
      mech.setMotorToMecRatio(-1);
      fail("Negative motorToMecRatio? What are you, a time traveler?");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSetMaxMovementEdgeCases() {
    AbstractMechanism mech = new AbstractMechanism(new EmptyMotor(-1)) {};
    try {
      mech.setMaxMovement(0);
      fail("Zero max movement? The mechanism will never move. Genius!");
    } catch (IllegalArgumentException e) {
    }
    try {
      mech.setMaxMovement(-10);
      fail("Negative max movement? Are we moving backwards forever?");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSetMaxVelocityEdgeCases() {
    AbstractMechanism mech = new AbstractMechanism(new EmptyMotor(-1)) {};
    try {
      mech.setMaxVelocity(0);
      fail("Zero max velocity? The mechanism is now a statue.");
    } catch (IllegalArgumentException e) {
    }
    try {
      mech.setMaxVelocity(-2);
      fail("Negative max velocity? Reverse only mode is not supported.");
    } catch (IllegalArgumentException e) {
    }
  }
}
