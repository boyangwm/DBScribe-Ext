using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Irony;
using Irony.Parsing;


namespace WM.UnitTestScribe {
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
        public string ReplaceQues(string beforestring)
        {
            return beforestring.Replace("?", "1");
        }

        public List<string> CheckTree(ParseTreeNode node, string targetText)
        {
            List<string> ids = new List<string>();
            if (node == null) return ids;
            if (node.ChildNodes == null) return ids;
            foreach (ParseTreeNode child in node.ChildNodes)
            {
                if (node.Term.Name == targetText) 
                { 
                    ids.Add(node.ChildNodes.First().Token.Text);  
                }
                foreach (var subid in CheckTree(child, targetText))
                {
                    ids.Add(subid);
                }
            }
            return ids;
        }

        public List<string> getFromId()
        {
            List<string> ids = new List<string>();
            if (stmtPoints == null) return ids;
            var point = stmtPoints.Find((ParseTreeNode pfrom) => pfrom.Term.Name == "fromClauseOpt");
            return CheckTree(point, "Id");
        }

        public List<string> getWhereId()
        {
            List<string> ids = new List<string>();

            if (stmtPoints == null) return ids;
            var point = stmtPoints.Find((ParseTreeNode pfrom) => pfrom.Term.Name == "whereClauseOpt");
            return CheckTree(point, "Id");
        }
        //public getWhereId()
    }
    
}
