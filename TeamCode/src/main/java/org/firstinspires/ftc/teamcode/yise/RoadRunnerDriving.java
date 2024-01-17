package org.firstinspires.ftc.teamcode.yise;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.opencv.core.Mat;

public class RoadRunnerDriving {

    //Drive class
    SampleMecanumDrive drive;

    // Used to track slow-mode versus normal mode
    public Speeds currentSpeed;
    double speedMultiplier;
    public enum Speeds {
        SLOW,
        NORMAL
    }


    //Coordinates of all truss poles on the field
    //2D array where each sub-array is a set of coordinates
    int[][] avoidancePositions = { {0, 70}, {0, 48}, {0, 24}, {0, -24}, {0, -48}, {0, -70}, {-24, 70}, {-25, 48}, {-24, 24}, {-24, -24}, {-24, -48}, {-24, -70} };

    //Distance from the poles before repulsion takes effect
    int repulsionRadius = 12;


    //Declare the constructor for the class
    public RoadRunnerDriving(HardwareMap hardwareMap) {
        drive = new SampleMecanumDrive(hardwareMap);
        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        drive.setPoseEstimate(new Pose2d(0, 0, Math.toRadians(0)));
        //drive.setPoseEstimate(PoseStorage.currentPose);

        // set default value for speed
        currentSpeed = Speeds.NORMAL;
        speedMultiplier = 1;
    }

    //Drives the robot based on gamepad input
    public void updateMotorsFromStick(Gamepad gamepad) {
        double repulsionPower = 0;

        //Loop through every obstacle position
        for (int i = 0; i < avoidancePositions.length; i++) {
            //Get x and y distance between the robot and every obstacle
            double xDist = drive.getPoseEstimate().getX() - avoidancePositions[i][0];
            double yDist = drive.getPoseEstimate().getY() - avoidancePositions[i][1];

            //Get direct distance between the robot and every obstacle
            double distanceToPoint = Math.sqrt( Math.pow(xDist, 2) + Math.pow(yDist, 2));

            //Check to see if the robot is close enough to the point to be affected by it
            if (distanceToPoint < repulsionRadius) {
                //Use the y distance between the robot and the point to create a sort of power gradient that gets weaker the further away the robot is
                //Ex: Point radius is 12 inches. Robot is 6 inches away from the point. Repulsion power = (12-6)/12 = 0.5
                repulsionPower = (repulsionRadius - Math.abs(yDist))/repulsionRadius;
            }

            //If robot is left of the obstacle, flip the repulsion power so the robot navigates away from pole instead of into it :)
            if (yDist < 0) {
                repulsionPower = - repulsionPower;
            }
        }

        //Set drive power based on gamepad inputs multiplied by the speed variable. For the y, also add on the repulsionPower for obstacle avoidance
        drive.setWeightedDrivePower(new Pose2d(
                -gamepad.left_stick_y * speedMultiplier,
                (-gamepad.left_stick_x * speedMultiplier) + repulsionPower,
                -gamepad.right_stick_x * speedMultiplier
        ));
    }


    //Toggles fast and slow speeds
    public void toggleSlowMode(Speeds targetSpeed) {

        // Set the speedMultiplier in case of SLOW mode
        if (currentSpeed == Speeds.SLOW) {
            currentSpeed = Speeds.NORMAL;
            speedMultiplier = 1;
        } else {
            currentSpeed = Speeds.SLOW;
            speedMultiplier = 0.5;
        }
    }

    //Updates the roadrunner coords; must be called every tick
    public void update() {
        drive.update();
    }

    //Accessor to get the robot position
    public Pose2d getPosition() {
        return drive.getPoseEstimate();
    }

    //Calibrate position using the middle april tag
    public void calibratePos(AprilTagDetection detection) {
        if (Parameters.allianceColor == Parameters.Color.RED) {
            drive.setPoseEstimate(new Pose2d(55.5 - detection.ftcPose.y, -(36.25 + detection.ftcPose.x), drive.getPoseEstimate().getHeading()));
        } else {
            drive.setPoseEstimate(new Pose2d(55.5 - detection.ftcPose.y, 36.25 + detection.ftcPose.x, drive.getPoseEstimate().getHeading()));
        }
    }

    //Roadrunner pathing to navigate to a corner based on alliance color
    public void navigateToCorner() {
        //Check if it is not driving already
        if (!drive.isBusy()) {
            //Check for alliance color
            if (Parameters.allianceColor == Parameters.Color.RED) {
                Trajectory navigateToCorner = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(-51, 51, Math.toRadians(135)))
                        .build();
                drive.followTrajectory(navigateToCorner);

            } else {
                Trajectory navigateToCorner = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(-51, -51, Math.toRadians(-135)))
                        .build();
                drive.followTrajectory(navigateToCorner);

            }
        }
    }

    //Navigate to closest board distance
    public void dropPixelNear() {

        if (!drive.isBusy()) {
            if (Parameters.allianceColor == Parameters.Color.RED) {

                Trajectory dropPixelNear = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(50.5,-36, Math.toRadians(180)))
                        .build();
                drive.followTrajectory(dropPixelNear);

            } else {

                Trajectory dropPixelNear = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(50.5,36, Math.toRadians(180)))
                        .build();
                drive.followTrajectory(dropPixelNear);

            }
        }
    }

    //Navigate to middle board distance
    public void dropPixelMid() {

        if (!drive.isBusy()) {
            if (Parameters.allianceColor == Parameters.Color.RED) {

                Trajectory dropPixelMid = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(46,-36, Math.toRadians(180)))
                        .build();
                drive.followTrajectory(dropPixelMid);

            } else {

                Trajectory dropPixelMid = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(46,36, Math.toRadians(180)))
                        .build();
                drive.followTrajectory(dropPixelMid);

            }
        }
    }

    //Navigate to far board distance
    public void dropPixelFar() {

        if (!drive.isBusy()) {
            if (Parameters.allianceColor == Parameters.Color.RED) {

                Trajectory dropPixelFar = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(39,-36, Math.toRadians(180)))
                        .build();
                drive.followTrajectory(dropPixelFar);

            } else {

                Trajectory dropPixelFar = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToLinearHeading(new Pose2d(39,36, Math.toRadians(180)))
                        .build();
                drive.followTrajectory(dropPixelFar);

            }
        }
    }
}

