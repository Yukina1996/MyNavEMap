package com.example.wang.mynavemap;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, WhuRouteListner{

    private MapView mMapView;
    private SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();
    private LocationManager locationManager;

    private Button m_btnRoute;
    private Button m_btnDirect;
    private Button m_btnMulti;
    private Button m_btnClosestFacilities;
    private RouteTask routeTask;
    private GraphicsOverlay routeGraphicsOverlay;
    private GraphicsOverlay locationGraphicsOverlay;

    public Point stop1Loc;
    public Point stop2Loc;
    public Point stop3Loc;

    public int flag=1;


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onLocationChanged(Location location)
        {
            if (location != null)
            {
                Point gnssLoc = new Point(location.getLongitude(), location.getLatitude(), SPATIAL_REFERENCE);
                locationGraphicsOverlay.getGraphics().clear();
                SimpleMarkerSymbol redCircleSymbol = new
                        SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 10);
                Graphic graphic = new Graphic(gnssLoc, redCircleSymbol);
                locationGraphicsOverlay.getGraphics().add(graphic);
                mMapView.setViewpointGeometryAsync(gnssLoc);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArcGISTiledLayer tiledLayer = new
                ArcGISTiledLayer("http://116.62.240.95/ArcGIS/rest/services/whu07_new/whu07_new/MapServer");

        mMapView=(MapView)findViewById(R.id.mapView);
        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC , 30.52, 114.35, 16);
        map.getBasemap().getBaseLayers().clear();
        map.getBasemap().getBaseLayers().add(tiledLayer);
        mMapView.setMap(map);

        m_btnRoute=(Button)findViewById(R.id.m_btnRoute);
        m_btnRoute.setOnClickListener(this);

        m_btnDirect=(Button)findViewById(R.id.m_btnDirect);
        m_btnDirect.setOnClickListener(this);

        m_btnMulti=(Button)findViewById(R.id.m_btnMulti);
        m_btnMulti.setOnClickListener(this);

        m_btnClosestFacilities=(Button)findViewById(R.id.m_btnClosestFacilities);
        m_btnClosestFacilities.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , 5000, 1,
                locationListener);

        //在onCreat中获取在线地图服务NAServer
        //final String strrouteTask=
        //"http://116.62.240.95/arcgis/rest/services/whu/whunet/NAServer/Route";

        // 初始化一个地图覆盖物对象（图层），用于放置最优路径数据
        routeGraphicsOverlay = new GraphicsOverlay();
        //将覆盖物对象（图层）添加到地图上
        mMapView.getGraphicsOverlays().add(routeGraphicsOverlay);
        //初始化放置信息的对象
        locationGraphicsOverlay=new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(locationGraphicsOverlay);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.m_btnRoute:
                try {
                    final String strrouteTask = "http://116.62.240.95/ArcGIS/rest/services/whu07_new/whu07_new/NAServer/Route";
                    WhuRouteTask m_WhuRouteTask = new WhuRouteTask(strrouteTask,this);

                    Point stop1Loc = new Point(114.355,30.543 ,SPATIAL_REFERENCE);
                    Point stop2Loc = new Point(114.354,30.531 ,SPATIAL_REFERENCE);
                    m_WhuRouteTask.setStops(Arrays.asList(stop1Loc, stop2Loc));
                    m_WhuRouteTask.setReturnRoutes(true);
                    m_WhuRouteTask.setReturnDirections(false);
                    //异步执行最短路径查询服务
                    m_WhuRouteTask.solveRouteAsync();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.m_btnDirect:

                LayoutInflater inflater1 = getLayoutInflater();
                View view1 = inflater1.inflate(R.layout.direct_dialog,null);
                final AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this).setTitle("请输入起点和终点的经纬度")
                        .setIcon(android.R.drawable.ic_dialog_info).setView(view1).create();
                final Button btnSave = (Button)view1.findViewById(R.id.btn_direct_save);
                final EditText startlat = (EditText)view1.findViewById(R.id.text_start_lat);
                final EditText startlon = (EditText)view1.findViewById(R.id.text_start_lon);
                final EditText destlat = (EditText)view1.findViewById(R.id.text_dest_lat);
                final EditText destlon = (EditText)view1.findViewById(R.id.text_dest_lon);

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog1.dismiss();

                        final String strrouteTask = "http://116.62.240.95/ArcGIS/rest/services/whu/whunet/NAServer/Route";
                        WhuRouteTask m_WhuRouteTask = new WhuRouteTask(strrouteTask,MainActivity.this);

                        Point stop1Loc = new Point(Double.valueOf(startlon.getText().toString()),Double.valueOf(startlat.getText().toString()) ,SPATIAL_REFERENCE);
                        Point stop2Loc = new Point(Double.valueOf(destlon.getText().toString()),Double.valueOf(destlat.getText().toString()) ,SPATIAL_REFERENCE);
                        m_WhuRouteTask.setStops(Arrays.asList(stop1Loc, stop2Loc));
                        m_WhuRouteTask.setReturnRoutes(true);
                        m_WhuRouteTask.setReturnDirections(false);
                        //异步执行最短路径查询服务
                        m_WhuRouteTask.solveRouteAsync();
                    }
                });
                alertDialog1.show();

                break;
            case R.id.m_btnMulti:
