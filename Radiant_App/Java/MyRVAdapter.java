package com.example.knight_radiant_app;

import android.content.Context;

import android.content.Intent;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.ViewHolder> {
    private ArrayList<String> mListItems;
    private Context mContext;
    private ArrayList<String> urlList;

    public MyRVAdapter(ArrayList<String> inputList,ArrayList<String> urls) {
         urlList = new ArrayList<>(urls);


        mListItems = inputList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        protected View itemLayout;
        protected TextView itemTvData;


        public ViewHolder(View view){
            super(view);
            itemLayout = view;
            itemTvData = (TextView) view.findViewById(R.id.hike_data);

            itemTvData.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @NonNull
    @Override
    public MyRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View myView = layoutInflater.inflate(R.layout.rv_item_layout,parent,false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final int adapterPosition = holder.getAdapterPosition();
        holder.itemTvData.setText(mListItems.get(adapterPosition));
        holder.itemLayout.setOnClickListener(new View.OnClickListener(){
                                                 @Override
                                                 public void onClick(View view) {

                                                     CharSequence link = urlList.get(adapterPosition);
                                                     Intent i = new Intent(Intent.ACTION_VIEW);
                                                     i.setData(Uri.parse((String) link));
                                                     mContext.startActivity(i);
                                                 }
                                             }
        );
    }

    public void remove(int position){
        mListItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {

        return mListItems.size();
    }

}
