using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;


namespace WM.UnitTestScribe
{
    class desMethod
    {
        public string swumsummary;
        public List<MethodDefinition> followmethods;
        public string name;
        public desMethod(string mName, string sum, List<MethodDefinition> followers)
        {
            this.swumsummary = sum;
            this.name = mName;
            this.followmethods = followers;
        }
    }

}