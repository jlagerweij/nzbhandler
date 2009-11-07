package net.lagerwey.nzb;

import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdAlias;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdJob;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdScript;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdStatus;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author Jos Lagerweij
 */
public class SabnzbdApi {

    private String url;
    private String username;
    private String password;
    private String apiKey;
    private static final String API_FILENAME = "name";
    private static final String API_CATEGORIES = "categories";
    private static final String API_CATEGORY = "category";
    private static final String API_SCRIPTS = "scripts";
    private static final String API_SCRIPT = "script";
    private static final String API_WARNINGS = "warnings";
    private static final String API_WARNING = "warning";
    private static final String CHANGE_COMPLETE_ACTION = "change_complete_action";

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


    public void addId() {
        // TODO addId
    }

    public void addUrl() {
        // TODO addUrl
    }

    /**
     * Resumes downloading.
     *
     * @return true if successfull, false otherwise.
     */
    public boolean resume() {
        // TODO resume
        HttpApi api = connectToSabnzbd();
        return api.executeApiCall(HttpApi.MODE_RESUME);
    }

    /**
     * Shutdown the server.
     *
     * @return true if successfull, false otherwise.
     */
    public boolean shutdown() {
        // TODO shutdown
        HttpApi api = connectToSabnzbd();
        return api.executeApiCall(HttpApi.MODE_SHUTDOW);
    }

    /**
     * Sets the auto shutdown feature on or off.
     *
     * @param autoShutdownOn True sets the auto shutdown feature on. False sets it off.
     * @return true if successfull, false otherwise.
     */
    public boolean autoShutdown(boolean autoShutdownOn) {
        // TODO autoshutdown
        HttpApi api = connectToSabnzbd();
        String autoShutdownOnValue = "0";
        if (autoShutdownOn) {
            autoShutdownOnValue = "1";
        }
        api.addParam(HttpApi.NAME, autoShutdownOnValue);
        return api.executeApiCall(HttpApi.MODE_AUTO_SHUTDOWN);
    }

    /**
     * Sets the speedlimit to a value.
     *
     * @param value The speedlimit.
     * @return true if successfull, false otherwise.
     */
    public boolean speedlimit(String value) {
        // TODO speedlimit
        HttpApi api = connectToSabnzbd();
        api.addParam(HttpApi.VALUE, value);
        return api.executeApiCall(HttpApi.MODE_SPEED_LIMIT);
    }

    /**
     * Sets the action for an empty queue.
     *
     * @param value Action to be executed when the download queue becomes empty: script_xxx, where 'xxx' is a script
     *              from the list returned by the api call get_scripts. shutdown_pc hibernate_pc shutdown_program
     * @return true if successfull, false otherwise.
     */
    public boolean emptyQueueAction(String value) {
        // TODO emptyQueueAction
        HttpApi api = connectToSabnzbd();
        api.addParam(HttpApi.NAME, CHANGE_COMPLETE_ACTION);
        api.addParam(HttpApi.VALUE, value);
        return api.executeApiCall(HttpApi.MODE_QUEUE);
    }

    public void version() {
        // TODO version
    }

    /**
     * Pauses the downloading.
     *
     * @return true if successfull, false otherwise.
     */
    public boolean pause() {
        // TODO pause
        HttpApi api = connectToSabnzbd();
        return api.executeApiCall(HttpApi.MODE_PAUSE);
    }

