package com.worthdoingbadly.simserver;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends Activity {

    public static TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("Hello from the main fork-of SimServer activity!");

        // subscriptionIds from testing menu (*#*#4636#*#* in dialer) Android 10+, also available in logcat


        TelephonyManager telephonyManagerGeneral = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyManager telephonyManagerSlotOne = null;
        TelephonyManager telephonyManagerSlotTwo = null;

        SubscriptionManager subscriptionManagerGeneral = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        SubscriptionInfo subscriptionManagerSlotOne = subscriptionManagerGeneral.getActiveSubscriptionInfoForSimSlotIndex(0);
        SubscriptionInfo subscriptionManagerSlotTwo = subscriptionManagerGeneral.getActiveSubscriptionInfoForSimSlotIndex(1);

        boolean simOneExists = false;
        boolean simTwoExists = false;

        try {
            telephonyManagerSlotOne = telephonyManagerGeneral.createForSubscriptionId(subscriptionManagerSlotOne.getSubscriptionId());
            simOneExists = true;
        } catch (NullPointerException e) {
            Log.d("IMSI-NOSIM", "No SIM card in slot one.");
            Context context = getApplicationContext();
            CharSequence text = "No SIM card in slot one.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        try {
            telephonyManagerSlotTwo = telephonyManagerGeneral.createForSubscriptionId(subscriptionManagerSlotTwo.getSubscriptionId());
            simTwoExists = true;
        } catch (NullPointerException e) {
            Log.d("IMSI-NOSIM", "No SIM card in slot two.");
            Context context = getApplicationContext();
            CharSequence text = "No SIM card in slot two.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        if(simOneExists) { String imsiSlotOne = telephonyManagerSlotOne.getSubscriberId(); }
        if(simTwoExists) { String imsiSlotTwo = telephonyManagerSlotTwo.getSubscriberId(); }
        if((!simOneExists) && (!simTwoExists)) {
            Log.d("IMSI-NOSIM", "There is no SIM card!");
            Context context = getApplicationContext();
            CharSequence text = "There is no SIM card inserted!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finishAndRemoveTask();
        }
        
        TextView versionNumber = (TextView)findViewById(R.id.versionNumber);
        versionNumber.setText(String.format("Version %s", BuildConfig.VERSION_NAME));

        if(simOneExists) {
            Button imsiSlotOneButton = (Button) findViewById(R.id.imsiCardOneButton);
            imsiSlotOneButton.setEnabled(true);
            TelephonyManager finalTelephonyManagerSlotOne = telephonyManagerSlotOne;
            imsiSlotOneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView imsiSlotOneTextOutput = (TextView) findViewById(R.id.imsiCardOneTextOutput);
                    String imsiSlotOne = finalTelephonyManagerSlotOne.getSubscriberId();
                    imsiSlotOneTextOutput.setText(imsiSlotOne);
                }
            });
        }

        if(simTwoExists) {
            Button imsiSlotTwoButton = (Button) findViewById(R.id.imsiCardTwoButton);
            imsiSlotTwoButton.setEnabled(true);
            TelephonyManager finalTelephonyManagerSlotTwo = telephonyManagerSlotTwo;
            imsiSlotTwoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView imsiSlotTwoTextOutput = (TextView) findViewById(R.id.imsiCardTwoTextOutput);
                    String imsiSlotTwo = finalTelephonyManagerSlotTwo.getSubscriberId();
                    imsiSlotTwoTextOutput.setText(imsiSlotTwo);
                }
            });
        }

        if(simOneExists) {
            Button serveSlotOneButton = (Button) findViewById(R.id.serveCardOneButton);
            serveSlotOneButton.setEnabled(true);
            TelephonyManager finalTelephonyManagerSlotOne1 = telephonyManagerSlotOne;
            serveSlotOneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        new SimHTTPDTwo(3333, finalTelephonyManagerSlotOne1).start();
                        Context context = getApplicationContext();
                        CharSequence text = "We are serving Slot1";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
                }
            });
        }

        if(simTwoExists) {
            Button serveSlotTwoButton = (Button) findViewById(R.id.serveCardTwoButton);
            serveSlotTwoButton.setEnabled(true);
            TelephonyManager finalTelephonyManagerSlotTwo1 = telephonyManagerSlotTwo;
            serveSlotTwoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                /*
                TextView authValueData = (TextView)findViewById(R.id.authValueTextBox);
                String authValueDataString = (String)authValueData.getText().toString();
                new SimHTTPDTwo(Integer.parseInt(authValueDataString).start();
                 */
                    try {
                        new SimHTTPDTwo(3334, finalTelephonyManagerSlotTwo1).start();
                        Context context = getApplicationContext();
                        CharSequence text = "We are serving Slot2";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
                }
            });
        }

    }




    public static String first(List<String> l) {
        if (l == null || l.size() == 0) return null;
        return l.get(0);
    }

    // ripped from AOSP
    // returns a byte array where the first element is the length and the rest is bytes decoded from
    // hex data from the string
    private static byte[] parseHex(String hex) {
        /* This only works for good input; don't throw bad data at it */
        if (hex == null) {
            return new byte[0];
        }

        if (hex.length() % 2 != 0) {
            throw new NumberFormatException(hex + " is not a valid hex string");
        }

        byte[] result = new byte[(hex.length()) / 2 + 1];
        result[0] = (byte) ((hex.length()) / 2);
        for (int i = 0, j = 1; i < hex.length(); i += 2, j++) {
            byte b = (byte)(Integer.parseInt(hex.substring(i, i + 2), 16) & 0xff);
            result[j] = b;
        }

        return result;
    }

    private static byte[] concatHex(byte[] array1, byte[] array2) {

        int len = array1.length + array2.length;

        byte[] result = new byte[len];

        int index = 0;
        if (array1.length != 0) {
            for (byte b : array1) {
                result[index] = b;
                index++;
            }
        }

        if (array2.length != 0) {
            for (byte b : array2) {
                result[index] = b;
                index++;
            }
        }

        return result;
    }

    private static String makeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String makeHex(byte[] bytes, int from, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02x", bytes[from + i]));
        }
        return sb.toString();
    }

    public static String runAuth(String randStr, String authnStr, TelephonyManager telephonyManagerTwo) {
        // public static TelephonyManager telephonyManager = null;
        // telephonyManager = telephonyManager;

        // https://android.googlesource.com/platform/frameworks/opt/net/wifi/+/refs/heads/nougat-release/service/java/com/android/server/wifi/WifiMonitor.java#314
        // https://cs.android.com/android/platform/superproject/+/master:packages/modules/Wifi/service/java/com/android/server/wifi/ClientModeImpl.java;l=5871;drc=master
        // https://cs.android.com/android/platform/superproject/+/master:packages/modules/Wifi/service/java/com/android/server/wifi/WifiCarrierInfoManager.java;l=1296;drc=master
        // shamelessly copied from AOSP
        byte[] rand = parseHex(randStr);
        byte[] authn = parseHex(authnStr);
        String base64Challenge = Base64.encodeToString(concatHex(rand, authn), Base64.NO_WRAP);
        System.out.println("Challenge: " + base64Challenge);

        /*
        getIccAuthentication does not work on old phones (e.g. Nexus 5) - I suspect the modem firmware did not implement the feature
        (I have a copy of the logcat output, but it's not public due to private information leak concerns)
         */
        String tmResponse = telephonyManagerTwo.getIccAuthentication(TelephonyManager.APPTYPE_USIM,
                TelephonyManager.AUTHTYPE_EAP_AKA, base64Challenge);
        System.out.println("Challenge response: " + tmResponse);

        boolean goodReponse = false;
        String response = null;
        if (tmResponse != null && tmResponse.length() > 4) {
            byte[] result = Base64.decode(tmResponse, Base64.DEFAULT);
            System.out.println("Hex Response - " + makeHex(result));
            byte tag = result[0];
            if (tag == (byte) 0xdb) {
                System.out.println("successful 3G authentication ");
                int resLen = result[1];
                String res = makeHex(result, 2, resLen);
                int ckLen = result[resLen + 2];
                String ck = makeHex(result, resLen + 3, ckLen);
                int ikLen = result[resLen + ckLen + 3];
                String ik = makeHex(result, resLen + ckLen + 4, ikLen);
                response = "{\"ik\": \"" + ik + "\", \"ck\": \"" + ck + "\", \"res\": \"" + res + "\"}";
                System.out.println("ik: " + ik + " ck: " + ck + " res: " + res);
            } else if (tag == (byte) 0xdc) {
                System.out.println("synchronisation failure");
                int autsLen = result[1];
                String auts = makeHex(result, 2, autsLen);
                response = "{\"auts\": \"" + auts + "\"}";
                System.out.println("auts:" + auts);
            } else {
                System.out.println("bad response - unknown tag = " + tag);
            }
        } else {
            System.out.println("bad response - " + tmResponse);
        }
        return response;
    }
    private class SimHTTPDTwo extends NanoHTTPD {
        public TelephonyManager telephonyManager;

        SimHTTPDTwo(int port, TelephonyManager telephonyManager) {
            super(port);
            this.telephonyManager = telephonyManager;
        }
        @Override
        public Response serve(IHTTPSession session) {
            // https://github.com/fasferraz/USIM-https-server
            Map<String, List<String>> params = session.getParameters();
            String type = first(params.get("type"));
            if (type == null) {
                return newFixedLengthResponse("no command");
            }
            if (type.equals("imsi")) {
                String imsi = this.telephonyManager.getSubscriberId();
                return newFixedLengthResponse("{\"imsi\": \"" + imsi + "\"}");
            } else if (type.equals("rand-autn")) {
                String rand = first(params.get("rand"));
                String autn = first(params.get("autn"));
                String response = runAuth(rand, autn, this.telephonyManager);
                return newFixedLengthResponse(response);
            } else {
                return newFixedLengthResponse("invalid command");
            }
        }
    }
}
