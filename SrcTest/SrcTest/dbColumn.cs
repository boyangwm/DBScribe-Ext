using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;


namespace WM.UnitTestScribe
{
    class dbColumn
    {
        public List<desMethod> directMethods;
        public List<desMethod> followMehtods;
        public List<desMethod> finalMethods;
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
            this.title = "Column:" + clName+",Table:" +tableName;
            this.attribute = "";
            this.methodsDes = "";
        }

        public void generateDescription(dataSchemer db)
        {
            attribute += "This column belongs to Table: " + tableName +". It contains data with type " + db.GetOneColumnInfo(tableName ,name, "DATA_TYPE")+". ";
            string length = db.GetOneColumnInfo(tableName, name, "CHARACTER_MAXIMUM_LENGTH");
            if (length != " ") attribute += "The max length of data is " + length + ". ";
            methodsDes = "<br><b>Methods directly access this table:</b>";
            foreach (var m in directMethods)
            {
                methodsDes += "</p> Method: " + m.name + ". This method could " + m.swumsummary + ".";
            }
            if (followMehtods.Count > 0)
            {
                methodsDes += "<br><br> <b>Methods might access this table:</b>";
                foreach (var m in followMehtods)
                {
                    methodsDes += "</p> Method: " + m.name + ". This method could " + m.swumsummary + ".";
                }
            }
            if (finalMethods.Count > 0)
            {
                methodsDes += "<br><br> <b>Methods might access this table and in the highest level:</b>";
                foreach (var m in finalMethods)
                {
                    methodsDes += "</p> Method: " + m.name + ". This method could " + m.swumsummary + ".";
                }
            }
        }
        public void insertMethod(desMethod m, string instruction)
        {
            if (instruction == "direct")
            {
                if (this.directMethods.Find(x => x == m) == null)
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