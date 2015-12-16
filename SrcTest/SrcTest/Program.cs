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
        /// <summary> Subject application location </summary>
        public static readonly string LocalProj = @"C:\SE-project\testcode\RiskIt"; 
        /// <summary> SrcML directory location </summary>
        public static readonly string SrcmlLoc = @"C:\SE-project\testcode\SrcML";
        public static string myConnectionString = "server=127.0.0.1;uid=root;" + "pwd=12345;database=riskit;";
        /*public static List<string> GetSqlFile(string varFileName)
        {
            List<string> alSql = new List<string>();
            if (!File.Exists(varFileName))
            {
                Console.WriteLine("NO DB!");
                return alSql;
            }
            StreamReader rs = new StreamReader(varFileName, System.Text.Encoding.Default);//注意编码
            string commandText = "";
            string varLine = "";
            while (rs.Peek() > -1)
            {
                varLine = rs.ReadLine();
                if (varLine == "")
                {
                    continue;
                }
                if (varLine.Last() == '-' || varLine.Last() == ';' || varLine.Last() == '"')
                {
                    commandText += varLine;
                    alSql.Add(commandText);
                    commandText = "";
                }
                else
                {
                    commandText += varLine;
                    //commandText = commandText.Replace("@database_name=N'dbhr'", string.Format("@database_name=N'{0}'", dbname));
                    //commandText += "\r\n";
                }
            }
            rs.Close();
            return alSql;
        }

        private static void DisplayData(System.Data.DataTable table)
        {
            foreach (System.Data.DataRow row in table.Rows)
            {
                foreach (System.Data.DataColumn col in table.Columns)
                {
                    Console.WriteLine("{0} = {1}", col.ColumnName, row[col]);
                }
                Console.WriteLine("============================");
            }
        }*/

        /// <summary>
        /// Command line testing
        /// </summary>
        /// <param name="args"></param>
        static void Main(string[] args)
        {

            args = new string[] { "extractor", "--loc", LocalProj, "--srcmlPath", SrcmlLoc };
            var options = new Options();
            string invokedVerb = null;
            object invokedVerbOptions = null;

            var dbsingle = new dataSchemer(myConnectionString);
            Console.WriteLine("ConnectData Success");
            Console.WriteLine();
            /*HashSet<TestCaseSummary> allS;
            List<AssertSTInfo> listassert;
            TestCaseSummary s1= new TestCaseSummary("abc",listassert,m);
            HomeGenerator hgTest = new HomeGenerator(allS);
            Console.ReadKey();*/
            SummaryGenerator smTest = new SummaryGenerator(LocalProj,SrcmlLoc);


            //database test
            /*var sqlfile = GetSqlFile(LocalProjDB);
            foreach (var query in sqlfile)
            {
                Console.WriteLine(query.ToString());
                Console.WriteLine();
                Console.ReadKey(true);
            }
            Console.WriteLine("DB READING FINISHED!");
            Console.ReadKey(true);*/

            /*string connectionString = GetConnectionString();
            using (SqlConnection connection = new SqlConnection(connectionString))
            {
                // Connect to the database then retrieve the schema information.
                connection.Open();
                DataTable table = connection.GetSchema("Tables");

                // Display the contents of the table.
                DisplayData(table);
                Console.WriteLine("Press any key to continue.");
                Console.ReadKey(true);
            }*/



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
            else if (invokedVerb == "hello")
            {
                Console.WriteLine("print hello");

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
