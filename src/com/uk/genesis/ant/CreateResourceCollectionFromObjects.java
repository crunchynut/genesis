package com.uk.genesis.ant;

import java.io.File;

import com.uk.genesis.model.EnumUtils;
import com.uk.genesis.model.GenesisObject;
import com.uk.genesis.model.GenesisObjectNotFoundException;
import com.uk.genesis.model.GenesisObjectType;
import com.uk.genesis.model.ModelException;
import com.uk.genesis.model.ModelReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileList;

/**
 * Creates a FileSet containing the files relevant to all matched objects.
 * @author paul.jones
 */
public class CreateResourceCollectionFromObjects extends BaseGenesisTask {

    private String type;
    private String in;
    private String resourcecollection;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getIn() {
        return in;
    }

    public void setIn(final String in) {
        this.in = in;
    }

    public String getResourcecollection() {
        return resourcecollection;
    }

    public void setResourcecollection(final String fileset) {
        this.resourcecollection = fileset;
    }

    public void execute() {
        validate();

        try {
            final FileList result = new FileList();
            result.setDir(new File("."));
            result.setProject(getProject());

            final ModelReader mr = getGenesisLoader().getModelReader();
            final GenesisObjectType objType = mr.findSingleObjectType(type);
            if (objType == null) {
                throw new BuildException("Type " + type + " is invalid",
                        getLocation());
            }

            // Get all the instances of the object
            GenesisObject[] instances = null;
            if (in != null) {
                final GenesisObject parent = objType.getParent().getInstance(in);
                instances = EnumUtils.enumerateChildObjectsOfType(parent,
                        objType);
            } else {
                final GenesisObjectType[] rootTypes = mr.getRootObjectTypes();
                instances = EnumUtils.enumerateChildObjectsOfType(null,
                        rootTypes, objType);
            }

            // Build the fileset
            for (GenesisObject obj : instances) {
                final FileList.FileName filename = new FileList.FileName();
                filename.setName(obj.getContentLocation().getAbsolutePath());

                result.addConfiguredFile(filename);
            }

            getProject().addReference(getResourcecollection(), result);
        } catch (ModelException ex) {
            throw new BuildException(ex.getMessage(), ex, getLocation());
        } catch (GenesisObjectNotFoundException ex) {
            throw new BuildException(ex.getMessage(), ex, getLocation());
        }

        // FilenameSelector sel = new FilenameSelector();
        // result.addFilename();
    }

    @Override
    protected void validate() {
        super.validate();

        if (type == null) {
            throw new BuildException("type property must be set");
        }
        if (resourcecollection == null) {
            throw new BuildException("fileset property must be set");
        }
    }
}
