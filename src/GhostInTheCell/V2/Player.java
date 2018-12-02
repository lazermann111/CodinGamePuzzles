package GhostInTheCell.V2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.sun.javafx.geom.Vec3f;

class Player {

	public List<Factory> factories = new ArrayList<>();
	public List<Troops> troops = new ArrayList<>();
	public List<Vec3f> distances = new ArrayList<>();

	private Scanner in;

	public Player Init() {
		in = new Scanner(System.in);
		int factoryCount = in.nextInt(); // the number of factories
		int linkCount = in.nextInt(); // the number of links between factories
		for (int i = 0; i < linkCount; i++) {
			int factory1 = in.nextInt();
			int factory2 = in.nextInt();
			int distance = in.nextInt();

			System.err.println("factory1 = " + factory1);
			System.err.println("factory2 = " + factory2);
			System.err.println("distance = " + distance);

			distances.add(new Vec3f(factory1, factory2, distance));
		}

		return this;
	}

	public void Run() {

		int Tick = 0;
		int MainBasesDistance = -1;
		while (true) {
			factories.clear();
			troops.clear();

			int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
			for (int i = 0; i < entityCount; i++) {
				int entityId = in.nextInt();
				String entityType = in.next();
				int arg1 = in.nextInt();
				int arg2 = in.nextInt();
				int arg3 = in.nextInt();
				int arg4 = in.nextInt();
				int arg5 = in.nextInt();

				switch (entityType) {
					case "FACTORY":
						Helpers.AddFactory(factories, entityId, arg1, arg2, arg3);
						break;
					case "TROOP":
						Helpers.AddTroops(troops, entityId, arg1, arg2, arg3, arg4, arg5);
						break;
					case "BOMB":
						handleEnemyBomb(entityId, arg1, arg2, arg3, arg4, arg5);
						break;
					default:
						throw new IllegalStateException("Unknown entity type: " + entityType);
				}
			}

			// Any valid action, such as "WAIT" or "MOVE source destination cyborgs"
			///System.out.println("WAIT");
			StringBuilder res = new StringBuilder();
			if (Tick == 0) {
				MainBasesDistance = InitMainBasesDistance(Helpers.StongestFactory(factories, OwnerType.ME),
						Helpers.StongestFactory(factories, OwnerType.OPPONENT));
			}
			if (Tick == 0 || Tick == 15) {
				SendBombs(res);
			}
			Factory myStrongest = Helpers.StongestFactory(factories, OwnerType.ME);
			if (myStrongest != null && myStrongest.CyborgsAmount > 0) {

				List<Factory> factoriesToUpgrade = factories.stream()
						.filter(f -> f.Owner == OwnerType.ME)
						.filter(f -> f.Production < 3)
						.filter(f -> f.CyborgsAmount >= 31)
						.collect(Collectors.toList());
				for (Factory f : factoriesToUpgrade) {
					res.append(f.UpgradeFactory());
				}

				System.err.println(" Helpers.MyFactories(factories) " + Helpers.MyFactories(factories).size());
				if (Helpers.MyFactories(factories).size() != 0)
				{

					captureNeutralBases(res);
					DefendBases(res);
				   if(factories.stream()    //todo   find better condition
						   .filter(a -> a.Production > 0)
						   .filter(a-> a.Owner == OwnerType.NEUTRAL)
						   .count() < 5)
				   {
					   attackEnemyBases(res);
				   }

				} else {
					res.append("WAIT;");
				}
				if(res.length() == 0)
				{
					res.append("WAIT;");
				}
				int idx = res.lastIndexOf(";");
				res.replace(idx, idx + 1, "");
				System.out.println(res.toString());
			} else {
				System.out.println("WAIT");
			}

			Tick++;
		}
	}

