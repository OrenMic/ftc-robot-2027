// package miscar.configs.mecs;

// import miscar.annotation.ConstantsConstructor;
// import miscar.annotation.ConstantsName;
// import miscar.configs.Config;
// import miscar.mecsIOs.features.EncoderFeature.ResetType;

// @ConstantsName(constantsName = "externalEncoderConfig")
// public class EncoderConfig extends Config {

// private int externalEncoderPort;
// private final double encoderToMecRatio;
// private boolean encoderInverted;
// private final ResetType resetType;

// @ConstantsConstructor
// public EncoderConfig(
// int externalEncoderPort,
// boolean encoderInverted,
// double encoderToMecRatio,
// ResetType resetType) {
// this.externalEncoderPort = externalEncoderPort;
// this.encoderInverted = encoderInverted;
// this.encoderToMecRatio = encoderToMecRatio;
// this.resetType = resetType;
// }

// /**
// * @param externalEncoderPort - The new port this external encoder
// shall be configured to
// * @return This config with the new {@code externalEncoderPort}
// */
// public EncoderConfig withEncoderPort(int externalEncoderPort) {
// this.externalEncoderPort = externalEncoderPort;
// return this;
// }

// /**
// * @return the port of the external encoder
// */
// public int getEncoderPort() {
// return externalEncoderPort;
// }

// /**
// * @return Whether or not the external encoder is inverted
// */
// public boolean getIsInverted() {
// return encoderInverted;
// }

// /**
// * @return The ratio of encoder movement to mec movement of
// */
// public double getEncoderToMecRatio() {
// return encoderToMecRatio;
// }

// /**
// * @return resetType - the {@code resetType} this encoder is
// configured to, see {@link ResetType}
// * for more
// */
// public ResetType getResetType() {
// return resetType;
// }

// /**
// * @param inverted - whether or not this external encoder is
// inverted
// * @return This config with the new {@code inverted} value
// */
// public EncoderConfig withInverted(boolean inverted) {
// this.encoderInverted = inverted;
// return this;
// }

// @Override
// public EncoderConfig clone() {
// return new EncoderConfig(externalEncoderPort, encoderInverted,
// encoderToMecRatio, resetType);
// }
// }
