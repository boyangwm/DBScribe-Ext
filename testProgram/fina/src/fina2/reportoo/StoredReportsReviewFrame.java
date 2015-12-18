package fina2.reportoo;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import fina2.BaseFrame;
import fina2.Main;
import fina2.i18n.LanguagePK;
import fina2.reportoo.server.ReportPK;
import fina2.reportoo.server.StoredReportInfo;
import fina2.reportoo.server.StoredReportsSession;
import fina2.reportoo.server.StoredReportsSessionHome;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;

public class StoredReportsReviewFrame extends JFrame {

    private static Logger log = Logger
            .getLogger(StoredReportsReviewFrame.class);

    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    private ArrayList reports = new ArrayList();
    private int currentTabIndex = -1;

    BorderLayout borderLayout = new BorderLayout();
    JTabbedPane jTabbedPane = new JTabbedPane();
    Spreadsheet sheet = SpreadsheetsManager.getInstance().createSpreadsheet();

    public StoredReportsReviewFrame() {
        try {
            jbInit();
            BaseFrame.ensureVisible(this);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void show(Collection storedReportInfo) {

        super.setVisible(true);

        for (Iterator iter = storedReportInfo.iterator(); iter.hasNext();) {
            StoredReportInfo storedReport = (StoredReportInfo) iter.next();

            try {
                InitialContext jndi = fina2.Main.getJndiContext();
                Object ref = jndi
                        .lookup("fina2/reportoo/server/StoredReportsSession");
                StoredReportsSessionHome home = (StoredReportsSessionHome) PortableRemoteObject
                        .narrow(ref, StoredReportsSessionHome.class);

                StoredReportsSession session = home.create();

                ReportPK reportPK = new ReportPK(storedReport.getReportId());
                LanguagePK langPK = new LanguagePK(storedReport.getLangId());

                byte[] reportData = session.getStoredReport(langPK, reportPK,
                        storedReport.getReportInfoHashCode());

                if (reportData != null) {
                    reports.add(reportData);
                    jTabbedPane.addTab(storedReport.getName(), new JPanel());
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                Main.generalErrorHandler(ex);
            }
        }
    }

    private void jbInit() throws Exception {

        getContentPane().setLayout(borderLayout);

        setTitle(ui.getString("fina2.report.storedReportsReviewFrame"));

        jTabbedPane.setFont(ui.getFont());
        jTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane
                .setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                reportChanged();
            }
        });

        this.getContentPane().add(jTabbedPane, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(sheet.getComponent(),
                java.awt.BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                formComponentResized(evt);
            }

            public void componentMoved(ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                exitForm(evt);
            }
        });
    }

    private void reportChanged() {

        if (jTabbedPane.getSelectedIndex() != currentTabIndex) {

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                if (currentTabIndex >= 0) {

                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    sheet.write(bo);
                    reports.remove(this.currentTabIndex);
                    reports.add(this.currentTabIndex, bo.toByteArray());
                    bo.close();
                }

                // Set the current tab index value
                this.currentTabIndex = jTabbedPane.getSelectedIndex();

                sheet.read((byte[]) reports.get(this.currentTabIndex));
                sheet.setViewMode(sheet.VIEW_FULL);
                sheet.afterShow();

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void formComponentMoved(java.awt.event.ComponentEvent evt) { //GEN-FIRST:event_formComponentMoved
        ui.putConfigValue("fina2.reportoo.StoredReportsReviewFrame.x",
                new Integer(getX()));
        ui.putConfigValue("fina2.reportoo.StoredReportsReviewFrame.y",
                new Integer(getY()));
    } //GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) { //GEN-FIRST:event_formComponentResized
        ui.putConfigValue("fina2.reportoo.StoredReportsReviewFrame.width",
                new Integer(getWidth()));
        ui.putConfigValue("fina2.reportoo.StoredReportsReviewFrame.height",
                new Integer(getHeight()));
    } //GEN-LAST:event_formComponentResized

    private void exitForm(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_exitForm
        // Dispose sheet
        sheet.dispose();
        dispose();
    }
}
