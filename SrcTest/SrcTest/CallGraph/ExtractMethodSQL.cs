using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;
using System.Text.RegularExpressions;
using Irony;
using Irony.Parsing;
using Microsoft.SqlServer.TransactSql.ScriptDom;
using WM.UnitTestScribe.ReportGenerator;
using WM.UnitTestScribe.Summary;

namespace WM.UnitTestScribe.CallGraph {
    class ExtractMethodSQL {
        /// <summary> Subject application location </summary>
        public string LocalProj;

        /// <summary> SrcML directory location </summary>
        public string SrcmlLoc;
        public HashSet<ColumnSummary> AllTestSummary;
        public List<dbTable> tablesInfo;
        public List<desMethod> methodsInfo;
        dataSchemer db;

        public ExtractMethodSQL(string localProj, string srcmlloc)
        {
            this.LocalProj = localProj;
            this.SrcmlLoc = srcmlloc;
            AllTestSummary = new HashSet<ColumnSummary>();
            tablesInfo = new List<dbTable>();
            methodsInfo = new List<desMethod>();
        }

        public int CheckExist(string Name, List<string[]> variables)
        {
            int i = 0;
            foreach(var vari in variables)
            {
                if (vari[0]==Name) return i;
                i++;
            }
            return -1;
        }

        public string GetVariableCont(string Name, List<string[]> variables)
        {
            foreach (var vari in variables)
            {
                if (vari[0] == Name) return vari[1];
            }
            UpdateVariable(Name, variables, "");
            return "";
        }

        public void UpdateVariable(string Name, List<string[]> variables, string cont)
        {
            int vno = 0;
            if (Name=="pureString")
            {
                variables.Add(new string[] { Name, cont }); 
                return;
            }
            vno = CheckExist(Name, variables);
            if (vno > -1 /*&& variables[vno][1]==""*/) variables[vno][1] = cont;
            else 
            {
                variables.Add(new string[] { Name, cont });
            }
        }
        public void FindRelated(MethodDefinition m, VariableDeclaration declcont, List<string[]> variables)
        {
            foreach (var stat in m.GetDescendants<Statement>())
            {
                //m.GetAncestors<TypeDefinition>
                if (stat is DeclarationStatement || stat is SwitchStatement || stat is ABB.SrcML.Data.ThrowStatement || stat is ABB.SrcML.Data.ReturnStatement) continue;
                if (stat.Content==null) continue;
                IEnumerable<Expression> exps=stat.Content.GetDescendants();
                if (exps.Count<Expression>()<=1) continue;
                var firstexp = exps.First<Expression>();
                if ((firstexp.ToString() != declcont.Name) || !(firstexp is NameUse)) continue;
                if (!(exps.ElementAt(1) is OperatorUse)) continue;
                OperatorUse opt = (OperatorUse)exps.ElementAt(1);
                string targetstring = Rebuildstring(declcont.Name, stat, variables);
                if (opt.Text == "+=") {UpdateVariable(declcont.Name, variables, GetVariableCont(declcont.Name, variables)+targetstring); }
                if (opt.Text == "=") {UpdateVariable(declcont.Name, variables, targetstring);}
            }
        }
        
        public string TakeQuotOff(string ori)
        {
            string result = "";
            if (ori.First() == '"' && ori.Last() == '"' && ori.Length > 1) result = ori.Substring(1, ori.Length - 2);
            else result = ori;
            return result;
        }

        public string GetExpCont(Expression exp, List<string[]> variables)
        {
            string teststring = "";

            if (exp is LiteralUse)
            {
                LiteralUse expl = (LiteralUse)exp;
                teststring = TakeQuotOff(expl.Text);
                return teststring;
                //Console.Write(exp + "   l   ");
            }
            teststring = GetVariableCont(exp.ToString(), variables);
                //Console.Write(exp + "   v   ");
            if (teststring=="") teststring = exp.ToString();
            //Console.WriteLine();
            return teststring;
        }

