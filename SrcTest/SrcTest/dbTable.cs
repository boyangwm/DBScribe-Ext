using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Irony;
using Irony.Parsing;
using ABB.SrcML.Data;

namespace WM.UnitTestScribe {
    class dbTable
    {
        public List<dbColumn> columns;
        public List<string> directMethods;
        public List<string> followMehtods;
        public string name;
        public dbTable(string tbName)
        {
            this.columns = new List<dbColumn>();
            this.directMethods = new List<string>();
            this.followMehtods = new List<string>();
            this.name = tbName;
        }
    }
    
}
