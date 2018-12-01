package MaxVsZombies;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.sun.javafx.geom.Vec2f;

class Player {

	public int PlayerX;
	public int PlayerY;

	public static void main(String args[]) {
		new Player().Run();
	}

	public void Run() {
		Scanner in = new Scanner(System.in);

		List<Human> humans = new ArrayList<>();
		List<Zombie> zombies = new ArrayList<>();
		List<Cluster<Human>> clusters = new ArrayList<>();

		// game loop
		while (true) {

			humans.clear();
			zombies.clear();
			clusters.clear();

			PlayerX = in.nextInt();
			PlayerY = in.nextInt();
			int humanCount = in.nextInt();
			for (int i = 0; i < humanCount; i++) {

				Human h = new Human();
				h.humanId = in.nextInt();
				h.humanX = in.nextInt();
				h.humanY = in.nextInt();
				h.Position = new Vec2f(h.humanX, h.humanY);
				humans.add(h);
			}
			int zombieCount = in.nextInt();
			for (int i = 0; i < zombieCount; i++) {

				Zombie z = new Zombie();

				z.zombieId = in.nextInt();
				z.zombieX = in.nextInt();
				z.zombieY = in.nextInt();
				z.zombieXNext = in.nextInt();
				z.zombieYNext = in.nextInt();
				z.Position = new Vec2f(z.zombieX, z.zombieY);
				z.PositionNext = new Vec2f(z.zombieXNext, z.zombieYNext);
				zombies.add(z);
			}

			clusters = ClusterizeHumans(humans, zombies);

			Vec2f move = null;
			Vec2f myPos = new Vec2f(PlayerX, PlayerY);
			move = clusters.stream().filter(a -> CanSave(a.NearestZombiePos, myPos, a.CenterOfCluster))
					.sorted((humanCluster, t1) -> t1.content.size()).findFirst().get().CenterOfCluster;
			Vec2f tmp = new Vec2f(move.x, move.y);
			//move = zombieId(zombies, 0);
			if (move.x == 0 && move.y == 0 /*||
                    zombies.stream().noneMatch(z -> DistanceBetween(z.PositionNext, tmp) < 1000)*/) {
				move = nearestZombie(zombies);
			}

			System.out.println(move.x + " " + move.y); // Your destination coordinates
		}
	}

	public boolean CanSave(Vec2f nearestzombiePos,
			Vec2f myPos,
			Vec2f clusterCenter) {
		//  System.err.println("nearestzombiePos " + nearestzombiePos);
		//  System.err.println("myPos " + myPos);
		//  System.err.println("clusterCenter " + clusterCenter);
		Boolean a = nearestzombiePos.distance(clusterCenter) > (myPos.distance(clusterCenter) / 2.5f - 1000);
		//   System.err.println("CanSave " + a);
		return a;
	}

	private List<Cluster<Human>> ClusterizeHumans(List<Human> humans, List<Zombie> zombies) {
		List<Cluster<Human>> clusters = new ArrayList<>();
		int ClusterRadius = 3000;
		List<Human> copy = new ArrayList<>(humans);

		while (!copy.isEmpty()) {
			Human first = copy.get(0);

			List<Human> affected = copy.stream().filter(a ->
					(a.Position.distance(first.Position)) < ClusterRadius)
					.collect(Collectors.toList());

			copy.removeAll(affected);
			Cluster<Human> cluster = new Cluster<>();
			cluster.content.addAll(affected);

			cluster.CenterOfCluster = Center(cluster.content.stream().map(a -> a.Position).collect(Collectors.toList()));
			final Vec2f center = cluster.CenterOfCluster;

            /*cluster.NearestZombiePos = zombies.stream().sorted((z1, z2) -> DistanceBetween(z1.Position, center))
                    .collect(Collectors.toList()).get(0).Position;*/

			Zombie nearest = zombies.get(0);
			int MinDist = (int) nearest.Position.distance(center);
			for (Zombie z : zombies) {

				if (z.Position.distance(center) < MinDist) {
					nearest = z;

					MinDist = (int) z.Position.distance(center);
				}
			}

			cluster.NearestZombiePos = nearest.Position;

			clusters.add(cluster);

         /*   System.err.println("New cluster size " + cluster.content.size() +
                    " any human id " + cluster.content.stream().map(a -> a.humanId).collect(Collectors.toList())
                    .toString() +
                    " NearestZombiePos " + cluster.NearestZombiePos +
                    " center " + cluster.CenterOfCluster);*/
		}

		return clusters;
	}

	private Vec2f Center(List<Vec2f> positions) {
		int x = (int) positions.stream().mapToInt(m -> (int) m.x).average().getAsDouble();
		int y = (int) positions.stream().mapToInt(m -> (int) m.y).average().getAsDouble();

		return new Vec2f(x, y);
	}

	public double DistanceToPlayer(Vec2f target) {

		return target.distance(PlayerX, PlayerY);
	}

	public Vec2f nearestZombie(List<Zombie> zombies) {
		Vec2f result = new Vec2f(0, 0);
		int minDist = Integer.MAX_VALUE;

		for (Zombie z : zombies) {
			// System.err.println("z PlayerX" + z.zombieXNext);
			// System.err.println("z PlayerY" + z.zombieYNext);

			Vec2f tmp = new Vec2f(z.zombieX, z.zombieY);
			if (DistanceToPlayer(tmp) < minDist) {
				minDist = (int) DistanceToPlayer(tmp);

				result = tmp;
			}
		}

		return result;
	}

}

class Human {
	public int humanId;
	public int humanX;
	public int humanY;
	public Vec2f Position;

}

class Zombie {
	public int zombieId;
	public int zombieX;
	public int zombieY;
	public int zombieXNext;
	public int zombieYNext;

	public Vec2f PositionNext;
	public Vec2f Position;

}

class Cluster<Human> {
	List<Human> content = new ArrayList<>();
	Vec2f CenterOfCluster;
	Vec2f NearestZombiePos;

}