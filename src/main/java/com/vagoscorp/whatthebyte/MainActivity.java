package com.vagoscorp.whatthebyte;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;

public class MainActivity extends Activity {

    final int TYPE_U_BYTE  = 0;
    final int TYPE_CHAR  = 1;
    final int TYPE_BIN = 2;
    final int TYPE_U_HEX = 3;
    final int TYPE_U_INT16  = 4;
    final int TYPE_U_INT32  = 5;
    final int TYPE_S_BYTE  = 6;
    final int TYPE_S_INT16  = 7;
    final int TYPE_S_INT32  = 8;
    final int TYPE_FLOAT  = 9;

    final int[] sA = {0,4,8,12,13,15,16,20,20,22,23};

    final int uByteM = 0;
    final int uByteU = 1;
    final int uByteH = 2;
    final int uByteL = 3;
    final int charM = 4;
    final int charU = 5;
    final int charH = 6;
    final int charL = 7;
    final int binaryM = 8;
    final int binaryU = 9;
    final int binaryH = 10;
    final int binaryL = 11;
    final int uHex = 12;
    final int uInt16H = 13;
    final int uInt16 = 14;
    final int uInt32 = 15;
    final int sByteM = 16;
    final int sByteU = 17;
    final int sByteH = 18;
    final int sByteL = 19;
    final int sInt16H = 20;
    final int sInt16 = 21;
    final int sInt32 = 22;
    final int sFloat = 23;

    final int maxuInt8 = 255;//(2^8) - 1;
    final int maxuInt16 = 65535;//(2^16) - 1;
    final long maxuInt32 = 4294967295L;//(2^32) - 1;
    //final long maxuInt64 = 18446744073709551615L;//(2 ^ 64) - 1;
    int focusedET = 0;

    final String RESTORE = "RESTORE";
    byte[] thatBytes = {0, 0, 0, 0};

    LinearLayout mainActivity;

    /*EditText[] editUByte = new EditText[4]; int editUByteRes[] = {R.id.uByteMS, R.id.uByteUS, R.id.uByteHS, R.id.uByteLS};
    EditText[] editChar = new EditText[4]; int editCharRes[] = {R.id.uByteMS, R.id.uByteUS, R.id.uByteHS, R.id.uByteLS};
    EditText[] editBin = new EditText[2]; int editBinRes[] = {R.id.uByteMS, R.id.uByteUS, R.id.uByteHS, R.id.uByteLS};
    EditText editHex;
    EditText[] editUInt16 = new EditText[2]; int editUInt16Res[] = {R.id.uByteMS, R.id.uByteUS, R.id.uByteHS, R.id.uByteLS};
    EditText editUInt32;
    EditText[] editSByte = new EditText[4]; int editSByteRes[] = {R.id.uByteMS, R.id.uByteUS, R.id.uByteHS, R.id.uByteLS};
    EditText[] editSInt16 = new EditText[2]; int editSInt16Res[] = {R.id.uByteMS, R.id.uByteUS, R.id.uByteHS, R.id.uByteLS};
    EditText editSInt32;
    EditText editFloat;*/

    int[] editTextRes = {R.id.uByteM,R.id.uByteU,R.id.uByteH,R.id.uByteL,
                            R.id.charM,R.id.charU,R.id.charH,R.id.charL,
                            R.id.uBinM,R.id.uBinU,R.id.uBinH,R.id.uBinL,
                            R.id.uHex,R.id.uInt16H,R.id.uInt16,R.id.uInt32,
                            R.id.sByteM,R.id.sByteU,R.id.sByteH,R.id.sByteL,
                            R.id.sInt16H,R.id.sInt16,R.id.sInt32,R.id.sFloat};

