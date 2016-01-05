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
using WM.UnitTestScribe.DatabaseInfo;
using WM.UnitTestScribe.MethodInfo;
using WM.UnitTestScribe.sqlAnalyzer;

namespace WM.UnitTestScribe
{
    public class Program
    {
        public static readonly string LocalProj = @"C:\GitHub\testProgram\RiskIt";
        public static readonly string SrcmlLoc = @"C:\GitHub\tool\SrcML";
        public static string myConnectionString = "server=127.0.0.1;uid=root;" + "pwd=12345;database=riskit;";

        static void Main()
        {
            var theDB = new dataSchemer(myConnectionString);
            Console.WriteLine("Connecting database success!");
            Console.WriteLine();
            
            var extractor = new ExtractMethodSQL(LocalProj, SrcmlLoc, theDB);
            extractor.run();
            Console.WriteLine("Building callGraph and extracting SQL sequences success!");
            Console.WriteLine();

            var mapper = new dbMethodMapper(extractor, theDB);
            mapper.run();
            Console.WriteLine("Mapping database with methods success!");
            Console.WriteLine();

            var reporter = new infoCollector(extractor, theDB);
            reporter.run();
            Console.WriteLine("Report has been generated! Press any key to exit..");
            Console.ReadKey(true);
        }
    }

}
