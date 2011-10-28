/*
 * @author Mike Mochan
 *
 *
 * For each of the following keystores identified
 * =================================
 * Custom Identity
 * <CustomIdentityKeyStoreFileName>/global/fs01/app/pd41/wlp10/domain/config/keystores/wls04-asap101.ams.voca.co.uk.jks</CustomIdentityKeyStoreFileName>
 * <CustomIdentityKeyStorePassPhrase>password</CustomIdentityKeyStorePassPhrase>
 *
 * Custom Trust
 * <CustomTrustKeyStoreFileName>/global/fs01/app/pd41/wlp10/domain/config/keystores/trust_newbacs.jks</CustomTrustKeyStoreFileName>
 * <CustomTrustKeyStorePassPhrase>zaPUcRabea2c</CustomTrustKeyStorePassPhrase>
 *
 * NodeManager
 * <NodeManagerCustomIdentityKeyStoreFile>nm-asap101.ams.voca.co.uk.jks</NodeManagerCustomIdentityKeyStoreFile>
 * ===================================
 *
 * Once you find one of these, get the Alias (which should always be
 * the same as the hostname, but lets get it from the properties anyway)
 *
 * Now retrieve the certificate from
 * Environment.Server.CustomIdentityKeyStoreFileName=/home/newbacspf02-2/wlserver81/domain/keystore/keystore
 *
 * Using the password from:
 * Environment.Server.CustomIdentityKeyStorePassPhrase=password
 *
 * Now check the expiration date on the certs to make sure
 * it's valid today and in 60 days.
 *
 * Check that the cert is signed by the newbacs_ca
 */

package com.uk.genesis.weblogic;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FilterSet;

/**
 * Work in progress to validate jks certificates.
 *
 * @author Mike Mochan
 *
 */
public class VerifyCerts extends Task {

    private int numofdays;
    private String filtername;
    private List<PropertyMatch> matches = new ArrayList<PropertyMatch>();
    private String srcfile;
    private X509Certificate xcert;
    private char[] storepass = {};
    private String keystoredir;
    private String password = "";
    private String alias = "";
    private String listenAddress = "";
    private char[] keypass = {};

