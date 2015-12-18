/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     12/24/2008 11:36:48 AM                       */
/*==============================================================*/


/*==============================================================*/
/* Table: IN_BANKS                                              */
/*==============================================================*/
CREATE TABLE IN_BANKS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   TYPEID                         NUMERIC(10,0),
   SHORTNAMESTRID                 NUMERIC(10,0),
   NAMESTRID                      NUMERIC(10,0),
   ADDRESSSTRID                   NUMERIC(10,0),
   PHONE                          VARCHAR(40),
   FAX                            VARCHAR(40),
   EMAIL                          VARCHAR(40),
   TELEX                          VARCHAR(40),
   SWIFTCODE                      VARCHAR(11),
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Index: BANKS_CODE                                            */
/*==============================================================*/
CREATE UNIQUE INDEX BANKS_CODE ON IN_BANKS
(
   CODE
);

/*==============================================================*/
/* Index: BANKS_TYPEID                                          */
/*==============================================================*/
CREATE INDEX BANKS_TYPEID ON IN_BANKS
(
   TYPEID
);

/*==============================================================*/
/* Table: IN_BANK_BRANCHES                                      */
/*==============================================================*/
CREATE TABLE IN_BANK_BRANCHES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   BANKREGIONSTRID                NUMERIC(10,0),
   NAMESTRID                      NUMERIC(10,0),
   SHORTNAMESTRID                 NUMERIC(10,0),
   ADDRESSSTRID                   NUMERIC(10,0),
   COMMENTSSTRID                  VARCHAR(40),
   CREATIONDATE                   DATE,
   DATEOFCHANGE                   DATE,
   BANKID                         NUMERIC(10,0),
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: IN_BANK_GROUPS                                        */
/*==============================================================*/
CREATE TABLE IN_BANK_GROUPS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   NAMESTRID                      NUMERIC(10,0),
   CRITERIONID                    NUMERIC(10,0),
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Index: BANKGROUPS_CODE                                       */
/*==============================================================*/
CREATE UNIQUE INDEX BANKGROUPS_CODE ON IN_BANK_GROUPS
(
   CODE
);

/*==============================================================*/
/* Table: IN_BANK_MANAGEMENT                                    */
/*==============================================================*/
CREATE TABLE IN_BANK_MANAGEMENT
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   NAMESTRID                      NUMERIC(10,0),
   LASTNAMESTRID                  NUMERIC(10,0),
   MANAGINGBODYID                 NUMERIC(10,0),
   POSTSTRID                      NUMERIC(10,0),
   PHONE                          VARCHAR(25),
   DATEOFAPPOINTMENT              DATE,
   CANCELDATE                     DATE,
   REGISTRATIONSTRID1             NUMERIC(10,0),
   REGISTRATIONSTRID2             NUMERIC(10,0),
   REGISTRATIONSTRID3             NUMERIC(10,0),
   COMMENTSSTRID1                 NUMERIC(10,0),
   COMMENTSSTRID2                 NUMERIC(10,0),
   BANKID                         NUMERIC(10,0),
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: IN_BANK_TYPES                                         */
/*==============================================================*/
CREATE TABLE IN_BANK_TYPES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   NAMESTRID                      NUMERIC(10,0),
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Index: BANKTYPES_CODE                                        */
/*==============================================================*/
CREATE UNIQUE INDEX BANKTYPES_CODE ON IN_BANK_TYPES
(
   CODE
);

/*==============================================================*/
/* Index: BANKTYPES_NAME                                        */
/*==============================================================*/
CREATE UNIQUE INDEX BANKTYPES_NAME ON IN_BANK_TYPES
(
   NAMESTRID
);

/*==============================================================*/
/* Table: IN_BRANCH_MANAGEMENT                                  */
/*==============================================================*/
CREATE TABLE IN_BRANCH_MANAGEMENT
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   NAMESTRID                      NUMERIC(10,0),
   LASTNAMESTRID                  NUMERIC(10,0),
   MANAGINGBODYID                 NUMERIC(10,0),
   POSTSTRID                      NUMERIC(10,0),
   PHONE                          VARCHAR(25),
   DATEOFAPPOINTMENT              DATE,
   CANCELDATE                     DATE,
   REGISTRATIONSTRID1             NUMERIC(10,0),
   REGISTRATIONSTRID2             NUMERIC(10,0),
   REGISTRATIONSTRID3             NUMERIC(10,0),
   COMMENTSSTRID1                 NUMERIC(10,0),
   COMMENTSSTRID2                 NUMERIC(10,0),
   BRANCHID                       NUMERIC(10,0),
   PRIMARY KEY (ID)
);
/*==============================================================*/
/* Table: IN_CRITERION                                          */
/*==============================================================*/
CREATE TABLE IN_CRITERION
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(30),
   NAMESTRID                      NUMERIC(10,0),
   ISDEFAULT                      NUMERIC(1,0)                   NOT NULL,
   PRIMARY KEY (ID)
);