        public string Rebuildstring(string varname, Statement targetstat, List<string[]> variables)
        {
            string teststring="";
            if (targetstat.Content == null) return teststring;
            IEnumerable<Expression> exps;
            if (targetstat is DeclarationStatement)
            {
                DeclarationStatement ds = (DeclarationStatement)targetstat;
                IEnumerable<VariableDeclaration> decls = ds.GetDeclarations();
                var decl = decls.ElementAt(0);
                if (decl.Name.ToString() != varname) return "wrong string";
                if (decl.Initializer == null) return teststring;
                exps = decl.Initializer.GetDescendantsAndSelf();
                for (var i = 0; i < exps.Count(); i++)
                {
                    var texp = exps.ElementAt(i);
                    if (texp is LiteralUse || texp is VariableUse)
                    {
                        teststring += GetExpCont(texp, variables);
                        continue;
                    }
                    if ((texp is OperatorUse) && texp.ToString() == "+" && i + 1 < exps.Count())
                    {
                        i = i + 1; teststring += GetExpCont(exps.ElementAt(i), variables); continue;
                    }

                }
            }
            else
            {
                exps=targetstat.Content.GetDescendants();
                if (exps.ElementAt(0).ToString() != varname) return "wrong string";
                var i = 0;
                for (i = 0; i < exps.Count(); i++)
                {
                    if (exps.ElementAt(i) is OperatorUse && (exps.ElementAt(i).ToString() == "=" || exps.ElementAt(i).ToString() == "+=")) break;
                }
                i++;
                for (; i < exps.Count(); i++)
                {
                    var texp = exps.ElementAt(i);
                    if ((texp is OperatorUse) && texp.ToString() == "+" && i + 1 < exps.Count())
                    {
                        i = i + 1; teststring += GetExpCont(exps.ElementAt(i), variables); continue;
                    }
                    else
                    {
                            teststring += GetExpCont(texp, variables);
                            continue;
                    }

                }
            }

            
            return teststring;
        }

