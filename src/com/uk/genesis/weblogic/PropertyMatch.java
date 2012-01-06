package com.uk.genesis.weblogic;

/**
 * defines pattern element - Used by all internal 
 * classes to pass property from ant task.
 * @author Mike Mochan
 */
public class PropertyMatch {
    private String pattern;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

}