/*==============================================================*/
/* Table: IN_DEFINITION_TABLES                                  */
/*==============================================================*/
CREATE TABLE IN_DEFINITION_TABLES
(
   ID                             NUMERIC(10,0),
   CODE                           VARCHAR(12),
   DEFINITIONID                   NUMERIC(10,0),
   NODEID                         NUMERIC(10,0),
   NODEVISIBLE                    NUMERIC(10,0),
   VISIBLELEVEL                   NUMERIC(10,0),
   TYPE                           NUMERIC(10,0),
   EVALTYPE                       NUMERIC(10,0)
);
/*==============================================================*/
/* Index: DEFTABLES_DEFINITIONID                                */
/*==============================================================*/
CREATE INDEX DEFTABLES_DEFINITIONID ON IN_DEFINITION_TABLES
(
   DEFINITIONID
);

/*==============================================================*/
/* Index: DEFTABLES_ID                                          */
/*==============================================================*/
CREATE INDEX DEFTABLES_ID ON IN_DEFINITION_TABLES
(
   ID
);

/*==============================================================*/
/* Index: DEFTABLES_NODEID                                      */
/*==============================================================*/
CREATE INDEX DEFTABLES_NODEID ON IN_DEFINITION_TABLES
(
   NODEID
);

/*==============================================================*/
/* Index: DEFTABLES_TYPE                                        */
/*==============================================================*/
CREATE INDEX DEFTABLES_TYPE ON IN_DEFINITION_TABLES
(
   TYPE
);

/*==============================================================*/
/* Table: IN_IMPORTED_RETURNS                                   */
/*==============================================================*/
CREATE TABLE IN_IMPORTED_RETURNS
(
   ID                   NUMERIC(10) NOT NULL,
   RETURNCODE           VARCHAR(12),
   BANKCODE             VARCHAR(12),
   VERSIONCODE          VARCHAR(12),
   PERIODSTART          DATE,
   PERIODEND            DATE,
   USERID               NUMERIC(10),
   LANGID               NUMERIC(10),
   UPLOADTIME           DATE,
   IMPORTSTART          DATE,
   IMPORTEND            DATE,
   STATUS               NUMERIC(1),
   CONTENT              BLOB,
   MESSAGE              VARCHAR(4000),
   PRIMARY KEY (ID)
);

/*==============================================================*/
/* Table: IN_LICENCES                                           */
/*==============================================================*/
CREATE TABLE IN_LICENCES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   TYPEID                         NUMERIC(10,0),
   CODE                           VARCHAR(12),
   CREATIONDATE                   DATE,
   DATEOFCHANGE                   DATE,
   REASONSTRID                    NUMERIC(10,0),
   OPERATIONAL                    NUMERIC(10,0),
   BANKID                         NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: IN_LICENCE_TYPES                                      */
/*==============================================================*/
CREATE TABLE IN_LICENCE_TYPES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   NAMESTRID                      NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: IN_MANAGING_BODIES                                    */
/*==============================================================*/
CREATE TABLE IN_MANAGING_BODIES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   POSTSTRID                      NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: IN_MDT_COMPARISON                                     */
/*==============================================================*/
CREATE TABLE IN_MDT_COMPARISON
(
   ID                             NUMERIC(10,0),
   NODEID                         NUMERIC(10,0),
   `CONDITION`                    NUMERIC(10,0),
   EQUATION                       VARCHAR(600)
)
;

/*==============================================================*/
/* Index: MDT_COMP_ID                                           */
/*==============================================================*/
CREATE INDEX MDT_COMP_ID ON IN_MDT_COMPARISON
(
   ID
);

/*==============================================================*/
/* Index: MDT_COMP_NODEID                                       */
/*==============================================================*/
CREATE INDEX MDT_COMP_NODEID ON IN_MDT_COMPARISON
(
   NODEID
);

/*==============================================================*/
/* Table: IN_MDT_DEPENDENT_NODES                                */
/*==============================================================*/
CREATE TABLE IN_MDT_DEPENDENT_NODES
(
   NODEID                         NUMERIC(10,0),
   DEPENDENTNODEID                NUMERIC(10,0)
)
;

