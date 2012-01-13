package com.uk.genesis.ant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

/**
 * Task for locating all usages of a given property.
 * @author paul.jones
 * @author hussein.badakhchani
 */
public class UsageLocator extends Task {

    private List<ResourceCollection> resources;
    private String token;

    public UsageLocator() {
        resources = new ArrayList<ResourceCollection>();
    }

    public void add(final ResourceCollection col) {
        this.resources.add(col);
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    @Override
    public void execute() {
        if (getToken() == null) {
            throw new BuildException("token should be set", getLocation());
        }

        log("Finding usages for @" + token + "@");

        try {
            boolean foundUsage = false;

            // Work through each of the supplied resources,
            // and search for tokens
            for (ResourceCollection col : resources) {
                for (final Iterator<?> resourceIt = col.iterator();
                        resourceIt.hasNext();) {
                    final Resource res = (Resource) resourceIt.next();

                    if (processStream(res.getName(), res.getInputStream())) {
                        // The file had the token in it
                        log("  " + res.getName());
                        foundUsage = true;
                    }
                }
            }

            if (! foundUsage) {
                log("  No usages found.");
            }
        } catch (IOException ex) {
            throw new BuildException("Failed to process resource",
                                        ex, getLocation());
        }
    }

    private boolean processStream(final String resName,
            final InputStream stream) throws IOException {
        final BufferedReader br = new BufferedReader(
                new InputStreamReader(stream));
        String line;

        // Process each of the lines
        while ((line = br.readLine()) != null) {
            if (line.contains("@" + token + "@")) {
                return true;
            }
        }

        return false;
    }
}
