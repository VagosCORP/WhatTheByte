package com.vagoscorp.whatthebyte;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;

public class MainActivity extends Activity {

    final int TYPE_U_BYTE  = 0;
    final int TYPE_CHAR  = 1;
    final int TYPE_BINARY = 2;
    final int TYPE_U_HEXADECIMAL  = 3;
    final int TYPE_U_INT16  = 4;
    final int TYPE_U_INT32  = 5;
    final int TYPE_S_BYTE  = 6;
    final int TYPE_S_INT16  = 7;
    final int TYPE_S_INT32  = 8;
    final int TYPE_FLOAT  = 9;

    final int uByteMS = 0;
    final int uByteUS = 1;
    final int uByteHS = 2;
    final int uByteLS = 3;
    final int charMS = 4;
    final int charUS = 5;
    final int charHS = 6;
    final int charLS = 7;
    final int binary = 8;
    final int uHexadecimal = 9;
    final int uInt16 = 10;
    final int uInt32 = 11;
    final int sByteMS = 12;
    final int sByteUS = 13;
    final int sByteHS = 14;
    final int sByteLS = 15;
    final int sInt16 = 16;
    final int sInt32 = 17;
    final int sFloat = 18;
    final int uInt16H = 19;
    final int sInt16H = 20;

    final int maxuInt8 = (2^8) - 1;//255
    final int maxuInt16 = (2^16) - 1;//65535
    final int maxuInt32 = (2^32) - 1;//4294967295L;
    final long maxuInt64 = (2 ^ 64) - 1;//18446744073709551615L

    final String RESTORE = "RESTORE";
    byte[] thatBytes = {0, 0, 0, 0};

    LinearLayout mainActivity;

