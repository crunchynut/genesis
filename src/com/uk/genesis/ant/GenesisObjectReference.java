package com.uk.genesis.ant;

import org.apache.tools.ant.Project;

public class GenesisObjectReference {

    private String type;
    private String name;
    private String ifCheck;

    public void setType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIf(final String ifCheck) {
        this.ifCheck = ifCheck;
    }

    public String getIf() {
        return ifCheck;
    }

    public boolean isValid(final Project project) {
        // If there was no if check, then we're always valid
        if (ifCheck == null) {
            return true;
        }

        // Get all the properties we need to check
        String[] properties = ifCheck.split(",");
        for (String property : properties) {
            String cleanProp = property.trim();

            if (cleanProp.length() > 0) {
                // If the property has an undefined value,
                //then this object reference is not valid.
                if (project.getProperty(cleanProp) == null) {
                    return false;
                }
            }
        }

        // All properties validated successfully
        return true;
    }
}
