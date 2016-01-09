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
        //Before running program, please guarantee your device has installed MySQL and you already run the database script of your target project.

        //Foreach project, we need change these three variables
        public static readonly string ProjLoc = @"\\vmware-host\Shared Folders\Documents\Visual Studio 2013\Projects\";
        public static readonly string testProj = @"xinco";
        public static readonly string databaseSchemaName = @"xinco";

        //These locations are just auto-generated, you don't need change them.
        public static string SrcmlLoc = ProjLoc + @"tool\SrcML";
        public static string testLoc = ProjLoc + @"testProgram\" + testProj;
        public static string reportLoc = ProjLoc + @"reports\report_of_" + testProj + ".html";
        public static string myConnectionString = "server=127.0.0.1;uid=root;" + "pwd=12345;database=" + databaseSchemaName + ";";

        static void Main()
        {
            /*This class would connect program with MySQL to:
             * 1:get target database schema's information
             * 2:save information into table classes and method classes we defined;*/
            var theDB = new dataSchemer(myConnectionString);
            Console.WriteLine("Connecting database success!");
            Console.WriteLine();

            /*This class would go through all the methods in your target project to:
             * 1:find out all the sql statements;
             * 2:build call graph;
             * 3:generate method descriptions for methods with local sql invocations.*/
            var extractor = new ExtractMethodSQL(testLoc, SrcmlLoc, theDB);
            extractor.run();
            Console.WriteLine("Building callGraph and extracting SQL sequences success!");
            Console.WriteLine();

            /*This class would:
             * 1:save methods information into tables class and column class;
             * 2:generate methods description for methods without local sql but still in call graph.*/
            var mapper = new dbMethodMapper(extractor, theDB);
            mapper.run();
            Console.WriteLine("Mapping database with methods success!");
            Console.WriteLine();

            //This class would generate reports according to all table classes and column classes.
            var reporter = new infoCollector(extractor, theDB,reportLoc);
            reporter.run();
            Console.WriteLine("Report has been generated! Press any key to exit..");
            Console.ReadKey(true);

            //PS: the details about each class would be described in the manual I submit.
        }
    }

}