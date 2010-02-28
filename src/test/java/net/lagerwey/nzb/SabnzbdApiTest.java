package net.lagerwey.nzb;

import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdAlias;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdJob;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdScript;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdStatus;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Jos Lagerweij
 */
public class SabnzbdApiTest {

    private SabnzbdApi sab;

    @Before
    public void before() {
        sab = spy(new SabnzbdApi());
        sab.setApiKey("api-key");
        sab.setUrl("http://localhost:9000/sabnzbd");
        sab.setUsername("username");
        sab.setPassword("password");
    }

    @Test
    public void testAddId() {
        sab.addId();
    }

    @Test
    public void testAddUrl() {
        sab.addUrl();
    }

    @Test
    public void testShutdown() {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        sab.shutdown();
        verify(api).executeApiCall(HttpApi.MODE_SHUTDOW);
    }

    @Test
    public void testAutoShutdown() {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        sab.autoShutdown(true);
        verify(api).executeApiCall(HttpApi.MODE_AUTO_SHUTDOWN);
    }

    @Test
    public void testSpeedLimit() {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        sab.speedlimit("1000");
        verify(api).executeApiCall(HttpApi.MODE_SPEED_LIMIT);
    }

    @Test
    public void testEmptyQueueAction() {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        sab.emptyQueueAction("action");
        verify(api).executeApiCall(HttpApi.MODE_QUEUE);
    }

    @Test
    public void testVersion() {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        sab.version();
    }

    @Test
    public void testQueueStatus() {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        SabnzbdStatus returnValue = new SabnzbdStatus();
        returnValue.setDiskspace1("10");
        returnValue.setDiskspace2("20");
        returnValue.setTimeleft("0:00:00");
        returnValue.setHave_warnings(null);
        returnValue.setMb("0.0");
        returnValue.setMbleft("0.0");
        returnValue.setKbpersec("0.0");
        returnValue.setNoofslots("0");
        returnValue.setPaused(Boolean.FALSE);
        returnValue.setJobs(new ArrayList<SabnzbdJob>());
        when(api.execute(eq(SabnzbdStatus.class), any(GetMethod.class), any(SabnzbdAlias.class), any(SabnzbdAlias.class)))
                .thenReturn(returnValue);

        SabnzbdStatus status = sab.queueStatus();
        assertTrue(status.getDiskspace1().length() > 0);
        assertTrue(status.getDiskspace2().length() > 0);
        assertEquals("0:00:00", status.getTimeleft());
        assertEquals(null, status.getHave_warnings());
        assertEquals("0.0", status.getMb());
        assertEquals("0.0", status.getMbleft());
        assertEquals("0.0", status.getKbpersec());
        assertEquals("0", status.getNoofslots());
        assertEquals(Boolean.FALSE, status.isPaused());
        assertEquals(0, status.getJobs().size());
    }

    @Test
    public void testCategoriesList() {
        ArrayList<String> returnList = new ArrayList<String>();
        returnList.add("Category1");

        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        when(api.executeList(eq(String.class), any(GetMethod.class), any(SabnzbdAlias.class), any(SabnzbdAlias.class)))
                .thenReturn(returnList);

        List<String> categories = sab.categoriesList();
        assertEquals(1, categories.size());
        assertEquals("Category1", categories.get(0));
    }

    @Test
    public void testScriptsList() {
        ArrayList<SabnzbdScript> returnList = new ArrayList<SabnzbdScript>();
        returnList.add(new SabnzbdScript());

        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        when(api.executeList(eq(SabnzbdScript.class), any(GetMethod.class), any(SabnzbdAlias.class), any(SabnzbdAlias.class)))
                .thenReturn(returnList);

        List<SabnzbdScript> scripts = sab.scriptsList();
        assertEquals(1, scripts.size());
    }

    @Test
    public void testWarningsList() {
        ArrayList<String> returnList = new ArrayList<String>();
        returnList.add("Warning1");

        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        when(api.executeList(eq(String.class), any(GetMethod.class), any(SabnzbdAlias.class), any(SabnzbdAlias.class)))
                .thenReturn(returnList);

        List<String> warnings = sab.warningsList();
        assertTrue("No warnings on the system..", warnings.size() > 0);
        assertEquals("Warning1", warnings.get(0));
    }

