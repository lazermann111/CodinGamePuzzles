package MaxVsZombies;

import javafx.geometry.Pos;

import java.util.*;
import java.util.stream.Collectors;


class Player {


    public static int X;
    public static int Y;

    static class Human   {
        public int humanId;
        public int humanX;
        public int humanY;
        public Pair<Integer,Integer> Position;


    }

    static class Zombie   {
        public int zombieId;
        public int zombieX;
        public int zombieY;
        public int zombieXNext;
        public int zombieYNext;

        public Pair<Integer,Integer> PositionNext;
        public Pair<Integer,Integer> Position;


    }

    static class Pair<F, S> {
        public F first;
        public S second;
        public static Pair<Integer, Integer> EMPTY_PAIR = new Pair<>(0,0) ;
        /**
         * Constructor for a Pair.
         *
         * @param first the first object in the Pair
         * @param second the second object in the pair
         */
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        /**
         * Compute a hash code using the hash codes of the underlying objects
         *
         * @return a hashcode of the Pair
         */
        @Override
        public int hashCode() {
            return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }
    }
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);


        List<Human> humans = new ArrayList<>();
        List<Zombie> zombies = new ArrayList<>();
        List<Cluster<Human>> clusters = new ArrayList<>();

        // game loop
        while (true) {

            humans.clear();
            zombies.clear();
            clusters.clear();

            X = in.nextInt();
            Y = in.nextInt();
            int humanCount = in.nextInt();
            for (int i = 0; i < humanCount; i++) {

                Human h = new Human();
                h. humanId = in.nextInt();
                h. humanX = in.nextInt();
                h. humanY = in.nextInt();
                h.Position = new Pair<>(h. humanX , h.humanY);
                humans.add(h);
            }
            int zombieCount = in.nextInt();
            for (int i = 0; i < zombieCount; i++) {

                Zombie z = new Zombie();

                z. zombieId = in.nextInt();
                z. zombieX = in.nextInt();
                z. zombieY = in.nextInt();
                z. zombieXNext = in.nextInt();
                z. zombieYNext = in.nextInt();
                z.Position = new Pair<>(z. zombieX , z.zombieY);
                z.PositionNext = new Pair<>(z. zombieXNext , z.zombieYNext);
                zombies.add(z);
            }

            clusters = ClusterizeHumans(humans, zombies);


            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            Pair<Integer,Integer> move = null;
            Pair<Integer,Integer> myPos = new Pair<>(X,Y)  ;
            move = clusters.stream().filter(a -> CanSave(a.NearestZombiePos, myPos, a.CenterOfCluster)).sorted((humanCluster, t1) -> t1.content.size()).findFirst().get().CenterOfCluster;
            Pair<Integer,Integer> tmp = new Pair<>(move.first, move.second);
            //move = zombieId(zombies, 0);
            if(move.equals( Pair.EMPTY_PAIR)  /*||
                    zombies.stream().noneMatch(z -> DistanceBetween(z.PositionNext, tmp) < 1000)*/)
            {
               // System.err.println("EMPTY_PAIR" );
                move = nearestZombie(zombies);
            }

            System.out.println(move.first + " " + move.second); // Your destination coordinates
        }
    }

    public static boolean CanSave(Pair<Integer,Integer> nearestzombiePos,
                            Pair<Integer,Integer> myPos,
                            Pair<Integer,Integer> clusterCenter)
    {
        System.err.println("nearestzombiePos " + nearestzombiePos);
        System.err.println("myPos " + myPos);
        System.err.println("clusterCenter " + clusterCenter);
        Boolean a = DistanceBetween(nearestzombiePos,clusterCenter)>(DistanceBetween(myPos,clusterCenter)/2.5f - 1000);
        System.err.println("CanSave " + a);
        return a;
    }

    private  static List<Cluster<Human>> ClusterizeHumans(List<Human> humans, List<Zombie> zombies)
    {
        List<Cluster<Human>> clusters = new ArrayList<>();
        int ClusterRadius = 3000;
        List<Human> copy = new ArrayList<>(humans);

        while (!copy.isEmpty())
        {
            Human first = copy.get(0);

            List<Human> affected = copy.stream().filter(a ->
                    DistanceBetween(a.Position, first.Position) < ClusterRadius)
                    .collect(Collectors.toList());

            copy.removeAll(affected);
            Cluster<Human> cluster = new Cluster<>();
            cluster.content.addAll(affected);

            cluster.CenterOfCluster = Center(cluster.content.stream().map(a -> a.Position).collect(Collectors.toList()));
            final Pair<Integer, Integer> center =  cluster.CenterOfCluster;

            /*cluster.NearestZombiePos = zombies.stream().sorted((z1, z2) -> DistanceBetween(z1.Position, center))
                    .collect(Collectors.toList()).get(0).Position;*/

            Zombie nearest = zombies.get(0);
            int MinDist = DistanceBetween(nearest.Position, center);
            for (Zombie z : zombies)
            {

                if(DistanceBetween(z.Position, center) < MinDist){
                    nearest = z;

                    MinDist =DistanceBetween(z.Position, center);
                }
            }

            cluster.NearestZombiePos = nearest.Position;

            clusters.add(cluster);

            System.err.println("New cluster size " + cluster.content.size() +
                    " any human id " + cluster.content.stream().map(a -> a.humanId).collect(Collectors.toList())
                    .toString() +
                    " NearestZombiePos " + cluster.NearestZombiePos +
                    " center " + cluster.CenterOfCluster);
        }

        return clusters;
    }

    private static Pair<Integer, Integer>  Center(List<Pair<Integer, Integer>> positions)
    {
       // for (Pair<Integer, Integer> a : positions)
       // {
       //     System.err.println("Human pos " + a);
       // }

        int x = (int) positions.stream().mapToInt(m -> m.first).average().getAsDouble();
        int y = (int) positions.stream().mapToInt(m -> m.second).average().getAsDouble();
        Pair<Integer, Integer> result = new Pair<>(x,y);

       // System.err.println("Result " + result);
        return result;
    }

    static class Cluster<Human>
    {
        List<Human> content = new ArrayList<>();
        Pair<Integer, Integer> CenterOfCluster;
        Pair<Integer, Integer> NearestZombiePos;

    }



    public static int DistanceBetween(Pair<Integer, Integer> source, Pair<Integer, Integer> target)
    {
      //  System.err.println("source " + source);
    //    System.err.println("target " + target);
        int r = (int) Math.sqrt (Math.pow(source.first - target.first, 2) + Math.pow(source.second - target.second, 2));;

       // System.err.println("result is " + r);
        return r;
    }

    public static double DistanceTo(Pair<Integer, Integer> target)
    {
        return Math.sqrt(Math.pow(X - target.first, 2) + Math.pow(Y - target.second, 2)) ;
    }

    public static Pair<Integer,Integer> zombieId(List<Zombie> zombies, Integer id)
    {
        Pair<Integer,Integer> result = new Pair<>(0,0);

        Optional<Zombie> z =  zombies.stream().filter((s) -> s.zombieId == id).findFirst();
        if (z.isPresent())
        {
        //    System.err.println("z.isPresent()" );
            result.first = z.get().zombieX;
            result.second = z.get().zombieY;
        }

        return result;
    }


    public static Pair<Integer,Integer> nearestZombie(List<Zombie> zombies)
    {
        Pair<Integer,Integer> result = new Pair<>(0,0);
        int minDist = Integer.MAX_VALUE;

        for (Zombie z :zombies )
        {
           // System.err.println("z X" + z.zombieXNext);
           // System.err.println("z Y" + z.zombieYNext);

            Pair<Integer,Integer> tmp = new Pair<>(z.zombieX, z.zombieY);
            if(DistanceTo(tmp) < minDist)
            {
                minDist = (int) DistanceTo(tmp);

                result = tmp;
            }
        }

        return result;
    }




}