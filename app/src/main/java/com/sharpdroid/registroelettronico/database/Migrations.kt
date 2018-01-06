package com.sharpdroid.registroelettronico.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

object Migrations {
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("DROP TABLE IF EXISTS ABSENCE")
                execSQL("DROP TABLE IF EXISTS COMMUNICATION")
                execSQL("DROP TABLE IF EXISTS FILE")
                execSQL("DROP TABLE IF EXISTS FOLDER")
                execSQL("DROP TABLE IF EXISTS GRADE")
                execSQL("DROP TABLE IF EXISTS LESSON")
                execSQL("DROP TABLE IF EXISTS NOTE")
                execSQL("DROP TABLE IF EXISTS PERIOD")
                execSQL("DROP TABLE IF EXISTS REMOTE_AGENDA")
                execSQL("DROP TABLE IF EXISTS SUBJECT")
                execSQL("DROP TABLE IF EXISTS SUBJECT_TEACHER")
                execSQL("DROP TABLE IF EXISTS TEACHER")

                execSQL("CREATE TABLE `ABSENCE` (`ID` INTEGER NOT NULL, `TYPE` TEXT NOT NULL, `DATE` INTEGER NOT NULL, `JUSTIFIED` INTEGER NOT NULL, `REASON_CODE` TEXT, `REASON_DESC` TEXT, `PROFILE` INTEGER NOT NULL, `H_POS` INTEGER NOT NULL, `VALUE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `COMMUNICATION` (`ID` INTEGER NOT NULL, `DATE` INTEGER NOT NULL, `IS_READ` INTEGER NOT NULL, `EVT_CODE` TEXT NOT NULL, `MY_ID` INTEGER NOT NULL, `TITLE` TEXT NOT NULL, `CATEGORY` TEXT NOT NULL, `HAS_ATTACHMENT` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `FILE` (`ID` INTEGER NOT NULL, `CONTENT_NAME` TEXT NOT NULL, `OBJECT_ID` INTEGER NOT NULL, `TYPE` TEXT NOT NULL, `DATE` INTEGER NOT NULL, `FOLDER` INTEGER NOT NULL, `TEACHER` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `FOLDER` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `FOLDER_ID` INTEGER NOT NULL, `NAME` TEXT NOT NULL, `LAST_UPDATE` INTEGER NOT NULL, `TEACHER` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL)")
                execSQL("CREATE TABLE `GRADE` (`M_CODE` TEXT NOT NULL, `M_COMPONENT_POS` INTEGER NOT NULL, `M_DATE` INTEGER NOT NULL, `M_DESCRIPTION` TEXT NOT NULL, `ID` INTEGER NOT NULL, `M_NOTES` TEXT NOT NULL, `M_PERIOD` INTEGER NOT NULL, `M_PERIOD_NAME` TEXT NOT NULL, `M_STRING_VALUE` TEXT NOT NULL, `M_SUBJECT_ID` INTEGER NOT NULL, `M_TYPE` TEXT NOT NULL, `M_UNDERLINED` INTEGER NOT NULL, `M_VALUE` REAL NOT NULL, `M_WEIGHT_FACTOR` REAL NOT NULL, `PROFILE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `LESSON` (`M_ARGUMENT` TEXT NOT NULL, `M_AUTHOR_NAME` TEXT NOT NULL, `M_CLASS_DESCRIPTION` TEXT NOT NULL, `M_CODE` TEXT NOT NULL, `M_DATE` INTEGER NOT NULL, `M_DURATION` INTEGER NOT NULL, `M_HOUR_POSITION` INTEGER NOT NULL, `ID` INTEGER NOT NULL, `M_SUBJECT_CODE` TEXT NOT NULL, `M_SUBJECT_DESCRIPTION` TEXT NOT NULL, `M_SUBJECT_ID` INTEGER NOT NULL, `M_TYPE` TEXT NOT NULL, `PROFILE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `NOTE` (`M_AUTHOR` TEXT NOT NULL, `M_DATE` INTEGER NOT NULL, `ID` INTEGER NOT NULL, `M_STATUS` INTEGER NOT NULL, `M_TEXT` TEXT NOT NULL, `M_WARNING` TEXT NOT NULL, `M_TYPE` TEXT NOT NULL, `PROFILE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `PERIOD` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `M_CODE` TEXT NOT NULL, `M_DESCRIPTION` TEXT NOT NULL, `M_END` INTEGER NOT NULL, `M_FINAL` INTEGER NOT NULL, `M_POSITION` INTEGER NOT NULL, `M_START` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL)")
                execSQL("CREATE TABLE `REMOTE_AGENDA` (`ID` INTEGER NOT NULL, `START` INTEGER NOT NULL, `END` INTEGER NOT NULL, `IS_FULL_DAY` INTEGER NOT NULL, `NOTES` TEXT NOT NULL, `AUTHOR` TEXT NOT NULL, `SUBJECT` TEXT NULL, `PROFILE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `SUBJECT` (`ID` INTEGER NOT NULL, `DESCRIPTION` TEXT NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("CREATE TABLE `SUBJECT_TEACHER` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `SUBJECT` INTEGER NOT NULL, `TEACHER` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL)")
                execSQL("CREATE TABLE `TEACHER` (`ID` INTEGER NOT NULL, `TEACHER_NAME` TEXT NOT NULL, PRIMARY KEY(`ID`))")