    @Test
    public void testAddFile() throws IOException, AddFileException {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        PostMethod postMethod = new PostMethod();
        doReturn(postMethod).when(api).preparePostMethod();
        when(api.executeMethod(any(PostMethod.class))).thenReturn(HttpStatus.SC_OK);
        File tempFile = File.createTempFile("file", ".tmp");
        tempFile.deleteOnExit();

        sab.addFile(tempFile.getAbsolutePath(), "Default", "-1", null);
        assertTrue(tempFile.delete());
    }

    @Test
    public void testAddFile_AddFileException() throws IOException, AddFileException {
        HttpApi api = mock(HttpApi.class);
        when(api.executeMethod(any(PostMethod.class))).thenReturn(HttpStatus.SC_OK);

        File tempFile = File.createTempFile("file", ".tmp");
        tempFile.deleteOnExit();
        try {
            sab.addFile("test.nzb", "Default", "-1", "Script");
            fail("Expected AddFileException, but was not thrown.");
        } catch (AddFileException e) {
            assertEquals("File is not a normal file.", e.getMessage());
        }
        assertTrue(tempFile.delete());
    }

    @Test
    public void testAddFile_IOException() throws IOException, AddFileException {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        PostMethod postMethod = new PostMethod();
        doReturn(postMethod).when(api).preparePostMethod();
        when(api.executeMethod(any(PostMethod.class))).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("IOException in test.");
            }
        });

        File tempFile = File.createTempFile("file", ".tmp");
        tempFile.deleteOnExit();

        try {
            sab.addFile(tempFile.getAbsolutePath(), "Default", "-1", null);
            fail("Expected AddFileException, but was not thrown.");
        } catch (AddFileException e) {
            assertEquals("IOException in test.", e.getMessage());
        }
        assertTrue(tempFile.delete());
    }

    @Test
    public void testAddFile_NotOK() throws IOException, AddFileException {
        HttpApi api = mock(HttpApi.class);
        PostMethod postMethod = mock(PostMethod.class);
        doReturn(api).when(sab).connectToSabnzbd();
        doReturn(postMethod).when(api).preparePostMethod();
        doNothing().when(sab).addFileToMethod(anyString(), any(PostMethod.class));
        when(api.executeMethod(any(PostMethod.class))).thenReturn(HttpStatus.SC_METHOD_NOT_ALLOWED);

        try {
            sab.addFile("test.nzb", "Default", "-1", null);
            fail("Expected AddFileException, but was not thrown.");
        } catch (AddFileException e) {
            assertEquals("Failed uploading file [test.nzb]. Reason: Method Not Allowed. Response: null", e.getMessage());
        }
    }

    @Test
    public void testPauseAndResume() {
        HttpApi api = mock(HttpApi.class);
        doReturn(api).when(sab).connectToSabnzbd();
        SabnzbdStatus returnValue1 = new SabnzbdStatus();
        returnValue1.setPaused(Boolean.FALSE);
        returnValue1.setJobs(new ArrayList<SabnzbdJob>());
        SabnzbdStatus returnValue2 = new SabnzbdStatus();
        returnValue2.setPaused(Boolean.TRUE);
        returnValue2.setJobs(new ArrayList<SabnzbdJob>());
        SabnzbdStatus returnValue3 = new SabnzbdStatus();
        returnValue3.setPaused(Boolean.FALSE);
        returnValue3.setJobs(new ArrayList<SabnzbdJob>());
        when(api.execute(eq(SabnzbdStatus.class), any(GetMethod.class), any(SabnzbdAlias.class), any(SabnzbdAlias.class)))
                .thenReturn(returnValue1, returnValue2, returnValue3);
        when(api.executeApiCall(HttpApi.MODE_PAUSE)).thenReturn(true);
        when(api.executeApiCall(HttpApi.MODE_RESUME)).thenReturn(true);

        SabnzbdStatus status = sab.queueStatus();
        assertFalse(status.isPaused());

        boolean pauseSuccess = sab.pause();
        status = sab.queueStatus();
        assertTrue(pauseSuccess);
        assertTrue(status.isPaused());

        boolean resumeSuccess = sab.resume();
        status = sab.queueStatus();
        assertTrue(resumeSuccess);
        assertFalse(status.isPaused());
    }

}
