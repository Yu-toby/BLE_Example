package com.jetec.ble_example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private OnItemClick onItemClick;
    private List<ScannedData> arrayList = new ArrayList<>();
    private Activity activity;
    private LinkedList<HashMap<String,String>> rssiData;

    private int rssiAvg,Variance,count=0,datacount=0,diff=0,lastrssi,orirssi;
    private float dis;

    public RecyclerViewAdapter(Activity activity) {
        this.activity = activity;
    }
    public void OnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick;
    }
    /**清除搜尋到的裝置列表*/
    public void clearDevice(){
        this.arrayList.clear();
        notifyDataSetChanged();
    }
    /**若有不重複的裝置出現，則加入列表中*/
    public void addDevice(List<ScannedData> arrayList){
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName,tvAddress,tvInfo,tvRssi,tvAvRssi,tvDistance;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.textView_DeviceName);
            tvAddress = itemView.findViewById(R.id.textView_Address);
            tvInfo = itemView.findViewById(R.id.textView_ScanRecord);
            tvRssi = itemView.findViewById(R.id.textView_Rssi);
            tvAvRssi = itemView.findViewById(R.id.textView_AvRssi);
            tvDistance = itemView.findViewById(R.id.textView_Distance);
            rssiData = new LinkedList<>();

        }
    }

    @NonNull
    @Override
    /**產生介面*/
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanned_item,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    /**資料進來後對應的動作*/
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        orirssi = Integer.parseInt(arrayList.get(position).getRssi());

        HashMap<String,String> row = new HashMap<>();
        if (rssiData.size()<=15) {
            Log.v("wyc","<15");
            row.put("rssidata",arrayList.get(position).getRssi());
            rssiData.add(row);
        }
        else {
            Log.v("wyc",">15");
            diff = Math.abs(Math.abs(lastrssi)-Math.abs(rssiAvg/15));
            Log.v("wyc","lastrssi : " + lastrssi);
            Log.v("wyc","diff : " + diff);

            if (Math.abs(Math.abs(orirssi)-Math.abs(rssiAvg/15))<3) {
                row.put("rssidata",arrayList.get(position).getRssi());
                rssiData.add(row);
            }
            else {
                if (orirssi<(rssiAvg/15)) {
                    orirssi += diff;
                    row.put("rssidata", String.valueOf(orirssi));
                    rssiData.add(row);
                }
                else if (orirssi>(rssiAvg/15)) {
                    orirssi -= diff;
                    row.put("rssidata", String.valueOf(orirssi));
                    rssiData.add(row);
                }
            }
        }

//        Log.v("wyc","Name: " + arrayList.get(position).getDeviceName() + "ori:" + orirssi);

        if (rssiData.size()>=15) {
            /** 15個數的和 */
            rssiAvg = 0;
            for (int i=(rssiData.size()-1);i>=(rssiData.size()-15);i--) {
                rssiAvg += Integer.parseInt(rssiData.get(i).get("rssidata"));
            }

            /** RSSI換算距離 */
            int orirs = Integer.parseInt(arrayList.get(position).getRssi());
            dis = (float) ((float) Math.round(((float) Math.pow(10,(float)(- orirs - 63)/20))*100.0)/100.0);

            holder.tvName.setText(arrayList.get(position).getDeviceName());
            holder.tvAddress.setText("裝置位址："+arrayList.get(position).getAddress());         //54:6C:0E:9B:5C:79
            holder.tvInfo.setText("裝置挾帶的資訊：\n"+arrayList.get(position).getDeviceByteInfo());
            holder.tvAvRssi.setText("濾波強度："+ rssiAvg/15);       //濾波
            holder.tvRssi.setText("訊號強度："+arrayList.get(position).getRssi());     //原始訊號強度
            holder.tvDistance.setText("距離："+ dis + "m");
            holder.itemView.setOnClickListener(v -> {
                onItemClick.onItemClick(arrayList.get(position));
            });
//            Log.v("wyc","avg:" + rssiAvg/15);
//            Log.v("wyc","dis:" + dis);
//            Log.v("wyc","ori:" + arrayList.get(position).getRssi());

            lastrssi = Integer.parseInt(arrayList.get(position).getRssi());
        }
    }

    @Override
    /**總共幾筆資料*/
    public int getItemCount() {
        return arrayList.size();
    }
    interface OnItemClick{
        void onItemClick(ScannedData selectedDevice);
    }

//    public int distance(int rssi){
//        return (int) Math.pow(10,(Math.abs(rssi) - 59)/(20));
//    }


}
