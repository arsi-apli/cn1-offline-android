/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.cn1.offline.android.actions;

/**
 *
 * @author arsi
 */
public class AndroidManifest {

    public static final String MANIFEST = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "          package=\"com.mycompany.myapp\"\n"
            + "          android:versionCode=\"100\"\n"
            + "          android:versionName=\"1.0\"\n"
            + "          xmlns:tools=\"http://schemas.android.com/tools\"\n"
            + "          android:minSdkVersion=\"15\"\n"
            + "          android:installLocation=\"auto\">\n"
            + "    <uses-sdk android:minSdkVersion=\"15\"      android:targetSdkVersion=\"14\"  />\n"
            + "    <supports-screens android:smallScreens=\"true\"\n"
            + "                      android:normalScreens=\"true\"\n"
            + "                      android:largeScreens=\"true\"\n"
            + "                      android:xlargeScreens=\"true\"\n"
            + "                      android:anyDensity=\"true\" />\n"
            + "    <application  android:label=\"CoCo\"  android:icon=\"@drawable/icon\"  android:allowBackup=\"true\" >\n"
            + "        <meta-data android:name=\"com.google.android.gms.version\" android:value=\"@integer/google_play_services_version\"/>        \n"
            + "        <activity android:name=\"MyApplicationStub\"\n"
            + "                  android:theme=\"@style/CustomTheme\"\n"
            + "                  android:configChanges=\"orientation|keyboardHidden|screenSize\"\n"
            + "                  android:launchMode=\"singleTop\"\n"
            + "                  android:label=\"CoCo\">\n"
            + "            <intent-filter>\n"
            + "                <action android:name=\"android.intent.action.MAIN\" />\n"
            + "                <category android:name=\"android.intent.category.LAUNCHER\" />\n"
            + "            </intent-filter>\n"
            + "        </activity>\n"
            + "        <receiver android:name=\"com.codename1.impl.android.LocalNotificationPublisher\" ></receiver>\n"
            + "        <service android:name=\"com.codename1.impl.android.BackgroundFetchHandler\" android:exported=\"false\" />\n"
            + "        <activity android:name=\"com.codename1.impl.android.CodenameOneBackgroundFetchActivity\" android:theme=\"@android:style/Theme.NoDisplay\" />\n"
            + "        <activity android:name=\"com.codename1.location.CodenameOneBackgroundLocationActivity\" android:theme=\"@android:style/Theme.NoDisplay\" />\n"
            + "        <service android:name=\"com.codename1.location.BackgroundLocationHandler\" android:exported=\"false\" />\n"
            + "        <service android:name=\"com.codename1.location.GeofenceHandler\" android:exported=\"false\" />\n"
            + "        <service android:name=\"com.codename1.media.AudioService\" android:exported=\"false\" />    \n"
            + "    </application>\n"
            + "    <uses-feature android:name=\"android.hardware.touchscreen\" android:required=\"false\" />\n"
            + "    <uses-feature android:name=\"android.hardware.telephony\" android:required=\"false\" />\n"
            + "    <uses-permission android:name=\"android.permission.INTERNET\" android:required=\"false\" />\n"
            + "    <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" android:required=\"false\" />\n"
            + "</manifest>";
}
