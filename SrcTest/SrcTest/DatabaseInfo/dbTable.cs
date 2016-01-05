using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Irony;
using Irony.Parsing;
using ABB.SrcML.Data;
using Antlr3.ST;
using WM.UnitTestScribe.MethodInfo;

namespace WM.UnitTestScribe.DatabaseInfo{
    class dbTable
    {
        public List<dbColumn> columns;
        public List<desMethod> directMethods;
        public List<desMethod> followMehtods;
        public List<desMethod> finalMethods;
        public List<dbMethodSql> relationships;
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
            this.title = "Table: " + name;
            this.attribute = "";
            this.methodsDes = "";
            this.relationships = new List<dbMethodSql>();
        }

        public List<string> getRelationships(string name)
        {
            dbMethodSql tempMS = relationships.Find(x => x.methodName == name);
            return tempMS.sqlSequence;
        }

        public void insertRelationships(string name, string sql)
        {
            if (relationships.Find(x => x.methodName == name) == null)
            {
                relationships.Add(new dbMethodSql(name));
            }
            var tempMS = relationships.Find(x => x.methodName == name);
            if (tempMS.sqlSequence.Find(x => x == sql) == null)
            {
                tempMS.sqlSequence.Add(sql);
            }
            return;
        }

        public string TakeSpaceOff(string ori)
        {
            string result = "";
            if (ori.Last() == ' ') result = ori.Substring(0, ori.Length - 1);
            else result = ori;
            return result;
        }

        public string generateLeftIndex ()
        {
            StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
            StringTemplate st = group.GetInstanceOf("TableLeft");
            string finalstring = "";
            finalstring += "<a href=\"#" + this.title +"\">" + this.title +"</a><br>";
            foreach (var col in this.columns)
            {
                if (col.directMethods.Count==0) continue;
                st.SetAttribute("Columns", "Column:"+col.name);
                st.SetAttribute("Links", col.title);
            }
            finalstring += st.ToString();
            return finalstring;
        }

        public void generateDescription (dataSchemer db)
        {
            attribute = "This table contains columns: ";
            foreach (var col in this.columns)
            {
                if (col.directMethods.Count == 0) continue;
                attribute += "<a href=\"#" + col.title + "\">" + col.name + "</a>" + ", ";
            }
            attribute += " etc. ";
            attribute += " This table is created at" + TakeSpaceOff(db.GetOneTableInfo(name, "CREATE_TIME")) + ". ";
            attribute += "It contails " + db.GetOneTableInfo(name, "TABLE_ROWS") + " items totally. ";
            if (directMethods.Count==0)
            {
                methodsDes = "<br><b>No method interacts with this table directly.</b>";
                return;
            }
            methodsDes = "<br><b>Methods directly access this table:</b>";
                    foreach (var m in directMethods)
                    {
                        methodsDes += m.getHtmlDescribe(getRelationships(m.name),"table");
                    }
                    if (followMehtods.Count>0)
                    {
                        methodsDes += "<br><br><b>Methods might access this table:</b>";
                        foreach (var m in followMehtods)
                        {
                            methodsDes += m.getHtmlDescribe(getRelationships(m.name), "table");
                        }
                    }
                    if (finalMethods.Count > 0)
                    {
                        methodsDes += "<br><br><b>Methods might access this table and in the highest level:</b>";
                        foreach (var m in finalMethods)
                        {
                            methodsDes += m.getHtmlDescribe(getRelationships(m.name), "table");
                        }
                    }
        }
        public void insertMethod(desMethod m, string opt, string instruction)
        {
            if (instruction == "direct")
            {
                if (this.directMethods.Find(x => x.name == m.name) == null)
                {
                    this.directMethods.Add(m);
                }
                insertRelationships(m.name, opt);
                return;
            }
            if (instruction == "follow")
            {
                if (this.followMehtods.Find(x => x.name == m.name) == null)
                {
                    this.followMehtods.Add(m);
                }
                insertRelationships(m.name, opt);
                return;
            }
            if (instruction == "final")
            {
                if (this.finalMethods.Find(x => x.name == m.name) == null)
                {
                    this.finalMethods.Add(m);
                }
                insertRelationships(m.name, opt);
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
