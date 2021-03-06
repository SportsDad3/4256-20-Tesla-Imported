package frc.robot;

import java.util.logging.Logger;

public final class SwerveModule {
	public static final double ROTATOR_GEAR_RATIO = 1.0;
	public static final double TRACTION_GEAR_RATIO = 52.0 / 9.0;// updated 2019
	public static final double TRACTION_WHEEL_CIRCUMFERENCE = 4.0 * Math.PI;// inches
	// private final Talon rotation;
	private final RotationControl rotationControl;
	private final TractionControl tractionControl;
	private final double tareAngle;

	private double decapitated = 1.0;
	private double tractionDeltaPathLength = 0.0;
	private double tractionPreviousPathLength = 0.0;

	// This constructor is intended for use with the module which has an encoder on
	// the traction motor.
	public SwerveModule(final int rotatorID, int analogEncoderID, final boolean flippedSensor, final int tractionID,
			final boolean isTractionInverted, final double tareAngle) {
		// rotation = new Talon(rotatorID, ROTATOR_GEAR_RATIO, Talon.position,
		// Encoder.ANALOG, flippedSensor);
		rotationControl = new RotationControl(rotatorID, analogEncoderID);
		tractionControl = new TractionControl(tractionID);
		this.tareAngle = tareAngle;
	}

	/**
	 * This function prepares each motor individually, including setting PID values
	 * for the rotator.
	 **/
	public void init() {
	}

	/**
	 * This sets the tare angle. Positive means clockwise and negative means
	 * counter-clockwise.
	 **/
	public void setTareAngle(final double tareAngle) {
		setTareAngle(tareAngle, false);
	}

	/**
	 * This sets the tare angle. Positive means clockwise and negative means
	 * counter-clockwise. If relativeReference is true, tareAngle will be
	 * incremented rather than set.
	 **/
	public void setTareAngle(double tareAngle, final boolean relativeReference) {
		if (relativeReference)
			tareAngle += rotationControl.GetTareAngle();
		rotationControl.SetTareAngle(tareAngle);
	}

	/**
	 * Use wheel_chassisAngle to specify the wheel's orientation relative to the
	 * robot in degrees.
	 **/
	// public void swivelTo(final double wheel_chassisAngle) {
	// rotation.quickSet(decapitateAngle(wheel_chassisAngle), true);

	// }
	public void swivelTo(double targetAngle) {
		rotationControl.SetAngle(decapitateAngle(targetAngle));
	}

	/**
	 * Use wheel_fieldAngle to specify the wheel's orientation relative to the field
	 * in degrees.
	 **/
	public void swivelWith(final double wheel_fieldAngle, final double chassis_fieldAngle) {
		swivelTo(convertToRobot(wheel_fieldAngle, chassis_fieldAngle));
	}

	/**
	 * This function sets the master and slave traction motors to the specified
	 * speed, from -1 to 1. It also makes sure that they turn in the correct
	 * direction, regardless of decapitated state.
	 **/
	public void set(final double speed) {
		tractionControl.set(speed * decapitated);
	}

	public void checkTractionEncoder() {
		final double currentPathLength = tractionPathLength();
		tractionDeltaPathLength = currentPathLength - tractionPreviousPathLength;
		tractionPreviousPathLength = currentPathLength;
	}

	/**
	 * A shortcut to call completeLoopUpdate on all the Talons in the module.
	 **/
	public void completeLoopUpdate() {
		rotationControl.completeLoopUpdate();
		tractionControl.completeLoopUpdate();
	}

	/**
	 * Threshold should be specified in degrees. If the rotator is within that many
	 * degrees of its target, this function returns true.
	 **/
	public boolean isThere(final double threshold) {
		return Math.abs(rotationControl.getRotationMotor().getPIDError()) <= threshold;
	}

	/**
	 * This function makes sure the module rotates no more than 90 degrees from its
	 * current position. It should be used every time a new angle is being set to
	 * ensure quick rotation.
	 **/
	public double decapitateAngle(double endAngle) {
		double encoderPosition = rotationControl.getCurrentAngle();
		while (endAngle <= -180) {
			endAngle += 360;
		}
		while (endAngle > 180) {
			endAngle -= 360;
		}

		while (endAngle - encoderPosition > 180) {
			encoderPosition += 360;
		}

		while (endAngle - encoderPosition < -180) {
			encoderPosition -= 360;
		}

		if (Math.abs(endAngle - encoderPosition) > 90) {
			decapitated = -1;
		} else {
			decapitated = 1;
		}

		return decapitated == -1 ? endAngle + 180 : endAngle;

	}

	public double tractionSpeed() {
		return TRACTION_WHEEL_CIRCUMFERENCE * tractionControl.getRPS();// returns in/sec
	}

	public double tractionPathLength() {
		// return tractionControl.getPosition()*TRACTION_WHEEL_CIRCUMFERENCE/12.0;
		return 0;
	}

	public double deltaDistance() {
		return tractionDeltaPathLength;
	}

	public double deltaXDistance() {
		return tractionDeltaPathLength
				* Math.sin(convertToField(rotationControl.getCurrentAngle(), Robot.gyroHeading) * Math.PI / 180.0);
	}

	public double deltaYDistance() {
		return tractionDeltaPathLength
				* Math.cos(convertToField(rotationControl.getCurrentAngle(), Robot.gyroHeading) * Math.PI / 180.0);
	}

	public RotationControl getRotationMotor() {
		return rotationControl;
	}

	public TractionControl getTractionMotor() {
		return tractionControl;
	}

	public double getDecapitated() {
		return decapitated;
	}

	public void setParentLogger(final Logger logger) {
		// rotationControl.setParentLogger(logger);
		// tractionControl.setParentLogger(logger);
	}

	/**
	 * This function translates angles from the robot's perspective to the field's
	 * orientation. It requires an angle and input from the gyro.
	 **/
	public static double convertToField(final double wheel_robotAngle, final double chassis_fieldAngle) {
		return Compass.validate(wheel_robotAngle + chassis_fieldAngle);
	}

	/**
	 * This function translates angles from the field's orientation to the robot's
	 * perspective. It requires an angle and input from the gyro.
	 **/
	public static double convertToRobot(final double wheel_fieldAngle, final double chassis_fieldAngle) {
		return Compass.validate(wheel_fieldAngle - chassis_fieldAngle);
	}
	
	
	public void resetEncoderValue(){
		tractionControl.resetEncoder();
	}


    public double getIntegratedSensorENcoderCounts(){
		return tractionControl.getPositionFromIntegratedSensor();
	}

	public double getRPM() {
		return tractionControl.getRPM();
	}

	public double getAngle() {
		return rotationControl.getCurrentAngle();
	}
    
}