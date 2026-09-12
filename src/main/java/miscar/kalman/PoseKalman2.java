package miscar.kalman;

import org.littletonrobotics.junction.Logger;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class PoseKalman2 extends SubsystemBase {
    static RotationalKalman rotationalKalman =
        new RotationalKalman(new Rotation2d(), VecBuilder.fill(0.1), VecBuilder.fill(0.9));

    TranslationKalman translationKalman;

    public PoseKalman2(Vector<N3> stateStdDevs, Vector<N3> visionStdDevs) {
        translationKalman = new TranslationKalman(
            VectorUntil.getFirstN(Nat.N2(), stateStdDevs),
            VectorUntil.getFirstN(Nat.N2(), visionStdDevs),
            this::getRotationRad);
    }

    public void predict(Vector<N3> u) {
        rotationalKalman.predict(VecBuilder.fill(u.get(2)));
        translationKalman.predict(VecBuilder.fill(u.get(0), u.get(1)));
    }

    public void correct(Vector<N3> y, Vector<N3> stddevs) {
        rotationalKalman.correct(VecBuilder.fill(y.get(2)), VecBuilder.fill(stddevs.get(2)));
        translationKalman.correct(VecBuilder.fill(y.get(0), y.get(1)),
                VecBuilder.fill(stddevs.get(0), stddevs.get(1)));
    }

    public Rotation2d getRotation() {
        return rotationalKalman.getRotation();
    }

    public double getRotationRad() {
        return rotationalKalman.getRotationRad();
    }

    public double getX() {
        return translationKalman.getX();
    }

    public double getY() {
        return translationKalman.getY();
    }

    public Pose2d getPose() {
        return new Pose2d(getX(), getY(), getRotation());
    }

    public void setPose(Rotation2d rawGyro, Pose2d pose) {
        rotationalKalman.setPose(rawGyro, pose.getRotation());
        translationKalman.setPose(pose);
    }

    public double updateRotation(Rotation2d newRotation2d) {
        return rotationalKalman.updateRotation(newRotation2d);
    }

    @Override
    public void periodic() {
        Logger.recordOutput(getName() + "/rotation",
                MathUtil.angleModulus(getRotation().getRadians()));
        Logger.recordOutput(getName() + "/p", rotationalKalman.getP(0, 0));
        Logger.recordOutput(getName() + "/pose", getPose());
    }

}
