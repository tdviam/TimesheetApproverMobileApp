package com.couture.timesheetapprover;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;

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
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private static String URL = "https://10.140.0.4:9443/teamworks/webservices/CCSYS/CISWebservice.tws";
    ArrayList<Timesheet> timesheets = new ArrayList<Timesheet>();
    private ListView mListView;
    private Button btnCallBPM;
    private String pmName;
    //private String taskId;

    private static final HostnameVerifier DUMMY_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mListView = (ListView) findViewById(R.id.timesheet_list_view);
        btnCallBPM = (Button) findViewById(R.id.btnCallBPM);

        pmName = getIntent().getStringExtra("PM_NAME");

        OpenConnectionTask openConnection = new OpenConnectionTask(getApplicationContext());
        openConnection.execute();

        btnCallBPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timesheets.clear();
                OpenConnectionTask openConnection = new OpenConnectionTask(getApplicationContext());
                openConnection.execute();
            }
        });
    }

    private class OpenConnectionTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;

        public OpenConnectionTask (Context context){
            mContext = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                URL url = new URL(URL);

                HttpsURLConnection connection = (HttpsURLConnection) url
                        .openConnection();
                connection.setHostnameVerifier(DUMMY_VERIFIER);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                connection.setRequestProperty("SOAPAction",
                        "http://CCSYS/CISWebservice.tws/getAllActiveTasks");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //XML
                String reqXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cis=\"http://CCSYS/CISWebservice.tws\">\n" +
                        "<soapenv:Header/>\n" +
                        "<soapenv:Body>\n" +
                        "<cis:getAllActiveTasks>\n" +
                        "<cis:username>" + pmName + "</cis:username>\n" +
                        "</cis:getAllActiveTasks>\n" +
                        "</soapenv:Body>\n" +
                        "</soapenv:Envelope>";

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

                    NodeList tasks = doc.getElementsByTagName("taskDetails");
                    Element elementTask = (Element) tasks.item(0);

                    NodeList items = elementTask.getChildNodes();
                    System.out.println("node list" + items.getLength());
                    for (int i = 0; i < items.getLength(); i++) {
                        Element element = (Element) items.item(i);

                        Timesheet ts = new Timesheet();

                        NodeList taskId = element.getElementsByTagName("taskId");
                        Element line = (Element) taskId.item(0);
                        ts.setTaskId(line.getTextContent());
                        System.out.println("taskId: " + line.getTextContent());

                        NodeList username = element.getElementsByTagName("username");
                        line = (Element) username.item(0);
                        ts.setUsername(line.getTextContent());
                        System.out.println("username: " + line.getTextContent());

                        NodeList startDate = element.getElementsByTagName("startDate");
                        line = (Element) startDate.item(0);
                        String sDate1 = line.getTextContent();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Calendar c = Calendar.getInstance();
                            c.setTime(sdf.parse(sDate1));
                            c.add(Calendar.DATE, 1);

                            String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                            ts.setStartDate(date);
                            System.out.println("startDate: " + date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        NodeList endDate = element.getElementsByTagName("endDate");
                        line = (Element) endDate.item(0);
                        sDate1 = line.getTextContent();

                        try {
                            Calendar c = Calendar.getInstance();
                            c.setTime(sdf.parse(sDate1));
                            c.add(Calendar.DATE, 1);

                            String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                            ts.setEndDate(date);
                            System.out.println("endDate: " + date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        timesheets.add(ts);
                    }
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

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result != null) {
                //Log.i("Message", "Successful - " + taskDetails);
                if(timesheets.size() == 0){
                    mListView.setAdapter(null);

                }else{
                    TaskAdapter adapter = new TaskAdapter(mContext, timesheets, pmName);
                    mListView.setAdapter(adapter);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
