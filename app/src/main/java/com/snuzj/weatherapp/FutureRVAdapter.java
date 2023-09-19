package com.snuzj.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FutureRVAdapter extends RecyclerView.Adapter<FutureRVAdapter.ViewHolder>{

    private Context context;
    private ArrayList<FutureRVModel> futureRVModelArrayList;

    public FutureRVAdapter(Context context, ArrayList<FutureRVModel> futureRVModelArrayList) {
        this.context = context;
        this.futureRVModelArrayList = futureRVModelArrayList;
    }

    @NonNull
    @Override
    public FutureRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.future_weather_item,parent,false);
        return new FutureRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FutureRVAdapter.ViewHolder holder, int position) {
        FutureRVModel model = futureRVModelArrayList.get(position);

        holder.temperatureTv.setText(model.getTemperature()+"°C");
        Picasso.get().load("https://openweathermap.org/img/w/"+model.getIcon()+".png").into(holder.conditionIv);

        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try{
            Date t = input.parse(model.getDate());
            holder.dateTv.setText(input.format(t));
        } catch (ParseException e){
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return futureRVModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTv,temperatureTv, conditionTv;
        private ImageView conditionIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            conditionTv = itemView.findViewById(R.id.conditionTv);
            temperatureTv = itemView.findViewById(R.id.temperatureTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            conditionIv = itemView.findViewById(R.id.conditionIv);


        }
    }
}
