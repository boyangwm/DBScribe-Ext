using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Irony;
using Irony.Parsing;


namespace WM.UnitTestScribe.sqlAnalyzer{
    class sqlStmtParser
    {
        public string nowStmt;
        public ParseTree wholeTree;
        public Parser parser;
        public bool isStmt;
        public ParseTreeNode stmtType;
        public ParseTreeNodeList stmtPoints;

        public sqlStmtParser(string tempstmt)
        {
            this.nowStmt = ReplaceQues(tempstmt);

            this.parser = new Parser(new SqlGrammar());
            this.wholeTree = parser.Parse(nowStmt);
            //Some non-typical sql couldn't be parsed right now
            if (wholeTree.Root != null)
            {
                this.isStmt = true;
                this.stmtType = wholeTree.Root.ChildNodes.First();
                this.stmtPoints = stmtType.ChildNodes;
                
            }
        }

        //This method is used to replace the question mark in a statement. For some target project, their sql invocation looks like "insert (?,?,?) into tableName" but this is not acceptable for the grammar of Irony, so I replace these "?" by this method.
        public string ReplaceQues(string beforestring)
        {
            return beforestring.Replace("?", "1");
        }

        //Irony does not provide a method to check all the nodes in their parse tree so I wrote this method to check all the nodes.
        public List<string> CheckEntireTree(ParseTreeNode node, string targetText)
        {
            List<string> ids = new List<string>();
            if (node == null) return ids;
            if (node.ChildNodes == null) return ids;
            foreach (ParseTreeNode child in node.ChildNodes)
            {
                if (child.Term.Name == targetText)
                {
                    foreach (var target in child.ChildNodes)
                    {
                        if (target.Token != null) ids.Add(target.Token.Text);
                    }
                }
                else
                {
                    if (child.ChildNodes != null)
                    {
                        ids = AddList(ids, CheckEntireTree(child, targetText));
                    }
                }
            }
            return ids;
        }

        //When we get a new Id in getAllIds we need this method to add them into list.
        public List<string> AddList(List<string> mainList, List<string> addList)
        {
            foreach(var term in addList)
            {
                if (mainList.Find(x => x==term)==null) mainList.Add(term);
            }
            return mainList;
        }

        //Get all the parse node with mark "Id" in the parse tree. In Irony, all the table names and column names would be marked as "Id" so this method could get them together and we would seperate them in the class "dbMethodMapper".
        public List<string> getAllIds()
        {
            List<string> ids = new List<string>();
            if (stmtPoints == null) return ids;
            ids=CheckEntireTree(stmtType,"Id");
            return ids;
        }

    }
    
}
