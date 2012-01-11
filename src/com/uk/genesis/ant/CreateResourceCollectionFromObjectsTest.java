package com.uk.genesis.ant;

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import junit.framework.TestCase;

public class CreateResourceCollectionFromObjectsTest extends TestCase {
    public void testValidatesTypeParameter() {
        final CreateResourceCollectionFromObjects task = new CreateResourceCollectionFromObjects();
        task.setProject(new Project());
        task.setGenesisxml("src/test/etc/genesis.xml");
        // task.setType("/Enterprise/Datacentre");
        task.setResourcecollection("FileSet");

        try {
            task.execute();
            fail("Should have checked type property");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage().contains("type"));
        }
    }

    public void testValidatesFilesetParameter() {
        final CreateResourceCollectionFromObjects task = new CreateResourceCollectionFromObjects();
        task.setProject(new Project());
        task.setGenesisxml("etc/testdata/genesis.xml");
        task.setType("/Enterprise/Datacentre");
        // task.setFileset("Fileset");

        try {
            task.execute();
            fail("Should have checked fileset property");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage().contains("fileset"));
        }
    }

    public void testHandlesNoInParameter() {
        final CreateResourceCollectionFromObjects task = new CreateResourceCollectionFromObjects();
        task.setProject(new Project());
        task.setGenesisxml("etc/testdata/genesis.xml");
        task.setType("/Enterprise/Datacentre");
        // task.setIn("/MyEnterprise-00/MyDatacentre-00");
        task.setResourcecollection("DatacentreName");

        task.execute();
    }

    public void testHandlesInParameter() {
        final CreateResourceCollectionFromObjects task = new CreateResourceCollectionFromObjects();
        task.setProject(new Project());
        task.setGenesisxml("etc/testdata/genesis.xml");
        task.setType("/Enterprise/Datacentre");
        task.setIn("/MyEnterprise-00");
        task.setResourcecollection("FileSet");

        task.execute();
    }

    public void testValidatesType() {
        final Project project = new Project();

        final CreateResourceCollectionFromObjects task = new CreateResourceCollectionFromObjects();
        task.setProject(project);
        task.setGenesisxml("etc/testdata/genesis.xml");
        task.setType("/Enterprise/Datacentre-Invalid");
        task.setResourcecollection("Fileset");

        try {
            task.execute();
            fail("Should have thrown error on invalid type");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage().contains(
                    "/Enterprise/Datacentre-Invalid"));
            assertTrue(ex.getMessage().contains("invalid"));
        }
    }

    public void testCreatesFileset() {
        final Project proj = new Project();

        final CreateResourceCollectionFromObjects task = new CreateResourceCollectionFromObjects();
        task.setProject(proj);
        task.setGenesisxml("etc/testdata/genesis.xml");
        task.setType("/Enterprise/Datacentre");
        task.setResourcecollection("FileSet");

        task.execute();

        assertNotNull(proj.getReference("FileSet"));
        assertTrue(proj.getReference("FileSet") instanceof ResourceCollection);
    }

    public void testFilesetHasValidEntries() {
        final Project proj = new Project();

        final CreateResourceCollectionFromObjects task = new CreateResourceCollectionFromObjects();
        task.setProject(proj);
        task.setGenesisxml("etc/testdata/genesis.xml");
        task.setType("/Enterprise/Datacentre");
        task.setResourcecollection("FileSet");

        task.execute();

        final ResourceCollection rc = (ResourceCollection) proj
                .getReference("FileSet");
        boolean foundDatacentre = false;
        for (final Iterator fsIt = rc.iterator(); fsIt.hasNext();) {
            final FileResource res = (FileResource) fsIt.next();

            assertEquals(
                    new File(
                            "etc/testdata/config/MyEnterprise-00/physical/MyDatacentre-00/datacentre.xml")
                            .getAbsoluteFile(), res.getFile().getAbsoluteFile());

            foundDatacentre = true;
        }

        assertTrue(foundDatacentre);
    }
}
