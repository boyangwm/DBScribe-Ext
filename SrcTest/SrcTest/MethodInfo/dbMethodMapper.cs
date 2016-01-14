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

        //This method would analyze all the methods saved in the extractor and mapping these methods with correct table or column.
        public void run()
        {
            Console.WriteLine("Mapping method information with database items");
            if (extractor.methodsInfo.Count==0)
            {
                Console.WriteLine("Wrong extractor! quit..");
                Console.ReadKey(true);
                return;
            }
            int progressCount=0;
            foreach (desMethod m in extractor.allDirectMethods)
            {
                foreach (var p in m.sqlStmts)
                {
                    updateConnection(p, m, p.stmtType.ToString());
                }
                progressCount++;
                Console.WriteLine("Progress: " + progressCount * 100 / extractor.allDirectMethods.Count + "%..");
            }
        }

        //This method would extract all the ids contain in one sql statement. These ids might be table name, column name or just some id useless. Then, we would check how many table name and column name are contained in the sql statement "p1". Finally, we would connect each table and column in the id list with this method "m".
        public void updateConnection(sqlStmtParser p1, desMethod m, string opt)
        { 
            //This means all the component id in one sql statement. This id might be table name, column name or just some id useless.
            List<string> idList = p1.getAllIds();
            if (idList== null) return;
            List<dbTable> tableIds = new List<dbTable>();
            List<dbColumn> columnIds = new List<dbColumn>();

            //We check how many table name the id list contains and how many columns id it contians.
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

            //We connect each table id in the id list with this method "m".
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

            //we connect all the column id in the id list with the method m.
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
