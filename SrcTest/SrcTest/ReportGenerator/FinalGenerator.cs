using System;
using System.CodeDom.Compiler;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Antlr3.ST;
using WM.UnitTestScribe.Summary;
using System.IO;
using ABB.SrcML.Data;


namespace WM.UnitTestScribe.ReportGenerator {
    public class FinalGenerator {
        public HashSet<SingleSummary> AllTableSummary { get; private set; }
        public HashSet<SingleSummary> AllColumnSummary { get; private set; }
        public string AllShiftFunction;
        public string projectSum;

        public FinalGenerator(HashSet<SingleSummary> allTableSummary, HashSet<SingleSummary> allColumnSummary, string allShiftFunction, string projSum) {
            this.AllTableSummary = allTableSummary;
            this.AllColumnSummary = allColumnSummary;
            this.AllShiftFunction = allShiftFunction;
            this.projectSum = projSum;
        }

        public void Generate(string path) {
            StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
            StringTemplate st = group.GetInstanceOf("CourseHome");
            int ID = 1;
            st.SetAttribute("Function", AllShiftFunction);
            st.SetAttribute("ProjectSum", projectSum);
            foreach (var testSummary in AllTableSummary) {
                st.SetAttribute("IDNum", ID++);
                st.SetAttribute("Title", testSummary.title);
                st.SetAttribute("Content", testSummary.attributions + "</p>" + testSummary.methodInfo);
                st.SetAttribute("Index", testSummary.tableIndex);
                foreach (var subSummary in AllColumnSummary)
                {
                    if (subSummary.tableName != testSummary.tableName) continue;
                    st.SetAttribute("IDNum", ID++);
                    st.SetAttribute("Title", subSummary.title);
                    st.SetAttribute("Content", subSummary.attributions + "</p>" + subSummary.methodInfo);
                }
                
            }

            String result = st.ToString();
            StreamWriter writetext = new StreamWriter(path);
            writetext.WriteLine(result);
            writetext.Close();

        }

    }
}
