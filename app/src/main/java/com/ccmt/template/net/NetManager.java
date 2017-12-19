package com.ccmt.template.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.NetUtil;
import com.ccmt.library.util.ThreadManager;
import com.ccmt.template.traffic.TrafficManagerFactory;
import com.ccmt.template.util.CommonUtil;
import com.ccmt.template.util.IOUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author myx
 *         by 2017-07-21
 */
public class NetManager {

    @SuppressWarnings("unused")
    private static final String START_TIME_INTERNET_WIFI = "start_time_internet_wifi";
    @SuppressWarnings("unused")
    private static final String START_TIME_INTERNET_MOBILE = "start_time_internet_mobile";
    private static final long START_TIME_VALUE = 6000;
    private Map<String, Long> mAllForceRquestStartTimes;
    private List<Integer> mAllForceRquestCapabilities;
    private List<Integer> mAllForceRquestTransportTypes;
    private List<String> mAllForceRquestStartTimeUrls;

    private NetManager() {
        mAllForceRquestStartTimes = new HashMap<>();
        mAllForceRquestCapabilities = new ArrayList<>();
        mAllForceRquestTransportTypes = new ArrayList<>();
        mAllForceRquestStartTimeUrls = new ArrayList<>();
    }

    @SuppressWarnings("WeakerAccess")
    public interface IOnForceRquestListener {
        /**
         * 获取到的数据
         *
         * @param bs
         */
        @SuppressWarnings("JavaDoc")
        void onForceRquestSuccess(byte[] bs);

        /**
         * 请求超时
         */
        void onForceRquestTimeout();
    }

    @SuppressWarnings("unused")
    public static NetManager createNetManager() {
        LruMap lruMap = LruMap.getInstance();
        String name = NetManager.class.getName();
        NetManager netManager = (NetManager) lruMap.get(name);
        if (netManager == null) {
            netManager = new NetManager();
            lruMap.put(name, netManager);
        }
        return netManager;
    }

    @SuppressWarnings("unused")
    public int getCurrentNetType(Context context) {
        return NetUtil.getNetworkType(context);
    }

    /**
     * 用移动数据访问网络
     *
     * @param context
     */
    @SuppressWarnings("JavaDoc")
    private void forceRquest(Context context, String url, int capability,
                             int transportType, IOnForceRquestListener onForceRquestListener) {
        if (mAllForceRquestCapabilities.contains(capability)
                && mAllForceRquestTransportTypes.contains(transportType)
                && mAllForceRquestStartTimeUrls.contains(url)) {
            LogUtil.i("已经处于请求状态,等上次请求结束后才能再发起新的请求.");
            return;
        }

        boolean wifiConnected = NetUtil.isWIFIConnected(context);
        boolean mobileConnected = NetUtil.isMobileConnected(context);
        LogUtil.i("wifiConnected -> " + wifiConnected);
        LogUtil.i("mobileConnected -> " + mobileConnected);

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
                .getUidRxBytesWifi(getUid(context));
        long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
                .getUidRxBytesMobile(getUid(context));
        long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
                .getUidTxBytesWifi(getUid(context));
        long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
                .getUidTxBytesMobile(getUid(context));
        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);

        NetworkRequest.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            builder = new NetworkRequest.Builder();
            builder.addCapability(capability);

            String key = "" + capability + transportType + url;

