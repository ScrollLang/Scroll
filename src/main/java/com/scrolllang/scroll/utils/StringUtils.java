package com.scrolllang.scroll.utils;

public class StringUtils {

	/**
	 * Appends the english order suffix to the given number.
	 * 
	 * @param i the number
	 * @return 1st, 2nd, 3rd, 4th, etc.
	 */
	public static String fancyOrderNumber(int i) {
		int iModTen = i % 10;
		int iModHundred = i % 100;
		if (iModTen == 1 && iModHundred != 11)
			return i + "st";
		if (iModTen == 2 && iModHundred != 12)
			return i + "nd";
		if (iModTen == 3 && iModHundred != 13)
			return i + "rd";
		return i + "th";
	}

}
