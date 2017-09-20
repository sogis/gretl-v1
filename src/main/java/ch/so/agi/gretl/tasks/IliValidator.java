package ch.so.agi.gretl.tasks;


import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import ch.so.agi.gretl.util.TaskUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.interlis2.validator.Validator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class IliValidator extends DefaultTask {
    private GretlLogger log;

    @InputFiles
    public List<Object> dataFiles;
	@Input
	public String models = null;
	@Input
	public String modeldir = null;
	@InputFile
	public Object configFile = null;
	@Input
	public boolean forceTypeValidation = false;
	@Input
	public boolean disableAreaValidation = false;
	@Input
	public boolean multiplicityOff = false;
	@Input
	public boolean allObjectsAccessible=false;
    @Input
    public boolean skipPolygonBuilding=false;
	@OutputFile
	public Object logFile = null;
	@OutputFile
	public Object xtflogFile = null;
	@InputDirectory
	public Object pluginFolder = null;
	@Input
	public String proxy = null;
	@Input
	public Integer proxyPort = null;

    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(IliValidator.class);

        if (dataFiles==null || dataFiles.size()==0) {
            return;
        }
        Settings settings=new Settings();
        settings.setValue(Validator.SETTING_DISABLE_STD_LOGGER, Validator.TRUE);
        List<String> files=new ArrayList<String>();
        for(Object fileObj:dataFiles) {
        	String fileName=this.getProject().file(fileObj).getPath();
        	files.add(fileName);
        }
        
        	if(models!=null) {
            	settings.setValue(Validator.SETTING_MODELNAMES, models);
        	}
        	if(modeldir!=null) {
            	settings.setValue(Validator.SETTING_ILIDIRS, modeldir);
        	}
        	if(configFile!=null) {
                settings.setValue(Validator.SETTING_CONFIGFILE, this.getProject().file(configFile).getPath());
        	}
        	if(forceTypeValidation) {
            	settings.setValue(Validator.SETTING_FORCE_TYPE_VALIDATION,Validator.TRUE);
        	}
        	if(disableAreaValidation) {
            	settings.setValue(Validator.SETTING_DISABLE_AREA_VALIDATION,Validator.TRUE);
        	}
        	if(multiplicityOff) {
            	settings.setValue(Validator.SETTING_MULTIPLICITY_VALIDATION,ch.interlis.iox_j.validator.ValidationConfig.OFF);
        	}
        	if(allObjectsAccessible){
            	settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE,Validator.TRUE);
        	}
        	if(skipPolygonBuilding) {
            	settings.setValue(ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES, ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES_DO);
        	}
        	if(logFile!=null) {
                settings.setValue(Validator.SETTING_LOGFILE, this.getProject().file(logFile).getPath());
        	}
        	if(xtflogFile!=null) {
                settings.setValue(Validator.SETTING_XTFLOG, this.getProject().file(xtflogFile).getPath());
        	}
        	if(pluginFolder!=null) {
                settings.setValue(Validator.SETTING_PLUGINFOLDER, this.getProject().file(pluginFolder).getPath());
        	}
        	if(proxy!=null) {
        	    settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        	}
        	if(proxyPort!=null) {
        	    settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        	}
        
        try {
        	boolean ret=Validator.runValidation(files.toArray(new String[files.size()]), settings);
        } catch (Exception e) {
            log.error("failed to validate data", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }

}

