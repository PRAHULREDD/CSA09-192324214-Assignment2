import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardTestSuite {

    static class Player {
        final String name;
        final int runs;

        Player(String name, int runs) {
            this.name = name;
            this.runs = runs;
        }

        @Override
        public String toString() {
            return name + "[" + runs + "]";
        }
    }

    // Wildcard utility: accepts List<? extends T> (PECS: Producer Extends)
    public static <T> void addAllToBoard(Leaderboard<T> board, List<? extends T> batch) {
        for (T item : batch) {
            board.add(item);
        }
    }

    public static void main(String[] args) {
        Comparator<Team> properOrder = Comparator
                .comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
                .thenComparing(t -> t.name);

        // Dataset 1: Multi-level ties
        Leaderboard<Team> tieBoard = new Leaderboard<>(properOrder);
        tieBoard.add(new Team("Titans", 12, 5));
        tieBoard.add(new Team("Royals", 12, 5));
        tieBoard.add(new Team("Kings", 12, 8));
        tieBoard.add(new Team("Knights", 9, 10));
        tieBoard.add(new Team("Alphas", 9, 10));
        System.out.println("Dataset 1 (Multi-level ties): " + tieBoard.ranking());

        // Dataset 2: Deliberately designed to break subtraction-based comparator (integer overflow!)
        Team extremeHigh = new Team("OverflowHigh", Integer.MAX_VALUE, 0);
        Team extremeLow = new Team("OverflowLow", -10, 0);

        // Faulty subtraction comparator: b.points - a.points overflows int!
        Comparator<Team> faultySubtraction = (a, b) -> b.points - a.points;
        System.out.println("Faulty subtraction compare(extremeHigh, extremeLow): "
                + faultySubtraction.compare(extremeHigh, extremeLow) + " (WRONG: positive means High < Low!)");
        System.out.println("Safe Integer.compare(extremeHigh, extremeLow):       "
                + properOrder.compare(extremeHigh, extremeLow) + " (CORRECT: negative means High ranks first)");

        // Reusing Leaderboard<T> for Players
        Leaderboard<Player> playerBoard = new Leaderboard<>(
                Comparator.comparingInt((Player p) -> p.runs).reversed().thenComparing(p -> p.name));
        List<Player> squad = new ArrayList<>();
        squad.add(new Player("Virat", 540));
        squad.add(new Player("Rohit", 540));
        squad.add(new Player("Gill", 610));
        addAllToBoard(playerBoard, squad);
        System.out.println("Reused Player Leaderboard: " + playerBoard.ranking());
    }
}
