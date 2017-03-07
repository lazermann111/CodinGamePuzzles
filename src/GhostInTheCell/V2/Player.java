package GhostInTheCell.V2;

import com.sun.javafx.geom.Vec3f;

import java.util.*;
import java.util.stream.Collectors;



class Player {


    public List<Factory> factories = new ArrayList<>();
    public List<Troops> troops = new ArrayList<>();
    public List<Vec3f> distances = new ArrayList<>();

    private Scanner in;

    public Player Init()
    {
        in = new Scanner(System.in);
        int factoryCount = in.nextInt(); // the number of factories
        int linkCount = in.nextInt(); // the number of links between factories
        for (int i = 0; i < linkCount; i++) {
            int factory1 = in.nextInt();
            int factory2 = in.nextInt();
            int distance = in.nextInt();

            distances.add(new Vec3f(factory1, factory2, distance));
        }

        return this;
    }



    public void Run()
    {

        int Tick = 0;
        int MainBasesDistance = -1;
        while (true)
        {
            factories.clear();
            troops.clear();

            int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            for (int i = 0; i < entityCount; i++)
            {
                int entityId = in.nextInt();
                String entityType = in.next();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                int arg5 = in.nextInt();

                switch (entityType)
                {
                    case "FACTORY": Helpers.AddFactory(factories,entityId,arg1, arg2 , arg3); break;
                    case "TROOP": Helpers.AddTroops(troops,entityId,arg1, arg2 , arg3, arg4, arg5); break;
                    case "BOMB": break;
                    default: throw new IllegalStateException("Unknown entity type: " + entityType);
                }
            }

            // Any valid action, such as "WAIT" or "MOVE source destination cyborgs"
            ///System.out.println("WAIT");
            StringBuilder res = new StringBuilder();
            if(Tick == 0) MainBasesDistance = InitMainBasesDistance(Helpers.StongestFactory(factories, OwnerType.ME), Helpers.StongestFactory(factories, OwnerType.OPPONENT));
            if(Tick == 0 || Tick == 10) SendBombs(res);
            Factory myStrongest = Helpers.StongestFactory(factories, OwnerType.ME);
            if (myStrongest.CyborgsAmount > 0)
            {

                List<Factory> factoriesToUpgrade = factories.stream()
                        .filter(f -> f.Owner == OwnerType.ME)
                        .filter(f -> f.Production  < 3)
                        .filter(f -> f.CyborgsAmount >= 30)
                        .collect(Collectors.toList());
                for (Factory f: factoriesToUpgrade)
                {
                    res.append(f.UpgradeFactory()) ;
                }

                // Factory dest = NearestFactory(myStrongest.Id , OwnerType.NEUTRAL, 1);
              //  float percentage = (Tick == 1) || (Tick == MainBasesDistance) ? 1 : 0.5f;
                // 0st tick - bomb,
                // 1st - pushing all troops to enemy  main base
                // MainBasesDistance - ETA of enemy bomb - pushing all troops away from base

               /* Factory dest = Tick == 1 ?
                        StongestFactory(OwnerType.OPPONENT) :
                        NearestFactory(myStrongest.Id , OwnerType.NEUTRAL, 1);
                if(dest == null)
                {
                    dest = NearestFactory(myStrongest.Id , OwnerType.OPPONENT, 0);
                    // percentage = Math.min(dest.CyborgsAmount + 1, myStrongest.CyborgsAmount * percentage) /myStrongest.CyborgsAmount  + 1;
                }
                if(dest != null && Tick != 0)// 0st tick - bomb only
                {
                  //  percentage = dest.CyborgsAmount + 1;
                    DefendBases(res);
                    //System.out.println(myStrongest.MoveTo(dest.Id, percentage));
                    res.append(myStrongest.MoveTo(dest.Id, (int) (myStrongest.CyborgsAmount * percentage)))  ;
                }*/

                if(Helpers.MyFactories(factories).size() != 0)
                {

                         for(Factory f : Helpers.MyFactories(factories))
                         {
                            List<Factory> nearest =  Helpers.NearestFactories(factories, distances, f.Id)
                                                            .subList(0,3);

                            for (Factory f2 : nearest)
                            {
                                res.append(f.MoveTo(f2.Id, f2.CyborgsAmount/5));
                            }
                         }

                         DefendBases(res);
                }
                else
                {
                    res.append("WAIT;") ;
                }
                int idx = res.lastIndexOf(";");
                res.replace(idx, idx + 1, "");
                System.out.println(res.toString());
            }
            else
            {
                System.out.println("WAIT");
            }

            Tick++;
        }
    }