        public void run(dataSchemer database) {
            this.db = database;
            buildTables();
            Console.Out.WriteLine("Invoke method sql parser");
            List<string[]> variables = new List<string[]>() { };

            string dataDir = @"TESTNAIVE_1.0";
            //string proPath = @"C:\Users\boyang.li@us.abb.com\Documents\RunningTest\Input\ConsoleApplication1";
            //string proPath = @"C:\Users\boyang.li@us.abb.com\Documents\RunningTest\Input\SrcML\ABB.SrcML";
            using (var project = new DataProject<CompleteWorkingSet>(dataDir, this.LocalProj, this.SrcmlLoc)) {

                Console.WriteLine("============================");
                string unknownLogPath = Path.Combine(project.StoragePath, "unknown.log");
                DateTime start = DateTime.Now, end;
                Console.WriteLine("============================");
                using (var unknownLog = new StreamWriter(unknownLogPath)) {
                   project.UnknownLog = unknownLog;
                   project.UpdateAsync().Wait();
                    
                }
                end = DateTime.Now;

                NamespaceDefinition globalNamespace;
                project.WorkingSet.TryObtainReadLock(5000, out globalNamespace);
                try {

                    // Step 1.   Build the call graph
                    Console.WriteLine("{0,10:N0} files", project.Data.GetFiles().Count());
                    Console.WriteLine("{0,10:N0} namespaces", globalNamespace.GetDescendants<NamespaceDefinition>().Count());
                    Console.WriteLine("{0,10:N0} types", globalNamespace.GetDescendants<TypeDefinition>().Count());
                    Console.WriteLine("{0,10:N0} methods", globalNamespace.GetDescendants<MethodDefinition>().Count());
                    
                    
                    var methods = globalNamespace.GetDescendants<MethodDefinition>();
                    CGManager cgm = new CGManager();
                    cgm.BuildCallGraph(methods);

                    int sqlCount = 0;
                    int functionCount = 0;
                    int previoussql = 0;
                    
                    /* Code for checking unknown case
                      */int vno = 0;
                    int i = 0;

                    List<string> unSolvedSQLs = new List<string>(); //unsolved for UMAS
                    unSolvedSQLs.Add("retreiveMatchingStudents");
                    unSolvedSQLs.Add("rePost");
                    unSolvedSQLs.Add("deleteUser");

                    foreach (MethodDefinition m in methods) {
                        //Console.WriteLine("Method Name : {0}", m.GetFullName());
                        /* Code for checking unknown case
                          */vno = 1; i = 0;
                            desMethod methodDes;
                        foreach (var vari in unSolvedSQLs)
                        {
                            if (vari == m.Name) { vno = i;}                          
                            i++;
                        }
                        if (vno < 0) continue;/**/
                        //if (m.Name != "createAgentTable") continue;
                        var stats = m.GetDescendants<Statement>();
                        variables.Clear();
                        //Console.WriteLine("Method: " + m.Name);
                        string targetstring;
                        foreach (var stat in stats)
                        {
                            if (stat.Content != null) // && string.Compare(stat.GetXmlName(),"DeclStmt")==0) || string.Compare(stat.GetXmlName(),"Statement")==0))
                            {
                                if (stat is DeclarationStatement){
                                    DeclarationStatement ds=(DeclarationStatement)stat;
                                    var cont = ds.Content;
                                    var decls = ds.GetDeclarations();
                                    //Console.WriteLine("Statment1: " + cont);
                                    foreach (var decld in decls)
                                    {
                                        if (decld.VariableType.ToString().Equals("String", StringComparison.OrdinalIgnoreCase) || decld.VariableType.ToString().Equals("StringBuilder", StringComparison.OrdinalIgnoreCase) || decld.VariableType.ToString().Equals("StringBuffer", StringComparison.OrdinalIgnoreCase)) ;
                                        {
                                            targetstring = "";
                                            //Console.WriteLine("Statment: " + decld + " , Variable:  " + decld.Name);
                                            targetstring = Rebuildstring(decld.Name.ToString(), stat, variables);
                                            UpdateVariable(decld.Name, variables, targetstring);
                                            FindRelated(m, decld, variables);
                                            /*foreach (var ss in variables)
                                            {
                                                Console.WriteLine("variable "+ss[0]+ ",   contont: "+ ss[1]);
                                            }
                                            Console.WriteLine("----");*/
                                        }
                                    }
                                }
                                else
                                {
                                    foreach (var exp in stat.Content.GetDescendantsAndSelf())
                                    {
                                        if (exp.GetDescendants().Count() > 0) continue;
                                            sqlStmtParser p1 = new sqlStmtParser(TakeQuotOff(exp.ToString()));
                                            if (p1.isStmt == true)
                                            {
                                                //sqlCount++;
                                                //updateConnection(p1,m);
                                                UpdateVariable("pureString",variables, TakeQuotOff(exp.ToString()));
                                                //Console.WriteLine("SQL --> " + expl.Text);
                                                //Console.WriteLine("Yes it is! TYPE: " + p1.stmtType);
                                            }
                                    }
                                }
                            }                        
                        }
                        if (variables.Count > 0)
                        {
                            //Console.WriteLine("---Total List for method: " + m.Name + " ---");
                            foreach (var list in variables)
                            {
                                if (list[1]!="")
                                {
                                    //Console.WriteLine(list[0] + " --> " + list[1]);
                                    sqlStmtParser p1 = new sqlStmtParser(list[1]);
                                    //sqlStmtParser p1 = new sqlStmtParser("INSERT INTO education VALUES (1)");
                                    if (p1.isStmt == true)
                                    {
                                        Console.WriteLine(list[0] + " --> " + list[1]);
                                        Console.WriteLine("Yes it is! TYPE: " + p1.stmtType);
                                        Console.WriteLine();
                                        sqlCount++;
                                        methodDes = getMethodInfo(m,cgm);
                                        updateConnection(p1,methodDes,cgm);
                                    }
                                    else
                                    {
                                       // Console.WriteLine("Woops :(");
                                    }
                                    //Console.WriteLine();
                                }
                            }
                            if (sqlCount > previoussql)
                            {
                                functionCount++;
                                var declaringClass = m.GetAncestors<TypeDefinition>().FirstOrDefault();
                                var nameSpaceName = GetNamespaceByMethod(m);
                                var className = "";
                                if (declaringClass != null)
                                {
                                    className = declaringClass.Name;
                                }
                                Console.WriteLine(nameSpaceName + "," + className + "," + m.Name);

                                previoussql = sqlCount;

                                //Console.WriteLine(nameSpaceName + "," + className + "," + m.Name + "Swum Description : " + desc);
                                //Console.ReadKey(true);


                            }
                            
                            //SwumSummary sum = new SwumSummary(m);
                            //sum.BasicSummary();

                            //InvokeCallGraphGenerator tracer = new InvokeCallGraphGenerator(m, cgm);
                            
                            /*Console.WriteLine("Analyzing for Method " + m.GetFullName() +" Finished!");
                            Console.WriteLine("Method Info: " + sum.Describe());
                            //Console.WriteLine("Trace this method from it: ");
                            //tracer.traceFromMethod();
                            Console.WriteLine("Trace methods to it: ");
                            tracer.traceToMethod();
                            Console.WriteLine();
                            Console.ReadKey();*/
                        }
                    }


                    Console.WriteLine("Method Analyzing Finished! Total SQL found: " + sqlCount + ". Total Mehtods: " + functionCount + " / " + methods.Count());
                    GenerateSummary();
                    Console.ReadKey(true);

                } finally {
                    project.WorkingSet.ReleaseReadLock();
                }

            }
        }

