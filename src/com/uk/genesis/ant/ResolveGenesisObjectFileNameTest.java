package com.uk.genesis.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import junit.framework.TestCase;

public class ResolveGenesisObjectFileNameTest extends TestCase {
    public void testThrowsBuildExceptionForMissingGenesisXml() {
        final Project project = new Project();
        final ResolveGenesisObjectFileName task = new ResolveGenesisObjectFileName();
        task.setProject(project);

        // task.setGenesisxml("genesis.xml");
        task.setType("/Test/Object");
        task.setName("/Parent/Child");
        task.setProperty("foundobject");

        try {
            task.execute();
            fail("Should have thrown BuildException");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage().contains("genesisxml")
                    && ex.getMessage().contains("required"));
        }
    }

    public void testThrowsBuildExceptionForMissingType() {
        final Project project = new Project();
        final ResolveGenesisObjectFileName task = new ResolveGenesisObjectFileName();
        task.setProject(project);

        task.setGenesisxml("genesis.xml");
        // task.setType("/Test/Object");
        task.setName("/Parent/Child");
        task.setProperty("foundobject");

        try {
            task.execute();
            fail("Should have thrown BuildException");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage().contains("type")
                    && ex.getMessage().contains("required"));
        }
    }

    public void testThrowsBuildExceptionForMissingName() {
        final Project project = new Project();
        final ResolveGenesisObjectFileName task = new ResolveGenesisObjectFileName();
        task.setProject(project);

        task.setGenesisxml("genesis.xml");
        task.setType("/Test/Object");
        // task.setName("/Parent/Child");
        task.setProperty("foundobject");

        try {
            task.execute();
            fail("Should have thrown BuildException");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage().contains("name")
                    && ex.getMessage().contains("required"));
        }
    }

    public void testThrowsBuildExceptionForMissingProperty() {
        final Project project = new Project();
        final ResolveGenesisObjectFileName task = new ResolveGenesisObjectFileName();
        task.setProject(project);

        task.setGenesisxml("genesis.xml");
        task.setType("/Test/Object");
        task.setName("/Parent/Child");
        // task.setProperty("foundobject");

        try {
            task.execute();
            fail("Should have thrown BuildException");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage().contains("property")
                    && ex.getMessage().contains("required"));
        }
    }

    public void testSetsPropertyForValidQuery() {
        final Project project = new Project();
        final ResolveGenesisObjectFileName task = new ResolveGenesisObjectFileName();
        task.setProject(project);

        task.setGenesisxml("etc/testdata/genesis.xml");
        task.setType("/Enterprise/Datacentre/Rack/Node");
        task.setName("/MyEnterprise-00/MyDatacentre-00/MyRack-00/MyNode-00");
        task.setProperty("foundobject");

        task.execute();

        final File expected = new File(
                "etc/testdata/config/MyEnterprise-00/physical/MyDatacentre-00/MyRack-00/MyNode-00/node.xml");
        assertEquals(expected.getAbsolutePath(),
                project.getProperty("foundobject"));
    }
}
