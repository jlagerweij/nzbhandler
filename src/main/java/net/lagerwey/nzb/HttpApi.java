package net.lagerwey.nzb;

import com.thoughtworks.xstream.XStream;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdAlias;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jos Lagerweij
 */
public class HttpApi {
    public static final String APIKEY = "apikey";
    public static final String MA_USERNAME = "ma_username";
    public static final String MA_PASSWORD = "ma_password";

    public static final String MODE = "mode";
    public static final String OUTPUT = "output";
    public static final String MODE_QSTATUS = "qstatus";
    public static final String OUTPUT_XML = "xml";
    public static final String MODE_ADDFILE = "addfile";
    public static final String CAT = "cat";
    public static final String POST_PROCESSING = "pp";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String MODE_GET_CATS = "get_cats";
    public static final String MODE_GET_SCRIPTS = "get_scripts";
    public static final String MODE_WARNINGS = "warnings";
    public static final String MODE_PAUSE = "pause";
    public static final String MODE_RESUME = "resume";
    public static final String MODE_SHUTDOW = "shutdown";
    public static final String MODE_AUTO_SHUTDOWN = "autoshutdown";
    public static final String MODE_SPEED_LIMIT = "speedlimit";
    public static final String MODE_QUEUE = "queue";

    private ArrayList<NameValuePair> queryParams;
    private HttpClient client;
    private String url;
    private String username;
    private String password;
    private String apiKey;
    private boolean debug;

    public HttpApi(String url, String username, String password, String apiKey) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;

        client = new HttpClient();
        queryParams = new ArrayList<NameValuePair>();
        setDebug(false);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }



    public void addParam(String name, String value) {
        queryParams.add(new NameValuePair(name, value));
    }

    public GetMethod prepareGetMethod() {
        addParam(APIKEY, apiKey);
        if (!StringUtils.isEmpty(username) || !StringUtils.isEmpty(password)) {
            addParam(MA_USERNAME, username);
            addParam(MA_PASSWORD, password);
        }

        GetMethod method = new GetMethod(url + "/api");
        NameValuePair valuePairs[] = queryParams.toArray(new NameValuePair[queryParams.size()]);
        method.setQueryString(valuePairs);

        return method;
    }

    public PostMethod preparePostMethod() {
        addParam(APIKEY, apiKey);
        if (!StringUtils.isEmpty(username) || !StringUtils.isEmpty(password)) {
            addParam(MA_USERNAME, username);
            addParam(MA_PASSWORD, password);
        }

        PostMethod method = new PostMethod(url + "/api");
        NameValuePair valuePairs[] = queryParams.toArray(new NameValuePair[queryParams.size()]);
        method.setQueryString(valuePairs);

        return method;
    }

    public <T> List<T> executeList(Class<T> toParse, GetMethod method, SabnzbdAlias... alias) {
        if (toParse == null) {
            throw new IllegalArgumentException("toParse");
        }

        List<T> returnValue = null;
        try {
            int responseCode = client.executeMethod(method);
            if (responseCode == HttpStatus.SC_OK) {
                if (debug) {
                    System.out.println(method.getResponseBodyAsString());
                }
                XStream xstream = new XStream();
                for (SabnzbdAlias anAlias : alias) {
                    xstream.alias(anAlias.getName(), anAlias.getClazz());
                }

                returnValue = (List<T>) xstream.fromXML(method.getResponseBodyAsStream());
            } else {
                JOptionPane.showMessageDialog(null, (new StringBuilder()).append("SABnzbd reported an error, check your configuration! Message from SABnzbd: ").append(method.getResponseBodyAsString()).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            ExceptionDialog.showExceptionDialog(e);
        }
        return returnValue;
    }

    public <T> T execute(Class<T> toParse, GetMethod method, SabnzbdAlias... alias) {
        if (toParse == null) {
            throw new IllegalArgumentException("toParse");
        }

        T returnValue = null;

        try {
            int responseCode = client.executeMethod(method);
            if (responseCode == HttpStatus.SC_OK) {
                if (debug) {
                    System.out.println(method.getResponseBodyAsString());
                }
                XStream xstream = new XStream();
                for (SabnzbdAlias anAlias : alias) {
                    xstream.alias(anAlias.getName(), anAlias.getClazz());
                }
                returnValue = (T) xstream.fromXML(method.getResponseBodyAsStream());
            } else {
                JOptionPane.showMessageDialog(null, (new StringBuilder()).append("SABnzbd reported an error, check your configuration! Message from SABnzbd: ").append(method.getResponseBodyAsString()).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            ExceptionDialog.showExceptionDialog(e);
        }
        return returnValue;

    }

    public int executeMethod(PostMethod method) throws IOException {
        return client.executeMethod(method);
    }

    public int executeMethod(GetMethod method) throws IOException {
        return client.executeMethod(method);
    }

    public boolean executeApiCall(String mode) {
        boolean success = false;
        addParam(HttpApi.MODE, mode);
        GetMethod method = prepareGetMethod();
        try {
            int responseCode = executeMethod(method);
            if (responseCode == HttpStatus.SC_OK) {
                success = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }


}