/*==============================================================*/
/* Index: DEPEND_DEPNODEID                                      */
/*==============================================================*/
CREATE INDEX DEPEND_DEPNODEID ON IN_MDT_DEPENDENT_NODES
(
   DEPENDENTNODEID
);

/*==============================================================*/
/* Index: DEPEND_NODEID                                         */
/*==============================================================*/
CREATE INDEX DEPEND_NODEID ON IN_MDT_DEPENDENT_NODES
(
   NODEID
);

/*==============================================================*/
/* Table: IN_MDT_NODES                                          */
/*==============================================================*/
CREATE TABLE IN_MDT_NODES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(30),
   NAMESTRID                      NUMERIC(10,0),
   PARENTID                       NUMERIC(10,0),
   TYPE                           NUMERIC(10,0),
   DATATYPE                       NUMERIC(10,0),
   EQUATION                       VARCHAR(3700),
   SEQUENCE                       NUMERIC(10,0),
   EVALMETHOD                     NUMERIC(10,0),
   DISABLED                       NUMERIC(10,0),
   REQUIRED                       NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Index: MDT_CODE                                              */
/*==============================================================*/
CREATE UNIQUE INDEX MDT_CODE ON IN_MDT_NODES
(
   CODE
);

/*==============================================================*/
/* Index: MDT_PARENTID                                          */
/*==============================================================*/
CREATE INDEX MDT_PARENTID ON IN_MDT_NODES
(
   PARENTID
);

/*==============================================================*/
/* Table: IN_PERIODS                                            */
/*==============================================================*/
CREATE TABLE IN_PERIODS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   PERIODNUMBER                   NUMERIC(10,0),
   FROMDATE                       DATE,
   TODATE                         DATE,
   PERIODTYPEID                   NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: IN_PERIOD_TYPES                                       */
/*==============================================================*/
CREATE TABLE IN_PERIOD_TYPES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   NAMESTRID                      NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: IN_REGIONS                                            */
/*==============================================================*/
CREATE TABLE IN_REGIONS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CITYSTRID                      NUMERIC(10,0),
   REGIONSTRID                    NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: IN_RETURNS                                            */
/*==============================================================*/
CREATE TABLE IN_RETURNS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   SCHEDULEID                     NUMERIC(10,0),
   VERSIONID                      NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Index: RETURNS_SCHEDULEID                                    */
/*==============================================================*/
CREATE UNIQUE INDEX RETURNS_SCHEDULEID ON IN_RETURNS
(
   SCHEDULEID
);

/*==============================================================*/
/* Table: IN_RETURN_DEFINITIONS                                 */
/*==============================================================*/
CREATE TABLE IN_RETURN_DEFINITIONS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   NAMESTRID                      NUMERIC(10,0),
   TYPEID                         NUMERIC(10,0),
   FORMAT                         LONGBLOB,
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Index: DEF_CODE                                              */
/*==============================================================*/
CREATE INDEX DEF_CODE ON IN_RETURN_DEFINITIONS
(
   CODE
);

/*==============================================================*/
/* Table: IN_RETURN_ITEMS                                       */
/*==============================================================*/
CREATE TABLE IN_RETURN_ITEMS
(
   ID                             NUMERIC(10,0),
   RETURNID                       NUMERIC(10,0),
   TABLEID                        NUMERIC(10,0),
   NODEID                         NUMERIC(10,0),
   ROWNUMBER                      NUMERIC(10,0),
   VALUE                          VARCHAR(255),
   NVALUE                         DOUBLE,
   VERSIONID                      NUMERIC(10,0)
)
;

/*==============================================================*/
/* Index: RETURN_COMPLEX2                                       */
/*==============================================================*/
CREATE INDEX RETURN_COMPLEX2 ON IN_RETURN_ITEMS
(
   RETURNID,
   NODEID,
   TABLEID
);

/*==============================================================*/
/* Index: RETURN_ITEMS_ID                                       */
/*==============================================================*/
CREATE INDEX RETURN_ITEMS_ID ON IN_RETURN_ITEMS
(
   ID
);

/*==============================================================*/
/* Index: RETURN_ITEMS_NODEID                                   */
/*==============================================================*/
CREATE INDEX RETURN_ITEMS_NODEID ON IN_RETURN_ITEMS
(
   NODEID
);

