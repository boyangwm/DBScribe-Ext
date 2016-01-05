using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using WM.UnitTestScribe.DatabaseInfo;
using WM.UnitTestScribe.sqlAnalyzer;

namespace WM.UnitTestScribe.MethodInfo {
    class dbMethodMapper {
        ExtractMethodSQL extractor;
        dataSchemer db;
        List<dbTable> tablesInfo;

        public dbMethodMapper(ExtractMethodSQL ex, dataSchemer dbsche)
        {
            this.extractor = ex;
            this.db = dbsche;
            this.tablesInfo = db.tablesInfo;
        }

        public void run()
        {
            Console.WriteLine("Mapping method information with database items");
            if (extractor.methodsInfo.Count==0)
            {
                Console.WriteLine("Wrong extractor! quit..");
                Console.ReadKey(true);
                return;
            }
            foreach (desMethod m in extractor.allDirectMethods)
            {
                foreach (var p in m.sqlStmts)
                {
                    updateConnection(p, m, p.stmtType.ToString());
                }
            }
        }
        public void updateConnection(sqlStmtParser p1, desMethod m, string opt)
        { 
            List<string> idList = p1.getAllIds();
            if (idList== null) return;
            List<dbTable> tableIds = new List<dbTable>();
            List<dbColumn> columnIds = new List<dbColumn>();

            foreach (var id in idList)
            {
                if (id != null)
                {
                    if (tablesInfo.Find(x => x.name.Equals(id, StringComparison.OrdinalIgnoreCase)) != null)
                    {
                        var tempTable = tablesInfo.Find(x => x.name.Equals(id, StringComparison.OrdinalIgnoreCase));
                        tableIds.Add(tempTable);
                        foreach (var col in tempTable.columns)
                        {
                            if (idList.Find(x => x.Equals(col.name, StringComparison.OrdinalIgnoreCase)) != null) columnIds.Add(col);
                        }
                    }
                }
            }

            for (int i = 0; i < tablesInfo.Count; i++)
            {
                if (tableIds.Find(x => x == tablesInfo[i]) != null)
                {
                    tablesInfo[i].insertMethod(m, opt, "direct");
                    foreach (var mm in m.followmethods)
                    {
                        var tempMeDes = extractor.getMethodInfo(mm);
                        tablesInfo[i].insertMethod(tempMeDes, opt, "follow");
                    }
                    foreach (var mm in m.finalmethods)
                    {
                        var tempMeDes = extractor.getMethodInfo(mm);
                        tablesInfo[i].insertMethod(tempMeDes, opt, "final");
                    }
                }
            }
            if (columnIds.Count == 0) return;

            for (int i=0; i < tablesInfo.Count; i++)
            {
                for (int j=0; j<tablesInfo[i].columns.Count; j++)
                {
                    if (columnIds.Find(x => x == tablesInfo[i].columns[j]) != null)
                    {
                        tablesInfo[i].columns[j].insertMethod(m, opt, "direct");
                        foreach (var mm in m.followmethods)
                        {
                            var tempMeDes = extractor.getMethodInfo(mm);
                            tablesInfo[i].columns[j].insertMethod(tempMeDes, opt, "follow");
                        }
                        foreach (var mm in m.finalmethods)
                        {
                            var tempMeDes = extractor.getMethodInfo(mm);
                            tablesInfo[i].columns[j].insertMethod(tempMeDes, opt, "final");
                        }
                    }
                }
            }
        }
       
    }
}
