package com.uk.genesis;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.uk.genesis.archetype.ArchetypeFactory;
import com.uk.genesis.archetype.ArchetypeFactoryImpl;
import com.uk.genesis.model.ConfigurableModelReader;
import com.uk.genesis.model.GenesisEnvironment;
import com.uk.genesis.model.ModelReader;

/**
 * Loader for accessing the Genesis object model.
 *
 * @author paul.jones
 * @author hussein.badakhchani
 */
public class GenesisLoader {
    private ModelReader mReader;
    private ArchetypeFactory mActionFactory;

    public GenesisLoader() {
    }

    /**
     * Requests that the loader parse the given genesisXml file.
     *
     * @param genesisXml the genesis.xml file to load.
     */
    public void load(final String genesisXml)
            throws IOException, InvalidGenesisConfigurationException {
        try {
            // Load the XML
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                    domFactory.setNamespaceAware(true); // Enable name space support
                    final DocumentBuilder builder = domFactory.newDocumentBuilder();
                    final Document doc = builder.parse(genesisXml);

            // Work out where our root is
            final File root = new File(genesisXml).getAbsoluteFile().getParentFile().getAbsoluteFile();

            // Find the various XML elements we need to parse
            final Element model = findModelDeclaration(doc);
            final Element archetypes = findArchetypesDeclaration(doc);

            // Parse the model and actions
            final GenesisEnvironment env = new GenesisEnvironment(root, model, archetypes);
            parseModel(env);
            parseActions(env);
        } catch (XPathExpressionException ex) {
            throw new InvalidGenesisConfigurationException("Failed to parse XML", ex);
        } catch (ParserConfigurationException ex) {
            throw new InvalidGenesisConfigurationException("Failed to parse XML", ex);
        } catch (SAXException ex) {
            throw new InvalidGenesisConfigurationException("Failed to parse XML", ex);
        }
    }

    /**
     * Retrieves the ModelReader instance. This requires the load call to have
     * previously succeeded.
     * @return the current model reader.
     */
    public ModelReader getModelReader() {
        return mReader;
    }

    /**
     * Retrieves the ArchetypeFactory instance. This provides access to the archetypes defined
     * in the genesis configuration.
     * @return the action factory.
     */
    public ArchetypeFactory getArchetypeFactory() {
        return mActionFactory;
    }

    private Element findModelDeclaration(final Document doc)
            throws XPathExpressionException {
        final GenesisNamespaceContext nsCtx = new GenesisNamespaceContext();
        nsCtx.addNamespace("genesis", "http://genesis.uk.com/schemas/1.0/genesis-root");

        final XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                xpath.setNamespaceContext(nsCtx);
                final XPathExpression expr = xpath.compile("/genesis:genesis/genesis:model");

        return (Element) expr.evaluate(doc, XPathConstants.NODE);
    }

    private Element findArchetypesDeclaration(final Document doc)
            throws XPathExpressionException {
        final GenesisNamespaceContext nsCtx = new GenesisNamespaceContext();
        nsCtx.addNamespace("genesis", "http://genesis.uk.com/schemas/1.0/genesis-root");

        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(nsCtx);
        final XPathExpression expr = xpath.compile("/genesis:genesis/genesis:archetypes");

        return (Element) expr.evaluate(doc, XPathConstants.NODE);
    }

    private void parseModel(final GenesisEnvironment env)
            throws InvalidGenesisConfigurationException {
        final String reader = env.getGenesisModel().getAttribute("reader");
        if (reader == null || reader.length() == 0) {
            throw new InvalidGenesisConfigurationException("No reader attribute specified on model");
        }
        try {
            final Class<?> modelClass = this.getClass().getClassLoader().loadClass(reader);
            if (!ModelReader.class.isAssignableFrom(modelClass)) {
                throw new InvalidModelReaderException("Specified reader does not implement ModelReader");
            }

            mReader = (ModelReader) modelClass.newInstance();

            if (mReader instanceof ConfigurableModelReader) {
                final ConfigurableModelReader configurable = (ConfigurableModelReader) mReader;

                configurable.configure(env);
            }
        } catch (ClassNotFoundException ex) {
            throw new InvalidModelReaderException("Invalid reader", ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidModelReaderException("Cannot instantiate reader", ex);
        } catch (InstantiationException ex) {
            throw new InvalidModelReaderException("Cannot instantiate reader", ex);
        }
    }

    private void parseActions(final GenesisEnvironment env)
            throws InvalidGenesisConfigurationException {
        mActionFactory = new ArchetypeFactoryImpl(env, mReader);
    }
}