    EditText[] editTexts = new EditText[21];
    TextView checkSum;
    TextView sCheckSum;
    int dataType = 19;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = findViewById(R.id.MainActivity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mainActivity.setBackgroundColor(Color.parseColor("#ff303030"));
        editTexts[0] = findViewById(R.id.uByteMS);
        editTexts[1] = findViewById(R.id.uByteUS);
        editTexts[2] = findViewById(R.id.uByteHS);
        editTexts[3] = findViewById(R.id.uByteLS);
        editTexts[4] = findViewById(R.id.charMS);
        editTexts[5] = findViewById(R.id.charUS);
        editTexts[6] = findViewById(R.id.charHS);
        editTexts[7] = findViewById(R.id.charLS);
        editTexts[8] = findViewById(R.id.uBinary);
        editTexts[9] = findViewById(R.id.uHexadecimal);
        editTexts[10] = findViewById(R.id.uInt16);
        editTexts[11] = findViewById(R.id.uInt32);
        editTexts[12] = findViewById(R.id.sByteMS);
        editTexts[13] = findViewById(R.id.sByteUS);
        editTexts[14] = findViewById(R.id.sByteHS);
        editTexts[15] = findViewById(R.id.sByteLS);
        editTexts[16] = findViewById(R.id.sInt16);
        editTexts[17] = findViewById(R.id.sInt32);
        editTexts[18] = findViewById(R.id.sFloat);
        editTexts[19] = findViewById(R.id.uInt16H);
        editTexts[20] = findViewById(R.id.sInt16H);
        checkSum = findViewById(R.id.checkSum);
        sCheckSum = findViewById(R.id.sCheckSum);
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (dataType == TYPE_CHAR) {
                    if (count < 2)
                        trans2Bytes(dataType);
                } else
                    trans2Bytes(dataType);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dataType == TYPE_CHAR) {
                    int l = s.length();
                    if (l > 1) {
                        CharSequence cha = s.subSequence(l - 1, l - 1);
                        s.replace(0, l - 1, cha);
                    }
                }
            }
        };
        for (int i1 = 0; i1 < 21; i1++) {
            final int i = i1;
            editTexts[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    for (int i2 = 0; i2 < 21; i2++)
                        editTexts[i2].removeTextChangedListener(textWatcher);
                    if (hasFocus) {
                        if (i == 0 || i == 1 || i == 2 || i == 3)
                            dataType = TYPE_U_BYTE;
                        if (i == 4 || i == 5 || i == 6 || i == 7)
                            dataType = TYPE_CHAR;
                        if (i == 8)
                            dataType = TYPE_BINARY;
                        if (i == 9)
                            dataType = TYPE_U_HEXADECIMAL;
                        if (i == 10 || i == 19)
                            dataType = TYPE_U_INT16;
                        if (i == 11)
                            dataType = TYPE_U_INT32;
                        if (i == 12 || i == 13 || i == 14 || i == 15)
                            dataType = TYPE_S_BYTE;
                        if (i == 16 || i == 20)
                            dataType = TYPE_S_INT16;
                        if (i == 17)
                            dataType = TYPE_S_INT32;
                        if (i == 18)
                            dataType = TYPE_FLOAT;
                        editTexts[i].addTextChangedListener(textWatcher);
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
        dataType = 19;
        transmutation(thatBytes);
    }

    void restoreThatBytes() {
        SharedPreferences shapre = getPreferences(MODE_PRIVATE);
        long value = shapre.getLong("RESTORE", 0);
        byte[] uInt32 = ByteBuffer.allocate(8).putLong(value).array();
        thatBytes[0] = uInt32[4];
        thatBytes[1] = uInt32[5];
        thatBytes[2] = uInt32[6];
        thatBytes[3] = uInt32[7];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray(RESTORE, thatBytes);
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
            editTexts[uByteMS].setText(valX);
            valX = (0xFF & bytes[1]) + "";
            editTexts[uByteUS].setText(valX);
            valX = (0xFF & bytes[2]) + "";
            editTexts[uByteHS].setText(valX);
            valX = (0xFF & bytes[3]) + "";
            editTexts[uByteLS].setText(valX);
        }
        if(dataType != TYPE_CHAR) {
            valX = ((char)bytes[0]) + "";
            editTexts[charMS].setText(valX);
            valX = ((char)bytes[1]) + "";
            editTexts[charUS].setText(valX);
            valX = ((char)bytes[2]) + "";
            editTexts[charHS].setText(valX);
            valX = ((char)bytes[3]) + "";
            editTexts[charLS].setText(valX);
        }
        if(dataType != TYPE_BINARY) {
            editTexts[binary].setText(Long.toBinaryString(valUInt));
        }
        if(dataType != TYPE_U_HEXADECIMAL) {
            editTexts[uHexadecimal].setText(Long.toHexString(valUInt));
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
            editTexts[sByteMS].setText(valX);
            valX = bytes[1] + "";
            editTexts[sByteUS].setText(valX);
            valX = bytes[2] + "";
            editTexts[sByteHS].setText(valX);
            valX = bytes[3] + "";
            editTexts[sByteLS].setText(valX);
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
        /*if(dataType != TYPE_S_INT64) {
            valX = valInt + "";
            editTexts[sInt64].setText(valX);
        }*/
        if(dataType != TYPE_FLOAT) {
            valX = valFloat + "";
            editTexts[sFloat].setText(valX);
        }
        /*if(dataType != TYPE_DOUBLE) {
            valX = valFloat + "";
            editTexts[sDouble].setText(valX);
        }*/
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
        try {
            switch(type) {
                case(TYPE_U_BYTE): {
                    int value;
                    for(int i = 0; i < 4; i++) {
                        theString = editTexts[i].getText().toString();
                        value = 0;
                        if(!theString.equals(""))
                            value = Integer.parseInt(theString);
                        if(value > maxuInt8)
                            Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                        byte[] uInt8 = ByteBuffer.allocate(4).putInt(value).array();
                        theBytes[i] = uInt8[3];
                    }
                    break;
                }
                case(TYPE_CHAR): {
                    byte[] sas;
                    for(int i = 0; i < 4; i++) {
                        sas = editTexts[i + 4].getText().toString().getBytes();
                        if(sas.length > 0)
                            theBytes[i] = sas[0];
                        else
                            theBytes[i] = 0;
                    }
                    break;
                }
                case(TYPE_BINARY): {
                    theString = editTexts[binary].getText().toString();
                    long value = 0;
                    if(!theString.equals(""))
                        value = Long.parseLong(theString, 2);
                    if(value > maxuInt32)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    byte[] uInt32 = ByteBuffer.allocate(8).putLong(value).array();
                    theBytes[0] = uInt32[4];
                    theBytes[1] = uInt32[5];
                    theBytes[2] = uInt32[6];
                    theBytes[3] = uInt32[7];
                    break;
                }
                case(TYPE_U_HEXADECIMAL): {
                    theString = editTexts[uHexadecimal].getText().toString();
                    long value = 0;
                    if(!theString.equals(""))
                        value = Long.parseLong(theString, 16);
                    if(value > maxuInt32)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    byte[] uInt32 = ByteBuffer.allocate(8).putLong(value).array();
                    theBytes[0] = uInt32[4];
                    theBytes[1] = uInt32[5];
                    theBytes[2] = uInt32[6];
                    theBytes[3] = uInt32[7];
                    break;
                }
                case(TYPE_U_INT16): {
                    theString = editTexts[uInt16].getText().toString();
                    theStringH = editTexts[uInt16H].getText().toString();
                    int value = 0;
                    int valueH = 0;
                    if(!theString.equals(""))
                        value = Integer.parseInt(theString);
                    if(!theStringH.equals(""))
                        valueH = Integer.parseInt(theStringH);
                    if(value > maxuInt16)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    if(valueH > maxuInt16)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    byte[] uInt16 = ByteBuffer.allocate(4).putInt(value).array();
                    byte[] uInt16H = ByteBuffer.allocate(4).putInt(valueH).array();
                    theBytes[3] = uInt16[3];
                    theBytes[2] = uInt16[2];
                    theBytes[1] = uInt16H[3];
                    theBytes[0] = uInt16H[2];
                    break;
                }
                case(TYPE_U_INT32): {
                    theString = editTexts[uInt32].getText().toString();
                    long value = 0;
                    if(!theString.equals(""))
                        value = Long.parseLong(theString);
                    if(value > maxuInt32)
                        Toast.makeText(this, R.string.overFlow, Toast.LENGTH_SHORT).show();
                    byte[] uInt32 = ByteBuffer.allocate(8).putLong(value).array();
                    theBytes[0] = uInt32[4];
                    theBytes[1] = uInt32[5];
                    theBytes[2] = uInt32[6];
                    theBytes[3] = uInt32[7];
                    break;
                }
                case(TYPE_S_BYTE): {
                    for(int i = 0; i < 4; i++) {
                        theString = editTexts[i + 12].getText().toString();
                        if(!theString.equals(""))
                            theBytes[i] = Byte.parseByte(theString);
                    }
                    break;
                }
                case(TYPE_S_INT16): {
                    theString = editTexts[sInt16].getText().toString();
                    theStringH = editTexts[sInt16H].getText().toString();
                    short value = 0;
                    short valueH = 0;
                    if(!theString.equals(""))
                        value = Short.parseShort(theString);
                    if(!theStringH.equals(""))
                        valueH = Short.parseShort(theStringH);
                    byte[] shortBytes = ByteBuffer.allocate(2).putShort(value).array();
                    byte[] shortBytesH = ByteBuffer.allocate(2).putShort(valueH).array();
                    theBytes[3] = shortBytes[1];
                    theBytes[2] = shortBytes[0];
                    theBytes[1] = shortBytesH[1];
                    theBytes[0] = shortBytesH[0];
                    break;
                }
                case(TYPE_S_INT32): {
                    theString = editTexts[sInt32].getText().toString();
                    int value = 0;
                    if(!theString.equals(""))
                        value = Integer.parseInt(theString);
                    theBytes = ByteBuffer.allocate(4).putInt(value).array();
                    break;
                }
                case(TYPE_FLOAT): {
                    theString = editTexts[sFloat].getText().toString();
                    float value = 0;
                    if(!theString.equals(""))
                        value = Float.parseFloat(theString);
                    theBytes = ByteBuffer.allocate(4).putFloat(value).array();
                    break;
                }
            }
            transmutation(theBytes);
        } catch (NumberFormatException nEx) {
            nEx.printStackTrace();
//            nEx.getMessage();
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
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
