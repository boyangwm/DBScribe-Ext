using CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using CommandLine.Text;
using WM.UnitTestScribe.CallGraph;
using System.Collections;
using System.IO;
using System.Data;
using System.Data.SqlClient;
using MySql.Data.MySqlClient;
using WM.UnitTestScribe.ReportGenerator;
using WM.UnitTestScribe.Summary;

namespace WM.UnitTestScribe
{
    public class Program
    {
        public static readonly string LocalProj = @"C:\GitHub\testProgram\RiskIt";
        public static readonly string SrcmlLoc = @"C:\GitHub\tool\SrcML";
        public static string myConnectionString = "server=127.0.0.1;uid=root;" + "pwd=12345;database=riskit;";

        static void Main(string[] args)
        {

            args = new string[] { "extractor", "--loc", LocalProj, "--srcmlPath", SrcmlLoc };
            var options = new Options();
            string invokedVerb = null;
            object invokedVerbOptions = null;

            var dbsingle = new dataSchemer(myConnectionString);
            Console.WriteLine("ConnectData Success");
            Console.WriteLine();

            if (!CommandLine.Parser.Default.ParseArguments(args, options,
                (verb, verbOptions) =>
                {
                    invokedVerb = verb;
                    invokedVerbOptions = verbOptions;
                }))
            {
                Environment.Exit(CommandLine.Parser.DefaultExitCodeFail);
            }
            if (invokedVerb == "extractor")
            {
                var callGraphOp = (CallgraphOptions)invokedVerbOptions;
                var generator = new ExtractMethodSQL(callGraphOp.LocationsPath, callGraphOp.SrcMLPath);
                generator.run(dbsingle);
            }
            else
            {
                Console.WriteLine("Run command, try again!");

            }
        }


        private class Options
        {
            [VerbOption("extractor", HelpText = "Analyze stereotype of methods in the project")]
            public CallgraphOptions StereotypeVerb { get; set; }


            [VerbOption("findAllTestCases", HelpText = "find all test cases in a project")]
            public TestCaseOptions FindAllTestCaseVerb { get; set; }

            [VerbOption("hello", HelpText = "Print hello for testing")]
            public HelloOptions HelloVerb { get; set; }

            [HelpVerbOption]
            public string GetUsage(string verb)
            {
                return HelpText.AutoBuild(this, verb);
            }
        }




        /// <summary>
        /// Stereotype detector
        /// </summary>
        private class CallgraphOptions
        {
            /// <summary> Subject application location </summary>
            [Option("loc", Required = true, HelpText = "The subject project folder")]
            public string LocationsPath { get; set; }

            [Option("srcmlPath", Required = true, HelpText = "The path to Srcml.exe")]
            public string SrcMLPath { get; set; }
        }



        /// <summary>
        /// Options for findAllTestCases
        /// </summary>
        private class TestCaseOptions
        {
            /// <summary> Subject application location </summary>
            [Option("loc", Required = true, HelpText = "The subject project folder")]
            public string LocationsPath { get; set; }

            [Option("srcmlPath", Required = true, HelpText = "The path to Srcml.exe")]
            public string SrcMLPath { get; set; }
        }


        /// <summary>
        /// print hello for testing 
        /// </summary>
        private class HelloOptions
        {
        }
    }

}
