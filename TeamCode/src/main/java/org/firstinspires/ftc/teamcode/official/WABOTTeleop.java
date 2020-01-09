package org.firstinspires.ftc.teamcode.official;

/*
 * Wright Angle Robotics #6427 2019-2020
 */

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.angleDrifter.RoutableRobot;
import org.firstinspires.ftc.teamcode.angleDrifter.Vector2D;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;


@TeleOp(name="WABOTTeleop", group="WABOT")
public class  WABOTTeleop extends OpMode {


    // Declare OpMode members.
    WABOTHardware h;

    private RoutableRobot robot;

    // IMU
    WABOTImu imu;

    private WABOTVuforia vuforia;

    private final String VUFORIA_KEY = "ATs85vP/////AAABmedvSEuRQ0j9uYwlATaryQxyeVF6AtDWjTZ/2e6s8KELjPp1fDUV3Nn3X1xEZSoPk0Y81/6kr2k/8Q0xdlNkCDIJ+qBpXM8vpA+5qL7mYY6KthDalcBqD8pKiEBiSy0gW0wzniDtDR/Bf4ndSizQgoI10u9PD248vTfkt8NxJLsgM98pyCyeYZ2c16yLcASypCOhFJvljA7M6DM+qfWgWnOWXiVd2OZLsLtFcHZu4aEKjCHwqnlk9KYSI5BT8I4i+3FoE/JffsIzAl/iXMPu7w6eJJXYqNq7lGCzMRwfn+6OoYA51sy/Ahr/uyWUj/u0nzgF/IlRkteKXks+eUok5kFLeT2KxkbpNVwie11YgQRg";
    private final VuforiaLocalizer.CameraDirection CAMERA_DIRECTION = VuforiaLocalizer.CameraDirection.BACK;
    private final boolean CAMERA_IS_PORTRAIT = false;

    // Intermediate values for input
    double as1 = 0;
    double as2 = 0.178;
    double as3 = 0.8;
    float intakePow = 0;

    private DogeCV detector;

    double servoPosLeft = 0.8;
    double servoPosRight = 0.5;