	private  void captureNeutralBases(StringBuilder res)
	{
		int neutral =0;
		for (Factory myFactory : Helpers.MyFactories(factories))
		{
			//System.err.println("MyFactory " + myFactory);
			List<Factory> nearest = Helpers.NearestFactories(factories, distances, myFactory.Id, OwnerType.NEUTRAL,31).stream()
					.filter(a -> a.Production > 0).collect(
					Collectors.toList());


			for (Factory toCapture : nearest)
			{
				if(myFactory.CyborgsAmount - toCapture.CyborgsAmount < 5) continue;
				System.err.println("nearest neutral bases " + toCapture);
				res.append(myFactory.MoveTo(toCapture.Id, Math.min(toCapture.CyborgsAmount + 1, myFactory.CyborgsAmount - 1)));
			}

		}


	}
	private void attackEnemyBases(StringBuilder res)
	{
		for (Factory myFactory : Helpers.MyFactories(factories))
		{
			//System.err.println("MyFactory " + myFactory);
			List<Factory> nearest = Helpers.NearestFactories(factories, distances, myFactory.Id, OwnerType.OPPONENT,3)   ;


			for (Factory toCapture : nearest)
			{
				//System.err.println("attackEnemyBases  " + toCapture);
				if(myFactory.CyborgsAmount - toCapture.CyborgsAmount < 5) continue;
				res.append(myFactory.MoveTo(toCapture.Id, Math.min(toCapture.CyborgsAmount +1, myFactory.CyborgsAmount - 2)));
			}
		}
	}

	private int InitMainBasesDistance(Factory f1, Factory f2) {
		return Helpers.DistanceToFactory(distances, f1.Id, f2.Id);
	}

	private void DefendBases(StringBuilder res)
	{
		for (Factory f : Helpers.MyFactories(factories))
		{
			int myIncomingTroops = Helpers.IncomingTroops(troops, f.Id,OwnerType. ME).stream().mapToInt(a -> a.Amount).sum();
			int opponentIncomingTroops = Helpers.IncomingTroops(troops, f.Id, OwnerType.OPPONENT).stream().mapToInt(a -> a.Amount).sum();

			boolean needToDefend = myIncomingTroops + f.CyborgsAmount <= opponentIncomingTroops;
			if (needToDefend )
			{
				System.err.println("DefendBases needToDefend " + f);
				Factory nearest = Helpers.NearestFactory(factories, distances, f.Id, OwnerType.ME, 0,1);
				if (nearest != null)
				{
					System.err.println("DefendBases needToDefend 2" + nearest);
					res.append(nearest.MoveTo(f.Id, Math.max(opponentIncomingTroops - myIncomingTroops + f.CyborgsAmount +1, f.CyborgsAmount -1)));
				}
			}
		}
	}

	private void SendBombs(StringBuilder res) {


		//Factory me = Helpers.StongestFactory(factories, OwnerType.ME);
		Optional<Factory> opponent = factories
				.stream()
				.filter(f -> f.Owner == OwnerType.OPPONENT)
				.sorted(Comparator.comparingInt(o -> o.Production))
				.limit(3).max(Comparator.comparingInt(o -> o.CyborgsAmount));
		if(!opponent.isPresent()) return;


		Factory me = Helpers.NearestFactory(factories, distances, opponent.get().Id, OwnerType.ME, 0,1);

		if (me != null) {
			res.append(me.SendBomb(opponent.get().Id));
		}
	}

	public static void main(String args[]) {
		Player p = new Player();
		p.Init().Run();
	}
}

class Helpers {
	public static void AddTroops(List<Troops> troops, int id, int owner, int source, int destination, int amount, int turnsToDestination) {
		Troops t = new Troops(id, source, destination, amount, turnsToDestination);
		switch (owner) {
			case -1:
				t.Owner =OwnerType. OPPONENT;
				break;
			case 0:
				t.Owner = OwnerType.NEUTRAL;
				break;
			case 1:
				t.Owner = OwnerType.ME;
				break;
			default:
				break;

		}

		troops.add(t);
	}

	public static void AddFactory(List<Factory> factories, int id, int owner, int cyborgs, int production) {
		Factory f = new Factory(id, cyborgs, production);
		switch (owner) {
			case -1:
				f.Owner = OwnerType.OPPONENT;
				break;
			case 0:
				f.Owner =OwnerType.NEUTRAL;
				break;
			case 1:
				f.Owner =OwnerType.ME;
				break;
			default:
				break;

		}

		factories.add(f);
	}

