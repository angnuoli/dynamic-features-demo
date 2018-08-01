package com.gjxhlan.dynamicfeatures.ondemand;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gjxhlan.dynamicfeatures.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DynamicFeatureActivity1 extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int resid = getResources().getIdentifier("activity_image_collection_cop", "layout", getPackageName());
        setContentView(getLayoutInflater().inflate(resid, null));

        RecyclerView recyclerView = findViewById(getResources().getIdentifier("images_list_cop", "id", getPackageName()));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            list.add("view" + i);
        }
        adapter.setList(list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.notifyDataSetChanged();
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        List<String> list = new ArrayList<>();

        public void setList(List<String> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(getBaseContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 500));
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ImageView imageView = (ImageView) holder.itemView;
            imageView.setImageResource(R.drawable.view6);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
