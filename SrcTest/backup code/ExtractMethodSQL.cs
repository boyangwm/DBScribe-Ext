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

namespace WM.UnitTestScribe.CallGraph {
    class ExtractMethodSQL {
        /// <summary> Subject application location </summary>
        public string LocalProj;

        /// <summary> SrcML directory location </summary>
        public string SrcmlLoc;

        public ExtractMethodSQL(string localProj, string srcmlloc)
        {
            this.LocalProj = localProj;
            this.SrcmlLoc = srcmlloc;
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

        /*public void TsqlParseQuery(string unknowstring)
        {
            TSql110Parser parser = new TSql110Parser(true);
            TextReader rd = new StringReader(ReplaceQues(unknowstring));
            IList<ParseError> errors;
            TSqlFragment fragments = parser.Parse(rd, out errors);
            var stats = parser.ParseStatementList(rd, out errors);

            //TSqlStatement tsql = (TSqlStatement)fragments;
            //Microsoft.SqlServer.TransactSql.ScriptDom.SelectStatement selects= (SelectStatement)tsql;

            Console.WriteLine("Input: " + ReplaceQues(unknowstring));

            SQLVisitor myVisitor = new SQLVisitor();
            fragments.Accept(myVisitor);

            myVisitor.DumpStatistics(); 

            foreach (var token in fragments.ScriptTokenStream)
            {
                if (token.TokenType.ToString()!="WhiteSpace") Console.WriteLine(token.Text + " " + token.TokenType + " " + token.IsKeyword());
            }
            Console.ReadKey(true);
        }*/

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
            vno = CheckExist(Name, variables);
            if (vno > -1 /*&& variables[vno][1]==""*/) variables[vno][1] = cont;
            else variables.Add(new string[] { Name, cont });
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

            if (exp is VariableUse)
            {
                VariableUse expv = (VariableUse)exp;
                teststring = GetVariableCont(expv.Name,variables);
                //Console.Write(exp + "   v   ");
            }
            if (exp is LiteralUse)
            {
                LiteralUse expl = (LiteralUse)exp;
                teststring = TakeQuotOff(expl.Text);
                //Console.Write(exp + "   l   ");
            }
            else teststring = exp.ToString();
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
            }
            else
            {
                exps=targetstat.Content.GetDescendants();
                if (exps.ElementAt(0).ToString() != varname) return "wrong string";
            }

            for (var i = 0; i < exps.Count(); i++)
            {
                var texp = exps.ElementAt(i);
                if (texp is LiteralUse) 
                { 
                    teststring += GetExpCont(texp,variables); 
                    continue; 
                }
                if ((texp is OperatorUse) && texp.ToString() == "+" && i+1<exps.Count()) 
                {
                    i = i + 1; teststring += GetExpCont(exps.ElementAt(i),variables); continue;
                }
                
            }
            return teststring;
        }

        public void run(dataSchemer db) {
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

                    List<string> unSolvedSQLs = new List<string>(); // List of city names
                    unSolvedSQLs.Add("retreiveMatchingStudents");
                    unSolvedSQLs.Add("rePost");
                    unSolvedSQLs.Add("findTopStateByCode");
                    unSolvedSQLs.Add("browseUserProperties");/**/

                    foreach (MethodDefinition m in methods) {
                        //Console.WriteLine("Method Name : {0}", m.GetFullName());
                        /* Code for checking unknown case
                          */vno = -1; i = 0;
                        foreach (var vari in unSolvedSQLs)
                        {
                            if (vari == m.Name) vno = i;
                            i++;
                        }
                        if (vno < 0) continue;/**/
                        //if (m.Name != "createAgentTable") continue;
                        var stats = m.GetDescendants<Statement>();
                        variables.Clear();

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
                                        if (decld.VariableType.ToString() == "String" || decld.VariableType.ToString() == "StringBuilder" || decld.VariableType.ToString() == "StringBuffer")
                                        {
                                            targetstring = "";
                                            //Console.WriteLine("Statment: " + decld + " , Variable:  " + decld.Name);
                                            targetstring = Rebuildstring(decld.Name.ToString(), stat, variables);
                                            UpdateVariable(decld.Name, variables, targetstring);
                                            FindRelated(m, decld, variables);
                                            //Console.WriteLine("----");      
                                        }
                                    }
                                } 
                            }                        
                        }
                        if (variables.Count > 0)
                        {
                            Console.WriteLine("---Total List for method: " + m.Name + " ---");
                            foreach (var list in variables)
                            {
                                if (list[1]!="")
                                {
                                    Console.WriteLine(list[0] + " --> " + list[1]);
                                    sqlStmtParser p1 = new sqlStmtParser(list[1]);
                                    //sqlStmtParser p1 = new sqlStmtParser("INSERT INTO education VALUES (1)");
                                    if (p1.isStmt == true)
                                    {
                                        //Console.WriteLine(list[0] + " --> " + list[1]);
                                        Console.WriteLine("Yes it is! TYPE: " + p1.stmtType);
                                        sqlCount++;
                                        /* Extract Info of database
                                         * string tableId = "";
                                        foreach (var id in p1.getFromId())
                                        {
                                            if (id != null) Console.WriteLine("FROM: " + id);
                                            tableId = id; 
                                        }
                                        foreach (var col in p1.getWhereId())
                                        {
                                            if (col != null)
                                            {
                                                Console.Write("Where: " + col + ", ");
                                                db.showOneColumn(tableId, col, "COLUMN_TYPE");
                                            }
                                        }
                                        //Console.WriteLine("Total SQL Found: " + sqlCount);
                                        //Console.WriteLine();*/
                                    }
                                    else
                                    {
                                        Console.WriteLine("Woops :(");
                                    }
                                    Console.WriteLine();
                                }
                            }
                            if (sqlCount > previoussql)
                            {
                                functionCount++;
                                previoussql = sqlCount;
                            }
                            //SwumSummary sum = new SwumSummary(m);
                            //sum.BasicSummary();

                            //InvokeCallGraphGenerator tracer = new InvokeCallGraphGenerator(m, cgm);
                            
                            /*Console.WriteLine("Analyzing for Method " + m.GetFullName() +" Finished!");
                            Console.WriteLine("Method Info: " + sum.Describe());
                            Console.WriteLine("Trace this method from it: ");
                            tracer.traceFromMethod();
                            Console.WriteLine("Trace methods to it: ");
                            tracer.traceToMethod();*/
                            Console.WriteLine();
                        }
                    }


                    Console.WriteLine("Method Analyzing Finished! Total SQL found: " + sqlCount + ". Total Mehtods: " + functionCount);
                    Console.ReadKey(true);

                } finally {
                    project.WorkingSet.ReleaseReadLock();
                }

            }
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
