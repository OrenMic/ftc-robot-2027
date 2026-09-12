// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package frc.robot.util;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;

public record Bounds(double minX, double maxX, double minY, double maxY) {
  public enum FlipAxis {
    X, Y, XY
  }

  /**
   * Whether the translation is contained within the bounds & the
   * flipped bounds.
   */
  public boolean containsFlipped(Translation2d translation, FlipAxis flipAxis) {
    Translation2d flippedPose = switch (flipAxis) {
      case X -> AllianceFlipUtil.applyX(translation, !AllianceFlipUtil.shouldFlip());
      case Y -> AllianceFlipUtil.applyY(translation, !AllianceFlipUtil.shouldFlip());
      case XY -> AllianceFlipUtil.apply(translation, !AllianceFlipUtil.shouldFlip());
      default -> Translation2d.kZero;
    };

    return contains(flippedPose);
  }

  /**
   * Whether the translation is contained within the bounds & the
   * flipped bounds.
   */
  public boolean containsFlipped(Pose2d pose, FlipAxis flipAxis) {
    return containsFlipped(pose.getTranslation(), flipAxis);
  }

  /** Whether the translation is contained within the bounds. */
  public boolean contains(Pose2d pose) {
    return contains(pose.getTranslation());
  }

  /** Whether the translation is contained within the bounds. */
  public boolean contains(Translation2d translation) {
    return translation.getX() >= minX() && translation.getX() <= maxX()
        && translation.getY() >= minY() && translation.getY() <= maxY();

  }

  /** Clamps the translation to the bounds. */
  public Translation2d clamp(Translation2d translation) {
    return new Translation2d(MathUtil.clamp(translation.getX(), minX(), maxX()),
        MathUtil.clamp(translation.getY(), minY(), maxY()));
  }
}
