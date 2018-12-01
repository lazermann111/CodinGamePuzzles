package GhostInTheCell;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sun.javafx.geom.Vec3f;

class Helpers {
	public static void AddTroops(List<Troops> troops, int id, int owner, int source, int destination, int amount, int turnsToDestination) {
		Troops t = new Troops(id, source, destination, amount, turnsToDestination);
		switch (owner) {
			case -1:
				t.Owner = OwnerType.OPPONENT;
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
				f.Owner = OwnerType.NEUTRAL;
				break;
			case 1:
				f.Owner = OwnerType.ME;
				break;
			default:
				break;

		}

		factories.add(f);
	}

	public static Factory StongestFactory(List<Factory> factories, OwnerType owner) {
		Factory res = factories.stream()
				.filter(f -> f.Owner == owner)
				.sorted(Comparator.comparingInt(a -> -a.CyborgsAmount))
				.findFirst()
				.get();
		System.err.println(" StongestFactory " + res);
		return res;
	}

	public static Factory NearestFactory(List<Factory> factories, List<Vec3f> distances, int originId, OwnerType ownerType, int minProduction) {
		Optional<Factory> a = factories.stream()
				.filter(f -> f.Owner == ownerType)
				.filter(f -> DistanceToFactory(distances, originId, f.Id) != -1)
				.sorted(Comparator.comparingInt(b -> DistanceToFactory(distances, originId, b.Id)))
				.filter(f -> f.Production >= minProduction)
				.findFirst();
		a.ifPresent(factory -> System.err.println(" NearestFactory for origin " + originId + "  is " + factory));
		return a.isPresent() ? a.get() : null;
	}

	public static List<Factory> MyFactories(List<Factory> factories) {
		return factories.stream()
				.filter(f -> f.Owner == OwnerType.ME)
				.collect(Collectors.toList());
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
		System.err.println(" NearestFactories for origin " + originId + "  is " + res);
		return res;
	}

	public static List<Troops> IncomingTroops(List<Troops> troops, int destinationId, OwnerType ownerType) {
		List<Troops> res = troops.stream()
				.filter(f -> f.Owner == ownerType)
				.filter(f -> f.DestinationFactoryId >= destinationId)
				.collect(Collectors.toList());
		System.err.println(" IncomingTroops for destination " + destinationId + "  is " + res);
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

	public static List<Factory> NearestFactories(List<Factory> factories, List<Vec3f> distances, int originId) {
		List<Factory> res = factories.stream()
				.filter(f -> DistanceToFactory(distances, originId, f.Id) != -1)
				.sorted(Comparator.comparingInt(a -> DistanceToFactory(distances, originId, a.Id)))
				.collect(Collectors.toList());
		System.err.println(" NearestFactories for origin " + originId + "  is " + res);
		return res;
	}

}

class Factory {
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

	public String MoveTo(int destinationId, int amount) {
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
