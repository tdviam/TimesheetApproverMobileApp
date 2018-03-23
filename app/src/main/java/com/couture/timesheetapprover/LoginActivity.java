package com.couture.timesheetapprover;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    EditText txtUsername;

    private Boolean existingUser = true;
    private static String URL = "https://10.140.0.4:9443/teamworks/webservices/CCSYS/CISWebservice.tws";
    private static final HostnameVerifier DUMMY_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtUsername = (EditText) findViewById(R.id.txtUsername);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean pass = true;

                if(txtUsername.getText().toString().length() == 0){
                    txtUsername.setError("Invalid Username. Please try again!");
                    pass = false;
                }

                if(pass){
                    Boolean result = false;
                    LoginTask loadTask = new LoginTask();
                    try {
                        result = loadTask.execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if(existingUser){
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("PM_NAME", txtUsername.getText().toString());
                        startActivity(intent);
                    }else{
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getRootView().getContext());
                        // set title
                        alertDialogBuilder.setTitle("Alert");

                        // set dialog message
                        alertDialogBuilder.setMessage("Username is not existed!");
                        alertDialogBuilder.setNeutralButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {

                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
            }
        });
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {

                URL url = new URL(URL);

                HttpsURLConnection connection = (HttpsURLConnection) url
                        .openConnection();
                connection.setHostnameVerifier(DUMMY_VERIFIER);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                connection.setRequestProperty("SOAPAction",
                        "http://CCSYS/CISWebservice.tws/checkExistingManager");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //XML
                String reqXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cis=\"http://CCSYS/CISWebservice.tws\">\n" +
                        "<soapenv:Header/>\n" +
                        "<soapenv:Body>\n" +
                        "<cis:checkExistingManager>\n" +
                        "<cis:username>" + txtUsername.getText().toString() + "</cis:username>\n" +
                        "</cis:checkExistingManager>\n" +
                        "</soapenv:Body>\n" +
                        "</soapenv:Envelope>";
                //System.out.println("xml ... " + reqXML);
                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(reqXML);
                wr.flush();

                int responseCode = connection.getResponseCode();
                System.out.println("Code ... " + responseCode);
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));;
                    StringBuilder sb = new StringBuilder();

                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(sb.toString()));

                    Document doc = db.parse(is);
                    //NodeList nodes = doc.getElementsByTagName("item");

                    NodeList tasks = doc.getElementsByTagName("existing");
                    Element elementTask = (Element) tasks.item(0);

                    //NodeList items = elementTask.getChildNodes();
                    System.out.println("elementTask " + elementTask.getTextContent());
                    existingUser = Boolean.valueOf(elementTask.getTextContent());
                    //System.out.println("node list" + existingUser);

                    return true;
                }

                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