/*==============================================================*/
/* Index: RETURN_ITEMS_RETURNID                                 */
/*==============================================================*/
CREATE INDEX RETURN_ITEMS_RETURNID ON IN_RETURN_ITEMS
(
   RETURNID
);

/*==============================================================*/
/* Index: RETURN_ITEMS_ROWNUMBER                                */
/*==============================================================*/
CREATE INDEX RETURN_ITEMS_ROWNUMBER ON IN_RETURN_ITEMS
(
   ROWNUMBER
);

/*==============================================================*/
/* Index: RETURN_ITEMS_TABLEID                                  */
/*==============================================================*/
CREATE INDEX RETURN_ITEMS_TABLEID ON IN_RETURN_ITEMS
(
   TABLEID
);

/*==============================================================*/
/* Table: IN_RETURN_STATUSES                                    */
/*==============================================================*/
CREATE TABLE IN_RETURN_STATUSES
(
   ID                             NUMERIC(10,0),
   RETURNID                       NUMERIC(10,0),
   STATUS                         NUMERIC(10,0),
   STATUSDATE                     DATETIME,
   USERID                         NUMERIC(10,0),
   NOTE                           VARCHAR(4096),
   VERSIONID                      NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: IN_RETURN_TYPES                                       */
/*==============================================================*/
CREATE TABLE IN_RETURN_TYPES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   NAMESTRID                      NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Index: RETTYPES_CODE                                         */
/*==============================================================*/
CREATE UNIQUE INDEX RETTYPES_CODE ON IN_RETURN_TYPES
(
   CODE
);

/*==============================================================*/
/* Index: RETTYPES_NAME                                         */
/*==============================================================*/
CREATE UNIQUE INDEX RETTYPES_NAME ON IN_RETURN_TYPES
(
   NAMESTRID
);

/*==============================================================*/
/* Table: IN_RETURN_VERSIONS                                    */
/*==============================================================*/
CREATE TABLE IN_RETURN_VERSIONS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   SEQUENCE                       NUMERIC(10,0),
   DESCSTRID                      NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: IN_SCHEDULES                                          */
/*==============================================================*/
CREATE TABLE IN_SCHEDULES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   BANKID                         NUMERIC(10,0),
   DEFINITIONID                   NUMERIC(10,0),
   PERIODID                       NUMERIC(10,0),
   DELAY                          NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Index: SCHEDULES_BANKID                                      */
/*==============================================================*/
CREATE INDEX SCHEDULES_BANKID ON IN_SCHEDULES
(
   BANKID
);

/*==============================================================*/
/* Index: SCHEDULES_COMPLEX1                                    */
/*==============================================================*/
CREATE INDEX SCHEDULES_COMPLEX1 ON IN_SCHEDULES
(
   BANKID,
   PERIODID
);

/*==============================================================*/
/* Index: SCHEDULES_DEFINITIONID                                */
/*==============================================================*/
CREATE INDEX SCHEDULES_DEFINITIONID ON IN_SCHEDULES
(
   DEFINITIONID
);

/*==============================================================*/
/* Index: SCHEDULES_PERIODID                                    */
/*==============================================================*/
CREATE INDEX SCHEDULES_PERIODID ON IN_SCHEDULES
(
   PERIODID
);

/*==============================================================*/
/* Table: MM_BANK_GROUP                                         */
/*==============================================================*/
CREATE TABLE MM_BANK_GROUP
(
   BANKID                         NUMERIC(10,0)                  NOT NULL,
   BANKGROUPID                    NUMERIC(10,0)                  NOT NULL,
   PRIMARY KEY (BANKID, BANKGROUPID)
)
;

/*==============================================================*/
/* Table: OUT_REPORTS                                           */
/*==============================================================*/
CREATE TABLE OUT_REPORTS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   PARENTID                       NUMERIC(10,0),
   NAMESTRID                      NUMERIC(10,0),
   TYPE                           NUMERIC(10,0),
   TEMPLATE                       LONGBLOB,
   INFO                           LONGBLOB,
   SEQUENCE             NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: OUT_REPORTS_LANG                                      */
/*==============================================================*/
CREATE TABLE OUT_REPORTS_LANG
(
   REPORTID                       NUMERIC(10,0),
   LANGID                         NUMERIC(10,0),
   TEMPLATE                       LONGBLOB
)
;

/*==============================================================*/
/* Table: OUT_REPORTS_SCHEDULE                                  */
/*==============================================================*/
CREATE TABLE OUT_REPORTS_SCHEDULE
(
   REPORTID                       NUMERIC(10,0)                  NOT NULL,
   LANGID                         NUMERIC(10,0)                  NOT NULL,
   INFO                           LONGBLOB                       NOT NULL,
   HASHCODE                       NUMERIC(10,0)                  NOT NULL,
   STATUS                         NUMERIC(10,0),
   ONDEMAND                       NUMERIC(10,0),
   SCHEDULETIME                   DATETIME,
   USERID                         NUMERIC(10,0)                  NOT NULL
)
;

/*==============================================================*/
/* Table: OUT_REPOSITORY                                        */
/*==============================================================*/
CREATE TABLE OUT_REPOSITORY
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   PARENTID                       NUMERIC(10,0),
   NAME                           VARCHAR(50)                    NOT NULL,
   SCRIPT                         VARCHAR(2048),
   TYPE                           NUMERIC(10,0)                  NOT NULL,
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: OUT_REPOSITORY_PARAMS                                 */
/*==============================================================*/
CREATE TABLE OUT_REPOSITORY_PARAMS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   FORMULAID                      NUMERIC(10,0)                  NOT NULL,
   TYPE                           NUMERIC(10,0)                  NOT NULL,
   NAME                           VARCHAR(50)
)
;

