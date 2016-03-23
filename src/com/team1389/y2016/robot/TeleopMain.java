package com.team1389.y2016.robot;

import org.strongback.command.Command;
import org.strongback.components.ui.ContinuousRange;

import com.team1389.base.TeleopBase;
import com.team1389.base.util.CommandsUtil;
import com.team1389.base.util.DoubleConstant;
import com.team1389.base.util.control.ConfigurablePid;
import com.team1389.base.util.control.ConfigurablePid.PIDConstants;
import com.team1389.base.util.control.PositionControllerRampCommand;
import com.team1389.base.util.control.SetpointProvider;
import com.team1389.base.util.control.SpeedControllerSetCommand;
import com.team1389.base.util.testing.TalonMonitorCommand;
import com.team1389.y2016.robot.commands.JoystickDriveCommand;
import com.team1389.y2016.robot.commands.JoystickMotorCommand;
import com.team1389.y2016.robot.control.ArmSetpointProvider;
import com.team1389.y2016.robot.control.FlywheelControl;
import com.team1389.y2016.robot.control.FlywheelControlCommand;
import com.team1389.y2016.robot.control.IntakeControlCommand;
import com.team1389.y2016.robot.control.LowGoalElevationControl;
import com.team1389.y2016.robot.control.TurntableControl;

public class TeleopMain extends TeleopBase{
	RobotLayout layout;
	ConfigurablePid pidC;
	
	public TeleopMain(RobotLayout layout) {
		this.layout = layout;
		System.out.println("layout in teleop:" + layout);
//		pidC = new ConfigurablePid("pid config", new PIDConstants(0, 0, 0, 0, 0));
//		target = new DoubleConstant("maxChange", 0.1);
	}

	@Override
	public void setupTeleop() {
		layout.io.configFollowerTalonsToWorkAroundDumbGlitch();
//		layout.subsystems.armSetpointProvider.setSetpoint(layout.io.armElevationMotor.getPosition());
		layout.subsystems.initAll();
	}

	@Override
	public  Command provideCommand() {
		
		SetpointProvider yAxis = new LowGoalElevationControl(layout.io.controllerManip.getAxis(1));

//		Command elevation = new PositionControllerRampCommand(layout.io.armElevationMotor, 
//				yAxis, new PIDConstants(.6, 0, 0, 0, 0), .24, 0, .12);
		
		//uncomment
		Command elevationPidDo = layout.subsystems.elevation;
		Command elevationControl = new ArmSetpointProvider(layout.io.controllerManip, layout.subsystems.armSetpointProvider);
		Command elevation = CommandsUtil.combineSimultaneous(elevationControl, elevationPidDo);
		Command turntable = new TurntableControl(layout.io.controllerManip, layout.io.simpleTurntable);

		Command drive = new JoystickDriveCommand(layout.subsystems.drivetrain, layout.io.controllerDriver, 1.0);

		Command intake = new IntakeControlCommand(layout.io.intakeMotor, layout.io.controllerManip.getAxis(1),
				layout.io.controllerManip.getButton(9), layout.io.ballHolderIR1);
		
		Command flywheelBasic = new FlywheelControl(layout.io.flywheelMotorA, layout.io.controllerManip);
//		Command flywheel = CommandsUtil.combineSimultaneous(
//				new FlywheelControlRPS(layout.subsystems.flywheelSetpointProvider, layout.io.controllerManip),
//				layout.subsystems.flywheelFollowCommand
//		);
		
		SpeedControllerSetCommand flywheelSpeed = new SpeedControllerSetCommand(layout.subsystems.flywheelSpeedController, 0.0);
		Command flywheel = new FlywheelControlCommand(layout.io.controllerManip, flywheelSpeed);
		Command flywheelAll = CommandsUtil.combineSimultaneous(flywheel, flywheelSpeed);
		
//		SetpointProvider xAxis = new JoystickSetpointControlAriStyleWithReset(layout.io.controllerDriver.getAxis(3),
//				 layout.io.controllerDriver.getButton(1), -.3, .3, 0.003, 0);
//		
//		
//		Command yaw = new PositionControllerRampCommand(layout.io.turntableMotor, xAxis,
//				new PIDConstants(1, 0, 0, 0, 0), .3, -.3, .12);
				
		Command monitorFlywheel = new TalonMonitorCommand(layout.io.flywheelMotorA, "flywheel speed");
		
		Command testIntake = new JoystickMotorCommand(layout.io.intakeMotor, layout.io.controllerDriver.getAxis(0), 1.0);
//		return CommandsUtil.combineSimultaneous(testIntake, flywheelBasic, monitorFlywheel);
		
//		return CommandsUtil.combineSimultaneous(flywheel, monitorFlywheel, testIntake);
		
		Command monitorTurntable = new TalonMonitorCommand(layout.io.simpleTurntable, "turntable");

		SetpointProvider xAxis = new JoystickSetpointControlAriStyleWithReset(layout.io.controllerManip.getAxis(0),
				 layout.io.controllerManip.getButton(1), -.3, .3, 0.003, 0);
		
		//comment out this section for arm control
		xAxis = new SetpointProvider() {
			
			@Override
			public double getSetpoint() {
				return 0;
			}
		};

		Command yaw = new PositionControllerRampCommand(layout.io.turntableMotor, xAxis,
				new PIDConstants(1, 0, 0, 0, 0), .3, -.3, .12);
		
		return CommandsUtil.combineSimultaneous(drive, yaw, monitorTurntable, elevation, intake, flywheelAll);
	}
	
	private SetpointProvider joystickSetpointProvider(ContinuousRange joystickAxis, double max, double min){
		return new SetpointProvider(){

			@Override
			public double getSetpoint() {
				double joy = joystickAxis.read();
				if (joy > 0){
					joy *= max;
				} else {
					joy *= min;
				}
				return joy;
			}
			
		};
	}
}
