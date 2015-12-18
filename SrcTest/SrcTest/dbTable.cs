using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Irony;
using Irony.Parsing;
using ABB.SrcML.Data;

namespace WM.UnitTestScribe {
    class dbTable
    {
        public List<dbColumn> columns;
        public List<desMethod> directMethods;
        public List<desMethod> followMehtods;
        public List<desMethod> finalMethods;
        public string name;
        public string title;
        public string attribute;
        public string methodsDes;
        public dbTable(string tbName)
        {
            this.columns = new List<dbColumn>();
            this.directMethods = new List<desMethod>();
            this.followMehtods = new List<desMethod>();
            this.finalMethods = new List<desMethod>();
            this.name = tbName;
            this.title = "Table:" + name;
            this.attribute = "";
            this.methodsDes = "";
        }

        public void generateDescription (dataSchemer db)
        {
            attribute = "This table contains columns: ";
            foreach (var column in columns)
            {
                if (column==columns.Last()) continue;
                attribute += column.name + ", ";
            }

            attribute += columns.Last()+". ";
            attribute += " The create time of table: " + db.GetOneTableInfo(name, "CREATE_TIME") + ". ";
            attribute += "It contails " + db.GetOneTableInfo(name, "TABLE_ROWS") + " items totally. ";
            methodsDes = "<br><b>Methods directly access this table:</b>";
                    foreach (var m in directMethods)
                    {
                        methodsDes += "</p> Method: " + m.name + ". This method could " + m.swumsummary + ".";
                    }
                    if (followMehtods.Count>0)
                    {
                        methodsDes += "<br><br><b>Methods might access this table:</b>";
                        foreach (var m in followMehtods)
                        {
                            methodsDes += "</p> Method: " + m.name + ". This method could " + m.swumsummary + ".";
                        }
                    }
                    if (finalMethods.Count > 0)
                    {
                        methodsDes += "<br><br><b>Methods might access this table and in the highest level:</b>";
                        foreach (var m in finalMethods)
                        {
                            methodsDes += "</p> Method: " + m.name + ". This method could " + m.swumsummary + ".";
                        }
                    }
        }
        public void insertMethod (desMethod m, string instruction)
        {
            if (instruction=="direct")
            {
                if (this.directMethods.Find(x => x==m)==null)
                {
                    this.directMethods.Add(m);
                }
                return;
            }
            if (instruction == "follow")
            {
                if (this.followMehtods.Find(x => x == m) == null)
                {
                    this.followMehtods.Add(m);
                }
                return;
            }
            if (instruction == "final")
            {
                if (this.finalMethods.Find(x => x == m) == null)
                {
                    this.finalMethods.Add(m);
                }
                return;
            }
            return;
        }
    }
    
}

/*string description = "Method: " + m.name + ", function: " + m.swumsummary + ";";
                            if (tablesInfo[i].directMethods.Find(x => x == description) == null)
                            {
                                tablesInfo[i].directMethods.Add(description);
                            }
                            foreach(var mm in m.followmethods)
                            {
                                var mmdes = getMethodInfo(mm, cgm);
                                string mmdescription = "Method: " + mmdes.name + ", function: " + mmdes.swumsummary + ";";
                                if (tablesInfo[i].followMehtods.Find(x => x == mmdescription) == null)
                                {
                                    tablesInfo[i].followMehtods.Add(mmdescription);
                                }
                            }
                            break;*/