        public desMethod getMethodInfo(MethodDefinition m, CGManager cgm)
        {
            desMethod typeM;
            if (methodsInfo.Count > 0)
            {
                foreach (var method in methodsInfo)
                {
                    if (method.name == m.GetFullName()) return method;
                }
            }
            SwumSummary swumSummary = new SwumSummary(m);
            swumSummary.BasicSummary();
            List<MethodDefinition> followers = new List<MethodDefinition>();
            string desc = swumSummary.Describe();
            InvokeCallGraphGenerator tracer = new InvokeCallGraphGenerator(m, cgm);
            followers = tracer.traceToMethod();
            typeM=new desMethod(m.GetFullName(), desc, followers);
            methodsInfo.Add(typeM);
            return typeM;
        }
        public void buildTables()
        {
            foreach (var tableName in db.getTableName())
            {
                dbTable tempTable = new dbTable(tableName);
                foreach (var columnName in db.getColumnName(tableName))
                {
                    tempTable.columns.Add(new dbColumn(columnName));
                }
                tablesInfo.Add(tempTable);
            }
        }
        public void updateConnection(sqlStmtParser p1, desMethod m, CGManager cgm)
        {
            //if ((p1.stmtType.ToString() != "selectStmt") && (p1.stmtType.ToString() != "insertStmt") && (p1.stmtType.ToString() != "updateStmt") && (p1.stmtType.ToString() != "deleteStmt")) return;
            if (p1.getFromId() == null) return;
            List<string> idList= new List<string>();
            foreach (var id in p1.getFromId())
            {
                if (id != null)
                {
                    if (tablesInfo.Find(x => x.name.Equals(id, StringComparison.OrdinalIgnoreCase)) != null) idList.Add(id);
                }
            }

            if (p1.getWhereId().Count==0)
            {
                foreach (var id in idList)
                {
                    for (var i = 0; i < tablesInfo.Count; i++)
                    {
                        if (tablesInfo[i].name.Equals(id, StringComparison.OrdinalIgnoreCase))
                        {
                            string description = "Method: " + m.name + ", function: " + m.swumsummary + ";";
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
                            break;
                        }
                    }
                }
                return;
            }

            foreach (var col in p1.getWhereId())
            {
                if (col != null)
                {
                    foreach (var id in idList)
                    {
                        for (var i = 0; i < tablesInfo.Count; i++)
                        {
                            if (tablesInfo[i].name == id)
                            {
                                List<dbColumn> tempcol = tablesInfo[i].columns;
                                for (var j=0; j<tempcol.Count;j++)
                                {
                                    if (tempcol[j].name.Equals(col, StringComparison.OrdinalIgnoreCase))
                                    {
                                        string description = "Method: " + m.name + ", function: " + m.swumsummary + ";";
                                        if (tempcol[j].directMethods.Find(x => x == description) == null)
                                        {
                                            tempcol[j].directMethods.Add(description);
                                        }
                                        foreach (var mm in m.followmethods)
                                        {
                                            var mmdes = getMethodInfo(mm, cgm);
                                            string mmdescription = "Method: " + mmdes.name + ", function: " + mmdes.swumsummary + ";";
                                            if (tempcol[j].followMehtods.Find(x => x == mmdescription) == null)
                                            {
                                                tempcol[j].followMehtods.Add(mmdescription);
                                            }
                                        }
                                        break;
                                    }
                                }
                                tablesInfo[i].columns = tempcol;
                                break;
                            }
                        }
                    }                    
                }
            }
        }
        public void GenerateSummary()
        {
            Console.WriteLine("Now let's generate summary..");
            foreach(var table in this.tablesInfo)
            {
                if (table.directMethods.Count>0)
                {
                    string title = "Table: " + table.name;
                    string description = "The create time of table: " + db.GetOneTableInfo(table.name, "CREATE_TIME");
                    string methodInfo = "Directed related Methods:";
                    foreach (var m in table.directMethods)
                    {
                        methodInfo += "</p>" + m;
                    }
                    if (table.followMehtods.Count>0)
                    {
                        methodInfo += "</p> Tracing Methods:";
                        foreach (var m in table.followMehtods)
                        {
                            methodInfo += "</p>" + m;
                        }
                    }
                    ColumnSummary tcSummary = new ColumnSummary(title, description, methodInfo);
                    AllTestSummary.Add(tcSummary);
                }
                foreach(var column in table.columns)
                {
                    if (column.directMethods.Count == 0) continue;
                    string title = "Table: " + table.name + " , column: " + column.name;
                    string description = "The type is: " + db.GetOneColumnInfo(table.name, column.name, "COLUMN_TYPE");
                    string methodInfo = "Directed related Methods:";
                    foreach (var m in column.directMethods)
                    {
                        methodInfo += "</p>" + m;
                    }
                    if (column.followMehtods.Count > 0)
                    {
                        methodInfo += "</p> Tracing Methods:";
                        foreach (var m in column.followMehtods)
                        {
                            methodInfo += "</p>" + m;
                        }
                    }
                    ColumnSummary tcSummary = new ColumnSummary(title, description, methodInfo);
                    AllTestSummary.Add(tcSummary);
                }
            }
            FinalGenerator homePageGenerator = new FinalGenerator(this.AllTestSummary);
            homePageGenerator.Generate(@"c:\temp\test.html");
        }
        private string GetNamespaceByMethod(MethodDefinition md)
        {
            var allNameSpace = md.GetAncestors<NamespaceDefinition>();
            string nameSpace = "";
            foreach (var ns in allNameSpace)
            {
                if (ns.Name == "")
                {
                    continue;
                }
                if (nameSpace == "")
                {
                    nameSpace += ns.Name;
                }
                else
                {
                    nameSpace = ns.Name + "." + nameSpace;
                }

            }
            return nameSpace;
        }
    }

