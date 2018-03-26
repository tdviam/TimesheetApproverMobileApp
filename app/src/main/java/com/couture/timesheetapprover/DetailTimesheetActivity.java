package com.couture.timesheetapprover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DetailTimesheetActivity extends AppCompatActivity {

    private TextView subject, dateRange, totalHour, mon, monHours, monDesc;
    private TextView tue, tueHours, tueDesc;
    private TextView wed, wedHours, wedDesc;
    private TextView thu, thuHours, thuDesc;
    private TextView fri, friHours, friDesc;
    private TextView sat, satHours, satDesc;
    private TextView sun, sunHours, sunDesc;
    private Button btnApprove, btnReject;
    private Timesheet ts = new Timesheet();

    private String pmName, username, taskId, startDate, endDate;

    private static String URL = "https://10.140.0.4:9443/teamworks/webservices/CCSYS/CISWebservice.tws";
    private static final HostnameVerifier DUMMY_VERIFIER = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_timesheet);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        pmName = getIntent().getStringExtra("PM_NAME");
        username = getIntent().getStringExtra("USER_NAME");
        startDate = getIntent().getStringExtra("START_DATE");
        endDate = getIntent().getStringExtra("END_DATE");
        taskId = getIntent().getStringExtra("TASK_ID");

        //System.out.println("taskID " + taskId);
        //System.out.println("start date " + startDate);

        ShowTimesheetDetails openConnection = new ShowTimesheetDetails(getApplicationContext());
        openConnection.execute();
    }

    private class ShowTimesheetDetails extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;

        public ShowTimesheetDetails (Context context){
            mContext = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                java.net.URL url = new URL(URL);

                HttpsURLConnection connection = (HttpsURLConnection) url
                        .openConnection();
                connection.setHostnameVerifier(DUMMY_VERIFIER);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                connection.setRequestProperty("SOAPAction",
                        "http://CCSYS/CISWebservice.tws/populateTimesheetPM");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //XML
                String reqXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cis=\"http://CCSYS/CISWebservice.tws\">\n" +
                        "<soapenv:Header/>\n" +
                        "<soapenv:Body>\n" +
                        "<cis:populateTimesheetPM>\n" +
                        "<cis:userName>" + username + "</cis:userName>\n" +
                        "<cis:pmName>" + pmName + "</cis:pmName>\n" +
                        "<cis:startDate>" + startDate + "</cis:startDate>\n" +
                        "<cis:endDate>" + endDate + "</cis:endDate>\n" +
                        "</cis:populateTimesheetPM>\n" +
                        "</soapenv:Body>\n" +
                        "</soapenv:Envelope>";
                System.out.println("reqXML " + reqXML);
                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(reqXML);
                wr.flush();

                int responseCode = connection.getResponseCode();
                System.out.println("Code ... " + responseCode);
                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

                    NodeList tasks = doc.getElementsByTagName("timesheetDetails");
                    Element elementTask = (Element) tasks.item(0);

                    NodeList items = elementTask.getChildNodes();
                    System.out.println("node list" + items.getLength());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    for (int i = 0; i < items.getLength(); i++) {
                        Element item = (Element) items.item(i);

                        if(item.getNodeName().equals("username")){
                            System.out.println("username " + item.getTextContent());
                            ts.setUsername(item.getTextContent());
                        }else if(item.getNodeName().equals("dateRange")) {

                            Element startDate = (Element) item.getFirstChild();
                            String sDate1 = startDate.getTextContent();
                            Calendar c = Calendar.getInstance();
                            try {
                                c.setTime(sdf.parse(sDate1));
                                c.add(Calendar.DATE, 1);

                                String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                                ts.setStartDate(date);
                                System.out.println("startDate: " + date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Element endDate = (Element) startDate.getNextSibling();
                            String sDate2 = endDate.getTextContent();
                            //Calendar c = Calendar.getInstance();
                            try {
                                c.setTime(sdf.parse(sDate2));
                                c.add(Calendar.DATE, 1);

                                String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                                ts.setEndDate(date);
                                System.out.println("endDate: " + date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }else if(item.getNodeName().equals("mon")){
                            DailyLog mon;

                            mon = getDailyLog(item, sdf);
                            //System.out.println("mon: " + mon.getDate());
                            ts.setMon(mon);
                        }else if(item.getNodeName().equals("tue")){
                            DailyLog tue;

                            tue = getDailyLog(item, sdf);

                            ts.setTue(tue);
                        }else if(item.getNodeName().equals("wed")){
                            DailyLog wed;

                            wed = getDailyLog(item, sdf);

                            ts.setWed(wed);
                        }else if(item.getNodeName().equals("thu")){
                            DailyLog thu;

                            thu = getDailyLog(item, sdf);

                            ts.setThu(thu);
                        }else if(item.getNodeName().equals("fri")){
                            DailyLog fri;

                            fri = getDailyLog(item, sdf);

                            ts.setFri(fri);
                        }else if(item.getNodeName().equals("sat")){
                            DailyLog sat;

                            sat = getDailyLog(item, sdf);

                            ts.setSat(sat);
                        }else if(item.getNodeName().equals("sun")){
                            DailyLog sun;

                            sun = getDailyLog(item, sdf);

                            ts.setSun(sun);
                        }else if(item.getNodeName().equals("totalRegularHours")){
                            System.out.println("totalRegularHours " + item.getTextContent());
                            ts.setTotalHours(item.getTextContent());
                        }

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
                subject = (TextView) findViewById(R.id.subject);
                subject.setText(ts.getUsername());

                dateRange = (TextView) findViewById(R.id.dateRange);
                dateRange.setText(ts.getStartDate() + " - " + ts.getEndDate());

                totalHour =  (TextView) findViewById(R.id.totalHours);
                totalHour.setText(ts.getTotalHours());

                if(ts.getMon() != null) {
                    mon = (TextView) findViewById(R.id.mon);
                    mon.setText(ts.getMon().getDate());

                    monHours = (TextView) findViewById(R.id.monHours);
                    monHours.setText(ts.getMon().getHours());

                    monDesc = (TextView) findViewById(R.id.monDesc);
                    monDesc.setText(ts.getMon().getDescription());
                }

                if(ts.getTue() != null) {
                    tue = (TextView) findViewById(R.id.tue);
                    tue.setText(ts.getTue().getDate());

                    tueHours = (TextView) findViewById(R.id.tueHours);
                    tueHours.setText(ts.getTue().getHours());

                    tueDesc = (TextView) findViewById(R.id.tueDesc);
                    tueDesc.setText(ts.getTue().getDescription());
                }

                if(ts.getWed() != null) {
                    wed = (TextView) findViewById(R.id.wed);
                    wed.setText(ts.getWed().getDate());

                    wedHours = (TextView) findViewById(R.id.wedHours);
                    wedHours.setText(ts.getWed().getHours());

                    wedDesc = (TextView) findViewById(R.id.wedDesc);
                    wedDesc.setText(ts.getWed().getDescription());
                }
                if(ts.getThu() != null) {
                    thu = (TextView) findViewById(R.id.thu);
                    thu.setText(ts.getThu().getDate());

                    thuHours = (TextView) findViewById(R.id.thuHours);
                    thuHours.setText(ts.getThu().getHours());

                    thuDesc = (TextView) findViewById(R.id.thuDesc);
                    thuDesc.setText(ts.getThu().getDescription());
                }

                if(ts.getFri() != null) {
                    fri = (TextView) findViewById(R.id.fri);
                    fri.setText(ts.getFri().getDate());

                    friHours = (TextView) findViewById(R.id.friHours);
                    friHours.setText(ts.getFri().getHours());

                    friDesc = (TextView) findViewById(R.id.friDesc);
                    friDesc.setText(ts.getFri().getDescription());
                }

                if(ts.getSat() != null) {
                    sat = (TextView) findViewById(R.id.sat);
                    sat.setText(ts.getSat().getDate());

                    satHours = (TextView) findViewById(R.id.satHours);
                    satHours.setText(ts.getSat().getHours());

                    satDesc = (TextView) findViewById(R.id.satDesc);
                    satDesc.setText(ts.getSat().getDescription());
                }

                if(ts.getSun() != null) {
                    sun = (TextView) findViewById(R.id.sun);
                    sun.setText(ts.getSun().getDate());

                    sunHours = (TextView) findViewById(R.id.sunHours);
                    sunHours.setText(ts.getSun().getHours());

                    sunDesc = (TextView) findViewById(R.id.sunDesc);
                    sunDesc.setText(ts.getSun().getDescription());
                }


                btnApprove = (Button) findViewById(R.id.btnApprove);
                btnApprove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CompleteTask completeTask = new CompleteTask("APPROVED");
                        completeTask.execute();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("PM_NAME", pmName);
                        startActivity(intent);
                    }
                });

                btnReject = (Button) findViewById(R.id.btnReject);
                btnReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CompleteTask completeTask = new CompleteTask("REJECTED");
                        completeTask.execute();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("PM_NAME", pmName);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private class CompleteTask extends AsyncTask<Void, Void, Boolean> {
        private String flowStatus;

        public CompleteTask ( String flowStatus){

            this.flowStatus = flowStatus;
        }

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
                        "http://CCSYS/CISWebservice.tws/completeTasks");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                //XML
                String reqXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cis=\"http://CCSYS/CISWebservice.tws\">\n" +
                        "<soapenv:Header/>\n" +
                        "<soapenv:Body>\n" +
                        "<cis:completeTasks>\n" +
                        "<cis:status>" + flowStatus + "</cis:status>\n" +
                        "<cis:taskId>" + taskId + "</cis:taskId>\n" +
                        "<cis:pmName>" + pmName + "</cis:pmName>\n" +
                        "<cis:username>" + username + "</cis:username>\n" +
                        "<cis:startDate>" + startDate + "</cis:startDate>\n" +
                        "<cis:endDate>" + endDate + "</cis:endDate>\n" +
                        "</cis:completeTasks>\n" +
                        "</soapenv:Body>\n" +
                        "</soapenv:Envelope>";

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(reqXML);
                wr.flush();

                int responseCode = connection.getResponseCode();
                System.out.println("Code ... " + responseCode);


                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }

    }

    private DailyLog getDailyLog(Element item, SimpleDateFormat sdf){

        DailyLog log = new DailyLog();

        NodeList date = item.getElementsByTagName("date");
        Element line = (Element) date.item(0);
        String sDate1 = line.getTextContent();
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(sDate1));
            c.add(Calendar.DATE, 1);

            String sDate = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
            //System.out.println("Date: " + sDate);
            log.setDate(sDate);
            //System.out.println("Date: " + log.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        NodeList hours = item.getElementsByTagName("regularHours");
        line = (Element) hours.item(0);
        log.setHours(line.getTextContent());
        //System.out.println("hours: " + log.getHours());

        NodeList desc = item.getElementsByTagName("description");
        line = (Element) desc.item(0);
        log.setDescription(line.getTextContent());
        //System.out.println("description: " + line.getTextContent());

        return log;
    }
}