/*==============================================================*/
/* Table: OUT_STORED_REPORTS                                    */
/*==============================================================*/
CREATE TABLE OUT_STORED_REPORTS
(
   REPORTID                       NUMERIC(10,0)                  NOT NULL,
   LANGID                         NUMERIC(10,0)                  NOT NULL,
   INFO                           LONGBLOB                       NOT NULL,
   REPORTRESULT                   LONGBLOB                       NOT NULL,
   HASHCODE                       NUMERIC(10,0)                  NOT NULL,
   USERID                         NUMERIC(10,0)                  NOT NULL,
   STOREDATE                      DATETIME
)
;

/*==============================================================*/
/* Table: SYS_LANGUAGES                                         */
/*==============================================================*/
CREATE TABLE SYS_LANGUAGES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   CODE                           VARCHAR(12),
   NAME                           VARCHAR(24),
   DATEFORMAT                     VARCHAR(24),
   NUMBERFORMAT                   VARCHAR(24),
   FONTFACE                       VARCHAR(24),
   FONTSIZE                       NUMERIC(10,0),
   HTMLCHARSET                    VARCHAR(20),
   XMLENCODING                    VARCHAR(20),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: SYS_MENUS                                             */
/*==============================================================*/
CREATE TABLE SYS_MENUS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   PARENTID                       NUMERIC(10,0),
   NAMESTRID                      NUMERIC(10,0),
   TYPE                           NUMERIC(10,0),
   ACTIONKEY                      VARCHAR(40),
   APPLICATION                    VARCHAR(80),
   SEQUENCE                       NUMERIC(10,0),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: SYS_PERMISSIONS                                       */
/*==============================================================*/
CREATE TABLE SYS_PERMISSIONS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   NAMESTRID                      NUMERIC(10,0),
   IDNAME                         VARCHAR(80),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: SYS_PROPERTIES                                        */
/*==============================================================*/
CREATE TABLE SYS_PROPERTIES
(
   PROP_KEY                       VARCHAR(64)                    NOT NULL,
   VALUE                          VARCHAR(256),
   PRIMARY KEY (PROP_KEY)
)
;