    EditText[] editTexts = new EditText[24];
    TextView checkSum;
    TextView sCheckSum;
    Button eraseChars;
    int dataType = 13;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = findViewById(R.id.MainActivity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mainActivity.setBackgroundColor(Color.parseColor("#ff303030"));
        context = this;
        for(int i = 0; i < 24; i++) {
            editTexts[i] = findViewById(editTextRes[i]);
            final int i2 = i;
            if(i >= charM && i <= charL) {
                editTexts[i2].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editTexts[i2].selectAll();
                    }
                });
            }
        }
        checkSum = findViewById(R.id.checkSum);
        sCheckSum = findViewById(R.id.sCheckSum);
        eraseChars = findViewById(R.id.eraseChars);
        eraseChars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 4; i < 8; i++)
                    editTexts[i].setText("");
            }
        });
        eraseChars.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                for(int i = 0; i < 24; i++)
                    editTexts[i].setText("");
                return true;
            }
        });
        final TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(dataType == TYPE_CHAR)
                    editTexts[focusedET].selectAll();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(dataType == TYPE_CHAR)
                    editTexts[focusedET].selectAll();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(dataType == TYPE_CHAR)
                    editTexts[focusedET].selectAll();
                trans2Bytes(dataType);
            }
        };
        for (int i1 = 0; i1 < 24; i1++) {
            final int i = i1;
            editTexts[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    for (int i2 = 0; i2 < 24; i2++)
                        editTexts[i2].removeTextChangedListener(textWatcher);
                    if (hasFocus) {
                        if (i <= uByteL)
                            dataType = TYPE_U_BYTE;
                        else if (i <= charL)
                            dataType = TYPE_CHAR;
                        else if (i <= binaryL)
                            dataType = TYPE_BIN;
                        else if (i == uHex)
                            dataType = TYPE_U_HEX;
                        else if (i <= uInt16)
                            dataType = TYPE_U_INT16;
                        else if (i == uInt32)
                            dataType = TYPE_U_INT32;
                        else if (i <= sByteL)
                            dataType = TYPE_S_BYTE;
                        else if (i <= sInt16)
                            dataType = TYPE_S_INT16;
                        else if (i == sInt32)
                            dataType = TYPE_S_INT32;
                        else //if (i == sFloat)
                            dataType = TYPE_FLOAT;
                        editTexts[i].selectAll();
                        editTexts[i].addTextChangedListener(textWatcher);
                        focusedET = i;
                    }
                }
            });
        }
        if(savedInstanceState == null)
            restoreThatBytes();
        else {
            thatBytes = savedInstanceState.getByteArray(RESTORE);
            if(thatBytes == null || thatBytes.length != 4)
                restoreThatBytes();
        }
        dataType = 11;
        transmutation(thatBytes);
    }

    void restoreThatBytes() {
        SharedPreferences shapre = getPreferences(MODE_PRIVATE);
        long value = shapre.getLong("RESTORE", 0);
        byte[] uInt32a = ByteBuffer.allocate(8).putLong(value).array();
        thatBytes[0] = uInt32a[4];
        thatBytes[1] = uInt32a[5];
        thatBytes[2] = uInt32a[6];
        thatBytes[3] = uInt32a[7];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putByteArray(RESTORE, thatBytes);
        super.onSaveInstanceState(outState);
    }

    void transmutation(byte[] bytes) {
        thatBytes = bytes;
        byte[] the2Bytes = {0, 0};
        byte[] the2BytesH = {0, 0};
        byte[] theu4Bytes = {0, 0, 0, 0};
        byte[] theu4BytesH = {0, 0, 0, 0};
        byte[] the8Bytes = {0, 0, 0, 0, 0, 0, 0, 0};
        the8Bytes[4] = bytes[0];
        the8Bytes[5] = bytes[1];
        the8Bytes[6] = bytes[2];
        the8Bytes[7] = bytes[3];
        the2BytesH[0] = bytes[0];
        the2BytesH[1] = bytes[1];
        the2Bytes[0] = bytes[2];
        the2Bytes[1] = bytes[3];
        theu4BytesH[2] = bytes[0];
        theu4BytesH[3] = bytes[1];
        theu4Bytes[2] = bytes[2];
        theu4Bytes[3] = bytes[3];
        //Toast.makeText(this, "bytes = " + theBytes[3] + " " + theBytes[2] + " " + theBytes[1] + " " + theBytes[0],Toast.LENGTH_SHORT).show();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        float valFloat = byteBuffer.getFloat();
        byteBuffer = ByteBuffer.wrap(bytes);
        int valInt = byteBuffer.getInt();
        byteBuffer = ByteBuffer.wrap(theu4BytesH);
        long valUShortH = byteBuffer.getInt();
        byteBuffer = ByteBuffer.wrap(theu4Bytes);
        long valUShort = byteBuffer.getInt();
        byteBuffer = ByteBuffer.wrap(the8Bytes);
        long valUInt = byteBuffer.getLong();
        byteBuffer = ByteBuffer.wrap(the2Bytes);
        short valShort = byteBuffer.getShort();
        byteBuffer = ByteBuffer.wrap(the2BytesH);
        short valShortH = byteBuffer.getShort();
        String valX;
        if(dataType != TYPE_U_BYTE) {
            valX = (0xFF & bytes[0]) + "";
            editTexts[uByteM].setText(valX);
            valX = (0xFF & bytes[1]) + "";
            editTexts[uByteU].setText(valX);
            valX = (0xFF & bytes[2]) + "";
            editTexts[uByteH].setText(valX);
            valX = (0xFF & bytes[3]) + "";
            editTexts[uByteL].setText(valX);
        }
        if(dataType != TYPE_CHAR) {
            valX = ((char)bytes[0]) + "";
            editTexts[charM].setText(valX);
            valX = ((char)bytes[1]) + "";
            editTexts[charU].setText(valX);
            valX = ((char)bytes[2]) + "";
            editTexts[charH].setText(valX);
            valX = ((char)bytes[3]) + "";
            editTexts[charL].setText(valX);
        }
        if(dataType != TYPE_BIN) {
            /*String formTextH = String.format("%16s", Long.toBinaryString(valUShortH)).
                    replace(" ", "0");*/
            String formTextM = String.format("%8s", Integer.toBinaryString(0xFF & bytes[0])).
                            replace(" ", "0");
            String formTextU =String.format("%8s", Integer.toBinaryString(0xFF & bytes[1])).
                            replace(" ", "0");
            /*String formText = String.format("%16s", Long.toBinaryString(valUShort)).
                    replace(" ", "0");*/
            String formTextH = String.format("%8s", Integer.toBinaryString(0xFF & bytes[2])).
                            replace(" ", "0");
            String formTextL = String.format("%8s", Integer.toBinaryString(0xFF & bytes[3])).
                            replace(" ", "0");
            editTexts[binaryM].setText(formTextM);
            editTexts[binaryU].setText(formTextU);
            editTexts[binaryH].setText(formTextH);
            editTexts[binaryL].setText(formTextL);
        }
        if(dataType != TYPE_U_HEX) {
            editTexts[uHex].setText(Long.toHexString(valUInt));
        }
        if(dataType != TYPE_U_INT16) {
            valX = valUShort + "";
            editTexts[uInt16].setText(valX);
            valX = valUShortH + "";
            editTexts[uInt16H].setText(valX);
        }
        if(dataType != TYPE_U_INT32) {
            valX = valUInt + "";
            editTexts[uInt32].setText(valX);
        }
        if(dataType != TYPE_S_BYTE) {
            valX = bytes[0] + "";
            editTexts[sByteM].setText(valX);
            valX = bytes[1] + "";
            editTexts[sByteU].setText(valX);
            valX = bytes[2] + "";
            editTexts[sByteH].setText(valX);
            valX = bytes[3] + "";
            editTexts[sByteL].setText(valX);
        }
        if(dataType != TYPE_S_INT16) {
            valX = valShort + "";
            editTexts[sInt16].setText(valX);
            valX = valShortH + "";
            editTexts[sInt16H].setText(valX);
        }
        if(dataType != TYPE_S_INT32) {
            valX = valInt + "";
            editTexts[sInt32].setText(valX);
        }
        if(dataType != TYPE_FLOAT) {
            valX = valFloat + "";
            editTexts[sFloat].setText(valX);
        }
        int sum = 0;
        int sSum = 0;
        for(int i = 0; i < 4; i++) {
            sum += 0xFF & bytes[i];
            sSum += bytes[i];
        }
        String st = "ByteSum = " + sum;
        String sSt = "ByteSum = " + sSum;
        checkSum.setText(st);
        sCheckSum.setText(sSt);
        SharedPreferences shapre = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = shapre.edit();
        editor.putLong(RESTORE, valUInt);
        editor.commit();
    }

    void trans2Bytes(int type) {
        byte[] theBytes = {0, 0, 0, 0};
        String theString;
        String theStringH;
        int value = 0;
        int valueH = 0;
        long valueL = 0;
        byte[] uInt8;
        byte[] uInt16a;
        byte[] uInt16Ha;
        byte[] uInt32a;
        try {
            switch(type) {
                case(TYPE_U_BYTE):
                    for(int i = 0; i < 4; i++) {
                        theString = editTexts[i + sA[type]].getText().toString();
                        value = 0;
                        if(!theString.equals(""))
                            value = Integer.parseInt(theString);
                        if(value > maxuInt8)
                            Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                        uInt8 = ByteBuffer.allocate(4).putInt(value).array();
                        theBytes[i] = uInt8[3];
                    }
                    break;
                case(TYPE_CHAR):
                    byte[] sas;
                    for(int i = 0; i < 4; i++) {
                        sas = editTexts[i + sA[type]].getText().toString().getBytes();
                        if(sas.length > 0)
                            theBytes[i] = sas[0];
                        else
                            theBytes[i] = 0;
                    }
                    break;
                case(TYPE_BIN):
                    for(int i = 0; i < 4; i++) {
                        theString = editTexts[i + sA[type]].getText().toString();
                        value = 0;
                        if (!theString.equals(""))
                            value = Integer.parseInt(theString, 2);
                        if(value > maxuInt8)
                            Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                        uInt8 = ByteBuffer.allocate(4).putInt(value).array();
                        theBytes[i] = uInt8[3];
                    }
                    break;
                case(TYPE_U_HEX):
                    theString = editTexts[uHex].getText().toString();
                    if(!theString.equals(""))
                        valueL = Long.parseLong(theString, 16);
                    if(valueL > maxuInt32)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    uInt32a = ByteBuffer.allocate(8).putLong(valueL).array();
                    theBytes[0] = uInt32a[4];
                    theBytes[1] = uInt32a[5];
                    theBytes[2] = uInt32a[6];
                    theBytes[3] = uInt32a[7];
                    break;
                case(TYPE_U_INT16):
                    theString = editTexts[uInt16].getText().toString();
                    theStringH = editTexts[uInt16H].getText().toString();
                    if(!theString.equals(""))
                        value = Integer.parseInt(theString);
                    if(!theStringH.equals(""))
                        valueH = Integer.parseInt(theStringH);
                    if(value > maxuInt16)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    if(valueH > maxuInt16)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    uInt16a = ByteBuffer.allocate(4).putInt(value).array();
                    uInt16Ha = ByteBuffer.allocate(4).putInt(valueH).array();
                    theBytes[0] = uInt16Ha[2];
                    theBytes[1] = uInt16Ha[3];
                    theBytes[2] = uInt16a[2];
                    theBytes[3] = uInt16a[3];
                    break;
                case(TYPE_U_INT32):
                    theString = editTexts[uInt32].getText().toString();
                    if(!theString.equals(""))
                        valueL = Long.parseLong(theString);
                    if(valueL > maxuInt32)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    uInt32a = ByteBuffer.allocate(8).putLong(valueL).array();
                    theBytes[0] = uInt32a[4];
                    theBytes[1] = uInt32a[5];
                    theBytes[2] = uInt32a[6];
                    theBytes[3] = uInt32a[7];
                    break;
                case(TYPE_S_BYTE):
                    for(int i = 0; i < 4; i++) {
                        theString = editTexts[i + sA[type]].getText().toString();
                        if(!theString.equals(""))
                            theBytes[i] = Byte.parseByte(theString);
                    }
                    break;
                case(TYPE_S_INT16):
                    theString = editTexts[sInt16].getText().toString();
                    theStringH = editTexts[sInt16H].getText().toString();
                    short valueS = 0;
                    short valueHS = 0;
                    if(!theString.equals(""))
                        valueS = Short.parseShort(theString);
                    if(!theStringH.equals(""))
                        valueHS = Short.parseShort(theStringH);
                    byte[] shortBytes = ByteBuffer.allocate(2).putShort(valueS).array();
                    byte[] shortBytesH = ByteBuffer.allocate(2).putShort(valueHS).array();
                    theBytes[0] = shortBytesH[0];
                    theBytes[1] = shortBytesH[1];
                    theBytes[2] = shortBytes[0];
                    theBytes[3] = shortBytes[1];
                    break;
                case(TYPE_S_INT32):
                    theString = editTexts[sInt32].getText().toString();
                    if(!theString.equals(""))
                        value = Integer.parseInt(theString);
                    theBytes = ByteBuffer.allocate(4).putInt(value).array();
                    break;
                case(TYPE_FLOAT):
                    theString = editTexts[sFloat].getText().toString();
                    float valueF = 0;
                    if(!theString.equals(""))
                        valueF = Float.parseFloat(theString);
                    theBytes = ByteBuffer.allocate(4).putFloat(valueF).array();
                    break;
            }
            transmutation(theBytes);
        } catch (NumberFormatException nEx) {
            nEx.printStackTrace();
            Toast.makeText(this, nEx.getMessage()/*R.string.numFormExc*/, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
