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
using WM.UnitTestScribe.Summary;
using WM.UnitTestScribe.CallGraph;
using WM.UnitTestScribe.DatabaseInfo;
using WM.UnitTestScribe.sqlAnalyzer;

namespace WM.UnitTestScribe.MethodInfo{
    class ExtractMethodSQL {
        public string LocalProj;
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

        //This method is running after we generate the class and it contains two parts: 1. generating call graph of the target project; 2. calling GoThroughMethods to check taht does each method contain SQL local invocation or not.
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

        //Checking each method to figure out all SQL local invocations. The analysis contains: 1. finding all the variables that SQL invocation like variable with type "String" (The details about handle these variables are achieved in method "statIsDeclaration"); 2. finding all the string usage directly like "Select * from tableName (This is achieved by statIsOthers.)
        public void GoThroughMethods(IEnumerable<MethodDefinition> methods)
        {
            string methodNameForTest = "";
            int previoussql = 0;
            foreach (MethodDefinition m in methods)
            {
                List<string[]> variables = new List<string[]>();
                variables.Clear();
                var stats = m.GetDescendants<Statement>();

                foreach (var stat in stats) //checking each statement in the method
                {
                    if (stat.Content == null) continue;
                    if (stat is DeclarationStatement) 
                    { 
                        statIsDeclaration(m, stat, variables); 
                    }
                    statIsOthers(m, stat, variables); 
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
                        methodNameForTest += m.GetFullName() + "\r\n";
                        Console.WriteLine("============================");
                        previoussql = sqlCount;
                    }
                }
            }
        }

        //Finding all the variables that SQL invocation. At first we check the variable's type, if it could not save string like type "int" then we do not check it. Then we figure out all the statement that are related to this variable by method "FindRelated" and then rebuild this variable based on these statements by calling "Rebuldstring". Finally for each variable that could save string, we would know it's final status.
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

        //For all the other statement, we directly check all the expression in the statment to see is it a sql invocation or not. One special case is that the expression is a "MethodCall". In this case, the variable passed into the method also could be a sql invocation and we handle this case by method "handleFunctionCall".
        public void statIsOthers(MethodDefinition m, Statement stat, List<string[]> variables)
        {
            if (stat.Content == null) return;
            IEnumerable<Expression> exps = stat.Content.GetDescendants();

            foreach (var exp in exps)
            {
                if (exp is MethodCall)
                {
                    handleFunctionCall(exp, variables);
                }
                if (exp.GetDescendants().Count() > 0) continue;
                if ((exp is LiteralUse) == false) continue;
                var testString=TakeQuotOff(exp.ToString());
                if (testString!="") UpdateVariable("pureString", variables, testString);
            }

            //handle the assignment statement
            if (exps.Count<Expression>() <= 1) return;
            var firstexp = exps.First<Expression>();
            if (!(firstexp is NameUse)) return;
            if (!(exps.ElementAt(1) is OperatorUse)) return;
            OperatorUse opt = (OperatorUse)exps.ElementAt(1);
            string targetstring = Rebuildstring(firstexp.ToString(), stat, variables);
            if (targetstring != "") UpdateVariable("pureString", variables, targetstring);
        }

        //When we know an expression is a "MethodCall", we check it's arguments and handle the case like argument is m.call(string1 + string2). String1 plus string2 might be a invocation.
        public string handleFunctionCall(Expression exp, List<string[]> variables)
        {
            string returnResult = "FunctionCall";
            MethodCall mc = (MethodCall)exp;
            if (mc.Arguments.Count == 0) return returnResult;
            var arg = mc.Arguments[0];
            if (arg.Components.Count == 0) return returnResult;
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
                    i = i + 1; 
                    callPara += GetExpCont(exps.ElementAt(i), variables); 
                    continue;
                }
            }
            UpdateVariable("argument", variables, callPara);
            return returnResult;
        }

        //This would return the class desMethod of the method "m". The details of class desMethod is written below this class.
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

        //When the list "methodsInfo" does not contain the information of a method "m", we could generate a new "desMethod" class for "m" and save the new class into lists.
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

        //This method would check the variables for us. For some variables, it might be defined as: variable1 = variable2 + variable3. Then we need to check what is variable2 and variable3 to help us rebuild variable1.
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

        //If list "variables" contian the variable of "Name" then we would return it's value.
        public string GetVariableCont(string Name, List<string[]> variables)
        {
            foreach (var vari in variables)
            {
                if (vari[0] == Name) return vari[1];
            }
            UpdateVariable(Name, variables, "");
            return "";
        }

        //This would update the list "variables" with variable "Nmae" whose content is "cont".
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

        //This method is used to find all the statement related the the statement "declcont".
        public void FindRelated(MethodDefinition m, VariableDeclaration declcont, List<string[]> variables)
        {
            foreach (var stat in m.GetDescendants<Statement>())
            {
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

        //This method is remove the quote of a string. For some cases, if the string is wrapped by the quote then our sql analyzer could not find it.
        public string TakeQuotOff(string ori)
        {
            string result = "";
            if (ori.First() == '"' && ori.Last() == '"' && ori.Length > 1) result = ori.Substring(1, ori.Length - 2);
            else result = ori;
            return result;
        }

        //This method is used to return the value of an expression. For example, a "MethodCall" expression would return "FunctionCall" for rebuild a string.
        public string GetExpCont(Expression exp, List<string[]> variables)
        {
            string teststring = "";

            if (exp is MethodCall) return "FunctionCall";

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

        //This is used for rebuilding a complete string for the variable with name "varname".
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
