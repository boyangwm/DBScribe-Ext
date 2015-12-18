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
        public List<MethodDefinition> finalmethods;
        public MethodDefinition methodself;
        public string name;
        public desMethod(MethodDefinition m, string sum, List<MethodDefinition> followers, List<MethodDefinition> finals)
        {
            this.swumsummary = sum;
            this.methodself = m;
            this.name = m.GetFullName();
            this.followmethods = followers;
            this.finalmethods = finals;
        }
    }

}