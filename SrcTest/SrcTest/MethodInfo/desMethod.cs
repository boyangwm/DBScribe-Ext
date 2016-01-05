using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;
using Antlr3.ST;
using WM.UnitTestScribe.sqlAnalyzer;


namespace WM.UnitTestScribe.MethodInfo
{
    class desMethod
    {
        public string swumsummary;
        public List<MethodDefinition> followmethods;
        public List<MethodDefinition> finalmethods;
        public MethodDefinition methodself;
        public string name;
        public List<sqlStmtParser> sqlStmts;


        public desMethod(MethodDefinition m, string sum, List<MethodDefinition> followers, List<MethodDefinition> finals)
        {
            this.swumsummary = TakeSpaceOff(sum);
            this.methodself = m;
            this.name = m.GetFullName();
            this.followmethods = followers;
            this.finalmethods = finals;
            this.sqlStmts = new List<sqlStmtParser>();
        }

        public string translateStmt(string stmt, string TorC)
        {
            string fullSentence = "";
            if (TorC == "table")
            {
                switch (stmt)
                {
                    case "createTableStmt":
                        fullSentence = "This method would create this table. ";
                        break;
                    case "createIndexStmt":
                        fullSentence = "This method would create the index of this table. ";
                        break;
                    case "alterStmt":
                        fullSentence = "This method would modify the columns of this table. ";
                        break;
                    case "dropTableStmt":
                        fullSentence = "This method would delete tis table. ";
                        break;
                    case "dropIndexStmt":
                        fullSentence = "This method would delete the index of this table. ";
                        break;
                    case "selectStmt":
                        fullSentence = "This method would select some data from this table. ";
                        break;
                    case "insertStmt":
                        fullSentence = "This method would insert data into this table. ";
                        break;
                    case "updateStmt":
                        fullSentence = "This method would update this table. ";
                        break;
                    case "deleteStmt":
                        fullSentence = "This method would delete data of this table. ";
                        break;
                    default:
                        fullSentence = "This method aceess this function with an unkown statement. ";
                        break;
                }
            }
            else if (TorC == "column")
            {
                switch (stmt)
                {
                    case "createTableStmt":
                        break;
                    case "createIndexStmt":
                        fullSentence = "This method would create index of this column. ";
                        break;
                    case "alterStmt":
                        fullSentence = "This method would modify the attribute of this column. ";
                        break;
                    case "dropTableStmt":
                        break;
                    case "dropIndexStmt":
                        fullSentence = "This method would delete the index of this column. ";
                        break;
                    case "selectStmt":
                        fullSentence = "This method would select data which is related to this column. ";
                        break;
                    case "insertStmt":
                        fullSentence = "This method would insert data which is related to this column. ";
                        break;
                    case "updateStmt":
                        fullSentence = "This method would update data which is related to this column. ";
                        break;
                    case "deleteStmt":
                        fullSentence = "This method would delete data which is related to this column. ";
                        break;
                    default:
                        fullSentence = "This method aceess this function with an unkown statement. ";
                        break;
                }
            }
            return fullSentence;
        }
        public string getHtmlDescribe(List<string> allSql, string TorC)
        {
            string htmlText = "";
            StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
            StringTemplate st = group.GetInstanceOf("MethodDes");
            st.SetAttribute("FunctionName", TakePointOff(name));
            st.SetAttribute("MethodFullName", name);
            st.SetAttribute("MethodName", methodself.Name);
            st.SetAttribute("MethodSum", swumsummary);
            if (allSql != null)
            {
                foreach (var singleSql in allSql)
                {
                    var tempText = translateStmt(singleSql, TorC);
                    if (tempText == "") continue;
                    st.SetAttribute("SQLs", translateStmt(singleSql, TorC));
                }
            }
            htmlText = st.ToString();
            return htmlText;
        }

        public string TakePointOff(string ori)
        {
            string result = "";
            result = ori.Replace(".", "");
            return result;
        }

        public string TakeSpaceOff(string ori)
        {
            string result = "";
            int pos = 0;
            while (ori[ori.Length - pos - 1] == ' ')
            {
                pos++;
            }
            result = ori.Substring(0, ori.Length - pos);
            return result;
        }

    }

}