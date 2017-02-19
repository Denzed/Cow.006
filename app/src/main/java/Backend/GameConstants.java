package Backend;

public class GameConstants {
    public static final int DECK_SIZE = 104;
    public static final int NOT_A_CARD = 0;
    static public final int ROWS = 4;
    public static final int COLUMNS = 5;
    static final int CARD_PENALTY[] = {0,
        1, 1, 1, 1, 2, 1, 1, 1, 1, 3,
        5, 1, 1, 1, 2, 1, 1, 1, 1, 3,
        1, 5, 1, 1, 2, 1, 1, 1, 1, 3,
        1, 1, 5, 1, 2, 1, 1, 1, 1, 3,
        1, 1, 1, 5, 2, 1, 1, 1, 1, 3,
        1, 1, 1, 1, 7, 1, 1, 1, 1, 3,
        1, 1, 1, 1, 2, 5, 1, 1, 1, 3,
        1, 1, 1, 1, 2, 1, 5, 1, 1, 3,
        1, 1, 1, 1, 2, 1, 1, 5, 1, 3,
        1, 1, 1, 1, 2, 1, 1, 1, 5, 3,
        1, 1, 1, 1};
    static final int ROUNDS = 10;
    static final int STOP_POINTS = 1;
    public enum SmallestTakeTypes {SMALLEST_TAKE, SMALLEST_NOT_TAKE}
}