    private int InitMainBasesDistance(Factory f1, Factory f2)
    {
        return Helpers.DistanceToFactory(distances,f1.Id, f2.Id);
    }

    private void DefendBases(StringBuilder res)
    {
        for (Factory f : Helpers.MyFactories(factories))
        {
            List<Troops> myTroops = Helpers.IncomingTroops(troops, f.Id, OwnerType.ME);
            List<Troops> oppTroops = Helpers.IncomingTroops(troops, f.Id, OwnerType.OPPONENT);

            boolean needToDefend = myTroops.stream().mapToInt(a -> a.Amount).sum() + f.CyborgsAmount <= oppTroops.stream().mapToInt(a -> a.Amount).sum();
            if(needToDefend)
            {
                Factory nearest = Helpers.NearestFactory(factories, distances, f.Id, OwnerType.ME, 0);
                if(nearest != null)  res.append(nearest.MoveTo(f.Id, 2)) ;
            }
        }
    }

    private void SendBombs(StringBuilder res)
    {
        Factory me = Helpers.StongestFactory(factories, OwnerType.ME);
        Factory opponent = Helpers.StongestFactory(factories, OwnerType.OPPONENT);
        res.append(me.SendBomb(opponent.Id));
    }







    public static void main(String args[])
    {
        Player p = new Player();
        p.Init().Run();
    }
}




class Helpers
{
    public static void AddTroops(List<Troops> troops,int id, int owner, int source, int destination, int amount , int turnsToDestination)
    {
        Troops t = new Troops(id, source, destination, amount, turnsToDestination);
        switch (owner)
        {
            case -1: t.Owner = OwnerType.OPPONENT; break;
            case 0: t.Owner = OwnerType.NEUTRAL; break;
            case 1: t.Owner = OwnerType.ME; break;
            default: break;

        }

        troops.add(t);
    }


    public static void AddFactory(List<Factory> factories,int id, int owner, int cyborgs, int production)
    {
        Factory f = new Factory(id, cyborgs, production);
        switch (owner)
        {
            case -1: f.Owner = OwnerType.OPPONENT; break;
            case 0: f.Owner = OwnerType.NEUTRAL; break;
            case 1: f.Owner = OwnerType.ME; break;
            default: break;

        }

        factories.add(f);
    }



    public static Factory StongestFactory(List<Factory> factories,OwnerType owner)
    {
        Factory res = factories.stream()
                .filter(f -> f.Owner == owner)
                .sorted(Comparator.comparingInt(a -> -a.CyborgsAmount))
                .findFirst()
                .get();
        System.err.println(" StongestFactory " + res);
        return res;
    }

    public static Factory NearestFactory(List<Factory> factories, List<Vec3f> distances, int originId, OwnerType ownerType, int minProduction)
    {
        Optional<Factory> a = factories.stream()
                .filter(f -> f.Owner == ownerType)
                .filter(f -> DistanceToFactory(distances,originId, f.Id) != -1)
                .sorted(Comparator.comparingInt(b -> DistanceToFactory(distances,originId, b.Id)))
                .filter(f -> f.Production >= minProduction)
                .findFirst();
        a.ifPresent(factory -> System.err.println(" NearestFactory for origin " + originId + "  is " + factory));
        return a.isPresent() ? a.get() : null;
    }



