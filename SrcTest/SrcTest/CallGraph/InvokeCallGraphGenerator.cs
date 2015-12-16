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
        List<List<MethodDefinition>> pathFromM;
        List<List<MethodDefinition>> pathToM;
        public InvokeCallGraphGenerator(MethodDefinition tempM, CGManager cgm)
        {
            this.m = tempM;
            this.pathFromM = cgm.FindCalleeList(m);
            this.pathToM = cgm.FindCallerList(m);
        }

        public List<MethodDefinition> traceFromMethod()
        {
            List<MethodDefinition> relatedM = new List<MethodDefinition>();
            foreach (List<MethodDefinition> path in pathFromM) {
                foreach (MethodDefinition mc in path)
                {
                    if (mc.GetFullName() != m.GetFullName())
                    {
                        if (relatedM.Find(x => x.GetFullName() == mc.GetFullName()) == null) relatedM.Add(mc);
                    }
                }
            }
            return relatedM;
        }

        public List<MethodDefinition> traceToMethod()
        {
            List<MethodDefinition> relatedM = new List<MethodDefinition>();
            foreach (List<MethodDefinition> path in pathToM)
            {
                foreach (MethodDefinition mc in path)
                {
                    if (mc.GetFullName() != m.GetFullName())
                    {
                        if (relatedM.Find(x => x.GetFullName() == mc.GetFullName()) == null) relatedM.Add(mc);
                    }
                }
            }
            return relatedM;
        }

    }
}
