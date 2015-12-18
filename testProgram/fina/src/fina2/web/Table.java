/*
 * Table.java
 *
 * Created on 26 Декабрь 2003 г., 22:27
 */

package fina2.web;

/**
 *
 * @author  zuka
 */
    public class Table {
        public int start_row;
        public int start_col;
        public int end_row;
        public int end_col;
        public fina2.metadata.MDTNodePK nodePK;
        public int type;
        public java.util.Vector rows;
        
        public Table() {
        }
    }
