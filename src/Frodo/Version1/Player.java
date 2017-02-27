package Frodo.Version1;

import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static String Alphabet = " abcdefghijklmnopqrstuvwxyz".toUpperCase();;
    public static char[] Alphabet_Array = Alphabet.toCharArray();;

/*>: Bilbo moves one zone to the right.
<: Bilbo moves one zone to the left.
+: Bilbo rolls the letter on the rune of the zone he is in one letter along the alphabet.
            -: Bilbo rolls the letter on the rune of the zone he is in one letter back through the alphabet.
.: Bilbo triggers the rune to add its letter to the magic phrase.*/

    public static char MOVE_RIGHT = '>';
    public static char MOVE_LEFT = '<';
    public static char LETTER_FORWARD = '+';
    public static char LETTER_BACKWARD = '-';
    public static char TRIGGER = '.';


    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String magicPhrase = in.nextLine();
        System.err.println("magicPhrase " + magicPhrase);
        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
        StringBuilder result = new StringBuilder();
        int currentRunePosition = 0;
        for (char c : magicPhrase.toCharArray())
        {

            System.err.println("currentRunePosition letter" + Alphabet_Array [currentRunePosition]);
            int relativeCharIndex = Alphabet.indexOf(c) - currentRunePosition;
            System.err.println("currentRunePosition " + currentRunePosition);
            System.err.println("relativeCharIndex " + relativeCharIndex);

            System.err.println("");
            if(relativeCharIndex != 0)
            {
                char command = relativeCharIndex > 0 ? LETTER_FORWARD : LETTER_BACKWARD;
                System.err.println("command " + command);
                appendMultiple(result, command, Math.abs(relativeCharIndex) );
            }
            result.append(TRIGGER);
            currentRunePosition = Alphabet.indexOf(c);
        }

        //System.out.println("+.>-.");
        System.out.println(result);
    }

    public static StringBuilder appendMultiple(StringBuilder source , char c, int times)
    {
        for (int i = 0; i < times; i++)
        {
            source.append(c);
        }
        return source;
    }
}