package fina2.util.search;

import java.util.StringTokenizer;

public class SearchWord {
	public static final int MAX_SEARCH_LEVEL = 5;

	// Search Word
	public static boolean searchWord(String source, String text, int level) {
		boolean result = false;
		switch (level) {
		case 1: {
			result = searchLevel1(source, text);
			break;
		}
		case 2: {
			result = searchLevel2(source, text);
			break;
		}
		case 3: {
			result = searchLevel3(source, text);
			break;
		}
		case 4: {
			result = searchLevel4(source, text);
			break;
		}
		default: {
			result = searchAll(source, text);
		}
		}
		return result;
	}

	// All Search
	private static boolean searchAll(String source, String text) {
		if (source.equals(text)) {
			return true;
		}
		String temp = null;
		for (int i = 0; i < source.length(); i++) {
			try {
				temp = source.substring(i, i + text.length());
			} catch (StringIndexOutOfBoundsException ex) {
				return false;
			}
			if (temp.equals(text)) {
				return true;
			}
		}
		return false;
	}

	// Search Level 1
	private static boolean searchLevel1(String source, String text) {
		StringTokenizer st1 = new StringTokenizer(source, "[");
		if (st1.hasMoreElements()) {
			StringTokenizer st2 = new StringTokenizer(st1.nextElement()
					.toString(), "]");
			if (st2.hasMoreElements()) {
				return text.equals(st2.nextElement());
			}
		}
		return false;
	}

	// Search Level 2
	private static boolean searchLevel2(String source, String text) {
		StringTokenizer st1 = new StringTokenizer(source, "[");
		if (st1.hasMoreElements()) {
			StringTokenizer st2 = new StringTokenizer(st1.nextElement()
					.toString(), "]");
			if (st2.hasMoreElements()) {
				return searchAll(st2.nextElement().toString(), text);
			}
		}
		return false;
	}

	// Search Level 3
	private static boolean searchLevel3(String source, String text) {
		StringTokenizer st1 = new StringTokenizer(source, "]");
		String src = null;
		while (st1.hasMoreElements()) {
			src = st1.nextElement().toString();
		}
		if (src != null)
			return src.equals(text);
		return false;
	}

	// Search Level 4
	private static boolean searchLevel4(String source, String text) {
		StringTokenizer st1 = new StringTokenizer(source, "]");
		String src = null;
		while (st1.hasMoreElements()) {
			src = st1.nextElement().toString();
		}
		if (src != null)
			return searchAll(src, text);
		return false;
	}
}
