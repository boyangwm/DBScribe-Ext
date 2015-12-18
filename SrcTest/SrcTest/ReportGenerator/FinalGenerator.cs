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


        public FinalGenerator(HashSet<SingleSummary> allTableSummary, HashSet<SingleSummary> allColumnSummary) {
            this.AllTableSummary = allTableSummary;
            this.AllColumnSummary = allColumnSummary;
        }



        public void Generate(string path) {
            StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
            StringTemplate st = group.GetInstanceOf("CourseHome");
            int ID = 1;
            List<String> allTableSigniture = new List<string>();
            List<String> allShift = new List<string>();
            foreach (var testSummary in AllTableSummary) {
                st.SetAttribute("IDNum", ID++);
                st.SetAttribute("Title", testSummary.title);
                st.SetAttribute("Content", testSummary.attributions + "</p>" + testSummary.methodInfo);
                allTableSigniture.Add("·" + testSummary.title);
                allShift.Add(testSummary.title);
                foreach (var subSummary in AllColumnSummary)
                {
                    if (subSummary.tableName != testSummary.tableName) continue;
                    st.SetAttribute("IDNum", ID++);
                    st.SetAttribute("Title", subSummary.title);
                    st.SetAttribute("Content", subSummary.attributions + "</p>" + subSummary.methodInfo);
                    allTableSigniture.Add("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + subSummary.title);
                    allShift.Add(subSummary.title);
                }
                
            }
            //allTableSigniture.Sort();
            //hyper index
            for (int i=0; i < allTableSigniture.Count; i++) 
            {
                st.SetAttribute("Items", allTableSigniture[i]);
                st.SetAttribute("Links", allShift[i]);
            }

            String result = st.ToString();

            StreamWriter writetext = new StreamWriter(path);
            writetext.WriteLine(result);
            writetext.Close();

        }

    }
}
