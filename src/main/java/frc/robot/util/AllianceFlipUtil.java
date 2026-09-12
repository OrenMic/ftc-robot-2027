package frc.robot.util;

import edu.wpi.first.math.geometry.*;
import miscar.util.AllianceUtil;

public class AllianceFlipUtil {
  public static double applyX(double x, boolean flip) {
    return flip ? FieldConstants.length - x : x;
  }

  public static double applyX(double x) {
    return applyX(x, shouldFlip());
  }

  public static Translation2d applyX(Translation2d translation, boolean flip) {
    return new Translation2d(flip ? FieldConstants.length - translation.getX() : translation.getX(),
        translation.getY());
  }

  public static Translation2d applyX(Translation2d translation) {
    return applyX(translation, shouldFlip());
  }

  public static Translation2d applyY(Translation2d translation, boolean flip) {
    return new Translation2d(translation.getX(),
        flip ? FieldConstants.length - translation.getY() : translation.getY());
  }

  public static Translation2d applyY(Translation2d translation) {
    return applyY(translation, shouldFlip());
  }

  public static double applyY(double y, boolean flip) {
    return flip ? FieldConstants.width - y : y;
  }

  public static double applyY(double y) {
    return applyY(y, shouldFlip());
  }

  public static Translation2d apply(Translation2d translation, boolean flip) {
    return new Translation2d(applyX(translation.getX(), flip), applyY(translation.getY(), flip));
  }

  public static Translation2d apply(Translation2d translation) {
    return apply(translation, shouldFlip());
  }

  public static Rotation2d apply(Rotation2d rotation, boolean flip) {
    return flip ? rotation.rotateBy(Rotation2d.kPi) : rotation;
  }

  public static Rotation2d apply(Rotation2d rotation) {
    return apply(rotation, shouldFlip());
  }

  public static Pose2d apply(Pose2d pose, boolean flip) {

    return flip ? new Pose2d(apply(pose.getTranslation(), flip), apply(pose.getRotation(), flip))
        : pose;
  }

  public static Pose2d apply(Pose2d pose) {

    return apply(pose, shouldFlip());
  }

  public static Translation3d apply(Translation3d translation, boolean flip) {
    return new Translation3d(applyX(translation.getX(), flip), applyY(translation.getY(), flip),
        translation.getZ());
  }

  public static Translation3d apply(Translation3d translation) {
    return apply(translation, shouldFlip());
  }

  public static Rotation3d apply(Rotation3d rotation, boolean flip) {
    return flip ? rotation.rotateBy(new Rotation3d(0.0, 0.0, Math.PI)) : rotation;
  }

  public static Rotation3d apply(Rotation3d rotation) {
    return apply(rotation, shouldFlip());
  }

  public static Pose3d apply(Pose3d pose, boolean flip) {
    return new Pose3d(apply(pose.getTranslation(), flip), apply(pose.getRotation(), flip));
  }

  public static Pose3d apply(Pose3d pose) {
    return apply(pose, shouldFlip());
  }

  public static boolean shouldFlip() {
    return !AllianceUtil.isRedAlliance();
  }
}