    public static List<Factory> MyFactories(List<Factory> factories)
    {
        return  factories.stream()
                .filter(f -> f.Owner == OwnerType.ME)
                .collect(Collectors.toList());
    }

    public static List<Factory> NearestFactories(List<Factory> factories, List<Vec3f> distances, int originId)
    {
        List<Factory> res = factories.stream()
                .filter(f -> DistanceToFactory(distances,originId, f.Id) != -1)
                .sorted(Comparator.comparingInt(a -> DistanceToFactory(distances, originId, a.Id)))
                .collect(Collectors.toList());
        System.err.println(" NearestFactories for origin " + originId + "  is " + res);
        return res;
    }

    public static List<Factory> NearestFactories(List<Factory> factories, List<Vec3f> distances, int originId, OwnerType ownerType, int minProduction, int maxCyborgs)
    {
        List<Factory> res = factories.stream()
                .filter(f -> f.Owner == ownerType)
                .filter(f -> DistanceToFactory(distances,originId, f.Id) != -1)
                .filter(f -> f.Production >= minProduction)
                .filter(f -> f.CyborgsAmount <= maxCyborgs)
                .sorted(Comparator.comparingInt(a -> DistanceToFactory(distances, originId, a.Id)))
                .collect(Collectors.toList());
        System.err.println(" NearestFactories for origin " + originId + "  is " + res);
        return res;
    }

    public static List<Troops> IncomingTroops(List<Troops> troops, int destinationId, OwnerType ownerType)
    {
        List<Troops> res = troops.stream()
                .filter(f -> f.Owner == ownerType)
                .filter(f -> f.DestinationFactoryId >= destinationId)
                .collect(Collectors.toList());
        System.err.println(" IncomingTroops for destination " + destinationId + "  is " + res);
        return res;
    }


    public static int DistanceToFactory(List<Vec3f> distances, float factory1Id, float factory2Id)
    {
        for (Vec3f d : distances)
        {
            if((d.x == factory1Id && d.y == factory2Id ) || (d.y == factory1Id && d.x == factory2Id))
            {
                return (int) d.z;
            }
        }

        return -1;
    }

}
class Factory
{
    public int Id;
    public int CyborgsAmount;
    public int Production;
    public OwnerType Owner;

    public Factory(int id, int cyborgsAmount, int production) {
        Id = id;
        CyborgsAmount = cyborgsAmount;
        Production = production;
    }

    @Override
    public String toString() {
        return "Factory{" +
                "Id=" + Id +
                ", CyborgsAmount=" + CyborgsAmount +
                ", Production=" + Production +
                ", Owner=" + Owner +
                '}';
    }

    public String MoveTo(int destinationId, int amount)
    {
        return "MOVE " + Id + " " + destinationId+ " " + amount+";";
    }

    public String UpgradeFactory()
    {
        return "INC " + Id +";";
    }

    public String SendBomb(int destinationId)
    {
        return "BOMB " + Id + " " + destinationId+";";
    }
}

class Troops
{
    public int Id;
    public int SourceFactoryId;
    public int DestinationFactoryId;
    public int Amount;
    public int TurnsToDestination;
    public OwnerType Owner;

    public Troops(int id, int sourceFactoryId, int destinationFactoryId, int amount, int turnsToDestination) {
        Id = id;
        SourceFactoryId = sourceFactoryId;
        DestinationFactoryId = destinationFactoryId;
        Amount = amount;
        TurnsToDestination = turnsToDestination;
    }

    @Override
    public String toString() {
        return "Troops{" +
                "Id=" + Id +
                ", SourceFactoryId=" + SourceFactoryId +
                ", DestinationFactoryId=" + DestinationFactoryId +
                ", Amount=" + Amount +
                ", TurnsToDestination=" + TurnsToDestination +
                ", Owner=" + Owner +
                '}';
    }
}

enum OwnerType
{
    ME, NEUTRAL, OPPONENT
}





