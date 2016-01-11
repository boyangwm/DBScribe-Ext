using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;
using System.Threading.Tasks;
using MySql.Data.MySqlClient;

namespace WM.UnitTestScribe.DatabaseInfo
{
    class dataSchemer
    {
        public MySqlConnection conn;
        public List<string> tablesNames;
        public List<dbTable> tablesInfo;

        public dataSchemer(string invokestring)
        {
            this.conn = new MySqlConnection(invokestring);
            this.tablesNames = getTableName();
            this.tablesInfo = new List<dbTable>();
            buildTables();
        }

        //This method would build new dbTable classes for each table and new dbColumn classes for each column. After executing this method, the class dataSchemer would contain all the information about tables and columns.
        public void buildTables()
        {
            foreach (var tableName in getTableName())
            {
                dbTable tempTable = new dbTable(tableName);
                foreach (var columnName in getColumnName(tableName))
                {
                    tempTable.columns.Add(new dbColumn(tableName, columnName));
                }
                tablesInfo.Add(tempTable);
            }
        }

        //This method would return all tables' names in the target schema.
        public List<string> getTableName()
        {
            List<string> tableList=new List<string>();
            try
            {
                conn.Open();
            }

            catch (MySql.Data.MySqlClient.MySqlException ex)
            {
                Console.WriteLine(ex.Message);
                return tableList;
            }
            MySqlCommand cmd = conn.CreateCommand();
            cmd.CommandText = "SHOW TABLES;";
            MySqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                for (int i = 0; i < reader.FieldCount; i++)
                {
                    tableList.Add(reader.GetValue(i).ToString());
                }
            }
            reader.Close();
            conn.Close();
            return tableList;
        }

        //This method would return all columns' names in one table.
        public List<string> getColumnName(string tableName)
        {
            List<string> columnList = new List<string>();
            try
            {
                conn.Open();
            }

            catch (MySql.Data.MySqlClient.MySqlException ex)
            {
                Console.WriteLine(ex.Message);
                return columnList;
            }
            MySqlCommand cmd = conn.CreateCommand();
            cmd.CommandText = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tableName + "';";
            MySqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                for (int i = 0; i < reader.FieldCount; i++)
                {
                    columnList.Add(reader.GetValue(i).ToString());
                }
            }
            reader.Close();
            conn.Close();
            return columnList;
        }

        //This method could return one attribute of one column. "desireAttribute" means which attribute we want to know.. The grammer refers to the link https://dev.mysql.com/doc/refman/5.7/en/columns-table.html.
        public string GetOneColumnInfo(string tableName, string columnName, string desireAttribute)
        {
            string info="";
            try
            {
                conn.Open();
            }

            catch (MySql.Data.MySqlClient.MySqlException ex)
            {
                Console.WriteLine(ex.Message);
            }
            MySqlCommand cmd = conn.CreateCommand();
            cmd.CommandText = "SELECT TABLE_NAME,COLUMN_NAME,"+desireAttribute+ " FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tableName + "' AND column_name = '" + columnName +"';";
            MySqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                for (int i = 2; i < reader.FieldCount; i++)
                {
                    info+= reader.GetValue(i).ToString();
                    info += " ";
                }
            }
            reader.Close();
            conn.Close();
            return info;
        }

        //This method could return one attibute of one table. The grammer of desireAttribute refers to the link https://dev.mysql.com/doc/refman/5.7/en/tables-table.html.
        public string GetOneTableInfo(string tableName, string desireAttribute)
        {
            string info = "";
            try
            {
                conn.Open();
            }

            catch (MySql.Data.MySqlClient.MySqlException ex)
            {
                Console.WriteLine(ex.Message);
            }
            MySqlCommand cmd = conn.CreateCommand();
            cmd.CommandText = "SELECT TABLE_NAME," + desireAttribute + " FROM INFORMATION_SCHEMA.TABLES WHERE table_name = '" + tableName + "';";
            MySqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                for (int i = 1; i < reader.FieldCount; i++)
                {
                    info += reader.GetValue(i).ToString();
                    info += " ";
                }
            }
            reader.Close();
            conn.Close();
            return info;
        }
    }
}
