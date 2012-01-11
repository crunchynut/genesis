package com.uk.genesis.ant;

import org.apache.tools.ant.Project;

import junit.framework.TestCase;

public class DefineTargetsForGenesisActionsTest extends TestCase {
    public void testDefinesCorrectActions() {
        final CreateFilterSetUsingObjects task = new CreateFilterSetUsingObjects();
        final Project project = new Project();
        task.init();
        task.setProject(project);
        task.setGenesisxml("src/test/etc/genesis.xml");
    }
}
