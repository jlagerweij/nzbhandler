package net.lagerwey.nzb;

import com.meterware.httpunit.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class SabnzbdWeb {
    private String filename;
    private String username;
    private String password;
    private WebConversation conversation;
    private String url;
    private String category;
    private String postProcessing;

    public SabnzbdWeb setUrl(String url) {
        this.url = url;
        return this;
    }

    public SabnzbdWeb() {
        conversation = new WebConversation();
    }

    public SabnzbdWeb setPassword(String password) {
        this.password = password;
        return this;
    }

    public SabnzbdWeb setUsername(String username) {
        this.username = username;
        return this;
    }

    public SabnzbdWeb setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public SabnzbdWeb setCategory(String category) {
        this.category = category;
        return this;
    }

    public SabnzbdWeb setPostProcessing(String postProcessing) {
        this.postProcessing = postProcessing;
        return this;
    }

    public void uploadFile() {
        login();

        final PostMethodWebRequest request = new
                PostMethodWebRequest(this.url + "/addFile");
        request.setMimeEncoded(true);
        request.setParameter("nzbfile", new UploadFileSpec[] {
                new UploadFileSpec(new File(filename))
            });
        if (this.category != null) {
            request.setParameter("cat", this.category);
        }
        if (this.postProcessing != null) {
            request.setParameter("pp", this.postProcessing);
        }


        try {
            WebResponse response = conversation.getResponse(request);
            if (response.getResponseCode() != 200) {
                throw new IOException("Error code " + response.getResponseCode() + " after upload.");
            }
        } catch (IOException e) {
            ExceptionDialog.showExceptionDialog(e);
        } catch (SAXException e) {
            ExceptionDialog.showExceptionDialog(e);
        }
    }

    public OptionItem[] retrieveCategories() {
        List<OptionItem> optionItems = new ArrayList<OptionItem>();

        if (this.url != null) {
            login();

            try {
                WebRequest request = new GetMethodWebRequest(this.url);
                WebResponse response = conversation.getResponse(request);
                String[] optionValues = response.getForms()[1].getOptionValues("cat");
                String[] options = response.getForms()[1].getOptions("cat");
                for (int i = 0; i < optionValues.length; i++) {
                    optionItems.add(new OptionItem(optionValues[i], options[i]));
                }
            } catch (IOException e) {
                ExceptionDialog.showExceptionDialog(e);
            } catch (SAXException e) {
                ExceptionDialog.showExceptionDialog(e);
            }
        }
        return optionItems.toArray(new OptionItem[optionItems.size()]);
    }

    public List<OptionItem> retrievePostProcessing() {
        List<OptionItem> optionItems = new ArrayList<OptionItem>();

        if (this.url != null) {
            login();

            try {
                WebRequest request = new GetMethodWebRequest(this.url);
                WebResponse response = conversation.getResponse(request);
                String[] optionValues = response.getForms()[1].getOptionValues("pp");
                String[] options = response.getForms()[1].getOptions("pp");
                for (int i = 0; i < optionValues.length; i++) {
                    optionItems.add(new OptionItem(optionValues[i], options[i]));
                }
            } catch (IOException e) {
                ExceptionDialog.showExceptionDialog(e);
            } catch (SAXException e) {
                ExceptionDialog.showExceptionDialog(e);
            }
        }
        return optionItems;
//        return optionItems.toArray(new OptionItem[optionItems.size()]);
    }


    private void login() {
        try {
            WebRequest baseRequest = new GetMethodWebRequest(this.url);
            WebResponse response = conversation.getResponse(baseRequest);
            if (response.getForms().length > 0 &&
                    response.getForms()[0].getParameterValue("ma_username") != null) {
                // We need to login to the system.
                if (this.username == null) {
                    throw new IOException("Authentication required - username is missing.");
                }
                response.getForms()[0].setParameter("ma_username", this.username);
                response.getForms()[0].setParameter("ma_password", this.password);
                response.getForms()[0].submit();
            }
        } catch (IOException e) {
            ExceptionDialog.showExceptionDialog(e);
        } catch (SAXException e) {
            ExceptionDialog.showExceptionDialog(e);
        }
    }

}