    // Speed modifier for drive controls
    private final double PRECISION_SPEED_MODIFIER = 0.5;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {

        //robot = new RoutableRobot();
        // Tell the driver that initialization is complete.
        h = new WABOTHardware(hardwareMap);
        runEncoder(true);
        //imu = new WABOTImu(hardwareMap);

        detector = new DogeCV(hardwareMap);

        //vuforia = new WABOTVuforia(VUFORIA_KEY, CAMERA_DIRECTION, hardwareMap, true, CAMERA_IS_PORTRAIT, h);

        //vuforia.activate();
        //telemetry.addData("Status", "Initialized");

        detector.startStreaming();
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {
        // Starting Positions for Servos
        //imu.activate();

        h.LArmServo.setPosition(h.LEFTARMSERVO_IN);
        h.RArmServo.setPosition(h.RIGHTARMSERVO_IN);
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
        // Gamepad2 input
        input();
        //telemetry.addData("PATH: ", robot.initPointsFromFile());
        // Drive train controls
        superDrive();

        telemetry.addData("ENCODER WHEEL POSITION: ", h.BLMotor.getCurrentPosition());

//        double newX = Math.cos(Math.toRadians(getNeatRotation(imu.getHeading())))*h.FLMotor.getCurrentPosition()*0.001;
//        double newY = Math.sin(getNeatRotation(Math.toRadians(getNeatRotation(imu.getHeading())) ))*h.FLMotor.getCurrentPosition()*0.001;
//        telemetry.addData("X MOVEMENT:", newX);
//        telemetry.addData("Y MOVEMENT:", newY);
    }

    private double getNeatRotation(double heading){
        if(heading < 0){
            heading += 360;
        }

        return heading;
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
        // Updating to log
        telemetry.addData("Status:", "Stopped");

        // Stops motors just in case
        h.FLMotor.setPower(0);
        h.FRMotor.setPower(0);
        h.BLMotor.setPower(0);
        h.BRMotor.setPower(0);
    }

    private void input(){

        //telemetry.addData("Heading: ", imu.getHeading());
        //telemetry.addData("Distance Side: ", getAverageDistance());

        /*if(!vuforia.run().equals("NULL")) {
            telemetry.addData("POS Z: ", vuforia.position.z);
            telemetry.addData("POS Y: ", vuforia.position.y);
            telemetry.addData("POS X: ", vuforia.position.x);
            telemetry.addData("Z ROT: ", vuforia.rotationP.z);
        }*/


        // Triggers control intake/outtake
        if(gamepad1.right_trigger > 0){
            intakePow = gamepad1.right_trigger;
        }else if(gamepad1.left_trigger > 0){
            intakePow = -gamepad1.left_trigger;
        } else {
            intakePow = 0;
        }
        h.LIntake.setPower(-intakePow);
        h.RIntake.setPower(intakePow);

        double liftPower = -gamepad2.right_stick_y;
        double armSlidePower = -gamepad2.left_stick_y;

        if(gamepad2.left_bumper && !gamepad2.right_bumper){
            liftPower *= 0.5;
            armSlidePower *= 0.5;
        }
        if(gamepad2.right_bumper && !gamepad2.left_bumper){
            liftPower *= 0.5;
            armSlidePower *= 0.5;
        }
        if(gamepad2.right_bumper && gamepad2.left_bumper){
            liftPower *= 0.25;
            armSlidePower *= 0.25;
        }

        h.slideArm.setPower(armSlidePower);
        h.liftMotor.setPower(liftPower);


        // TESTING SERVO POSITIONS

//        servoPosLeft += gamepad2.right_stick_y*0.008;
//        servoPosRight += gamepad2.left_stick_y*0.008;
//
//        h.leftIntakeServo.setPosition(servoPosLeft);
//        h.rightIntakeServo.setPosition(servoPosRight);
//
//        telemetry.addData("Servo Pos LEFT: ", servoPosLeft);
//        telemetry.addData("Servo Pos RIGHT: ", servoPosRight);

        if(gamepad2.a){
            h.LArmServo.setPosition(h.LEFTARMSERVO_IN);
            h.RArmServo.setPosition(h.RIGHTARMSERVO_IN);
        }
        if(gamepad2.b){
            h.LArmServo.setPosition(h.LEFTARMSERVO_OUT);
            h.RArmServo.setPosition(h.RIGHTARMSERVO_OUT);
        }

        if(gamepad1.x){
            h.leftFound.setPosition(0.5f);
            h.rightFound.setPosition(1f);
        }

        if(gamepad1.b){
            h.leftFound.setPosition(1f);
            h.rightFound.setPosition(0.5f);
        }
    }

    // Switch between encoder and non-encoder settings
    private void runEncoder(boolean withEncoder){
        if(withEncoder) {
            h.FLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            h.FRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            h.BLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            h.BRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            h.FLMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            h.FRMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            h.BLMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            h.BRMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }else{
            h.FLMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            h.FRMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            h.BLMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            h.BRMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

    }

    @Deprecated
    public double getAverageDistance(){
        /*double d = h.ods.getDistance(DistanceUnit.CM)+ h.ods3.getDistance(DistanceUnit.CM);
        d /= 2;

        double ratio = h.ods.getDistance(DistanceUnit.CM) / h.ods3.getDistance(DistanceUnit.CM) * 100;

        if(ratio < 75 || ratio > 125){
            if(h.ods.getDistance(DistanceUnit.CM) > h.ods3.getDistance(DistanceUnit.CM)){
                return h.ods3.getDistance(DistanceUnit.CM);
            } else {
                return h.ods.getDistance(DistanceUnit.CM);
            }
        } else {
            return d;
        }*/
        return 0;
    }

    // Tank drive controls
    private void tankDrive(){
        double leftStickY = gamepad1.left_stick_y;
        double rightStickY = gamepad1.right_stick_y;

        h.FLMotor.setPower(leftStickY);
        h.FRMotor.setPower(rightStickY);
        h.BLMotor.setPower(leftStickY);
        h.BRMotor.setPower(rightStickY);
    }

    // 360 omni-drive controls
    private void superDrive(){

        // Input
        double leftStickX = gamepad1.left_stick_x;
        double leftStickY = -gamepad1.left_stick_y;
        double rightStickY = -gamepad1.right_stick_y;
        double rightStickX = gamepad1.right_stick_x;

        // Calculating angle between X and Y inputs on the stick
        double angle = Math.atan2(leftStickY, leftStickX);
        angle = Math.toDegrees(angle);
        angle = Math.abs(angle);
        double angleS = Math.atan2(rightStickY, rightStickX);
        angleS = Math.toDegrees(angleS);
        angleS = Math.abs(angleS);
        // Altering value for sake of the program
        if(leftStickY < 0){
            angle = 360 - angle;
        }
        if(rightStickY < 0){
            angleS = 360 - angleS;
        }

        // Power variables
        double v1 = 0, v2 = 0, v3 = 0, v4 = 0;

        // Represents what quadrant our stick is in
        int quadrant = 0;

        // Calculating current quadrant
        if(leftStickX == 0 && leftStickY == 0){
            quadrant = 0;
        } else if(angle >= 0 && angle <= 90){
            quadrant = 1;
        } else if(angle > 90 && angle <= 180){
            quadrant = 2;
        } else if(angle > 180 && angle <= 270){
            quadrant = 3;
        } else if(angle > 270 && angle <= 360) {
            quadrant = 4;
        }

        int quadrantS = 0;

        // Calculating current quadrant
        if(rightStickX == 0 && rightStickY == 0){
            quadrantS = 0;
        } else if(angleS >= 0 && angleS <= 90){
            quadrantS = 1;
        } else if(angleS > 90 && angleS <= 180){
            quadrantS = 2;
        } else if(angleS > 180 && angleS <= 270){
            quadrantS = 3;
        } else if(angleS > 270 && angleS <= 360) {
            quadrantS = 4;
        }

        // Getting our composite input used as a backbone value for movement
        // Short explanation: Always a net Y value, but uses a different percent from each direction based on Y value
        double sampleY = leftStickY;
        double sampleYS = rightStickY;

        double magnitude = Math.abs(sampleY) + Math.abs((1-Math.abs(sampleY))*leftStickX);
        double magnitudeS = Math.abs(sampleYS) + Math.abs((1-Math.abs(sampleYS))*rightStickX);

        // Based on the quadrant, change the underlying function each wheel depends on
        if(quadrant == 1){
            v1 = magnitude*((angle-45)/45);
            v3 = magnitude*((angle-45)/45);
            v2 = magnitude;
            v4 = magnitude;
        } else if(quadrant == 2){
            v1 = magnitude;
            v3 = magnitude;
            v2 = magnitude*((135-angle)/45);
            v4 = magnitude*((135-angle)/45);
        } else if(quadrant == 3){
            v1 = magnitude*((225-angle)/45);
            v3 = magnitude*((225-angle)/45);
            v2 = -1*magnitude;
            v4 = -1*magnitude;
        } else if(quadrant == 4){
            v1 = -1*magnitude;
            v3 = -1*magnitude;
            v2 = -1*magnitude*((315-angle)/45);
            v4 = -1*magnitude*((315-angle)/45);
        } else if(quadrant == 0){
            v1 = 0;
            v2 = 0;
            v3 = 0;
            v4 = 0;
        }

        /*

        if(magnitudeS != 0) {
            if (quadrantS == 1) {
                v1 = magnitudeS * ((angleS - 45) / 45);
                v3 = magnitudeS;
                v2 = magnitudeS;
                v4 = magnitudeS * ((angleS - 45) / 45);
            } else if (quadrantS == 2) {
                v1 = magnitudeS;
                v3 = magnitudeS * ((135 - angleS) / 45);
                v2 = magnitudeS * ((135 - angleS) / 45);
                v4 = magnitudeS;
            } else if (quadrantS == 3) {
                v1 = magnitudeS * ((225 - angleS) / 45);
                v3 = -1 * magnitudeS;
                v2 = -1 * magnitudeS;
                v4 = magnitudeS * ((255 - angleS) / 45);
            } else if (quadrantS == 4) {
                v1 = -1 * magnitudeS;
                v3 = -1 * magnitudeS * ((angleS - 315) / 45);
                v2 = -1 * magnitudeS * ((angleS - 315) / 45);
                v4 = -1 * magnitudeS;
            } else if (quadrantS == 0) {
                v1 = 0;
                v2 = 0;
                v3 = 0;
                v4 = 0;
            }
        }
        */

        // If not using omni-drive, switch to normal turn
        // OLD CODE, KEEP PLEASSSE
        if(rightStickX != 0){
            v1 = -1*rightStickX;
            v2 = rightStickX;
            v3 = rightStickX;
            v4 = -1*rightStickX;
        }

        // Precision controls based on bumpers pressed
        if(gamepad1.left_bumper && !gamepad1.right_bumper){
            v1 *= 0.5;
            v2 *= 0.5;
            v3 *= 0.5;
            v4 *= 0.5;
        }
        if(gamepad1.right_bumper && !gamepad1.left_bumper){
            v1 *= 0.5;
            v2 *= 0.5;
            v3 *= 0.5;
            v4 *= 0.5;
        }
        if(gamepad1.right_bumper && gamepad1.left_bumper){
            v1 *= 0.25;
            v2 *= 0.25;
            v3 *= 0.25;
            v4 *= 0.25;
        }

        h.FRMotor.setPower(v1);
        h.FLMotor.setPower(v2);
        h.BLMotor.setPower(v3);
        h.BRMotor.setPower(v4);
    }

    // Normal holonomic drive
    private void holoDrive(){
        double leftStickX = gamepad1.right_stick_x;
        double leftStickY = -gamepad1.left_stick_y;
        double rightStickX = gamepad1.left_stick_x;
        double rightStickY = gamepad1.right_stick_y;

        double r = Math.hypot(leftStickX, leftStickY);
        double robotAngle = Math.atan2(leftStickY, leftStickX) - (Math.PI / 4);
        double leftX = rightStickX;
        double turn = leftX;

        double v1 = r * Math.cos(robotAngle) + turn;
        double v2 = r * Math.sin(robotAngle) - turn;
        double v3 = r * Math.sin(robotAngle) + turn;
        double v4 = r * Math.cos(robotAngle) - turn;

        if(gamepad1.right_bumper || gamepad1.left_bumper){
            v1 = v1 * PRECISION_SPEED_MODIFIER;
            v2 = v2 * PRECISION_SPEED_MODIFIER;
            v3 = v3 * PRECISION_SPEED_MODIFIER;
            v4 = v4 * PRECISION_SPEED_MODIFIER;
        }
        h.FLMotor.setPower(v1);
        h.FRMotor.setPower(v2);
        h.BLMotor.setPower(v3);
        h.BRMotor.setPower(v4);
    }

    // Clamp a double between a max and a min
    public double clamp(double min, double max, double value){
        if(value < min){
            value = min;
        } else if(value > max){
            value = max;
        }

        return value;
    }

}