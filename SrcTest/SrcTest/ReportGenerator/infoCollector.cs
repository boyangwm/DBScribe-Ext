using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using WM.UnitTestScribe.MethodInfo;
using WM.UnitTestScribe.Summary;
using WM.UnitTestScribe.DatabaseInfo;
using Antlr3.ST;

namespace WM.UnitTestScribe.ReportGenerator {
    class infoCollector {
        public HashSet<SingleSummary> AllTableSummary;
        public HashSet<SingleSummary> AllColumnSummary;
        public ExtractMethodSQL extractor;
        public string outputLoc;
        dataSchemer db;

        public infoCollector(ExtractMethodSQL ex, dataSchemer dbsche, string reportLoc)
        {
            this.AllTableSummary = new HashSet<SingleSummary>();
            this.AllColumnSummary = new HashSet<SingleSummary>();
            this.extractor = ex;
            this.db = dbsche;
            this.outputLoc = reportLoc;
        }

        //This method would generate the summary for the project and prepare all the other data for final report.
        public void run()
        {
            Console.WriteLine("Starting collecting data for reporter");

            StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
            StringTemplate stsum = group.GetInstanceOf("ProjectSummary");
            int tableCount = 0;
            int totalCount = 0;
            foreach (var tempT in db.tablesInfo)
            {
                if (tempT.directMethods.Count > 0) { tableCount++; totalCount++; }
                foreach (var tempC in tempT.columns)
                {
                    if (tempC.directMethods.Count > 0) totalCount++;
                }
            }
            stsum.SetAttribute("TableNumber", tableCount);
            stsum.SetAttribute("AllNumber", totalCount);
            stsum.SetAttribute("MethodNumber", extractor.allDirectMethods.Count);
            GenerateSummary(stsum.ToString());
        }

        //This function is used to generate the summary of whole target projet. The summary is showed in the left part of our report, the details is defined in the two HashSet above.
        public void GenerateSummary(string stsum)
        {
            Console.WriteLine("Now let's generate summary..");
            foreach(var table in db.tablesInfo)
            {
                table.generateDescription(db);
                if (table.directMethods.Count>0 || table.columns.Find(x => x.directMethods.Count>0)!=null)
                {
                    SingleSummary tcSummary = new SingleSummary(table.title, table.attribute, table.methodsDes,table.name,table.generateLeftIndex());
                    AllTableSummary.Add(tcSummary);
                }
                foreach(var column in table.columns)
                {
                    column.generateDescription(db);
                    if (column.directMethods.Count == 0) continue;
                    SingleSummary tcSummary = new SingleSummary(column.title, column.attribute, column.methodsDes,table.name,"");
                    AllColumnSummary.Add(tcSummary);
                }
            }

            //generating Javascript for webpage
            StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
            StringTemplate st = group.GetInstanceOf("GenerateScript");
            foreach(var me in extractor.methodsInfo)
            {
                st.SetAttribute("FunctionName",TakePointOff(me.name));
                st.SetAttribute("MethodFullName",me.name);
                st.SetAttribute("MethodName", me.methodself.Name);
            }
            FinalGenerator homePageGenerator = new FinalGenerator(this.AllTableSummary, this.AllColumnSummary,st.ToString(),stsum);
            homePageGenerator.Generate(outputLoc);
        }

        public string TakePointOff(string ori)
        {
            string result = "";
            result = ori.Replace(".", "");
            return result;
        }
    }
}
