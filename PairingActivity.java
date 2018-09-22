package com.multiaccess.chipsakti.component.printer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.multiaccess.chipsakti.R;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;



public class PairingActivity extends Activity implements Runnable{
    private static final String TAG = "PairingActivity";

    private ListView                    lsvw_paired;
    private DeviceListAdapter            mAdapter;
    private ProgressDialog              mProgressDlg;
    private ArrayList<BluetoothDevice>  mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter            mBluetoothAdapter;
    private ProgressDialog              mBluetoothConnectProgressDialog;
    private BluetoothDevice             bluetoothDevice;
    private BluetoothSocket             mBluetoothSocket;
    private UUID                        applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Bitmap                      mBarcode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_printer_pairing);

        lsvw_paired     = (ListView) findViewById(R.id.lsvw_paired_10);
        mAdapter	    = new DeviceListAdapter(this);

        lsvw_paired.setAdapter(mAdapter);
        mAdapter.setData(mDeviceList);
        ((TextView)findViewById(R.id.txvw_bonus_00)).setText("Bluetooth yang terpasang");

        Drawable arrow = getResources().getDrawable(R.drawable.ico_close);
        arrow.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY));
        ((ImageView)findViewById(R.id.imvw_right_00)).setImageDrawable(arrow);

        listener();
        /*
         * BLUETHOOTH CONNECTION
         */
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            showToast("No Paired Devices Found");
        } else {
            mDeviceList.addAll(pairedDevices);
        }
        mAdapter.notifyDataSetChanged();
        findViewById(R.id.mrly_right_00).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBarcode = createQRCode(getIntent().getStringExtra("INVOICE"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDeviceList.clear();
        if (mBluetoothAdapter != null){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices == null || pairedDevices.size() == 0) {
                showToast("No Paired Devices Found");
            } else {
                mDeviceList.addAll(pairedDevices);
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
            if (mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "Exe ", e);
        }
    }

    public void listener(){
        mAdapter.setOnConnectListenr(new DeviceListAdapter.OnConnectListener() {
            @Override
            public void onDeviceSelected(BluetoothDevice bt) {
                if (bt.getBondState() == BluetoothDevice.BOND_BONDED) {
                    try {
                        bluetoothDevice                 = bt;
                        mBluetoothConnectProgressDialog = ProgressDialog.show(PairingActivity.this, "Menghubungkan...", bt.getName() + " : " + bt.getAddress(), true, false);
                        Thread mBlutoothConnectThread   = new Thread(PairingActivity.this);
                        mBlutoothConnectThread.start();
                    }catch (Exception e){
                        showToast("Error Connecting to Bluetooth "+bt.getName());
                    }

                } else {
                    showToast("Pairing...");
                    pairDevice(bt);
                }
            }
        });


        mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();
        mProgressDlg 		= new ProgressDialog(this);
        mProgressDlg.setMessage("Pencarian...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });

        if (mBluetoothAdapter == null) {
            showUnsupported();
        } else {
            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

            registerReceiver(mReceiver, filter);

        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAGRZ", "requestCode " +requestCode +" | " + resultCode );
        if (requestCode == 1000){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            mDeviceList.addAll(pairedDevices);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void run() {
        try {
            mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            printData();

        } catch (IOException eConnectException) {
            Log.e(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);

            mBluetoothConnectProgressDialog.dismiss();
            PairingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PairingActivity.this,"Could Not Connect to "+bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(-99);
        PairingActivity.this.finish();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void showUnsupported() {
        showToast("Bluetooth is unsupported by this device");
    }
    private void showEnabled() {
        showToast("Bluetooth is Ready");
    }
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }

    private String getStruk(String pdata){

        return "TEST Koneksi";
    }

    private void printData(){
        Thread t = new Thread() {
            public void run() {
                try {
                    OutputStream os = mBluetoothSocket.getOutputStream();
                    StringBuilder sb = new StringBuilder();

                    StringBuilder sbSparator = new StringBuilder();
                    sbSparator.append("\n");
                    for (int i=0; i<31; i++){
                        sbSparator.append("-");
                    }
                    sbSparator.append("\n");

                    sb.append("Pembelian").append("\n");
                    sb.append(getIntent().getStringExtra("PRODUCT_NAME")).append("\n");
                    sb.append("Tujuan").append("\n");
                    sb.append(getIntent().getStringExtra("MSISDN")).append("\n");
                    sb.append("SN").append("\n");
                    sb.append(getIntent().getStringExtra("VSN")).append("\n");
                    sb.append("Waktu Transaksi").append("\n");
                    sb.append(getIntent().getStringExtra("DATE_TRANS"));


                    final String trx_id = getIntent().getStringExtra("INVOICE")+"\n";
                    String price    = getIntent().getStringExtra("SELLING_PRICE")+"\n";
                    String discount = getIntent().getStringExtra("DISCOUNT")+"";
                    String total    = getIntent().getStringExtra("TOTAL")+"\n";
                    String thks     = "\n Terimakasih telah berbelanja.Simpan struk ini sebagai bukti pembelian anda \n\n\n";

                    writeWithFormat(printImage(), new Formatter().get(), Formatter.centerAlign(),os);
                    writeWithFormat(trx_id.getBytes(), new Formatter().get(), Formatter.centerAlign(), os);
                    writeWithFormat(sb.toString().getBytes(), new Formatter().get(), Formatter.leftAlign(),os);
                    writeWithFormat(sbSparator.toString().getBytes(), new Formatter().get(), Formatter.leftAlign(),os);

                    writeWithFormat("Harga jual".getBytes(), new Formatter().get(), Formatter.leftAlign(),os);
                    String blank = " ";
                    for (int i=0; i<(31 - (price.length()+10)); i++){
                        blank = blank+" ";
                    }
                    writeWithFormat(blank.getBytes(), new Formatter().get(), Formatter.leftAlign(),os);
                    writeWithFormat(price.getBytes(), new Formatter().get(), Formatter.leftAlign(),os);

                    blank = " ";
                    for (int i=0; i<(31 - (discount.length()+7)); i++){
                        blank = blank+" ";
                    }

                    writeWithFormat("Diskon".getBytes(), new Formatter().get(), Formatter.leftAlign(),os);
                    writeWithFormat(blank.getBytes(), new Formatter().get(), Formatter.leftAlign(),os);
                    writeWithFormat(discount.getBytes(), new Formatter().get(), Formatter.leftAlign(),os);

                    writeWithFormat(sbSparator.toString().getBytes(), new Formatter().get(), Formatter.leftAlign(),os);

                    blank = " ";
                    for (int i=0; i<(31 - (total.length()+5)); i++){
                        blank = blank+" ";
                    }
                    writeWithFormat("Total".getBytes(), new Formatter().bold().get(), Formatter.leftAlign(),os);
                    writeWithFormat(blank.getBytes(), new Formatter().get(), Formatter.leftAlign(),os);
                    writeWithFormat(total.getBytes(), new Formatter().bold().get(), Formatter.rightAlign(),os);

                    writeWithFormat(thks.getBytes(), new Formatter().small2().get(), Formatter.leftAlign(),os);


                    os.flush();
                    mBluetoothConnectProgressDialog.dismiss();
                    onBackPressed();
                } catch (Exception e) {
                    Log.e(TAG, "Exe ", e);
                }
            }
        };
        t.start();
    }


    public boolean writeWithFormat(byte[] buffer, final byte[] pFormat, final byte[] pAlignment, OutputStream outputStream) {
        try {
            // Notify printer it should be printed with given alignment:
            outputStream.write(pAlignment);
            // Notify printer it should be printed in the given format:
            outputStream.write(pFormat);
            // Write the actual data:
            outputStream.write(buffer, 0, buffer.length);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
            return false;
        }
    }

    /*
     * BLUETHOOTH CONNECTION & RECEIVER
     */

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"BroadcastReceiver | action:  "+ action);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mAdapter.setData(mDeviceList);
                mAdapter.notifyDataSetChanged();
                mProgressDlg.dismiss();

            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                Log.d(TAG,"state : "+ state+ " | prevState :"+ prevState);
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Terhubung");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    showToast("Unpaired");
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };


    private byte[] printImage() {
        byte[] sendData = null;
        PrintPic pg = new PrintPic();
        pg.initCanvas(384);
        pg.initPaint();
        pg.drawImage(110, 0, mBarcode);
        sendData = pg.printDraw();
        return sendData;
    }

    private Bitmap createQRCode(String textToQR){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(textToQR, BarcodeFormat.QR_CODE,250,250);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 40, 40,170,170);
            ((ImageView)findViewById(R.id.imvw_test_00)).setVisibility(View.GONE);
            return croppedBitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
