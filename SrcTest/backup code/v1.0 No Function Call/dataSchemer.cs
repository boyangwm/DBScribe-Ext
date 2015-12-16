using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;
using System.Threading.Tasks;
using MySql.Data.MySqlClient;

namespace WM.UnitTestScribe
{
    class dataSchemer
    {
        public MySqlConnection conn;
        public List<string> tablesNames;

        public dataSchemer(string invokestring)
        {
            this.conn = new MySqlConnection(invokestring);
            this.tablesNames = getTableName();
        }

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
        public void showColumnInfo(string tableName)
        {
            try
            {
                conn.Open();
            }

            catch (MySql.Data.MySqlClient.MySqlException ex)
            {
                Console.WriteLine(ex.Message);
            }
            MySqlCommand cmd = conn.CreateCommand();
            cmd.CommandText = "SHOW COLUMNS FROM " + tableName + " FROM " + conn.Database + ";";
            MySqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                for (int i = 0; i < reader.FieldCount; i++)
                {
                    Console.WriteLine(reader.GetValue(i).ToString());
                }
            }
            reader.Close();
            conn.Close();
            return;
        }

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

        public void showOneColumn(string tableName, string columnName, string desireAttribute)
        {
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
                for (int i = 0; i < reader.FieldCount; i++)
                {
                    Console.Write(reader.GetValue(i).ToString()+ " ");
                }
                Console.WriteLine();
            }
            reader.Close();
            conn.Close();
            return;
        }

        //cmd.CommandText = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, CHARACTER_OCTET_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE AS SCALE, COLUMN_DEFAULT, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS;";
    }
}
