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
using WM.UnitTestScribe.Summary;
using WM.UnitTestScribe.CallGraph;
using WM.UnitTestScribe.DatabaseInfo;
using WM.UnitTestScribe.sqlAnalyzer;

namespace WM.UnitTestScribe.MethodInfo{
    class ExtractMethodSQL {
        /// <summary> Subject application location </summary>
        public string LocalProj;
        /// <summary> SrcML directory location </summary>
        public string SrcmlLoc;
        public List<desMethod> methodsInfo;
        public List<desMethod> allDirectMethods;
        public int sqlCount;
        public CGManager cgm;
        dataSchemer db;

        public ExtractMethodSQL(string localProj, string srcmlloc, dataSchemer databases)
        {
            this.LocalProj = localProj;
            this.SrcmlLoc = srcmlloc;
            this.methodsInfo = new List<desMethod>();
            this.db = databases;
            this.allDirectMethods = new List<desMethod>();
            this.sqlCount = 0;
        }
        public void run() {
            Console.WriteLine("Invoke method sql extractor");
            string dataDir = @"TESTNAIVE_1.0";
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
                    this.cgm = new CGManager();
                    cgm.BuildCallGraph(methods);
                    
                    GoThroughMethods(methods);

                    Console.WriteLine("Method Analyzing Finished! Total SQLs found: " + sqlCount+ ", total methods: " + allDirectMethods.Count);
                } finally {
                    project.WorkingSet.ReleaseReadLock();
                }

            }
        }

        public bool CompareTest(string mname)
        {
            List<string> forTestMethods = new List<string>(); //define a list for test
            forTestMethods.Add("getValues");
            forTestMethods.Add("FindTopIndustryCode");
            foreach (var vari in forTestMethods)
            {
                if (vari == mname) { return true; }
            }
            return false;
        }
        public void GoThroughMethods(IEnumerable<MethodDefinition> methods)
        {
            int previoussql = 0;
            /* Code for checking unknown case*/
            foreach (MethodDefinition m in methods)
            {
                //if (CompareTest(m.Name) == false) continue; //only check some methods to speed up the program for testing

                List<string[]> variables = new List<string[]>();
                variables.Clear();
                var stats = m.GetDescendants<Statement>();

                foreach (var stat in stats) //checking each statement in the method
                {
                    if (stat.Content == null) continue;
                    if (stat is DeclarationStatement) { statIsDeclaration(m, stat, variables); }
                    else { statIsOthers(m, stat, variables); }
                }   
                if (variables.Count > 0) //means this method might contain sql string
                {
                    foreach (var list in variables)
                    {
                        if (list[1] != "")
                        {
                            sqlStmtParser p1 = new sqlStmtParser(list[1]);
                            if (p1.isStmt == true)
                            {
                                Console.WriteLine(list[0] + " --> " + list[1]);
                                Console.WriteLine("Yes it is! TYPE: " + p1.stmtType);
                                sqlCount++;
                                var methodDes = getMethodInfo(m);
                                methodDes.sqlStmts.Add(p1);
                            }
                        }
                    }
                    if (sqlCount > previoussql) //check is there any new sql in this method
                    {
                        allDirectMethods.Add(getMethodInfo(m));
                        Console.WriteLine(m.GetFullName());
                        Console.WriteLine("============================");
                        previoussql = sqlCount;
                    }
                }
            }
        }

        public void statIsDeclaration(MethodDefinition m, Statement stat, List<string[]> variables)
        {
            DeclarationStatement ds = (DeclarationStatement)stat;
            var cont = ds.Content;
            var decls = ds.GetDeclarations();
            foreach (var decld in decls)
            {
                if (decld.VariableType.ToString().Equals("String", StringComparison.OrdinalIgnoreCase) || decld.VariableType.ToString().Equals("StringBuilder", StringComparison.OrdinalIgnoreCase) || decld.VariableType.ToString().Equals("StringBuffer", StringComparison.OrdinalIgnoreCase))
                {
                    string targetstring = "";
                    targetstring = Rebuildstring(decld.Name.ToString(), stat, variables);
                    UpdateVariable(decld.Name, variables, targetstring);
                    FindRelated(m, decld, variables);
                }
            }
        }

        public void statIsOthers(MethodDefinition m, Statement stat, List<string[]> variables)
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
                    string callPara = "";
                    for (int i = 0; i < exps.Count; i++)
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
                UpdateVariable("pureString", variables, TakeQuotOff(exp.ToString()));
            }
        }

        public desMethod getMethodInfo(MethodDefinition m)
        {
            desMethod typeM;
            if (methodsInfo.Count > 0)
            {
                foreach (var method in methodsInfo)
                {
                    if (method.name == m.GetFullName()) return method;
                }
            }
            typeM = newMethodInfo(m);
            return typeM;
        }

        public desMethod newMethodInfo(MethodDefinition m)
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

        public int CheckVariableExist(string Name, List<string[]> variables)
        {
            int i = 0;
            foreach (var vari in variables)
            {
                if (vari[0] == Name) return i;
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
            if (Name == "pureString" || Name == "argument")
            {
                variables.Add(new string[] { Name, cont });
                return;
            }
            vno = CheckVariableExist(Name, variables);
            if (vno > -1) variables[vno][1] = cont;
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
                if (stat.Content == null) continue;
                IEnumerable<Expression> exps = stat.Content.GetDescendants();
                if (exps.Count<Expression>() <= 1) continue;
                var firstexp = exps.First<Expression>();
                if ((firstexp.ToString() != declcont.Name) || !(firstexp is NameUse)) continue;
                if (!(exps.ElementAt(1) is OperatorUse)) continue;
                OperatorUse opt = (OperatorUse)exps.ElementAt(1);
                string targetstring = Rebuildstring(declcont.Name, stat, variables);
                if (opt.Text == "+=") { UpdateVariable(declcont.Name, variables, GetVariableCont(declcont.Name, variables) + targetstring); }
                if (opt.Text == "=") { UpdateVariable(declcont.Name, variables, targetstring); }
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
            }

            if (exp is VariableUse)
            {
                teststring = GetVariableCont(exp.ToString(), variables);
                if (teststring == "") teststring = exp.ToString();
                return teststring;
            }

            return "codeexpression";
        }

        public string Rebuildstring(string varname, Statement targetstat, List<string[]> variables)
        {
            string teststring = "";
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
                exps = targetstat.Content.GetDescendants();
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
    }
}
