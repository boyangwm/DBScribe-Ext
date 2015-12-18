package fina2.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import fina2.ui.table.TableRow;

@SuppressWarnings("serial")
public class Comparators implements Serializable {
	private static Logger log = Logger.getLogger(Comparators.class);

	// Comparator String
	public static class TableRowComparatorValueString implements
			Comparator<TableRow> {
		private int[] fieldOrder = null;

		public TableRowComparatorValueString(int... fieldOrder) {
			this.fieldOrder = fieldOrder;
		}

		@Override
		public int compare(TableRow o1, TableRow o2) {

			for (int a : fieldOrder) {
				try {
					String o1String = o1.getValue(a);
					String o2String = o2.getValue(a);
					int result = o1String.compareTo(o2String);
					if (result != 0) {
						return result;
					}
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			return 0;
		}

	}

	// Comparator Date
	public static class TableRowComparatorValueDate implements
			Comparator<TableRow> {
		private int[] fieldOrder = null;
		private DateFormat dateFormat = null;

		public TableRowComparatorValueDate(DateFormat dateFormat,
				int... fieldOrder) {
			this.fieldOrder = fieldOrder;
			this.dateFormat = dateFormat;
		}

		@Override
		public int compare(TableRow o1, TableRow o2) {
			for (int a : fieldOrder) {
				try {
					Date o1Date = dateFormat.parse(o1.getValue(a));
					Date o2Date = dateFormat.parse(o2.getValue(a));
					int result = o1Date.compareTo(o2Date);
					if (result != 0) {
						return result;
					}
				} catch (ParseException e) {
					log.error(e.getMessage(), e);
				}
			}
			return 0;
		}
	}
}
