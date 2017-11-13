package ch.so.agi.gretl.tasks;



import java.io.File;
import java.sql.SQLException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ioxwkf.dbtools.Csv2db;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.Connector;
import ch.so.agi.gretl.util.TaskUtil;


public class CsvImport extends DefaultTask {
    protected GretlLogger log;
    @Input
    public Connector database;
	@InputFile
	public Object dataFile=null;
    @Input
	String tableName=null;
    @Input
    @Optional
	public boolean firstLineIsHeader=true;
    @Input
    @Optional
	public String valueDelimiter=null;
    @Input
    @Optional
	public String valueSeparator=null;
    @Input
    @Optional
	public String schemaName=null;
	
    @TaskAction
    public void importData()
    {
        log = LogEnvironment.getLogger(CsvImport.class);
        if (database==null) {
        	throw new IllegalArgumentException("database must not be null");
        }
        if (tableName==null) {
        	throw new IllegalArgumentException("tableName must not be null");
        }
        if (dataFile==null) {
            return;
        }
        Settings settings=new Settings();
        settings.setValue(IoxWkfConfig.SETTING_DBTABLE, tableName);
        // set optional parameters
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE,firstLineIsHeader?IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER:IoxWkfConfig.SETTING_FIRSTLINE_AS_VALUE);
    	if(valueDelimiter!=null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER,valueDelimiter);
    	}
    	if(valueSeparator!=null) {
            settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR,valueSeparator);
    	}
    	if(schemaName!=null) {
            settings.setValue(IoxWkfConfig.SETTING_DBSCHEMA,schemaName);
    	}
        
        File data=this.getProject().file(dataFile);
        java.sql.Connection conn=null;
        try {
        	conn=database.connect();
        	if(conn==null) {
            	throw new IllegalArgumentException("connection must not be null");
        	}
    		Csv2db csv2db=new Csv2db();
    		csv2db.importData(data, conn, settings);
        	conn.commit();
        	conn.close();
        	conn=null;
        } catch (Exception e) {
            log.error("failed to run CvsImport", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }finally {
        	if(conn!=null) {
        		try {
					conn.rollback();
	        		conn.close();
				} catch (SQLException e) {
		            log.error("failed to rollback/close", e);
				}
        		conn=null;
        	}
        }
    }

}