                //UPDATED ALL REMOTE TABLES

                //NEED TO UPDATE OLD TABLES TO THE NEW ORM (communication_info, file_info, local_agenda, local_grade, profile, remote_agenda_info, subject_info)

                execSQL("ALTER TABLE COMMUNICATION_INFO RENAME TO COMMUNICATION_INFO_OLD")
                execSQL("CREATE TABLE `COMMUNICATION_INFO` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT NOT NULL, `CONTENT` TEXT NOT NULL, `PATH` TEXT NOT NULL)")
                execSQL("INSERT INTO COMMUNICATION_INFO(`ID`, `TITLE`, `CONTENT`, `PATH`) SELECT `ID`, `TITLE`, `CONTENT`, `PATH` FROM COMMUNICATION_INFO_OLD")
                execSQL("DROP TABLE IF EXISTS COMMUNICATION_INFO_OLD")

                execSQL("ALTER TABLE FILE_INFO RENAME TO FILE_INFO_OLD")
                execSQL("CREATE TABLE `FILE_INFO` (`ID` INTEGER NOT NULL, `PATH` TEXT NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("INSERT INTO FILE_INFO (ID, PATH) SELECT ID, PATH FROM FILE_INFO_OLD")
                execSQL("DROP TABLE IF EXISTS FILE_INFO_OLD")

                execSQL("ALTER TABLE LOCAL_AGENDA RENAME TO LOCAL_AGENDA_OLD")
                execSQL("UPDATE LOCAL_AGENDA_OLD SET COMPLETEDDATE=0 WHERE COMPLETEDDATE IS NULL")
                execSQL("CREATE TABLE `LOCAL_AGENDA` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT NOT NULL, `CONTENT` TEXT NOT NULL, `TYPE` TEXT NOT NULL, `DAY` INTEGER NOT NULL, `SUBJECT` INTEGER NOT NULL, `TEACHER` INTEGER NOT NULL, `COMPLETED_DATE` INTEGER, `PROFILE` INTEGER NOT NULL, `ARCHIVED` INTEGER NOT NULL)")
                execSQL("INSERT INTO LOCAL_AGENDA(ID, TITLE, CONTENT, TYPE, DAY, SUBJECT, TEACHER, COMPLETED_DATE, PROFILE, ARCHIVED) SELECT ID, TITLE, CONTENT, TYPE, DAY, SUBJECT, TEACHER, COMPLETEDDATE as COMPLETED_DATE, PROFILE, ARCHIVED FROM LOCAL_AGENDA_OLD")
                execSQL("DROP TABLE IF EXISTS LOCAL_AGENDA_OLD")

                execSQL("ALTER TABLE LOCAL_GRADE RENAME TO LOCAL_GRADE_OLD")
                execSQL("CREATE TABLE `LOCAL_GRADE` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `VALUE` REAL NOT NULL, `VALUE_NAME` TEXT NOT NULL, `SUBJECT` INTEGER NOT NULL, `PERIOD` INTEGER NOT NULL, `TYPE` TEXT NOT NULL, `PROFILE` INTEGER NOT NULL)")
                execSQL("INSERT INTO LOCAL_GRADE(ID, VALUE, VALUE_NAME, SUBJECT, PERIOD, TYPE, PROFILE) SELECT ID, VALUE, VALUENAME as VALUE_NAME, SUBJECT, PERIOD, TYPE, PROFILE FROM LOCAL_GRADE_OLD")
                execSQL("DROP TABLE IF EXISTS LOCAL_GRADE_OLD")

                execSQL("ALTER TABLE PROFILE RENAME TO PROFILE_OLD")
                execSQL("CREATE TABLE `PROFILE` (`USERNAME` TEXT NOT NULL, `NAME` TEXT NOT NULL, `PASSWORD` TEXT NOT NULL, `CLASSE` TEXT NOT NULL, `ID` INTEGER NOT NULL, `TOKEN` TEXT NOT NULL, `EXPIRE` INTEGER NOT NULL, `IDENT` TEXT NOT NULL, `IS_MULTI` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("INSERT INTO PROFILE(USERNAME, NAME, PASSWORD, CLASSE, ID, TOKEN, EXPIRE, IDENT, IS_MULTI) SELECT USERNAME, NAME, PASSWORD, CLASSE, ID, TOKEN, EXPIRE, IDENT, IS_MULTI FROM PROFILE_OLD")
                execSQL("DROP TABLE IF EXISTS PROFILE_OLD")

                execSQL("ALTER TABLE REMOTE_AGENDA_INFO RENAME TO REMOTE_AGENDA_INFO_OLD")
                execSQL("CREATE TABLE `REMOTE_AGENDA_INFO` (`ID` INTEGER NOT NULL, `COMPLETED` INTEGER NOT NULL, `ARCHIVED` INTEGER NOT NULL, `TEST` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
                execSQL("INSERT INTO REMOTE_AGENDA_INFO(ID, COMPLETED, ARCHIVED, TEST) SELECT ID, COMPLETED, ARCHIVED, TEST FROM REMOTE_AGENDA_INFO_OLD")
                execSQL("DROP TABLE IF EXISTS REMOTE_AGENDA_INFO_OLD")

                execSQL("ALTER TABLE SUBJECT_INFO RENAME TO SUBJECT_INFO_OLD")
                execSQL("CREATE TABLE `SUBJECT_INFO` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TARGET` REAL NOT NULL, `DESCRIPTION` TEXT NOT NULL, `DETAILS` TEXT NOT NULL, `CLASSROOM` TEXT NOT NULL, `SUBJECT` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL)")
                execSQL("INSERT INTO SUBJECT_INFO(ID, TARGET, DESCRIPTION, DETAILS, CLASSROOM, SUBJECT, PROFILE) SELECT ID, TARGET, DESCRIPTION, DETAILS, CLASSROOM, SUBJECT, PROFILE FROM SUBJECT_INFO_OLD")
                execSQL("DROP TABLE IF EXISTS SUBJECT_INFO_OLD")
            }
        }
    }
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE LOCAL_AGENDA RENAME TO LOCAL_AGENDA_OLD")
                execSQL("UPDATE LOCAL_AGENDA_OLD SET COMPLETED_DATE=0 WHERE COMPLETED_DATE IS NULL")
                execSQL("CREATE TABLE `LOCAL_AGENDA` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT NOT NULL, `CONTENT` TEXT NOT NULL, `TYPE` TEXT NOT NULL, `DAY` INTEGER NOT NULL, `SUBJECT` INTEGER NOT NULL, `TEACHER` INTEGER NOT NULL, `COMPLETED_DATE` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL, `ARCHIVED` INTEGER NOT NULL)")
                execSQL("INSERT INTO LOCAL_AGENDA(ID, TITLE, CONTENT, TYPE, DAY, SUBJECT, TEACHER, COMPLETED_DATE, PROFILE, ARCHIVED) SELECT ID, TITLE, CONTENT, TYPE, DAY, SUBJECT, TEACHER, COMPLETED_DATE, PROFILE, ARCHIVED FROM LOCAL_AGENDA_OLD")
                execSQL("DROP TABLE IF EXISTS LOCAL_AGENDA_OLD")
            }
        }
    }
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `TimetableItem` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profile` INTEGER NOT NULL, `start` REAL NOT NULL, `end` REAL NOT NULL, `dayOfWeek` INTEGER NOT NULL, `subject` INTEGER NOT NULL)")
        }
    }
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `TimetableItem` ADD COLUMN `color` INTEGER NOT NULL DEFAULT 0")
        }
    }
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `TimetableItem` ADD COLUMN `where` TEXT")
            database.execSQL("ALTER TABLE `TimetableItem` ADD COLUMN `notes` TEXT")
        }
    }
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `REMOTE_AGENDA` ADD COLUMN `SUBJECT_ID` INTEGER")
        }
    }
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `COMMUNICATION` RENAME TO `_`")
            database.execSQL("CREATE TABLE `COMMUNICATION` (`ID` INTEGER NOT NULL, `DATE` INTEGER NOT NULL, `IS_READ` INTEGER NOT NULL, `EVT_CODE` TEXT NOT NULL, `MY_ID` INTEGER NOT NULL, `TITLE` TEXT NOT NULL, `CATEGORY` TEXT, `HAS_ATTACHMENT` INTEGER NOT NULL, `PROFILE` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
            database.execSQL("INSERT INTO COMMUNICATION(`ID`, `DATE`, `IS_READ`, `EVT_CODE`, `MY_ID`, `TITLE`, `CATEGORY`, `HAS_ATTACHMENT`, `PROFILE`) SELECT `ID`, `DATE`, `IS_READ`, `EVT_CODE`, `MY_ID`, `TITLE`, `CATEGORY`, `HAS_ATTACHMENT`, `PROFILE` FROM `_`")
            database.execSQL("DROP TABLE `_`")
        }
    }
    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `EXCLUDED_MARKS` (`ID` INTEGER NOT NULL, PRIMARY KEY(`ID`))")
        }
    }
}