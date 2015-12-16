using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;
using Antlr3.ST;

namespace WM.UnitTestScribe.Summary {
    public class ColumnSummary {

        public string title;
        public string attributions;
        public string methodInfo;


        public ColumnSummary(string title, string description, string methodInfo) {
            this.title = title;
            this.attributions=description;
            this.methodInfo = methodInfo;
        }

    }


}