	public static Factory StongestFactory(List<Factory> factories, OwnerType owner) {
		Optional<Factory> res = factories.stream()
				.filter(f -> f.Owner == owner).min(Comparator.comparingInt(a -> -a.CyborgsAmount)) ;

		//System.err.println(" StongestFactory " + res);
		return res.orElse(null);
	}

	public static Factory NearestFactory(List<Factory> factories, List<Vec3f> distances, int originId, OwnerType ownerType, int minProduction, int limit) {
		Optional<Factory> a = factories.stream()
				.filter(f -> f.Owner == ownerType)
				.filter(f -> DistanceToFactory(distances, originId, f.Id) != -1)
				.sorted(Comparator.comparingInt(b -> DistanceToFactory(distances, originId, b.Id)))
				.filter(f -> f.Production >= minProduction)
				.findFirst();
		//a.ifPresent(factory -> System.err.println(" NearestFactory for origin " + originId + "  is " + factory));
		return a.isPresent() ? a.get() : null;
	}

	public static List<Factory> MyFactories(List<Factory> factories) {
		return factories.stream()
				.filter(f -> f.Owner == OwnerType.ME)
				.collect(Collectors.toList());
	}

	public static List<Factory> NearestFactories(List<Factory> factories, List<Vec3f> distances, int originId,  OwnerType ownerType, int limit) {
		List<Factory> res = factories.stream()
				.filter(f -> DistanceToFactory(distances, originId, f.Id) != -1)
				.filter(f -> f.Owner == ownerType)
				.limit(limit)
				.sorted(Comparator.comparingInt(a -> DistanceToFactory(distances, originId, a.Id)))

				.collect(Collectors.toList());
		//System.err.println(" NearestFactories for origin " + originId + "  is " + res);
		return res;
	}

	public static List<Factory> NearestFactories(List<Factory> factories, List<Vec3f> distances, int originId, OwnerType ownerType,
			int minProduction, int maxCyborgs) {
		List<Factory> res = factories.stream()
				.filter(f -> f.Owner == ownerType)
				.filter(f -> DistanceToFactory(distances, originId, f.Id) != -1)
				.filter(f -> f.Production >= minProduction)
				.filter(f -> f.CyborgsAmount <= maxCyborgs)
				.sorted(Comparator.comparingInt(a -> DistanceToFactory(distances, originId, a.Id)))
				.collect(Collectors.toList());
		//System.err.println(" NearestFactories for origin " + originId + "  is " + res);
		return res;
	}

	public static List<Troops> IncomingTroops(List<Troops> troops, int destinationId, OwnerType ownerType) {
		List<Troops> res = troops.stream()
				.filter(f -> f.Owner == ownerType)
				.filter(f -> f.DestinationFactoryId == destinationId)
				.collect(Collectors.toList());
		//System.err.println(" IncomingTroops for destination " + destinationId + "  is " + res);
		return res;
	}

	public static int DistanceToFactory(List<Vec3f> distances, float factory1Id, float factory2Id) {
		for (Vec3f d : distances) {
			if ((d.x == factory1Id && d.y == factory2Id) || (d.y == factory1Id && d.x == factory2Id)) {
				return (int) d.z;
			}
		}

		return -1;
	}

}

class Factory
{
	 int Id;
	 int CyborgsAmount;
	 int Production;
	 OwnerType Owner;
	 Factory(int id, int cyborgsAmount, int production) {
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
		if(amount < 0) {
			System.err.println("MoveTo negative amount! destId ==>"+destinationId);
			return "";
		}
		return "MOVE " + Id + " " + destinationId + " " + amount + ";";
	}

	public String UpgradeFactory() {
		return "INC " + Id + ";";
	}

	public String SendBomb(int destinationId) {
		return "BOMB " + Id + " " + destinationId + ";";
	}
}

class Troops {
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

enum OwnerType {
	ME, NEUTRAL, OPPONENT
}