            // 强制用移动数据
            builder.addTransportType(transportType);
            NetworkRequest build = builder.build();
            final boolean[] flag = {false};
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = 21)
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    LogUtil.i("onAvailable()");
                    if (!flag[0]) {
                        synchronized (NetManager.this) {
                            if (!flag[0]) {
                                flag[0] = true;
                            } else {
                                connectivityManager.unregisterNetworkCallback(this);
                                mAllForceRquestCapabilities.remove(Integer.valueOf(capability));
                                mAllForceRquestTransportTypes.remove(Integer.valueOf(transportType));
                                mAllForceRquestStartTimeUrls.remove(url);
                                mAllForceRquestStartTimes.remove(key);
                                return;
                            }
                        }
                    } else {
                        connectivityManager.unregisterNetworkCallback(this);
                        mAllForceRquestCapabilities.remove(Integer.valueOf(capability));
                        mAllForceRquestTransportTypes.remove(Integer.valueOf(transportType));
                        mAllForceRquestStartTimeUrls.remove(url);
                        mAllForceRquestStartTimes.remove(key);
                        return;
                    }
                    HttpURLConnection httpURLConnection;
                    try {
                        httpURLConnection = (HttpURLConnection) network
                                .openConnection(new URL(url));
                        httpURLConnection.connect();
                        int responseCode = httpURLConnection.getResponseCode();
                        LogUtil.i("responseCode -> " + responseCode);
                        byte[] bs = IOUtil.toByteArrayNew(httpURLConnection.getInputStream());
                        LogUtil.i("bs.length -> " + bs.length);

                        long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
                                .getUidRxBytesWifi(getUid(context));
                        long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
                                .getUidRxBytesMobile(getUid(context));
                        long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
                                .getUidTxBytesWifi(getUid(context));
                        long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
                                .getUidTxBytesMobile(getUid(context));
                        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
                        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
                        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
                        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);

                        ThreadManager.post(() -> {
                            if (onForceRquestListener != null) {
                                onForceRquestListener.onForceRquestSuccess(bs);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @SuppressLint("NewApi")
                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    LogUtil.i("onCapabilitiesChanged()");
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    LogUtil.i("onLinkPropertiesChanged()");
                }

                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    LogUtil.i("onLosing()");
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    LogUtil.i("onLost()");
                }
            };
            CommonUtil.listAdd(mAllForceRquestCapabilities, capability);
            CommonUtil.listAdd(mAllForceRquestTransportTypes, transportType);
            CommonUtil.listAdd(mAllForceRquestStartTimeUrls, url);
            mAllForceRquestStartTimes.put(key, System.currentTimeMillis());
            connectivityManager.requestNetwork(build, networkCallback);
            ThreadManager.executeAsyncTask(() -> {
                long end = System.currentTimeMillis() - mAllForceRquestStartTimes.get(key);
                while (end < START_TIME_VALUE) {
                    if (flag[0]) {
                        synchronized (NetManager.this) {
                            if (flag[0]) {
                                return;
                            }
                        }
                    }
                    end = System.currentTimeMillis() - mAllForceRquestStartTimes.get(key);
                }

                // 超时
                ThreadManager.post(() -> {
                    if (onForceRquestListener != null) {
                        onForceRquestListener.onForceRquestTimeout();
                    }
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                    mAllForceRquestCapabilities.remove(Integer.valueOf(capability));
                    mAllForceRquestTransportTypes.remove(Integer.valueOf(transportType));
                    mAllForceRquestStartTimeUrls.remove(url);
                    mAllForceRquestStartTimes.remove(key);
                });
            });
        }
    }

    @SuppressWarnings("unused")
    public void forceRquestInternetMobile(Context context, String url, IOnForceRquestListener onForceRquestListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            forceRquest(context, url, NetworkCapabilities.NET_CAPABILITY_INTERNET,
                    NetworkCapabilities.TRANSPORT_CELLULAR, onForceRquestListener);
        }
    }