    /*internal class SQLVisitor : TSqlFragmentVisitor
    {
        private int SELECTcount = 0;
        private int INSERTcount = 0;
        private int UPDATEcount = 0;
        private int DELETEcount = 0;

        private string GetNodeTokenText(TSqlFragment fragment)
        {
            StringBuilder tokenText = new StringBuilder();
            for (int counter = fragment.FirstTokenIndex; counter <= fragment.LastTokenIndex; counter++)
            {
                tokenText.Append(fragment.ScriptTokenStream[counter].Text);
            }

            return tokenText.ToString();
        }

        // SELECTs 
        public override void ExplicitVisit(SelectStatement node)
        {
            //Console.WriteLine("found SELECT statement with text: " + GetNodeTokenText(node)); 
            SELECTcount++;
        }

        // INSERTs 
        public override void ExplicitVisit(InsertStatement node)
        {
            INSERTcount++;
        }

        // UPDATEs 
        public override void ExplicitVisit(UpdateStatement node)
        {
            UPDATEcount++;
        }

        // DELETEs 
        public override void ExplicitVisit(DeleteStatement node)
        {
            DELETEcount++;
        }

        public void DumpStatistics()
        {
            Console.WriteLine(string.Format("Found {0} SELECTs, {1} INSERTs, {2} UPDATEs & {3} DELETEs",
                this.SELECTcount,
                this.INSERTcount,
                this.UPDATEcount,
                this.DELETEcount));
        }
    }*/ 
}
