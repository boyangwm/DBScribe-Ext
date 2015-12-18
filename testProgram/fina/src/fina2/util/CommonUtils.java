package fina2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import fina2.i18n.LanguageSession;
import fina2.i18n.LanguageSessionHome;

public class CommonUtils {

	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public static double formatNumber(double d, String pattern) {
		double res = 0.0;
		try {
			DecimalFormat df = new DecimalFormat(pattern);
			res = Double.valueOf(df.format(d));
		} catch (NumberFormatException ex) {
             throw new NumberFormatException("Could not format "+d+" with pattern "+pattern);
		}
		return res;
	}
	
	public static HashMap<Integer, String> getData(String ids) {
		HashMap<Integer, String> mm = new HashMap<Integer, String>();
		String arr[] = ids.split(",");

		String subArr[] = new String[500];
		String s = "";
		int k = 0;

		for (int i = 0; i <= arr.length - 500; i += 500) {
			System.arraycopy(arr, i, subArr, 0, subArr.length);
			for (int ii = 0; ii < subArr.length; ii++) {
				if (ii == subArr.length - 1)
					s += subArr[ii];
				else
					s += subArr[ii] + ",";
			}
			mm.put(k, s);
			k++;
			s = "";
		}
		int ss = arr.length / 500;
		if (arr.length % 500 != 0) {
			for (int i = ss * 500; i < arr.length; i++) {
				if (i == arr.length - 1)
					s += arr[i];
				else
					s += arr[i] + ",";
			}
			mm.put(k, s);

		}
		k = 0;
		s = "";
		subArr = null;
		return mm;
	}

}