    /**
     * Upload an NZB file to Sabnzbd.
     *
     * @param filename       The filename
     * @param category       The category ({@link #categoriesList()})
     * @param postProcessing The post processing option (-1, 0, 1, 2, 3)
     * @param script         The script from the list returned by the api call get_scripts
     * @throws AddFileException When adding a file fails.
     */
    public void addFile(String filename, String category, String postProcessing, String script) throws AddFileException {
        HttpApi api = connectToSabnzbd();
        api.addParam(HttpApi.MODE, HttpApi.MODE_ADDFILE);
        if (category != null) {
            api.addParam(HttpApi.CAT, category);
        }
        if (postProcessing != null) {
            api.addParam(HttpApi.POST_PROCESSING, postProcessing);
        }
        if (script != null) {
            api.addParam(API_SCRIPT, script);
        }
        PostMethod method = api.preparePostMethod();
        try {
            addFileToMethod(filename, method);

            int responseCode = api.executeMethod(method);
            if (responseCode == HttpStatus.SC_OK) {
                System.out.println("Upload completed.");
            } else {
                System.out.println("Upload FAILED: " + HttpStatus.getStatusText(responseCode));
                System.out.println(method.getResponseBodyAsString());
                throw new AddFileException(
                        "Failed uploading file [" + filename + "]. Reason: " + HttpStatus.getStatusText(
                                responseCode) + ". Response: " + method.getResponseBodyAsString());
            }
        } catch (FileNotFoundException e) {
            throw new AddFileException(e.getMessage(), e);
        } catch (IOException e) {
            throw new AddFileException(e.getMessage(), e);
        }

    }

    void addFileToMethod(String filename, PostMethod method) throws FileNotFoundException {
        System.out.println("Adding file for upload: " + filename);
        Part[] parts = {new FilePart(API_FILENAME, new File(filename))};
        method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
    }

    /**
     * Returns a list of categories.
     *
     * @return Returns a list of categories.
     */
    public List<String> categoriesList() {
        HttpApi api = connectToSabnzbd();
        api.addParam(HttpApi.MODE, HttpApi.MODE_GET_CATS);
        api.addParam(HttpApi.OUTPUT, HttpApi.OUTPUT_XML);
        GetMethod method = api.prepareGetMethod();

        return api.executeList(String.class, method
                , new SabnzbdAlias(API_CATEGORIES, List.class)
                , new SabnzbdAlias(API_CATEGORY, String.class)
        );
    }

    /**
     * Returns a list of warnings.
     *
     * @return Returns a list of warnings.
     */
    public List<String> warningsList() {
        // TODO warnings
        HttpApi api = connectToSabnzbd();
        api.addParam(HttpApi.MODE, HttpApi.MODE_WARNINGS);
        api.addParam(HttpApi.OUTPUT, HttpApi.OUTPUT_XML);
        GetMethod method = api.prepareGetMethod();

        return api.executeList(String.class, method
                , new SabnzbdAlias(API_WARNINGS, List.class)
                , new SabnzbdAlias(API_WARNING, String.class)
        );
    }

    /**
     * Returns a list of scripts.
     *
     * @return Returns a list of scripts.
     */
    public List<SabnzbdScript> scriptsList() {
        HttpApi api = connectToSabnzbd();
        api.addParam(HttpApi.MODE, HttpApi.MODE_GET_SCRIPTS);
        api.addParam(HttpApi.OUTPUT, HttpApi.OUTPUT_XML);
        GetMethod method = api.prepareGetMethod();

        return api.executeList(SabnzbdScript.class, method
                , new SabnzbdAlias(API_SCRIPTS, List.class)
                , new SabnzbdAlias(API_SCRIPT, SabnzbdScript.class)
        );
    }

    /**
     * Brief queue status report.
     *
     * @return The status of the Sabnzbd.
     */
    public SabnzbdStatus queueStatus() {
        HttpApi api = connectToSabnzbd();
        api.addParam(HttpApi.MODE, HttpApi.MODE_QSTATUS);
        api.addParam(HttpApi.OUTPUT, HttpApi.OUTPUT_XML);
        GetMethod method = api.prepareGetMethod();

        return api.execute(SabnzbdStatus.class,
                           method,
                           new SabnzbdAlias("queue", SabnzbdStatus.class),
                           new SabnzbdAlias("job", SabnzbdJob.class));
    }


    // ======================== Helper methods ==============================

    HttpApi connectToSabnzbd() {
        return new HttpApi(url, username, password, apiKey);
    }
}
