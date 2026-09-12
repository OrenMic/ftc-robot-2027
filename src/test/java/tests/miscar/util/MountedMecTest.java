package tests.miscar.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MountedMecTest {

  @Test
  public void checkIllegalEdgeCases() {
    // Edge case: motorToMecRatio == 0
    try {
      new miscar.util.MountedMec(() -> 0, 0);
      fail(
          "I don't know how, but you made MountedMec(() -> 0,1,0) NOT crash. Congratulations, you broke math!");
    } catch (IllegalArgumentException e) {
      // expected, do nothing
    }

    // Edge case: motorToMecRatio < 0
    try {
      new miscar.util.MountedMec(() -> 0, -1);
      fail(
          "Did you just invent time travel? Because negative gear ratios would make things spin backwards in time!");
    } catch (IllegalArgumentException e) {
      // expected, do nothing
    }
  }

  @Test
  public void testValidConstructionAndGetters() {
    miscar.util.MountedMec mec = new miscar.util.MountedMec(() -> 2.0, 4.0);
    assertNotNull(mec);
    assertEquals(2.0 * 4.0, mec.GetRawMecPose(), 1e-9);
    assertEquals(mec.GetRawMecPose(), mec.GetMecPose(), 1e-9); // offset default 0
    assertEquals(mec.GetMecPose(), mec.GetRobotRelativeMecPose(), 1e-9); // startPose default 0
    assertEquals(mec.GetRawMecPose() / 4.0, mec.GetMotorPose(), 1e-9);
    assertEquals(0.0, mec.getOffset(), 1e-9);
    assertEquals(0.0, mec.getStartPose(), 1e-9);
  }

  @Test
  public void testConstructorWithoutEncoder() {
    miscar.util.MountedMec mec = new miscar.util.MountedMec(() -> 5.0, 2.0);
    assertNotNull(mec);
    assertEquals(5.0 * 2.0, mec.GetRawMecPose(), 1e-9);
  }

  @Test
  public void testUpdateMotorToMecRatio() {
    miscar.util.MountedMec mec = new miscar.util.MountedMec(() -> 1.0, 3.0);
    mec.updateMotorToMecRatio(5.0);
    assertEquals(1.0 * 5.0, mec.GetRawMecPose(), 1e-9);
    assertEquals((1.0 * 5.0) / 5.0, mec.GetMotorPose(), 1e-9);
  }

  @Test
  public void testSetStartPoseAndOverrideOffset() {
    miscar.util.MountedMec mec = new miscar.util.MountedMec(() -> 2.0, 4.0);
    mec.setStartPose(7.0);
    mec.overrideOffset(5.0);
    assertEquals((2.0 * 4.0) - 5.0, mec.GetMecPose(), 1e-9);
    assertEquals(((2.0 * 4.0) - 5.0) + 7.0, mec.GetRobotRelativeMecPose(), 1e-9);
    assertEquals(5.0, mec.getOffset(), 1e-9);
    assertEquals(7.0, mec.getStartPose(), 1e-9);
  }

  @Test
  public void testUpdateOffset() {
    miscar.util.MountedMec mec = new miscar.util.MountedMec(() -> 3.0, 1.0);
    double newOffset = mec.updateOffset();
    assertEquals(3.0 * 1.0, newOffset, 1e-9);
    assertEquals(newOffset, mec.getOffset(), 1e-9);
  }

  @Test
  public void testConversions() {
    miscar.util.MountedMec mec = new miscar.util.MountedMec(() -> 4.0, 8.0);
    mec.overrideOffset(1.0);
    mec.setStartPose(3.0);
    // MecToMotor (accounts for offset)
    assertEquals((5.0 - 1.0) / 8.0, mec.MecToMotor(5.0), 1e-9);
    // MecToMotorRaw (no offset)
    assertEquals(5.0 / 8.0, mec.MecToMotorRaw(5.0), 1e-9);
    // RobotRelativeMecToMotor (accounts for offset and startPose)
    assertEquals(((5.0 - 3.0) - 1.0) / 8.0, mec.RobotRelativeMecToMotor(5.0), 1e-9);
    // RobotRelativeMecToMotorRaw (no offset)
    assertEquals((5.0 - 3.0) / 8.0, mec.RobotRelativeMecToMotorRaw(5.0), 1e-9);
    // MotorToMec
    assertEquals(2.0 * 8.0, mec.MotorToMec(2.0), 1e-9);
  }

  @Test
  public void testFallbackToMotorRatio() {
    // Construct with only motorToMecRatio (no encoderToMecRatio)
    miscar.util.MountedMec mec = new miscar.util.MountedMec(() -> 2.0, 5.0);
    // Should use motorToMecRatio for calculations
    assertEquals(2.0 * 5.0, mec.GetRawMecPose(), 1e-9);
    // Update motorToMecRatio and check that it updates the behavior
    mec.updateMotorToMecRatio(7.0);
    assertEquals(2.0 * 7.0, mec.GetRawMecPose(), 1e-9);
    // Also check GetMotorPose reflects the new ratio
    assertEquals((2.0 * 7.0) / 7.0, mec.GetMotorPose(), 1e-9);
  }
}
