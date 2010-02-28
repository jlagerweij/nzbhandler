package net.lagerwey.nzb;

import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdJob;
import net.lagerwey.nzb.net.lagerwey.nzb.domain.SabnzbdStatus;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Handler extends JFrame {

    private static final String KEY_SABNZBD_URL = "auth.url";
    private static final String KEY_USERNAME = "auth.username";
    private static final String KEY_PASSWORD = "auth.password";
    private static final String KEY_APIKEY = "auth.apikey";
    private static final String CONFIGURATION_FILE = "nzbhandler.properties";
    private static final String JAR_FILE_PREFIX = "jar:file:";

    private SabnzbdApi sabnzbd;
    private SabnzbdWeb sabnzbdWeb;

    private JTextField txtSabnzbdUrl;
    private JTextField txtApiKey;
    private JTextField txtUsername;
    private JTextField txtPassword;
    private JTextField txtFilename;
    private JComboBox cbxCategories;
    private JComboBox cbxPostProcessingOptions;

    private String filename;
    private JProgressBar pbarUpload;
    private JTable tblQueue;

    public Handler(String filename) throws HeadlessException, IOException {
        super();
        this.filename = filename;
        sabnzbd = new SabnzbdApi();
        sabnzbdWeb = new SabnzbdWeb();
        initComponents();
    }

    private void initComponents() throws IOException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog.showExceptionDialog(this, e);
        }
        this.setTitle("NZB Handler");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocation(400, 200);

        JLabel lblSabnzbdUrl = new JLabel("SabnzbdWeb home");
        this.txtSabnzbdUrl = new JTextField();
        String hostUrl = "http://ipaddress/sabnzbd";
        txtSabnzbdUrl.setText(hostUrl);
        JButton btnSabnzbdUrl = new JButton();
        btnSabnzbdUrl.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                BareBonesBrowserLaunch.openURL(txtSabnzbdUrl.getText());
            }
        });
        btnSabnzbdUrl.setText("Go");

        JLabel lblFilename = new JLabel("Filename");
        this.txtFilename = new JTextField();
        JButton btnBrowse = new JButton();
        btnBrowse.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser(txtFilename.getText());

                int returnVal = fileChooser.showOpenDialog(Handler.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    txtFilename.setText(file.getAbsolutePath());
                }
            }
        });
        btnBrowse.setText("...");

        JLabel lblApiKey = new JLabel("API key");
        txtApiKey = new JTextField();

        JLabel lblUsername = new JLabel("Username");
        this.txtUsername = new JTextField();

        JLabel lblPassword = new JLabel("Password");
        this.txtPassword = new JPasswordField();

        cbxCategories = new JComboBox();
        cbxCategories.setMaximumRowCount(20);
        JLabel lblCategories = new JLabel("categories");

        cbxPostProcessingOptions = new JComboBox();
        JLabel lblPostProcessingOptions = new JLabel("Post processing");

        JButton btnStart = new JButton();
        btnStart.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                startFileUpload();
            }
        });
        btnStart.setText("Upload file");

        readConfiguration();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(final WindowEvent e) {
                cbxCategories.requestFocusInWindow();
            }
        });

        JLabel lblQueue = new JLabel("Queue");
        tblQueue = new JTable();
        JScrollPane scrlQueue = new JScrollPane(tblQueue);
        scrlQueue.setPreferredSize(new Dimension(500,200));
        requestStatus();

        JLabel lblUpload = new JLabel("Upload progress");
        pbarUpload = new JProgressBar(0, 4);

        JPanel panel = new JPanel();
        this.getContentPane().add(panel);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblSabnzbdUrl)
                                .addComponent(lblApiKey)
                                .addComponent(lblFilename)
                                .addComponent(lblUsername)
                                .addComponent(lblPassword)
                                .addComponent(lblCategories)
                                .addComponent(lblQueue)
                                .addComponent(lblUpload)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(txtSabnzbdUrl)
                                .addComponent(txtApiKey)
                                .addComponent(txtFilename)
                                .addComponent(txtUsername)
                                .addComponent(txtPassword)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(cbxCategories)
                                        .addGap(10)
                                        .addComponent(lblPostProcessingOptions)
                                        .addGap(10)
                                        .addComponent(cbxPostProcessingOptions)
                                )
                                .addComponent(scrlQueue)
                                .addComponent(pbarUpload)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(btnSabnzbdUrl)
                                .addComponent(btnBrowse)
                                .addComponent(btnStart)
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblSabnzbdUrl, GroupLayout.Alignment.CENTER)
                                .addComponent(txtSabnzbdUrl)
                                .addComponent(btnSabnzbdUrl)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblUsername, GroupLayout.Alignment.CENTER)
                                .addComponent(txtUsername)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblPassword, GroupLayout.Alignment.CENTER)
                                .addComponent(txtPassword)
                        )
                        .addGap(30)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblApiKey, GroupLayout.Alignment.CENTER)
                                .addComponent(txtApiKey)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblFilename, GroupLayout.Alignment.CENTER)
                                .addComponent(txtFilename)
                                .addComponent(btnBrowse)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblCategories, GroupLayout.Alignment.CENTER)
                                .addComponent(cbxCategories)
                                .addComponent(lblPostProcessingOptions, GroupLayout.Alignment.CENTER)
                                .addComponent(cbxPostProcessingOptions)
                                .addComponent(btnStart)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblQueue)
                                .addComponent(scrlQueue)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lblUpload)
                                .addComponent(pbarUpload)
                        )
        );

        lblApiKey.setVisible(false);
        txtApiKey.setVisible(false);
        this.pack();
        this.setVisible(true);
    }

    private void requestStatus() {
        new Thread(new Runnable() {
            public void run() {
                SabnzbdStatus status = sabnzbd.queueStatus();
                if (status != null) {
                    DefaultTableModel tableModel = new DefaultTableModel(0, 2);
                    tableModel.setColumnIdentifiers(new String[] {"Name", "MB"});
                    for (SabnzbdJob job : status.getJobs()) {
                        DecimalFormat df = new DecimalFormat("#0");
                        String mbLeft = df.format(job.getMbleft());
                        String mb = df.format(job.getMb());
                        if (StringUtils.isNotEmpty(job.getFilename())) {
                            tableModel.addRow(new Object[]{job.getFilename(), mbLeft + "/" + mb});
                        }
                    }
                    tblQueue.setModel(tableModel);
                    tblQueue.getColumnModel().getColumn(0).setPreferredWidth(400);
                }
            }
        }).start();
    }

    private File getBaseDirectory() {
        CodeSource source = getClass().getProtectionDomain().getCodeSource();
        if (source == null) {
            return null;
        }

        File dataDir;
        try {
            String location = source.getLocation().toString();
            System.out.println("location1:" + location);
            if (location.startsWith(JAR_FILE_PREFIX)) {
                location = location.substring(JAR_FILE_PREFIX.length(), location.indexOf("!"));
                System.out.println("location2:" + location);
                dataDir = new File(location);
            } else {
                URI sourceURI = new URI(location);
                dataDir = new File(sourceURI);
            }
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }


        if (!dataDir.isDirectory()) {
            dataDir = dataDir.getParentFile();
        }
        return dataDir;
    }


    private void readConfiguration() {
        this.txtFilename.setText(this.filename);

        File propertiesFile = readPropertiesFile();
        if (propertiesFile.exists()) {
            Properties props = new SortedProperties();
            try {
                props.load(new FileInputStream(propertiesFile));
                this.txtSabnzbdUrl.setText(props.getProperty(KEY_SABNZBD_URL));
                this.txtUsername.setText(props.getProperty(KEY_USERNAME));
                this.txtPassword.setText(props.getProperty(KEY_PASSWORD));
                this.txtApiKey.setText(props.getProperty(KEY_APIKEY));
            } catch (IOException e) {
                ExceptionDialog.showExceptionDialog(this, e);
            }

            List<String> categories = new ArrayList<String>();
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                String category = props.getProperty("category_" + i);
                if (category == null) {
                    break;
                }
                categories.add(category);
            }

            List<OptionItem> postProcessingOptionsList = new ArrayList<OptionItem>();
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                String value = props.getProperty("postProcessing_" + i + ".value");
                String name = props.getProperty("postProcessing_" + i + ".name");
                if (value == null || name == null) {
                    break;
                }
                postProcessingOptionsList.add(new OptionItem(value, name));
            }

            try {
                sabnzbd.setUrl(txtSabnzbdUrl.getText());
                sabnzbd.setUsername(txtUsername.getText());
                sabnzbd.setPassword(txtPassword.getText());
                sabnzbd.setApiKey(txtApiKey.getText());

                if (categories.size() == 0) {
                    // Retrieve the categories from the server if the list is empty.
                    categories = sabnzbd.categoriesList();
                }
                cbxCategories.setModel(new DefaultComboBoxModel(categories.toArray(new String[categories.size()])));

                sabnzbdWeb.setUrl(txtSabnzbdUrl.getText());
                sabnzbdWeb.setUsername(txtUsername.getText());
                sabnzbdWeb.setPassword(txtPassword.getText());

                if (postProcessingOptionsList.size() == 0) {
                    postProcessingOptionsList = sabnzbdWeb.retrievePostProcessing();
                }
                OptionItem[] postProcessingItems = postProcessingOptionsList
                        .toArray(new OptionItem[postProcessingOptionsList.size()]);
                cbxPostProcessingOptions.setModel(new DefaultComboBoxModel(postProcessingItems));
            } catch (IllegalArgumentException e) {
                ExceptionDialog.showExceptionDialog(this, e);
            }
        }
    }

    private File readPropertiesFile() {
        String parentPath = getBaseDirectory().getAbsolutePath();

        File propertiesFile = new File(parentPath, CONFIGURATION_FILE);
        if (!propertiesFile.exists()) {
            String tmpdir = System.getProperty("java.io.tmpdir");
            System.out.println("Settings file not found in " + parentPath + ". Searching in " + tmpdir);
            propertiesFile = new File(tmpdir, CONFIGURATION_FILE);
        }
        System.out.println(
                "Using settings from " + propertiesFile.getAbsolutePath() + ". File exists: " + propertiesFile
                        .exists());
        return propertiesFile;
    }

    private File writeConfiguration() {

        Properties props = new SortedProperties();
        props.setProperty(KEY_SABNZBD_URL, txtSabnzbdUrl.getText());
        props.setProperty(KEY_USERNAME, txtUsername.getText());
        props.setProperty(KEY_PASSWORD, txtPassword.getText());
        props.setProperty(KEY_APIKEY, txtApiKey.getText());

        for (int i = 0; i < cbxCategories.getModel().getSize(); i++) {
            String category = (String) cbxCategories.getModel().getElementAt(i);
            props.setProperty("category_" + i, category);
        }

        for (int i = 0; i < cbxPostProcessingOptions.getModel().getSize(); i++) {
            OptionItem optionItem = (OptionItem) cbxPostProcessingOptions.getModel().getElementAt(i);
            props.setProperty("postProcessing_" + i + ".value", optionItem.getValue());
            props.setProperty("postProcessing_" + i + ".name", optionItem.getName());
        }

        File propertiesFile = readPropertiesFile();
        try {
            FileOutputStream stream = new FileOutputStream(propertiesFile);
            props.store(stream, "Properties of NzbHandler");
            stream.close();
        } catch (IOException e) {
            ExceptionDialog.showExceptionDialog(this, e);
        }
        return null;
    }

    protected void startFileUpload() {
        SwingWorker task = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                int progress = 0;
                writeConfiguration();
                pbarUpload.setValue(++progress);

                sabnzbd.setUrl(txtSabnzbdUrl.getText());
                sabnzbd.setUsername(txtUsername.getText());
                sabnzbd.setPassword(txtPassword.getText());
                sabnzbd.setApiKey(txtApiKey.getText());
                String uploadFilename = txtFilename.getText();
                String uploadCategory = (String) cbxCategories.getSelectedItem();
                String uploadPostProcessing = ((OptionItem) cbxPostProcessingOptions.getSelectedItem()).getValue();
                String uploadScript = null;
                pbarUpload.setValue(++progress);

                if (StringUtils.isEmpty(uploadFilename)) {
                    ExceptionDialog.showExceptionDialog(Handler.this, null, "Upload filename cannot be empty. Please select a filename.");
                }
                pbarUpload.setValue(++progress);

                try {
                    sabnzbd.addFile(uploadFilename, uploadCategory, uploadPostProcessing, uploadScript);
                    pbarUpload.setValue(++progress);
                    requestStatus();
                    JOptionPane.showMessageDialog(null, "Upload completed.");
                } catch (AddFileException e) {
                    ExceptionDialog.showExceptionDialog(e);
                }
                pbarUpload.setValue(0);

                dispose();
                return true;
            }
        };
        task.execute();

    }


    public static void main(String[] args) throws HeadlessException, IOException {
        String filename = null;
        if (args.length > 0) {
            filename = args[0];
        }
        new Handler(filename);
    }

}


	