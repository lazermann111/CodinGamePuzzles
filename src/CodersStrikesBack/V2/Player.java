package CodersStrikesBack.V2;

import com.sun.javafx.geom.Vec2d;

import java.util.*;


class Player {

    public static void main(String args[])
    {
        new Player().Run();

    }

    public  void Run() {
        Scanner in = new Scanner(System.in);

        // game loop
        int opponentX_prev= 0;
        int opponentY_prev= 0;
        Vec2d checkPoint_prev = null;
        int My_X_prev = 0;
        int My_Y_prev= 0;

        Boolean boostUsed =false;
        Boolean checkPointChanged =true;

        int lastTimeShieldUsed =0;
        List<Vec2d> checkPoints = new ArrayList<>();

        while (true) {

            int My_X = in.nextInt();
            int MY_Y = in.nextInt();
            int nextCheckpointX = in.nextInt(); // x position of the next check point
            int nextCheckpointY = in.nextInt(); // y position of the next check point
            int nextCheckpointDist = in.nextInt(); // distance_Opponent to the next checkpoint
            int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();

            Vec2d myVec = new Vec2d(My_X - My_X_prev, MY_Y - My_X_prev);
            Vec2d opponentVec = new Vec2d(opponentX - opponentX_prev, opponentY - opponentY_prev);


            int distance_Opponent = DistanceBetween(opponentX, My_X, opponentY, MY_Y);
            int distance_Opponent_prevTick = DistanceBetween(opponentX_prev, My_X_prev, opponentY_prev, My_Y_prev);

            //System.err.println("Tick " + Tick);
            //System.err.println("nextCheckpointAngle " + nextCheckpointAngle);
            //System.err.println("myVec " + myVec);
            //System.err.println("opponentVec " + opponentVec);

            //System.err.println("distance_Opponent " + distance_Opponent);
            //System.err.println("distance_Opponent_prevTick " + distance_Opponent_prevTick);

            Vec2d checkPoint = new Vec2d(nextCheckpointX, nextCheckpointY);
            checkPointChanged = checkPoint_prev != checkPoint;
            if(!checkPoints.contains(checkPoint) && checkPointChanged) // first lap
            {
               // System.err.println("First lap ");
               // System.err.println("checkPoint_prev  " + checkPoint_prev);
                if(checkPoint_prev != null && !checkPoints.contains(checkPoint_prev))checkPoints.add(checkPoint_prev);
                int thrust;
                if (nextCheckpointAngle > 45 || nextCheckpointAngle < -45)
                {
                    thrust = 0;
                }
                else if (nextCheckpointDist < 500)
                {
                    thrust = 20;
                }
                else if (nextCheckpointDist < 50)
                {
                    thrust = 0;
                }
                else
                {
                    thrust = 100;
                }
                String additional_command = "" + thrust;

                if (nextCheckpointDist > 5000 && !boostUsed && Math.abs(nextCheckpointAngle) < 5) // straight line to next chckPoint
                {
                    boostUsed = true;
                    System.err.println("BOOST!!!");
                    additional_command = "BOOST";
                }
               /* else if (Tick - lastTimeShieldUsed > 20 &&
                        distance_Opponent < 800 && (distance_Opponent_prevTick - distance_Opponent) < 300
                        && (distance_Opponent_prevTick - distance_Opponent) > 0) // enemy is approaching
                {
                    System.err.println("SHIELD!!!");
                    additional_command = "SHIELD";
                }*/
                System.out.println(nextCheckpointX + " " + nextCheckpointY + " " + additional_command);
            }

            else if(checkPointChanged)
            {

                int checkPointIndex = checkPoints.indexOf(checkPoint);
                Vec2d nextCheckPoint = checkPointIndex < (checkPoints.size() - 1) ?
                        checkPoints.get(checkPointIndex + 1) : checkPoints.get(0);

               // System.err.println("second lap ");
             //   System.err.println("checkPointIndex " + checkPointIndex);
             //   System.err.println("checkPointIndex size" + checkPoints.size());
             //   System.err.println("next chckpoint is " + nextCheckPoint);

                int thrust;

                if (nextCheckpointAngle > 45 || nextCheckpointAngle < -45)
                {
                    thrust = 0;
                }
                else if (nextCheckpointDist < 500)
                {
                    thrust = 20;
                }
                else if (nextCheckpointDist < 50)
                {
                    thrust = 0;
                }
                else
                {
                    thrust = 100;
                }

                String additional_command = "" + thrust;
                int drift_distance = distance_Opponent > 1000 ? 400 : 0;
                int x = nextCheckpointDist > drift_distance ? nextCheckpointX : (int) nextCheckPoint.x;
                int y = nextCheckpointDist > drift_distance ? nextCheckpointY : (int) nextCheckPoint.y;

                System.out.println( x+ " " + y + " " + additional_command);
            }


            opponentX_prev = opponentX;
            opponentY_prev = opponentY;
            My_X_prev = My_X;
            My_Y_prev = MY_Y;
            checkPoint_prev = checkPoint;
        }
    }


    private static int DistanceBetween(float x1, float x2, float y1, float y2)
    {
        //  System.err.println("source " + source);
        //    System.err.println("target " + target);
        int r = (int) Math.sqrt (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));;

        // System.err.println("result is " + r);
        return r;
    }
}