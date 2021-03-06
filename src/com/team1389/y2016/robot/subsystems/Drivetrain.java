package com.team1389.y2016.robot.subsystems;

import com.team1389.base.Subsystem;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;

public class Drivetrain extends Subsystem{
	CANTalon left;
	CANTalon right;
	public Drivetrain(CANTalon leftMotors, CANTalon rightMotors) {
//		super(leftMotors, rightMotors);
		left = leftMotors;
		right = rightMotors;
	}
	
	public void set(double leftSpeed, double rightSpeed){

		left.changeControlMode(TalonControlMode.PercentVbus);
		right.changeControlMode(TalonControlMode.PercentVbus);
		left.set(leftSpeed);
		right.set(rightSpeed);
	}
}
