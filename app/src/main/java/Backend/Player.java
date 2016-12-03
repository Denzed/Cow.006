package Backend;

import java.util.Scanner;

public class Player  extends AbstractPlayer{

    public int tellMove() {
        //by tapping the screen
        System.out.println("Please, make a move");
        System.out.print("Cards left: ");
        for (int x : hand){
            System.out.print(x + " ");
        }
        System.out.println();

        int value;
        Scanner in = new Scanner(System.in);
        while(true) {
            value = in.nextInt();
            if (hand.contains(value)) {
                hand.remove(Integer.valueOf(value));
                System.out.print("Played: " + value);
                System.out.print(" Cards left: ");
                for (int x : hand){
                    System.out.print(x + " ");
                }
                System.out.println();

                break;
            }
            else {
                System.out.println("You don't have this card\nPlease, make a move");
            }

            System.out.print("Cards left: ");
            for (int x : hand){
                System.out.print(x + " ");
            }
            System.out.println();

        }
        return value;
    }

    public int tellChosenRow() {
        System.out.println("Please, choose a row");
        int index;
        Scanner in = new Scanner(System.in);
        while(true) {
            index = in.nextInt();
            if (0 <= index && index < ROWS) {
                return index;
            }
            else {
                System.out.println("Not in range\nPlease, choose a row");
            }
        }
    }

}