//    @SuppressWarnings("unused")
//    public void forceRquestInternetVpn(Context context, String url, IOnForceRquestListener onForceRquestListener) {
//        if (Build.VERSION.SDK_INT >= 21) {
//            forceRquest(context, url, NetworkCapabilities.NET_CAPABILITY_INTERNET,
//                    NetworkCapabilities.TRANSPORT_VPN, onForceRquestListener);
//        }
//    }

    @SuppressWarnings("unused")
    public void forceRquestInternetVpn(Context context, String url, IOnForceRquestListener onForceRquestListener) {
        boolean wifiConnected = NetUtil.isWIFIConnected(context);
        boolean mobileConnected = NetUtil.isMobileConnected(context);
        LogUtil.i("wifiConnected -> " + wifiConnected);
        LogUtil.i("mobileConnected -> " + mobileConnected);

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
                .getUidRxBytesWifi(getUid(context));
        long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
                .getUidRxBytesMobile(getUid(context));
        long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
                .getUidTxBytesWifi(getUid(context));
        long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
                .getUidTxBytesMobile(getUid(context));
        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);

        if (Build.VERSION.SDK_INT >= 23) {
            Network[] ns = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network network : ns) {
                networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo.getType() != ConnectivityManager.TYPE_VPN) {
                    continue;
                }
                if (!networkInfo.isConnected()) {
                    continue;
                }
                ThreadManager.executeAsyncTask(() -> {
                    HttpURLConnection httpURLConnection = null;
                    try {
                        httpURLConnection = (HttpURLConnection) network
                                .openConnection(new URL(url));
                        httpURLConnection.connect();
                        int responseCode = httpURLConnection.getResponseCode();
                        LogUtil.i("responseCode -> " + responseCode);
                        byte[] bs = IOUtil.toByteArrayNew(httpURLConnection.getInputStream());
                        LogUtil.i("bs.length -> " + bs.length);

                        ThreadManager.post(() -> {
                            long uidRxBytesWifi2 = TrafficManagerFactory.createTrafficManager(context)
                                    .getUidRxBytesWifi(getUid(context));
                            long uidRxBytesMobile2 = TrafficManagerFactory.createTrafficManager(context)
                                    .getUidRxBytesMobile(getUid(context));
                            long uidTxBytesWifi2 = TrafficManagerFactory.createTrafficManager(context)
                                    .getUidTxBytesWifi(getUid(context));
                            long uidTxBytesMobile2 = TrafficManagerFactory.createTrafficManager(context)
                                    .getUidTxBytesMobile(getUid(context));
                            LogUtil.i("uidRxBytesWifi2 -> " + uidRxBytesWifi2);
                            LogUtil.i("uidRxBytesMobile2 -> " + uidRxBytesMobile2);
                            LogUtil.i("uidTxBytesWifi2 -> " + uidTxBytesWifi2);
                            LogUtil.i("uidTxBytesMobile2 -> " + uidTxBytesMobile2);
                            if (onForceRquestListener != null) {
                                onForceRquestListener.onForceRquestSuccess(bs);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

//    /**
//     * 循环Network方式
//     *
//     * @param context
//     */
//    @SuppressWarnings({"deprecation", "unused", "StatementWithEmptyBody", "TryWithIdenticalCatches", "JavaDoc"})
//    public void test1(Context context, ImageView imageView) {
////        LogUtil.showScreenInfo((Activity) context);
//
//        boolean wifiConnected = NetUtil.isWIFIConnected(context);
//        boolean mobileConnected = NetUtil.isMobileConnected(context);
//        LogUtil.i("wifiConnected -> " + wifiConnected);
//        LogUtil.i("mobileConnected -> " + mobileConnected);
//
//        long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                .getUidRxBytesWifi(getUid(context));
//        long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                .getUidRxBytesMobile(getUid(context));
//        long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                .getUidTxBytesWifi(getUid(context));
//        long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                .getUidTxBytesMobile(getUid(context));
//        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
//        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
//        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
//        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);
//
//        ConnectivityManager connectivityManager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (Build.VERSION.SDK_INT >= 21) {
//            Network[] networks = connectivityManager.getAllNetworks();
//            if (networks == null || networks.length == 0) {
//                return;
//            }
//            NetworkInfo networkInfo;
//            NetworkRequest.Builder builder;
//            final boolean[] flag = {false};
//            for (Network network : networks) {
//                networkInfo = connectivityManager.getNetworkInfo(network);
//                LogUtil.i("networkInfo -> " + networkInfo);
//                if (networkInfo != null) {
//                    builder = new NetworkRequest.Builder();
//                    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
//
//                    // 强制用移动数据
//                    builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
//
//                    // 如果是用wifi访问网络,参数传wifi的ssid.
//                    builder.setNetworkSpecifier(null);
//
//                    NetworkRequest build = builder.build();
//                    connectivityManager.requestNetwork(build, new ConnectivityManager.NetworkCallback() {
//                        @SuppressWarnings("TryWithIdenticalCatches")
//                        @RequiresApi(api = 21)
//                        @Override
//                        public void onAvailable(Network network) {
//                            super.onAvailable(network);
//                            LogUtil.i("onAvailable()");
//                            if (!flag[0]) {
//                                flag[0] = true;
//                            } else {
//                                connectivityManager.unregisterNetworkCallback(this);
//                                return;
//                            }
////                            connectivityManager.unregisterNetworkCallback(this);
//                            HttpURLConnection httpURLConnection;
//                            try {
//                                httpURLConnection = (HttpURLConnection) network
//                                        .openConnection(new URL("http://img4.imgtn.bdimg.com/it/u=620832562,805228098&fm=200&gp=0.jpg/"));
////                                httpURLConnection.setRequestMethod("POST");
//                                httpURLConnection.setDoInput(true);
////                                httpURLConnection.setDoOutput(true);
//                                httpURLConnection.setUseCaches(false);
//                                httpURLConnection.connect();
////                                int responseCode = httpURLConnection.getResponseCode();
////                                LogUtil.i("responseCode -> " + responseCode);
//                                byte[] bs = IOUtil.toByteArrayNew(httpURLConnection.getInputStream());
//                                LogUtil.i("bs.length -> " + bs.length);
//
//                                Bitmap bitmap = BitmapFactory.decodeByteArray(bs, 0, bs.length);
//                                ThreadManager.post(() -> imageView.setImageBitmap(bitmap));
////                                ThreadManager.post(() -> {
////                                    String str;
////                                    BufferedReader br = null;
////                                    try {
////                                        str = new String(bs, 0, bs.length, "utf-8");
////                                        StringReader sr = new StringReader(str);
////                                        br = new BufferedReader(sr);
////                                        String line = null;
////                                        while ((line = br.readLine()) != null) {
////                                            LogUtil.i("line -> " + line);
////                                        }
////                                    } catch (UnsupportedEncodingException e) {
////                                        e.printStackTrace();
////                                    } catch (IOException e) {
////                                        e.printStackTrace();
////                                    } finally {
////                                        if (br != null) {
////                                            try {
////                                                br.close();
////                                            } catch (IOException e) {
////                                                e.printStackTrace();
////                                            }
////                                        }
////                                    }
////                                });
//
//                                long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                                        .getUidRxBytesWifi(getUid(context));
//                                long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                                        .getUidRxBytesMobile(getUid(context));
//                                long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                                        .getUidTxBytesWifi(getUid(context));
//                                long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                                        .getUidTxBytesMobile(getUid(context));
//                                LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
//                                LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
//                                LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
//                                LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @SuppressLint("NewApi")
//                        @Override
//                        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
//                            super.onCapabilitiesChanged(network, networkCapabilities);
//                            LogUtil.i("onCapabilitiesChanged()");
//                        }
//
//                        @Override
//                        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
//                            super.onLinkPropertiesChanged(network, linkProperties);
//                            LogUtil.i("onLinkPropertiesChanged()");
//                        }
//
//                        @Override
//                        public void onLosing(Network network, int maxMsToLive) {
//                            super.onLosing(network, maxMsToLive);
//                            LogUtil.i("onLosing()");
//                        }
//
//                        @Override
//                        public void onLost(Network network) {
//                            super.onLost(network);
//                            LogUtil.i("onLost()");
//                        }
//                    });
//
////                    ThreadManager.executeAsyncTask(() -> {
////                        HttpURLConnection httpURLConnection;
////                        try {
////                            httpURLConnection = (HttpURLConnection) network
////                                    .openConnection(new URL("https://www.baidu.com/"));
////                            httpURLConnection.connect();
////                            int responseCode = httpURLConnection.getResponseCode();
////                            LogUtil.i("responseCode -> " + responseCode);
////                            byte[] bs = IOUtil.toByteArrayNew(httpURLConnection.getInputStream());
////                            LogUtil.i("bs.length -> " + bs.length);
////
////                            ThreadManager.post(() -> {
////                                String str;
////                                BufferedReader br = null;
////                                try {
////                                    str = new String(bs, 0, bs.length, "utf-8");
////                                    StringReader sr = new StringReader(str);
////                                    br = new BufferedReader(sr);
////                                    String line;
////                                    while ((line = br.readLine()) != null) {
////                                        LogUtil.i("line -> " + line);
////                                    }
////                                } catch (UnsupportedEncodingException e) {
////                                    e.printStackTrace();
////                                } catch (IOException e) {
////                                    e.printStackTrace();
////                                } finally {
////                                    if (br != null) {
////                                        try {
////                                            br.close();
////                                        } catch (IOException e) {
////                                            e.printStackTrace();
////                                        }
////                                    }
////                                }
////
////                                long uidRxBytesWifi1 = TrafficManagerFactory.createTrafficManager(context)
////                                        .getUidRxBytesWifi(getUid(context));
////                                long uidRxBytesMobile1 = TrafficManagerFactory.createTrafficManager(context)
////                                        .getUidRxBytesMobile(getUid(context));
////                                long uidTxBytesWifi1 = TrafficManagerFactory.createTrafficManager(context)
////                                        .getUidTxBytesWifi(getUid(context));
////                                long uidTxBytesMobile1 = TrafficManagerFactory.createTrafficManager(context)
////                                        .getUidTxBytesMobile(getUid(context));
////                                LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi1);
////                                LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile1);
////                                LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi1);
////                                LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile1);
////                            });
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////                    });
//                }
//            }
//        } else {
//            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
//            if (networkInfos == null || networkInfos.length == 0) {
//                return;
//            }
//            for (NetworkInfo networkInfo : networkInfos) {
//                LogUtil.i("networkInfo -> " + networkInfo);
//                if (networkInfo != null && networkInfo.isConnected()) {
//                    int type = networkInfo.getType();
//                    if (type == ConnectivityManager.TYPE_MOBILE
//                            || type == ConnectivityManager.TYPE_MOBILE_DUN
//                            || type == ConnectivityManager.TYPE_MOBILE_HIPRI
//                            || type == ConnectivityManager.TYPE_MOBILE_MMS
//                            || type == ConnectivityManager.TYPE_MOBILE_SUPL) {
//
//                    }
//                }
//            }
//        }
//    }

//    /**
//     * 非循环Network,直接请求网络方式.
//     *
//     * @param context
//     */
//    @SuppressWarnings({"unused", "JavaDoc"})
//    public void test2(Context context, ImageView imageView) {
//        boolean wifiConnected = NetUtil.isWIFIConnected(context);
//        boolean mobileConnected = NetUtil.isMobileConnected(context);
//        LogUtil.i("wifiConnected -> " + wifiConnected);
//        LogUtil.i("mobileConnected -> " + mobileConnected);
//
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                .getUidRxBytesWifi(getUid(context));
//        long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                .getUidRxBytesMobile(getUid(context));
//        long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                .getUidTxBytesWifi(getUid(context));
//        long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                .getUidTxBytesMobile(getUid(context));
//        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
//        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
//        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
//        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);
//
//        NetworkRequest.Builder builder;
//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            builder = new NetworkRequest.Builder();
//            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
//
//            // 强制用移动数据
//            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
//            NetworkRequest build = builder.build();
//            connectivityManager.requestNetwork(build, new ConnectivityManager.NetworkCallback() {
//                @SuppressWarnings("TryWithIdenticalCatches")
//                @RequiresApi(api = 21)
//                @Override
//                public void onAvailable(Network network) {
//                    super.onAvailable(network);
//                    connectivityManager.unregisterNetworkCallback(this);
//                    HttpURLConnection httpURLConnection;
//                    try {
//                        httpURLConnection = (HttpURLConnection) network
//                                .openConnection(new URL("https://www.baidu.com/"));
//                        httpURLConnection.connect();
//                        int responseCode = httpURLConnection.getResponseCode();
//                        LogUtil.i("responseCode -> " + responseCode);
//                        byte[] bs = IOUtil.toByteArrayNew(httpURLConnection.getInputStream());
//                        LogUtil.i("bs.length -> " + bs.length);
//
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(bs, 0, bs.length);
//                        ThreadManager.post(() -> imageView.setImageBitmap(bitmap));
////                        ThreadManager.post(() -> {
////                            String str;
////                            BufferedReader br = null;
////                            try {
////                                str = new String(bs, 0, bs.length, "utf-8");
////                                StringReader sr = new StringReader(str);
////                                br = new BufferedReader(sr);
////                                String line = null;
////                                while ((line = br.readLine()) != null) {
////                                    LogUtil.i("line -> " + line);
////                                }
////                            } catch (UnsupportedEncodingException e) {
////                                e.printStackTrace();
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            } finally {
////                                if (br != null) {
////                                    try {
////                                        br.close();
////                                    } catch (IOException e) {
////                                        e.printStackTrace();
////                                    }
////                                }
////                            }
////                        });
//
//                        long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                                .getUidRxBytesWifi(getUid(context));
//                        long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                                .getUidRxBytesMobile(getUid(context));
//                        long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                                .getUidTxBytesWifi(getUid(context));
//                        long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                                .getUidTxBytesMobile(getUid(context));
//                        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
//                        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
//                        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
//                        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//    }

//    /**
//     * 创建Url对象打开网络连接方式
//     *
//     * @param context
//     */
//    @SuppressWarnings({"JavaDoc", "unused"})
//    private void test3(Context context, ImageView imageView) {
//        boolean wifiConnected = NetUtil.isWIFIConnected(context);
//        boolean mobileConnected = NetUtil.isMobileConnected(context);
//        LogUtil.i("wifiConnected -> " + wifiConnected);
//        LogUtil.i("mobileConnected -> " + mobileConnected);
//
//        long uidRxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                .getUidRxBytesWifi(getUid(context));
//        long uidRxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                .getUidRxBytesMobile(getUid(context));
//        long uidTxBytesWifi = TrafficManagerFactory.createTrafficManager(context)
//                .getUidTxBytesWifi(getUid(context));
//        long uidTxBytesMobile = TrafficManagerFactory.createTrafficManager(context)
//                .getUidTxBytesMobile(getUid(context));
//        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
//        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
//        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
//        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);
//        ThreadManager.executeAsyncTask(() -> {
//            HttpURLConnection httpURLConnection;
//            try {
//                httpURLConnection = (HttpURLConnection) new URL("http://img4.imgtn.bdimg.com/it/u=620832562,805228098&fm=200&gp=0.jpg/")
//                        .openConnection();
////                                httpURLConnection.setRequestMethod("POST");
//                httpURLConnection.setDoInput(true);
////                                httpURLConnection.setDoOutput(true);
//                httpURLConnection.setUseCaches(false);
//                httpURLConnection.connect();
////                                int responseCode = httpURLConnection.getResponseCode();
////                                LogUtil.i("responseCode -> " + responseCode);
//                byte[] bs = IOUtil.toByteArrayNew(httpURLConnection.getInputStream());
//                LogUtil.i("bs.length -> " + bs.length);
//
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bs, 0, bs.length);
//                ThreadManager.post(() -> {
//                    imageView.setImageBitmap(bitmap);
//                    long uidRxBytesWifi2 = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytesWifi(getUid(context));
//                    long uidRxBytesMobile2 = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytesMobile(getUid(context));
//                    long uidTxBytesWifi2 = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidTxBytesWifi(getUid(context));
//                    long uidTxBytesMobile2 = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidTxBytesMobile(getUid(context));
//                    LogUtil.i("uidRxBytesWifi2 -> " + uidRxBytesWifi2);
//                    LogUtil.i("uidRxBytesMobile2 -> " + uidRxBytesMobile2);
//                    LogUtil.i("uidTxBytesWifi2 -> " + uidTxBytesWifi2);
//                    LogUtil.i("uidTxBytesMobile2 -> " + uidTxBytesMobile2);
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    /**
     * 将app接下来的请求都绑定到这个网络下请求
     *
     * @param context
     * @param network
     */
    @SuppressWarnings({"deprecation", "JavaDoc", "unused"})
    public void bindProcessToNetwork(Context context, Network network) {
        if (Build.VERSION.SDK_INT >= 23) {
            ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).bindProcessToNetwork(network);
        } else {
            // 23后这个方法舍弃了
            if (Build.VERSION.SDK_INT >= 21) {
                ConnectivityManager.setProcessDefaultNetwork(network);
            }
        }
    }

    /**
     * 在将来某个时间取消这个绑定网络的设置
     *
     * @param context
     */
    @SuppressWarnings({"deprecation", "JavaDoc", "unused"})
    public void unbindProcessToNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            connectivityManager.bindProcessToNetwork(null);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                ConnectivityManager.setProcessDefaultNetwork(null);
            }
        }
    }

    private int getUid(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
