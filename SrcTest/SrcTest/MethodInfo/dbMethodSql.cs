using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WM.UnitTestScribe.MethodInfo
{
    class dbMethodSql
    {
        public string methodName;
        public List<string> sqlSequence;

        public dbMethodSql(string name)
        {
            this.methodName = name;
            this.sqlSequence = new List<string>();
        }
    }
}