/*==============================================================*/
/* Table: SYS_ROLES                                             */
/*==============================================================*/
CREATE TABLE SYS_ROLES
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   NAMESTRID                      NUMERIC(10,0),
   CODE                           VARCHAR(12),
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: SYS_ROLE_MENUS                                        */
/*==============================================================*/
CREATE TABLE SYS_ROLE_MENUS
(
   ROLEID                         NUMERIC(10,0),
   MENUID                         NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_ROLE_PERMISSIONS                                  */
/*==============================================================*/
CREATE TABLE SYS_ROLE_PERMISSIONS
(
   ROLEID                         NUMERIC(10,0),
   PERMISSIONID                   NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_ROLE_REPORTS                                      */
/*==============================================================*/
CREATE TABLE SYS_ROLE_REPORTS
(
   ROLE_ID                        NUMERIC(10,0),
   REPORT_ID                      NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_ROLE_RETURNS                                      */
/*==============================================================*/
CREATE TABLE SYS_ROLE_RETURNS
(
   ROLE_ID                        NUMERIC(10,0),
   DEFINITION_ID                  NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_ROLE_RETURN_VERSIONS                              */
/*==============================================================*/
CREATE TABLE SYS_ROLE_RETURN_VERSIONS
(
   ROLE_ID                        NUMERIC(10,0),
   VERSION_ID                     NUMERIC(10,0),
   CAN_AMEND            NUMERIC(1,0) DEFAULT 0
)
;

/*==============================================================*/
/* Table: SYS_STRINGS                                           */
/*==============================================================*/
CREATE TABLE SYS_STRINGS
(
   ID                             NUMERIC(10,0)                  NOT NULL,
   LANGID                         NUMERIC(10,0),
   VALUE                          VARCHAR(255)
)
;

/*==============================================================*/
/* Index: STRINGS_COMPLEX                                       */
/*==============================================================*/
CREATE INDEX STRINGS_COMPLEX ON SYS_STRINGS
(
   ID,
   LANGID
);

/*==============================================================*/
/* Table: SYS_USERS                                             */
/*==============================================================*/
CREATE TABLE SYS_USERS
(
   ID                   NUMERIC(10,0) NOT NULL,
   LOGIN                          VARCHAR(15),
   PASSWORD                       VARCHAR(40),
   CHANGEPASSWORD                 NUMERIC(10,0),
   NAMESTRID                      NUMERIC(10,0),
   TITLESTRID                     NUMERIC(10,0),
   PHONE                          VARCHAR(40),
   EMAIL                          VARCHAR(40),
   BLOCKED                        NUMERIC(10,0),
   LASTLOGINDATE                  DATE,
   LASTPASSWORDCHANGEDATE DATE,
   PRIMARY KEY (ID)
)
;

/*==============================================================*/
/* Table: SYS_USERS_ROLES                                       */
/*==============================================================*/
CREATE TABLE SYS_USERS_ROLES
(
   USERID                         NUMERIC(10,0),
   ROLEID                         NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_USER_BANKS                                        */
/*==============================================================*/
CREATE TABLE SYS_USER_BANKS
(
   USERID                         NUMERIC(10,0),
   BANKID                         NUMERIC(10,0),
   CANAMEND             NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_USER_MENUS                                        */
/*==============================================================*/
CREATE TABLE SYS_USER_MENUS
(
   USERID                         NUMERIC(10,0),
   MENUID                         NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_USER_PASSWORDS                                    */
/*==============================================================*/
CREATE TABLE SYS_USER_PASSWORDS
(
   USERID                         NUMERIC(10,0)                  NOT NULL,
   PASSWORD                       VARCHAR(40)                    NOT NULL,
   STOREDATE                      DATETIME                       NOT NULL
)
;

/*==============================================================*/
/* Table: SYS_USER_PERMISSIONS                                  */
/*==============================================================*/
CREATE TABLE SYS_USER_PERMISSIONS
(
   USERID                         NUMERIC(10,0),
   PERMISSIONID                   NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_USER_REPORTS                                      */
/*==============================================================*/
CREATE TABLE SYS_USER_REPORTS
(
   USERID                         NUMERIC(10,0),
   REPORTID                       NUMERIC(10,0),
   CANAMEND                       NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_USER_RETURNS                                      */
/*==============================================================*/
CREATE TABLE SYS_USER_RETURNS
(
   USER_ID              NUMERIC(10,0),
   DEFINITION_ID                  NUMERIC(10,0)
)
;

/*==============================================================*/
/* Table: SYS_USER_RETURN_VERSIONS                              */
/*==============================================================*/
CREATE TABLE SYS_USER_RETURN_VERSIONS
(
   USER_ID              NUMERIC(10,0),
   VERSION_ID                     NUMERIC(10,0),
   CAN_AMEND            NUMERIC(1,0) DEFAULT 0
)
;

/*==============================================================*/
/* View: RESULT_VIEW                                            */
/*==============================================================*/
CREATE VIEW RESULT_VIEW AS SELECT RI.VALUE, RI.NVALUE, RI.NODEID, S.BANKID, P.PERIODTYPEID, P.FROMDATE, P.TODATE, 
P.ID AS PERIODID, RV.CODE AS VERSIONCODE, RV2.CODE AS LATESTVERSIONCODE
     FROM IN_SCHEDULES S,
          IN_PERIODS P,
          IN_RETURNS R,
          IN_RETURN_ITEMS RI,
          IN_RETURN_VERSIONS RV,
          IN_RETURN_VERSIONS RV2
     WHERE
       S.PERIODID = P.ID AND R.SCHEDULEID=S.ID AND RI.RETURNID=R.ID AND RV.ID = RI.VERSIONID AND RV2.ID = R.VERSIONID;

