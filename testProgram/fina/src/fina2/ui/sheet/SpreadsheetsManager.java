package fina2.ui.sheet;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import fina2.ui.sheet.openoffice.OOSheet;

public class SpreadsheetsManager {

    private ArrayList spreadsheets = new ArrayList();

    private static SpreadsheetsManager instance;

    protected SpreadsheetsManager() {
    }

    public static SpreadsheetsManager getInstance() {

        if (instance == null) {
            instance = new SpreadsheetsManager();
        }
        return instance;
    }

    public Spreadsheet createSpreadsheet() {
        return cacheSpreadsheet(new OOSheet());
    }

    public Spreadsheet createSpreadsheet(InputStream in) {
        return cacheSpreadsheet(new OOSheet(in));
    }

    public Spreadsheet createSpreadsheet(byte[] buff) {
        return cacheSpreadsheet(new OOSheet(buff));
    }
    public Spreadsheet createSpreadsheet(String frameTitle) {
        return cacheSpreadsheet(new OOSheet(frameTitle));
    }
    private Spreadsheet cacheSpreadsheet(Spreadsheet sheet) {

        spreadsheets.add(new WeakReference(sheet));
        return sheet;
    }

    public void disposeSheets() {

        for (java.util.Iterator iter = spreadsheets.iterator(); iter.hasNext();) {

            WeakReference weakRef = (WeakReference) iter.next();
            Spreadsheet sheet = (Spreadsheet) weakRef.get();

            if (sheet != null) {
                sheet.dispose();
            }
        }

        if (spreadsheets.size() > 0) {
            new OOSheet().terminate();
        }
    }
}
