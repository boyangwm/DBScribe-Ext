using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;
using Antlr3.ST;

namespace WM.UnitTestScribe.Summary {
    public class SingleSummary {

        public string title;
        public string attributions;
        public string methodInfo;
        public string tableName;
        public string tableIndex;

        public SingleSummary(string title, string description, string methodInfo, string table, string tIndex) {
            this.title = title;
            this.attributions=description;
            this.methodInfo = methodInfo;
            this.tableName = table;
            this.tableIndex = tIndex;
        }

    }


}
