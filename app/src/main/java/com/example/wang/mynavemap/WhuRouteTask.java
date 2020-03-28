package com.example.wang.mynavemap;

import android.os.AsyncTask;
import com.esri.arcgisruntime.geometry.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Administrator on 2017/9/25.
 */

public class WhuRouteTask extends AsyncTask<String, Object, String> {
    protected String  m_strNAServer;
    protected WhuRouteListner m_routelistner = null;
    protected List<Point> m_listStops;
    protected boolean bReturnDirections = false;
    protected boolean bReturnRoutes = true;

    public WhuRouteTask(String strNAServer, WhuRouteListner routelistner)
    {
        m_strNAServer = strNAServer;
        m_routelistner = routelistner;

    }

    public void setReturnDirections(boolean flag)
    {
        bReturnDirections = flag;
    }
    public void setReturnRoutes(boolean flag)
    {
        bReturnRoutes = flag;
    }
    public void setStops(List<Point> stops)
    {
        m_listStops = stops;
    }
    public void solveRouteAsync() {
        String strUrl = m_strNAServer + "/solve?";
        String strStops = "stops=";
        for(Point stop : m_listStops)
        {
            strStops+=stop.getX()+","+stop.getY()+";";
        }
        strUrl += strStops;
   /* strUrl += "&barriers=";
    strUrl += "&polylineBarriers=";
    strUrl += "&polygonBarriers=";
    strUrl += "&outSR=4326";
    strUrl += "&ignoreInvalidLocations=true";
    strUrl += "&accumulateAttributeNames=";
    strUrl += "&impedanceAttributeName=Length";
    strUrl += "&restrictionAttributeNames=";
    strUrl += "&attributeParameterValues=";
    strUrl += "&restrictUTurns=esriNFSBAllowBacktrack";
    strUrl += "&useHierarchy=false";*/
        strUrl += "&returnDirections="+bReturnDirections;
        strUrl += "&returnRoutes="+bReturnRoutes;
  /*strUrl += "&returnBarriers=false";
    strUrl += "&returnPolylineBarriers=false";
    strUrl += "&returnPolygonBarriers=false";
    strUrl += "&directionsLanguage=zh-CN";
    strUrl += "&directionsStyleName=";
    strUrl += "&outputLines=esriNAOutputLineTrueShape";
    strUrl += "&findBestSequence=false";
    strUrl += "&preserveFirstStop=true";
    strUrl += "&preserveLastStop=true";
    strUrl += "&useTimeWindows=false";
    strUrl += "&startTime=";
    strUrl += "&outputGeometryPrecision=";
    strUrl += "&outputGeometryPrecisionUnits = esriMeters";
    strUrl += "&directionsTimeAttributeName=";
    strUrl += "&directionsLengthUnits=esriNAUMiles";
   */
        strUrl += "&f=json";
        String[] urls = {strUrl};
        this.execute(urls);
    }

    @Override
    protected String doInBackground(String... params) {
        final StringBuffer resultBuffer = new StringBuffer();
        HttpURLConnection httpURLConnection = null;
        try {

            URL url = new URL(params[0]);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.connect();


            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;

            String tempLine = null;
            //响应失败
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }

            try {
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }

            } finally {

                if (reader != null) {
                    reader.close();
                }

                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            resultBuffer.append("other:" + e.toString());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        return  resultBuffer.toString();
    }

    @Override
    protected void onPostExecute(String strBuffer) {
        super.onPostExecute(strBuffer);
        if(m_routelistner!=null) {
            m_routelistner.onRoute(new WhuRoute(strBuffer));
        }
    }

}
