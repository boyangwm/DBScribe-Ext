using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;
using WM.UnitTestScribe.MethodInfo;

namespace WM.UnitTestScribe.DatabaseInfo
{
    class dbColumn
    {
        //this method could directly refer to dbTable, I create two classes just because I want to handle table and columns seperately and their descriptions are different.
        public List<desMethod> directMethods;
        public List<desMethod> followMehtods;
        public List<desMethod> finalMethods;
        public List<dbMethodSql> relationships;
        public string title;
        public string attribute;
        public string methodsDes;
        public string name;
        public string tableName;
        public dbColumn(string tlName, string clName)
        {
            this.directMethods = new List<desMethod>();
            this.followMehtods = new List<desMethod>();
            this.finalMethods = new List<desMethod>();
            this.name = clName;
            this.tableName = tlName;
            this.title = "Column: " + clName+", Table: " +tableName;
            this.attribute = "";
            this.methodsDes = "";
            this.relationships = new List<dbMethodSql>();
        }

        public string TakeSpaceOff(string ori)
        {
            string result = "";
            if (ori.Last() == ' ') result = ori.Substring(0, ori.Length - 1);
            else result = ori;
            return result;
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

        public void generateDescription(dataSchemer db)
        {
            attribute += "This column belongs to Table: " + tableName +". It contains data with type <b>" + TakeSpaceOff(db.GetOneColumnInfo(tableName ,name, "DATA_TYPE"))+"</b>. ";
            string length = db.GetOneColumnInfo(tableName, name, "CHARACTER_MAXIMUM_LENGTH");
            if (length != " ") attribute += "The max length of data is " + TakeSpaceOff(length) + ". ";
            if (directMethods.Count == 0)
            {
                methodsDes = "<br><b>No method interacts with this column directly.</b>";
                return;
            }
            methodsDes = "<br><b>Methods directly access this column:</b>";
            foreach (var m in directMethods)
            {
                methodsDes += m.getHtmlDescribe(getRelationships(m.name), "column", "directly");
            }
            if (followMehtods.Count > 0)
            {
                methodsDes += "<br><br> <b>Methods might access this column:</b>";
                foreach (var m in followMehtods)
                {
                    methodsDes += m.getHtmlDescribe(getRelationships(m.name), "column", "via delegation");
                }
            }
            if (finalMethods.Count > 0)
            {
                methodsDes += "<br><br> <b>Methods might access this column and in the highest level:</b>";
                foreach (var m in finalMethods)
                {
                    methodsDes += m.getHtmlDescribe(getRelationships(m.name), "column", "via delegation");
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