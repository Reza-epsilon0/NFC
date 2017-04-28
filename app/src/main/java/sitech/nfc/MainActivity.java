package sitech.nfc;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    String currentDateandTime;
    private NfcAdapter mAdapter;
    private AlertDialog mDialog;
    private NdefMessage mNdefPushMessage;
    private PendingIntent mPendingIntent;
    TextView txtReadTag;
    String tech2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.txtReadTag = (TextView) findViewById(R.id.txtData);
          resolveIntent(getIntent());     //a function
        this.mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();
        this.mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (this.mAdapter == null) {
            showMessage(R.string.error, R.string.no_nfc);
            finish();
            return;
        }
        this.mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(536870912), 0);
        this.mNdefPushMessage = new NdefMessage(
                new NdefRecord[]{newTextRecord("Message from NFC Reader :-)", Locale.ENGLISH, true)});
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if ("android.nfc.action.TAG_DISCOVERED".equals(action) ||
                "android.nfc.action.TECH_DISCOVERED".equals(action) ||
                "android.nfc.action.NDEF_DISCOVERED".equals(action)) {
            NdefMessage[] msgs;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra("android.nfc.extra.NDEF_MESSAGES");
            if (rawMsgs != null) {
                Log.e("mmmmmmmmyyyy","rawMsgs != null 59");
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                msgs = new NdefMessage[]{new NdefMessage(new NdefRecord[]{new NdefRecord((short) 5,
                        new byte[0],
                        intent.getByteArrayExtra("android.nfc.extra.ID"),
                        dumpTagData(intent.getParcelableExtra("android.nfc.extra.TAG")).getBytes())})};
                Log.e("mmmmmmmmyyyy","else of rawMsgs != null 59");
            }
            buildTagViews(msgs);
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mAdapter != null) {
            if (!this.mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }
            this.mAdapter.enableForegroundDispatch(this, this.mPendingIntent, null, null);
            this.mAdapter.enableForegroundNdefPush(this, this.mNdefPushMessage);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mAdapter != null) {
            this.mAdapter.disableForegroundDispatch(this);
            this.mAdapter.disableForegroundNdefPush(this);
        }
    }

    private void showMessage(int title, int message) {
        this.mDialog.setTitle(title);
        this.mDialog.setMessage(getText(message));
        this.mDialog.show();
    }

    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        Charset utfEncoding;
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        if (encodeInUtf8) {
            utfEncoding = Charset.forName("UTF-8");
        } else {
            utfEncoding = Charset.forName("UTF-16");
        }
        byte[] textBytes = text.getBytes(utfEncoding);
        byte[] data = new byte[((langBytes.length + 1) + textBytes.length)];
        data[0] = (byte) ((char) (langBytes.length + (encodeInUtf8 ? 0 : 128)));
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, langBytes.length + 1, textBytes.length);
        return new NdefRecord((short) 1, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    private void showWirelessSettingsDialog() {
//        Builder builder = new Builder(this);
//        builder.setMessage(R.string.nfc_disabled);
//        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialogInterface, int i) {
//                MainActivity.this.startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
//            }
//        });
//        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialogInterface, int i) {
//                MainActivity.this.finish();
//            }
//        });
//        builder.create().show();
    }



    private String dumpTagData(Parcelable p) {
        String tech;
        int i = 0;
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");
        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
//        for ( tech2 : tag.getTechList()) {
//            sb.append(tech2.substring(prefix.length()));
//            sb.append(", ");
//        }
        sb.delete(sb.length() - 2, sb.length());
        String[] techList = tag.getTechList();
        int length = techList.length;
        while (i < length) {
            String type;
            tech2 = techList[i];
            if (tech2.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                type = "Unknown";
                switch (mifareTag.getType()) {
                    case 0:
                        type = "Classic";
                        break;
                    case 1:
                        type = "Plus";
                        break;
                    case 2:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');
                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');
                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');
                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }
            if (tech2.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                type = "Unknown";
                switch (MifareUltralight.get(tag).getType()) {
                    case 1:
                        type = "Ultralight";
                        break;
                    case 2:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
            i++;
        }
        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; i--) {
            int b = bytes[i] & 255;
            if (b < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (byte b : bytes) {
            result += (((long) b) & 255) * factor;
            factor *= 256;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; i--) {
            result += (((long) bytes[i]) & 255) * factor;
            factor *= 256;
        }
        return result;
    }

    void buildTagViews(NdefMessage[] msgs) {
        if (msgs != null && msgs.length != 0) {
            String body;
            String tagId = new String(msgs[0].getRecords()[0].getType());
            Log.i("Tag Id", tagId);
            if (tagId.equals("nfcdemo:smsService")) {
                String[] data1 = new String(msgs[0].getRecords()[0].getPayload()).split("-");
                body = "Number:" + data1[0] + "<br/>SMS:" + data1[1];
            } else {
                body = new String(msgs[0].getRecords()[0].getPayload());
            }
            Log.i("ID", "id " + tagId);
            Log.i("Message", "text " + body);
            this.currentDateandTime = this.TIME_FORMAT.format(new Date());
            this.txtReadTag.setText(Html.fromHtml(body));

        }
    }

    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
}
