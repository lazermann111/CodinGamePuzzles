package CodeOfRings.V2;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Player {

	public static String Alphabet = " abcdefghijklmnopqrstuvwxyz".toUpperCase();
	;
	public static char[] Alphabet_Array = Alphabet.toCharArray();
	;
	public static int Alphabet_SIZE = Alphabet.length();

	public static char MOVE_RIGHT = '>';
	public static char MOVE_LEFT = '<';
	public static char LETTER_FORWARD = '+';
	public static char LETTER_BACKWARD = '-';
	public static char TRIGGER = '.';

	public static Map<Character, Integer> WordRunePosition = new HashMap<>();
	public static int CurrentRune = 0;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		String magicPhrase = in.nextLine();
		//System.err.println("magicPhrase " + magicPhrase);
		// Write an action using System.out.println()
		// To debug: System.err.println("Debug messages...");
		StringBuilder result = new StringBuilder();
		int currentRuneLetter = 0;
		int i = 0;
		char prevChar = ' ';
		for (char c : magicPhrase.toCharArray()) {
			if (WordRunePosition.containsKey(c)) {
				int t = WordRunePosition.get(c) - CurrentRune;
				MoveToRune(result, t);
				//System.err.println("CurrentRune is " + CurrentRune);
				//System.err.println("WordRunePosition for " + prevChar + " is " + WordRunePosition.get(prevChar));
				assert (CurrentRune == WordRunePosition.get(prevChar));
				//System.err.println("WordRunePosition for " + c + " is " + WordRunePosition.get(c));
			} else {
				currentRuneLetter = 0;

				if (i != 0) {
					// find free rune
					int idx = -555;
					//System.err.println("find free rune " + CurrentRune);
					for (int a = 1; i < Alphabet_SIZE; a++) {
						if (!WordRunePosition.containsValue(a)) {
							idx = a;
							break;
						}
					}
					//System.err.println("free rune found " + idx);
					MoveToRune(result, idx - CurrentRune);
					CurrentRune = idx;
					//System.err.println("new CurrentRune " + CurrentRune);
				}
				// System.err.println("currentRunePosition letter" + Alphabet_Array [currentRuneLetter]);
				int relativeCharIndex = Alphabet.indexOf(c);// - currentRuneLetter;
				// System.err.println("currentRunePosition " + currentRuneLetter);

				if (relativeCharIndex > Alphabet_SIZE / 2) {
					relativeCharIndex -= Alphabet_SIZE;
				}
				//System.err.println("relativeCharIndex " + relativeCharIndex);
				if (relativeCharIndex != 0) {
					char command = relativeCharIndex > 0 ? LETTER_FORWARD : LETTER_BACKWARD;
					//System.err.println("command " + command);
					appendMultiple(result, command, Math.abs(relativeCharIndex));
				}
				WordRunePosition.put(c, CurrentRune);
			}

			i++;
			result.append(TRIGGER);
			// currentRuneLetter = Alphabet.indexOf(c);
			CurrentRune = WordRunePosition.get(c);
			prevChar = c;
		}

		//System.out.println("+.>-.");
		System.out.println(result);
	}

	private static StringBuilder MoveToRune(StringBuilder source, int relativeIdx) {
		char command = relativeIdx > 0 ? MOVE_RIGHT : MOVE_LEFT;
		return appendMultiple(source, command, Math.abs(relativeIdx));
	}

	private static StringBuilder appendMultiple(StringBuilder source, char c, int times) {
		for (int i = 0; i < times; i++) {
			source.append(c);
		}
		return source;
	}
}