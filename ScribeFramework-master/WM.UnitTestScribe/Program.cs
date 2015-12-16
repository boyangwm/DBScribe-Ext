using CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using CommandLine.Text;
using WM.UnitTestScribe.CallGraph;

namespace WM.UnitTestScribe {
    public class Program {

        /// <summary> Subject application location </summary>
        public static readonly string LocalProj = @"D:\Research\myTestSubjects\Callgraph\CallgraphSubject";

        /// <summary> SrcML directory location </summary>
        public static readonly string SrcmlLoc = @"D:\Research\SrcML";


        /// <summary>
        /// Command line testing
        /// </summary>
        /// <param name="args"></param>
        static void Main(string[] args) {

           //args = new string[] { "hello" };
            args = new string[] { "callgraph", "--loc", LocalProj, "--srcmlPath", SrcmlLoc }; 
            var options = new Options();
            string invokedVerb = null;
            object invokedVerbOptions = null;


            if (!CommandLine.Parser.Default.ParseArguments(args, options,
                (verb, verbOptions) => {
                    invokedVerb = verb;
                    invokedVerbOptions = verbOptions;
                })) {
                Environment.Exit(CommandLine.Parser.DefaultExitCodeFail);
            }
            if (invokedVerb == "callgraph") {
                var callGraphOp = (CallgraphOptions)invokedVerbOptions;
                var generator = new InvokeCallGraphGenerator(callGraphOp.LocationsPath, callGraphOp.SrcMLPath);
                generator.run();
            } else if (invokedVerb == "hello") {
                Console.WriteLine("print hello");

            }
        }


        private class Options {
            [VerbOption("callgraph", HelpText = "Analyze stereotype of methods in the project")]
            public CallgraphOptions StereotypeVerb { get; set; }


            [VerbOption("findAllTestCases", HelpText = "find all test cases in a project")]
            public TestCaseOptions FindAllTestCaseVerb { get; set; }

            [VerbOption("hello", HelpText = "Print hello for testing")]
            public HelloOptions HelloVerb { get; set; }

            [HelpVerbOption]
            public string GetUsage(string verb) {
                return HelpText.AutoBuild(this, verb);
            }
        }




        /// <summary>
        /// Stereotype detector
        /// </summary>
        private class CallgraphOptions {
            /// <summary> Subject application location </summary>
            [Option("loc", Required = true, HelpText = "The subject project folder")]
            public string LocationsPath { get; set; }

            [Option("srcmlPath", Required = true, HelpText = "The path to Srcml.exe")]
            public string SrcMLPath { get; set; }
        }



        /// <summary>
        /// Options for findAllTestCases
        /// </summary>
        private class TestCaseOptions {
            /// <summary> Subject application location </summary>
            [Option("loc", Required = true, HelpText = "The subject project folder")]
            public string LocationsPath { get; set; }

            [Option("srcmlPath", Required = true, HelpText = "The path to Srcml.exe")]
            public string SrcMLPath { get; set; }
        }


        /// <summary>
        /// print hello for testing 
        /// </summary>
        private class HelloOptions {
        }
    }

}
