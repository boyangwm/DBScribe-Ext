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
using Antlr3.ST;

namespace WM.UnitTestScribe.CallGraph {
    class ExtractMethodSQL {
        /// <summary> Subject application location </summary>
        public string LocalProj;

        /// <summary> SrcML directory location </summary>
        public string SrcmlLoc;
        public HashSet<SingleSummary> AllTableSummary;
        public HashSet<SingleSummary> AllColumnSummary;
        public List<dbTable> tablesInfo;
        public List<desMethod> methodsInfo;
        public List<string> directMethods;
        dataSchemer db;

        public ExtractMethodSQL(string localProj, string srcmlloc)
        {
            this.LocalProj = localProj;
            this.SrcmlLoc = srcmlloc;
            this.AllTableSummary = new HashSet<SingleSummary>();
            this.AllColumnSummary = new HashSet<SingleSummary>();
            this.tablesInfo = new List<dbTable>();
            this.methodsInfo = new List<desMethod>();
            this.directMethods = new List<string>();
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
            if (Name=="pureString" || Name=="argument")
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

        public string TakePointOff(string ori)
        {
            string result = "";
            result = ori.Replace(".", "");
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
            }


            if (exp is VariableUse)
            {
                teststring = GetVariableCont(exp.ToString(), variables);
                if (teststring == "") teststring = exp.ToString();
                return teststring;
            }

            return "expression";
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
                    int previoussql = 0;
                    
                    /* Code for checking unknown case
                  */int vno = 0;
                    int i = 0;

                    List<string> forTestMethods = new List<string>(); //unsolved for UMAS
                    //forTestMethods.Add("calculateResult"); 
                    forTestMethods.Add("getValues");
                    forTestMethods.Add("FindTopIndustryCode");

                    foreach (MethodDefinition m in methods) {
                        vno = -1; i = 0;
                        desMethod methodDes;
                        foreach (var vari in forTestMethods)
                        {
                            if (vari == m.Name) { vno = i;}                          
                            i++;
                        }
                        if (vno < 0) continue;
                        i = 0;
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
                                    foreach (var decld in decls)
                                    {
                                        if (decld.VariableType.ToString().Equals("String", StringComparison.OrdinalIgnoreCase) || decld.VariableType.ToString().Equals("StringBuilder", StringComparison.OrdinalIgnoreCase) || decld.VariableType.ToString().Equals("StringBuffer", StringComparison.OrdinalIgnoreCase))
                                        {
                                            targetstring = "";
                                            targetstring = Rebuildstring(decld.Name.ToString(), stat, variables);
                                            UpdateVariable(decld.Name, variables, targetstring);
                                            FindRelated(m, decld, variables);
                                        }
                                    }
                                }
                                else
                                {
                                    foreach (var exp in stat.Content.GetDescendantsAndSelf())
                                    {
                                        if (exp is MethodCall)
                                        {
                                            MethodCall mc = (MethodCall)exp;
                                            if (mc.Arguments.Count == 0) continue;
                                            var arg = mc.Arguments[0];
                                            if (arg.Components.Count == 0) continue;
                                            var exps = arg.Components;
                                            string callPara="";
                                            for (i = 0; i < exps.Count; i++)
                                            {
                                                var texp = exps.ElementAt(i);
                                                if (texp is LiteralUse || texp is VariableUse)
                                                {
                                                    callPara += GetExpCont(texp, variables);
                                                    continue;
                                                }
                                                if ((texp is OperatorUse) && texp.ToString() == "+" && i + 1 < exps.Count)
                                                {
                                                    i = i + 1; callPara += GetExpCont(exps.ElementAt(i), variables); continue;
                                                }
                                            }
                                            UpdateVariable("argument", variables, callPara);
                                            
                                        }
                                        if (exp.GetDescendants().Count() > 0) continue;
                                        if ((exp is LiteralUse) == false) continue;
                                        UpdateVariable("pureString",variables, TakeQuotOff(exp.ToString()));
                                        
                                    }
                                }
                            }                        
                        }
                        if (variables.Count > 0)
                        {
                            foreach (var list in variables)
                            {
                                if (list[1]!="")
                                {
                                    sqlStmtParser p1 = new sqlStmtParser(list[1]);
                                    if (p1.isStmt == true)
                                    {
                                        Console.WriteLine(list[0] + " --> " + list[1]);
                                        Console.WriteLine("Yes it is! TYPE: " + p1.stmtType);
                                        Console.WriteLine();
                                        sqlCount++;
                                        methodDes = getMethodInfo(m,cgm);
                                        updateConnection(p1,methodDes,cgm,p1.stmtType.ToString());
                                    }
                                    else {}
                                }
                            }
                            if (sqlCount > previoussql)
                            {
                                var declaringClass = m.GetAncestors<TypeDefinition>().FirstOrDefault();
                                var className = "";
                                if (declaringClass != null)
                                {
                                    className = declaringClass.Name;
                                }
                                Console.WriteLine(m.GetFullName());
                                Console.WriteLine("============================");
                                Console.WriteLine();
                                directMethods.Add(m.GetFullName());
                                previoussql = sqlCount;
                            }
                        }
                    }


                    Console.WriteLine("Method Analyzing Finished! Total SQLs found: " + sqlCount+ ", total methods: " + directMethods.Count);
                    StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
                    StringTemplate stsum = group.GetInstanceOf("ProjectSummary");
                    int tableCount=0;
                    int totalCount=0;
                    foreach(var tempT in tablesInfo)
                    {
                        if (tempT.directMethods.Count>0) {tableCount++;totalCount++;}
                        foreach (var tempC in tempT.columns)
                        {
                            if (tempC.directMethods.Count>0) totalCount++;
                        }
                    }
                    stsum.SetAttribute("TableNumber",tableCount);
                    stsum.SetAttribute("AllNumber",totalCount);
                    stsum.SetAttribute("MethodNumber",directMethods.Count);
                    GenerateSummary(stsum.ToString());
                    Console.WriteLine("Report has been generated! Press any key to exit..");
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
            typeM = newMethodInfo(m,cgm);
            return typeM;
        }

        public desMethod newMethodInfo(MethodDefinition m, CGManager cgm)
        {
            desMethod typeM;
            SwumSummary swumSummary = new SwumSummary(m);
            swumSummary.BasicSummary();
            List<MethodDefinition> followers = new List<MethodDefinition>();
            List<MethodDefinition> finals = new List<MethodDefinition>();
            string desc = swumSummary.Describe();
            InvokeCallGraphGenerator tracer = new InvokeCallGraphGenerator(m, cgm);
            followers = tracer.traceToMethod();
            finals = tracer.traceToLastMethod();
            typeM = new desMethod(m, desc, followers, finals);
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
                    tempTable.columns.Add(new dbColumn(tableName, columnName));
                }
                tablesInfo.Add(tempTable);
            }
        }
        public void updateConnection(sqlStmtParser p1, desMethod m, CGManager cgm, string opt)
        { 
            List<string> idList = p1.getAllIds();
            if (idList== null) return;
            List<dbTable> tableIds = new List<dbTable>();
            List<dbColumn> columnIds = new List<dbColumn>();

            foreach (var id in idList)
            {
                if (id != null)
                {
                    if (tablesInfo.Find(x => x.name.Equals(id, StringComparison.OrdinalIgnoreCase)) != null)
                    {
                        var tempTable = tablesInfo.Find(x => x.name.Equals(id, StringComparison.OrdinalIgnoreCase));
                        tableIds.Add(tempTable);
                        foreach (var col in tempTable.columns)
                        {
                            if (idList.Find(x => x.Equals(col.name, StringComparison.OrdinalIgnoreCase)) != null) columnIds.Add(col);
                        }
                    }
                }
            }

            for (int i = 0; i < tablesInfo.Count; i++)
            {
                if (tableIds.Find(x => x == tablesInfo[i]) != null)
                {
                    tablesInfo[i].insertMethod(m, opt, "direct");
                    foreach (var mm in m.followmethods)
                    {
                        var tempMeDes = getMethodInfo(mm, cgm);
                        tablesInfo[i].insertMethod(tempMeDes, opt, "follow");
                    }
                    foreach (var mm in m.finalmethods)
                    {
                        var tempMeDes = getMethodInfo(mm, cgm);
                        tablesInfo[i].insertMethod(tempMeDes, opt, "final");
                    }
                }
            }
            if (columnIds.Count == 0) return;

            for (int i=0; i < tablesInfo.Count; i++)
            {
                for (int j=0; j<tablesInfo[i].columns.Count; j++)
                {
                    if (columnIds.Find(x => x == tablesInfo[i].columns[j]) != null)
                    {
                        tablesInfo[i].columns[j].insertMethod(m, opt, "direct");
                        foreach (var mm in m.followmethods)
                        {
                            var tempMeDes = getMethodInfo(mm, cgm);
                            tablesInfo[i].columns[j].insertMethod(tempMeDes, opt, "follow");
                        }
                        foreach (var mm in m.finalmethods)
                        {
                            var tempMeDes = getMethodInfo(mm, cgm);
                            tablesInfo[i].columns[j].insertMethod(tempMeDes, opt, "final");
                        }
                    }
                }
            }
        }
        public void GenerateSummary(string stsum)
        {
            Console.WriteLine("Now let's generate summary..");
            foreach(var table in this.tablesInfo)
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
            StringTemplateGroup group = new StringTemplateGroup("myGroup", @".\Templet");
            StringTemplate st = group.GetInstanceOf("GenerateScript");
            foreach(var me in methodsInfo)
            {
                st.SetAttribute("FunctionName",TakePointOff(me.name));
                st.SetAttribute("MethodFullName",me.name);
                st.SetAttribute("MethodName", me.methodself.Name);
            }
            FinalGenerator homePageGenerator = new FinalGenerator(this.AllTableSummary, this.AllColumnSummary,st.ToString(),stsum);
            homePageGenerator.Generate(@"c:\temp\test.html");
        }
    }
}
