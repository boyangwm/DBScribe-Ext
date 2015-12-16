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
        List<List<MethodDefinition>> paths;
        public InvokeCallGraphGenerator(MethodDefinition tempM, CGManager cgm)
        {
            this.m = tempM;
            this.paths = cgm.FindCalleeList(m);
        }

        public void traceFromMethod()
        {
            foreach (List<MethodDefinition> path in paths) {
                foreach (MethodDefinition mc in path) {
                    Console.Write("{0}--->", mc.Name);
                }
                Console.WriteLine("");
            }
        }

        public void traceToMethod()
        {
            int sum = 0;
            int pathNum = 0;
            foreach (List<MethodDefinition> path in paths)
            {
                sum += path.Count;
                pathNum++;
                foreach (MethodDefinition mc in path)
                {
                    Console.Write("{0}<---", mc.Name);
                }
                Console.WriteLine("");
            }
            Console.WriteLine("average level : " + (double)sum / pathNum);
        }

    }
}