    @SuppressWarnings("unchecked")
    public void execute() {

        String munge = "";
        String keystoreFile = "";
        FilterSet fs = (FilterSet) getProject().getReference(filtername);
        Map<String, String> map = fs.getFilterHash();

        for (Iterator<PropertyMatch> pm = matches.iterator(); pm.hasNext();) {
            PropertyMatch patternMatch = pm.next();
            Pattern globalPattern = Pattern.compile(patternMatch.getPattern());
            // System.out.println("Matching : " + GlobalPattern.toString());
            for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = map.get(key);
                if (globalPattern.matcher(key.toString()).matches()) {
                    // System.out.println("key = " + key);

                    munge = value.toString();
                    // System.out.println("Value = " + munge);
                    StringTokenizer st = new StringTokenizer(munge, "/");
                    // System.out.println("ST = " + st.toString());

                    String split = "";
                    for (int x = 0; st.hasMoreTokens(); x++ ) {
                        split = st.nextElement().toString();
                        if (split.endsWith("jks")) {
                            keystoreFile = split;
                            password = getPwd(key, map, globalPattern,
                                    keystoreFile);
                            alias = getAlias(key, map, globalPattern,
                                    keystoreFile);

                            // Get listen address
                            listenAddress = getListenAddress(map, key);
                            // get keypass - sets global var
                            getKeypass(map, key);
                            // Convert password to char []
                            // required format for cert password
                            char[] storepass = password.toCharArray();

                            if (! keystoreFile.startsWith("trust")
                                    && ! keystoreFile.contains("nm-")) {

                                // System.out.println("Processing :" +
                                // keystoreFile);

                                try {
                                    getX509cert(keystoreFile, storepass,
                                            keypass, alias, listenAddress);
                                } catch (CertificateException ce) {
                                    ce.printStackTrace();
                                } catch (KeyStoreException ke) {
                                    ke.printStackTrace();
                                } catch (NoSuchAlgorithmException ae) {
                                    ae.printStackTrace();
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                } catch (UnrecoverableKeyException uke) {
                                    uke.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public String getListenAddress(final Map<String, String> map,
            final Object key) {
        int strIdx = 0;
        String strKey = key.toString();
        strIdx = strKey.lastIndexOf(".");
        String strListenAddress = (strKey.substring(0, strIdx)
                                    + "." + "ListenAddress");

        if (map.containsKey(strListenAddress)) {
            Object value = map.get(strListenAddress);
            listenAddress = value.toString();
        }
        return listenAddress;

    }

    public String getPwd(final Object key, final Map<String, String> map,
            final Pattern globalPattern, final String keystoreFile) {
        int x = 0;
        String strKey = key.toString();
        String password = "";
        int strIdx = 0;
        String pwdKey = "";
        if (globalPattern.toString().contains("CustomIdentityKeyStoreFileName")) {
            strIdx = strKey.lastIndexOf(".");
            pwdKey = (strKey.substring(0, strIdx) + "."
            + "CustomIdentityKeyStorePassPhrase");
        }
        else if (globalPattern.toString().contains(
                "CustomTrustKeyStoreFileName")
                && x != 1) {
            x++ ;
            strIdx = strKey.lastIndexOf(".");
            pwdKey = (strKey.substring(0, strIdx) + "."
                    + "CustomTrustKeyStorePassPhrase");
        }
        if (map.containsKey(pwdKey)) {
            Object value = map.get(pwdKey);
            password = value.toString();
        }
        return password;
    }

    public String getAlias(final Object key, final Map<String, String> map,
            final Pattern globalPattern, final String keystoreFile) {
        String strKey = key.toString();
        int strIdx = 0;
        String alias = "";
        if (globalPattern.toString().contains("CustomIdentityKeyStoreFileName")) {
            strIdx = strKey.lastIndexOf(".");
            alias = (strKey.substring(0, strIdx) + "." + "PrivateKeyAlias");
        }
        if (map.containsKey(alias)) {
            Object value = map.get(alias);
            alias = value.toString();
        }
        return alias;
    }

    public void getX509cert(final String keystoreFile, final char[] storepass,
            final char[] keypass, final String alias, final String listenAddress)
            throws CertificateExpiredException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException,
            UnrecoverableKeyException {

        long today;
        long millisInDay;
        long nextDay;

        KeyStore keystore = KeyStore.getInstance("jks");
        FileInputStream keyStoreIS = new FileInputStream(getKeystoredir() + "/"
                + keystoreFile);
        System.out.println("Keystore alias is : " + alias);
        keystore.load(keyStoreIS, storepass);
        CertificateFactory cfInstance = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream certStream = new ByteArrayInputStream(keystore
                .getCertificate(alias).getEncoded());
        xcert = (X509Certificate) cfInstance.generateCertificate(certStream);

        // System.out.println(xcert.getSubjectDN());
        String strKey = "";
        String hostname = "";
        strKey = xcert.getSubjectDN().toString();
        int strIdx = strKey.indexOf(",");
        hostname = (strKey.substring(3, strIdx));

        if (hostname.equals(listenAddress)) {
            System.out.println("hostname verification success : "
                    + listenAddress);
        }
        keystore.getKey(alias, keypass);
        System.out.println("keypassword verification success");

        try {
            // Check validity with current date
            xcert.checkValidity();
            today = (System.currentTimeMillis());
            millisInDay = 1000 * 60 * 60 * 24;
            // add 60 days to current time
            nextDay = today + getNumofdays() * millisInDay;
            Date sixtyDays = new Date((long) nextDay);
            // check validity for future date
            xcert.checkValidity(sixtyDays);
            keyStoreIS.close();
        } catch (CertificateExpiredException ce) {
            System.out.println("EXPIRY WARNING  for Keystore " + keystoreFile
                    + " EXPIRY DATE --> " + xcert.getNotAfter());
        } catch (CertificateNotYetValidException nv) {
            System.out.println("***** Certificate not yet valid " + nv);
        }

    }

    public void checkKey() {

    }

    public void loadKeystor() {

    }

    public String getFiltername() {
        return filtername;
    }

    public void setFiltername(final String filtername) {
        this.filtername = filtername;
    }

    public void addConfiguredMatch(final PropertyMatch propMatch) {
        if (propMatch.getPattern() == null) {
            throw new BuildException("match must specify pattern",
                    getLocation());
        }
        matches.add(propMatch);
    }

    public String getSrcfile() {
        return srcfile;
    }

    public void setSrcfile(final String srcfile) {
        this.srcfile = srcfile;
    }

    public String getKeystoredir() {
        return keystoredir;
    }

    public void setKeystoredir(final String keystoredir) {
        this.keystoredir = keystoredir;
    }

    public char[] getStorepass() {
        return storepass;
    }

    public void setStorepass(final char[] storepass) {
        this.storepass = storepass;
    }

    public char[] getKeypass(final Map<String, String> map, final Object key) {
        int strIdx = 0;
        String strKey = key.toString();
        strIdx = strKey.lastIndexOf(".");
        String keypassKey = (strKey.substring(0, strIdx)
                            + "." + "PrivateKeyPassPhrase");

        if (map.containsKey(keypassKey)) {
            Object value = map.get(keypassKey);
            keypass = value.toString().toCharArray();
        }
        return keypass;
    }

    public void setKeypass(final char[] keypass) {
        this.keypass = keypass;
    }

    public int getNumofdays() {
        return numofdays;
    }

    public void setNumofdays(final int numofdays) {
        this.numofdays = numofdays;
    }
}