//                LayoutInflater inflater2 = getLayoutInflater();
//                View view2 = inflater2.inflate(R.layout.multi_dialog,null);
//                final AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this).setTitle("请输入起点、经停点和终点的经纬度")
//                        .setIcon(android.R.drawable.ic_dialog_info)
//                        .setView(view2).create();
//                final Button btnSave1 = (Button) view2.findViewById(R.id.btn_multi_save);
//                final EditText startlat1 = (EditText)view2.findViewById(R.id.text_start_lat);
//                final EditText startlon1 = (EditText)view2.findViewById(R.id.text_start_lon);
//                final EditText destlat1 = (EditText)view2.findViewById(R.id.text_dest_lat);
//                final EditText destlon1 = (EditText)view2.findViewById(R.id.text_dest_lon);
//                final EditText stop1lat = (EditText)view2.findViewById(R.id.text_stop1_lat);
//                final EditText stop1lon = (EditText)view2.findViewById(R.id.text_stop1_lon);
//
//                btnSave1.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        alertDialog2.dismiss();
//
//                        final String strrouteTask = "http://116.62.240.95/ArcGIS/rest/services/whu/whunet/NAServer/Route";
//                        WhuRouteTask m_WhuRouteTask = new WhuRouteTask(strrouteTask,MainActivity.this);
//
//                        Point stop1Loc = new Point(Double.valueOf(startlon1.getText().toString()),Double.valueOf(startlat1.getText().toString()) ,SPATIAL_REFERENCE);
//                        Point stop2Loc = new Point(Double.valueOf(stop1lon.getText().toString()),Double.valueOf(stop1lat.getText().toString()) ,SPATIAL_REFERENCE);
//                        Point stop3Loc = new Point(Double.valueOf(destlon1.getText().toString()),Double.valueOf(destlat1.getText().toString()) ,SPATIAL_REFERENCE);
//                        m_WhuRouteTask.setStops(Arrays.asList(stop1Loc, stop2Loc,stop3Loc));
//                        m_WhuRouteTask.setReturnRoutes(true);
//                        m_WhuRouteTask.setReturnDirections(false);
//                        //异步执行最短路径查询服务
//                        m_WhuRouteTask.solveRouteAsync();
//                    }
//                });
//                alertDialog2.show();

                LayoutInflater inflater2 = getLayoutInflater();
                View view2 = inflater2.inflate(R.layout.multi_dialog,null);

                LayoutInflater inflater3 = getLayoutInflater();
                final View view3 = inflater3.inflate(R.layout.add_point_dialog,null);

                final AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this).setTitle("请输入起点、经停点和终点的经纬度")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(view2).create();
                alertDialog2.show();

                final List<Point> points = null;

                final Button btnSave1 = (Button) view2.findViewById(R.id.btn_multi_save);
                final Button btnAdd1 = (Button)view2.findViewById(R.id.btn_multi_add);
                //final Button btnConf = (Button)view3.findViewById(R.id.btn_multi_confirm);

                final EditText startlat1 = (EditText)view2.findViewById(R.id.text_start_lat);
                final EditText startlon1 = (EditText)view2.findViewById(R.id.text_start_lon);
                final EditText destlat1 = (EditText)view2.findViewById(R.id.text_dest_lat);
                final EditText destlon1 = (EditText)view2.findViewById(R.id.text_dest_lon);


                btnAdd1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new AlertDialog.Builder(MainActivity.this).setTitle("请输入经停点的经纬度")
                                .setView(view3)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final EditText stoplat = (EditText)view3.findViewById(R.id.text_stop1_lat);
                                        final EditText stoplon = (EditText)view3.findViewById(R.id.text_stop1_lon);
                                        stop2Loc = new Point(Double.valueOf(stoplon.getText().toString()),Double.valueOf(stoplat.getText().toString()) ,SPATIAL_REFERENCE);
//                                        flag++;
                                    }
                                }).show();
                    }
                });

                btnSave1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String strrouteTask = "http://116.62.240.95/ArcGIS/rest/services/whu/whunet/NAServer/Route";
                        WhuRouteTask m_WhuRouteTask = new WhuRouteTask(strrouteTask,MainActivity.this);

                        stop1Loc = new Point(Double.valueOf(startlon1.getText().toString()),Double.valueOf(startlat1.getText().toString()) ,SPATIAL_REFERENCE);

                        stop3Loc = new Point(Double.valueOf(destlon1.getText().toString()),Double.valueOf(destlat1.getText().toString()) ,SPATIAL_REFERENCE);
                        m_WhuRouteTask.setStops(Arrays.asList(stop1Loc, stop2Loc,stop3Loc));
                        m_WhuRouteTask.setReturnRoutes(true);
                        m_WhuRouteTask.setReturnDirections(false);
                        //异步执行最短路径查询服务
                        m_WhuRouteTask.solveRouteAsync();

                        alertDialog2.dismiss();
                    }
                });

                break;
            case R.id.m_btnClosestFacilities:

                //添加搜索最近设施点的响应事件
                break;
        }

    }

    @Override
    public void onRoute(WhuRoute route) {
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH_DOT_DOT ,
                Color.argb (255, 255, 255, 0), 2);
        Graphic graphic = new Graphic(route.getRouteGeometry(), lineSymbol);
        routeGraphicsOverlay.getGraphics().add(graphic);
        mMapView.setViewpointGeometryAsync(route.getRouteGeometry());
        Toast.makeText(this, "路线如图 "+route.m_strRoute, Toast.LENGTH_SHORT ).show();
    }
}




