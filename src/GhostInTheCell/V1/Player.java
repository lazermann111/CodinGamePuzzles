package GhostInTheCell.V1;


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
                    case "FACTORY": AddFactory(entityId,arg1, arg2 , arg3); break;
                    case "TROOP": AddTroops(entityId,arg1, arg2 , arg3, arg4, arg5); break;
                    case "BOMB": break;
                    default: throw new IllegalStateException("Unknown entity type: " + entityType);
                }
            }

            // Any valid action, such as "WAIT" or "MOVE source destination cyborgs"
            ///System.out.println("WAIT");
            StringBuilder res = new StringBuilder();
            if(Tick == 0 || Tick == 10) SendBombs(res);
            Factory myStronget = StongestFactory(OwnerType.ME);
            if (myStronget.CyborgsAmount > 0)
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

                // Factory dest = NearestFactory(myStronget.Id , OwnerType.NEUTRAL, 1);
                Factory dest = Tick == 1 ? StongestFactory(OwnerType.OPPONENT) :  NearestFactory(myStronget.Id , OwnerType.NEUTRAL, 1);

                if(dest == null)
                {
                    dest = NearestFactory(myStronget.Id , OwnerType.OPPONENT, 0);
                }
                if(dest != null)
                {

                    float percentage = 0.7f;
                    //System.out.println(myStronget.MoveTo(dest.Id, percentage));
                    res.append(myStronget.MoveTo(dest.Id, (int) (myStronget.CyborgsAmount * percentage)))  ;
                }
                else
                {
                    res.append("WAIT;") ;
                    // System.out.println("WAIT");
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

    private void SendBombs(StringBuilder res)
    {
        Factory myStronget = StongestFactory(OwnerType.ME);
        Factory opponent = StongestFactory(OwnerType.OPPONENT);
        res.append(myStronget.SendBomb(opponent.Id));
    }

    public int DistanceToFactory(float factory1Id, float factory2Id)
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

    public Factory StongestFactory(OwnerType owner)
    {
        Optional<Factory> a = factories.stream()
                .filter(f -> f.Owner == owner)
                .sorted(Comparator.comparingInt(a1 -> -a1.CyborgsAmount))
                .findFirst()
                ;

        a.ifPresent(factory -> System.err.println(" StongestFactory " + factory));
        return a.isPresent() ? a.get() : null;
    }

    public Factory NearestFactory(int originId, OwnerType ownerType, int minProduction)
    {
        Optional<Factory> a = factories.stream()
                .filter(f -> f.Owner == ownerType)
                .filter(f -> DistanceToFactory(originId, f.Id) != -1)
                .sorted(Comparator.comparingInt(b -> DistanceToFactory(originId, b.Id)))
                .filter(f -> f.Production >= minProduction)
                .findFirst();
        a.ifPresent(factory -> System.err.println(" NearestFactory for origin " + originId + "  is " + factory));
        return a.isPresent() ? a.get() : null;
    }

    public List<Factory> NearestFactories(int originId, OwnerType ownerType, int minProduction, int maxCyborgs)
    {
        List<Factory> res = factories.stream()
                .filter(f -> f.Owner == ownerType)
                .filter(f -> DistanceToFactory(originId, f.Id) != -1)
                .filter(f -> f.Production >= minProduction)
                .filter(f -> f.CyborgsAmount <= maxCyborgs)
                .sorted(Comparator.comparingInt(a -> DistanceToFactory(originId, a.Id)))
                .collect(Collectors.toList());
        System.err.println(" NearestFactories for origin " + originId + "  is " + res);
        return res;
    }



    private void AddTroops(int id, int owner, int source, int destination, int amount , int turnsToDestination)
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


    private void AddFactory(int id, int owner, int cyborgs, int production)
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

    public static void main(String args[])
    {
        Player p = new Player();
        p.Init().Run();
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



