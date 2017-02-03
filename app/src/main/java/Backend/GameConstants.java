package Backend;

public class GameConstants {
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
    static final int DECK_SIZE = 104;
    static final int ROUNDS = 10;
    static final int ROWS = 4;
    static final int COLUMNS = 5;
    static final int STOP_POINTS = 66;
    public enum SmallestTakeTypes {SMALLEST_TAKE, SMALLEST_NOT_TAKE}
}
