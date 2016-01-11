using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABB.SrcML.Data;

namespace WM.UnitTestScribe.CallGraph {
    class InvokeCallGraphGenerator {
        MethodDefinition m;
        List<List<MethodDefinition>> pathToM;
        public InvokeCallGraphGenerator(MethodDefinition tempM, CGManager cgm)
        {
            this.m = tempM;
            this.pathToM = cgm.FindCallerList(m);
        }

        public List<MethodDefinition> traceToMethod()
        {
            List<MethodDefinition> relatedM = new List<MethodDefinition>();
            foreach (List<MethodDefinition> path in pathToM)
            {
                if (path.Count == 1) continue;
                foreach (MethodDefinition mc in path)
                {
                    if ((mc.GetFullName() != m.GetFullName()) && (mc.GetFullName() != path.Last().GetFullName()))
                    {
                        if (relatedM.Find(x => x.GetFullName() == mc.GetFullName()) == null) relatedM.Add(mc);
                    }
                }
            }
            return relatedM;
        }

        public List<MethodDefinition> traceToLastMethod()
        {
            List<MethodDefinition> relatedM = new List<MethodDefinition>();
            foreach (List<MethodDefinition> path in pathToM)
            {
                if (path.Count>1) relatedM.Add(path.Last());
            }
            return relatedM;
        }
    }
}
