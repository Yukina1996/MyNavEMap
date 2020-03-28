package com.example.wang.mynavemap;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Wang on 2017/9/22.
 */

public class WhuRoute {
    public String m_strRoute;
    private Polyline routeGeometry;

    WhuRoute(String strRoute) {
        m_strRoute = "";
        try {
            JSONObject routesObject = new JSONObject(strRoute).getJSONObject("routes");
            JSONObject spatialObject = routesObject.getJSONObject("spatialReference");
            JSONArray featuresArray = routesObject.getJSONArray("features");

            int wkid = spatialObject.getInt("wkid");
            SpatialReference SPATIAL_REFERENCE = SpatialReference.create(wkid);
            PointCollection points = new PointCollection(SPATIAL_REFERENCE);

            for (int i = 0; i < featuresArray.length(); i++) {
                JSONObject jfeature = featuresArray.getJSONObject(i);
                if (jfeature.has("geometry")) {
                    JSONObject geometryObject = jfeature.getJSONObject("geometry");
                    JSONArray pathsArray = geometryObject.getJSONArray("paths").getJSONArray(0);
                    for (int j = 0; j < pathsArray.length(); j++) {
                        JSONArray pointArray = pathsArray.getJSONArray(j);
                        points.add(new Point(pointArray.getDouble(0), pointArray.getDouble(1)));
                    }
                    // create the polyline from the point collection
                    routeGeometry = new Polyline(points);
                    //m_strRoute+=pathsArray;
                }

            }

            // create and add points to the point collection

        } catch (JSONException e) {
            e.printStackTrace();
            m_strRoute = e.getMessage();
        }

    }

    Polyline getRouteGeometry()
    {
        return routeGeometry;
    }
}
