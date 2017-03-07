package CodeOfRings.V1;
import java.util.*;

class Player {

    private static String Alphabet = " abcdefghijklmnopqrstuvwxyz".toUpperCase();;
    private static char[] Alphabet_Array = Alphabet.toCharArray();;

    private static char LETTER_FORWARD = '+';
    private static char LETTER_BACKWARD = '-';
    private static char TRIGGER = '.';


    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String magicPhrase = in.nextLine();
        System.err.println("magicPhrase " + magicPhrase);

        StringBuilder result = new StringBuilder();
        int currentRunePosition = 0;
        for (char c : magicPhrase.toCharArray())
        {

            //System.err.println("currentRunePosition letter" + Alphabet_Array [currentRunePosition]);
            int relativeCharIndex = Alphabet.indexOf(c) - currentRunePosition;
            //System.err.println("currentRunePosition " + currentRunePosition);
            //System.err.println("relativeCharIndex " + relativeCharIndex);

            System.err.println("");
            if(relativeCharIndex != 0)
            {
                char command = relativeCharIndex > 0 ? LETTER_FORWARD : LETTER_BACKWARD;
                //System.err.println("command " + command);
                appendMultiple(result, command, Math.abs(relativeCharIndex) );
            }
            result.append(TRIGGER);
            currentRunePosition = Alphabet.indexOf(c);
        }

        System.out.println(result);
    }

    private static StringBuilder appendMultiple(StringBuilder source, char c, int times)
    {
        for (int i = 0; i < times; i++)
        {
            source.append(c);
        }
        return source;
    }
}