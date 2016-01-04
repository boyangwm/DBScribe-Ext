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

        /*public List<string> CheckTree(ParseTreeNode node, string targetText)
        {
            List<string> ids = new List<string>();
            if (node == null) return ids;
            if (node.ChildNodes == null) return ids;
            foreach (ParseTreeNode child in node.ChildNodes)
            {
                if (node.Term.Name == targetText) 
                { 
                    foreach (var target in node.ChildNodes)
                    {
                        if (target.Token != null) ids.Add(target.Token.Text);
                    } 
                }
                foreach (var subid in CheckTree(child, targetText))
                {
                    ids.Add(subid);
                }
            }
            return ids;
        }*/

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

        public List<string> AddList(List<string> mainList, List<string> addList)
        {
            foreach(var term in addList)
            {
                if (mainList.Find(x => x==term)==null) mainList.Add(term);
            }
            return mainList;
        }
        /*public List<string> getTableId()
        {
            List<string> ids = new List<string>();
            if (stmtPoints == null) return ids;
            var point = stmtPoints.Find((ParseTreeNode pfrom) => pfrom.Term.Name == "fromClauseOpt");
            if (point != null) return CheckTree(point, "Id");
            point = stmtPoints.Find((ParseTreeNode pfrom) => (pfrom.Term.Name == "FROM" || pfrom.Term.Name == "TABLE"));
            if (point != null || stmtType.Term.Name == "insertStmt" || stmtType.Term.Name == "updateStmt" || stmtType.Term.Name == "deleteStmt")
            {
                ids.Add(stmtPoints.Find((ParseTreeNode pfrom) => pfrom.Term.Name == "Id").ChildNodes.First().Token.Text);
            }
            return ids;
        }*/

        public List<string> getAllIds()
        {
            List<string> ids = new List<string>();
            if (stmtPoints == null) return ids;
            ids=CheckEntireTree(stmtType,"Id");
            return ids;
        }

        /*public List<string> getColumnId()
        {
            List<string> ids = new List<string>();
            List<string> ids2 = new List<string>();

            if (stmtPoints == null) return ids;
            var point = stmtPoints.Find((ParseTreeNode pfrom) => pfrom.Term.Name == "whereClauseOpt" || pfrom.Term.Name == "fieldDefList");
            ids=CheckTree(point, "Id");
            ids2 = CheckTree(point, "string");
            foreach(var tempId in ids2)
            {
                ids.Add(tempId);
            }
            return ids;
        }*/
    }
    
}
