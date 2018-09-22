package com.multiaccess.chipsakti.component.printer;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.multiaccess.chipsakti.R;

import java.util.List;

public class DeviceListAdapter extends  BaseAdapter {
    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnPairButtonClickListener mListener;

    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.component_adapter_printer, null);

            holder 				= new ViewHolder();

            holder.nameTv		= (TextView) convertView.findViewById(R.id.txvw_name_20);
            holder.macAddr		= (TextView) convertView.findViewById(R.id.txvw_macaddr_21);
//            holder.imvw_bluetooth_10 = (ImageView) convertView.findViewById(R.id.imvw_bluetooth_10);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final BluetoothDevice device	= mData.get(position);

        holder.nameTv.setText(device.getName());
        holder.macAddr.setText(device.getAddress());
        holder.nameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectListener != null){
                    mConnectListener.onDeviceSelected(device);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView macAddr;
        ImageView imvw_bluetooth_10;
    }

    public interface OnPairButtonClickListener {
        public abstract void onPairButtonClick(int position);
    }

    private OnConnectListener mConnectListener;
    public void setOnConnectListenr(OnConnectListener pListener){
        mConnectListener = pListener;
    }
    public interface OnConnectListener{
        public void onDeviceSelected(BluetoothDevice bt);
    }
}