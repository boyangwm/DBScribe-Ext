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

        //For each SQL invocation, our "sqlStmtParser" only could return a simple typle like "selectStmt" and this method would translate the type into human languages.
        public string translateStmt(string stmt, string TorC, string optType)
        {
            string fullSentence = "";
            if (TorC == "table")
            {
                switch (stmt)
                {
                    case "createTableStmt":
                        fullSentence = "This method would <i><b>create</i></b> this table " + optType + ". ";
                        break;
                    case "createIndexStmt":
                        fullSentence = "This method would <i><b>create the index</i></b> of this table " + optType + ". ";
                        break;
                    case "alterStmt":
                        fullSentence = "This method would <i><b>modify the columns</i></b> of this table " + optType + ". ";
                        break;
                    case "dropTableStmt":
                        fullSentence = "This method would <i><b>delete</i></b> this table " + optType + ". ";
                        break;
                    case "dropIndexStmt":
                        fullSentence = "This method would <i><b>delete the index</i></b> of this table " + optType + ". ";
                        break;
                    case "selectStmt":
                        fullSentence = "This method would <i><b>select</i></b> some data from this table " + optType + ". ";
                        break;
                    case "insertStmt":
                        fullSentence = "This method would <i><b>insert</i></b> data into this table " + optType + ". ";
                        break;
                    case "updateStmt":
                        fullSentence = "This method would <i><b>update</i></b> this table " + optType + ". ";
                        break;
                    case "deleteStmt":
                        fullSentence = "This method would <i><b>delete</i></b> data of this table " + optType + ". ";
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
                        fullSentence = "This method would <i><b>create index</i></b> of this column " + optType + ". ";
                        break;
                    case "alterStmt":
                        fullSentence = "This method would <i><b>modify the attribute</i></b> of this column " + optType + ". ";
                        break;
                    case "dropTableStmt":
                        break;
                    case "dropIndexStmt":
                        fullSentence = "This method would <i><b>delete the index</i></b> of this column " + optType + ". ";
                        break;
                    case "selectStmt":
                        fullSentence = "This method would <i><b>select</i></b> data which is related to this column " + optType + ". ";
                        break;
                    case "insertStmt":
                        fullSentence = "This method would <i><b>insert</i></b> data which is related to this column " + optType + ". ";
                        break;
                    case "updateStmt":
                        fullSentence = "This method would <i><b>update</i></b> data which is related to this column " + optType + ". ";
                        break;
                    case "deleteStmt":
                        fullSentence = "This method would <i><b>delete</i></b> data which is related to this column " + optType + ". ";
                        break;
                    default:
                        fullSentence = "This method aceess this function with an unkown statement. ";
                        break;
                }
            }
            return fullSentence;
        }

        //This method would generate the description of this method and we could show the description in the final report.
        public string getHtmlDescribe(List<string> allSql, string TorC, string optType)
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
                    var tempText = translateStmt(singleSql, TorC, optType);
                    if (tempText == "") continue;
                    st.SetAttribute("SQLs", translateStmt(singleSql, TorC, optType));
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