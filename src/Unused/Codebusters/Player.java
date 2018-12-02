package Unused.Codebusters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec2f;

class Player {

	private static Vec2d[] TeamBases = new Vec2d[] {new Vec2d(0, 0), new Vec2d(16000, 9000)};

	private Scanner in;
	private static int GHOST_INTERACTION_DISTANCE = 400;

	int bustersPerPlayer; // the amount of busters you control
	int ghostCount; // the amount of ghosts on the map
	int myTeamId; // if this is 0, your base is on the top left of the map, if it is one, on the bottom right

	public List<Buster> busters = new ArrayList<>();
	public List<Ghost> ghosts = new ArrayList<>();

	Vec2d myBasePosition;

	public Player Init() {
		in = new Scanner(System.in);
		bustersPerPlayer = in.nextInt();
		ghostCount = in.nextInt();
		myTeamId = in.nextInt();
		myBasePosition = TeamBases[myTeamId];

		return this;
	}

	public static void main(String args[]) {
		new Player().Init().Run();
	}

	public void Run() {

		// game loop
		while (true) {
			ghosts.clear();
			busters.clear();

			int entities = in.nextInt(); // the number of busters and ghosts visible to you
			for (int i = 0; i < entities; i++) {
				int entityId = in.nextInt(); // buster id or ghost id
				int x = in.nextInt();
				int y = in.nextInt(); // position of this buster / ghost
				int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
				int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost.
				int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters attempting to trap this ghost.

				if (entityType == -1) {
					Helpers.AddGhost(ghosts, entityId, x, y, value);
				} else {
					Helpers.AddBuster(busters, entityId, x, y, entityType, state, value, myTeamId);
				}
			}

			StringBuilder res = new StringBuilder();
			for (int i = 0; i < bustersPerPlayer; i++) {

				Buster b = busters.get(0); // TODO
				if (b.IsCarryingGhost()) {
					CarryGhostToBase(b);
				} else {
					ChaseNearestGhost(b);
				}

				// System.out.println("MOVE 8000 4500"); // MOVE x y | BUST id | RELEASE
				System.out.println(res.toString());
			}
		}
	}

	private void ChaseNearestGhost(Buster b) {
		Ghost g = Helpers.NearestGhost(ghosts, b.Position);
		//if (g.)
	}

	private void CarryGhostToBase(Buster b) {

	}

}

class Helpers {
	public static void AddGhost(List<Ghost> ghosts, int id, int x, int y, int attackingBusters) {
		Ghost t = new Ghost(id, new Vec2f(x, y), attackingBusters);
		ghosts.add(t);
	}

	public static void AddBuster(List<Buster> busters, int id, int x, int y, int entityType, int state, int ghostCarried, int myTeamId) {
		Buster t = new Buster(id, new Vec2f(x, y), ghostCarried);

		t.Owner = entityType == myTeamId ? OwnerType.ME : OwnerType.OPPONENT;
		t.State = BusterState.values()[state];
		busters.add(t);
	}

	public static Ghost NearestGhost(List<Ghost> ghosts, Vec2f originPosition) {
		Optional<Ghost> a = ghosts.stream()
				.sorted(Comparator.comparingInt(b -> (int) b.Position.distance(originPosition)))
				.findFirst();
		a.ifPresent(g -> System.err.println(" NearestGhost for origin " + originPosition + "  is " + g));
		return a.isPresent() ? a.get() : null;
	}

}

class Ghost {
	public int Id;
	public Vec2f Position;
	public int AttackingBusters;

	public Ghost(int id, Vec2f position, int attackingBusters) {
		Id = id;
		Position = position;
		AttackingBusters = attackingBusters;
	}

	@Override
	public String toString() {
		return "Ghost{" +
				"Id=" + Id +
				", Position=" + Position +
				", AttackingBusters=" + AttackingBusters +
				'}';
	}
}

class Buster {
	public int Id;
	public Vec2f Position;

	public BusterState State;

	public OwnerType Owner;
	public int CarriedGhostId = -1;

	public boolean IsCarryingGhost() {
		return CarriedGhostId != -1;
	}

	public String Move() {
		return "MOVE " + Position.x + " " + Position.y + ";";
	}

	public String Bust(int Id) {
		return "Bust " + Id + ";";
	}

	public String Release() {
		return "RELEASE;";
	}

	public Buster(int id, Vec2f position, int carriedGhostId) {
		Id = id;
		Position = position;
		CarriedGhostId = carriedGhostId;
	}

	@Override
	public String toString() {
		return "Buster{" +
				"Id=" + Id +
				", Position=" + Position +
				", State=" + State +
				", Owner=" + Owner +
				", CarriedGhostId=" + CarriedGhostId +
				'}';
	}
}

enum BusterState {
	IDLE, CARRYING_GHOST
}

enum OwnerType {
	ME, NEUTRAL, OPPONENT
}
