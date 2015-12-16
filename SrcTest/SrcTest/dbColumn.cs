using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;


namespace WM.UnitTestScribe
{
    class dbColumn
    {
        public List<string> directMethods;
        public List<string> followMehtods;
        public string name;
        public dbColumn(string clName)
        {
            this.directMethods = new List<string>();
            this.followMehtods = new List<string>();
            this.name = clName;
        }
    }

}