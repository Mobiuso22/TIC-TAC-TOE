package com.experiments.tictactoe.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.experiments.tictactoe.R;
import com.experiments.tictactoe.models.Tile;
import com.experiments.tictactoe.utility.ItemClickListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;



public class TilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "TilesAdapter";
    private final List<Tile> tiles;
    private LayoutInflater inflater;
    private ItemClickListener<Tile> itemClickListener;

    public TilesAdapter(Context context, List<Tile> tiles) {
        this.tiles = tiles;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_tile, parent, false);
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
        TileViewHolder tileHolder = (TileViewHolder) holder;
        final Tile tile = tiles.get(position);

        int status = R.drawable.blank;
        switch (tile.getTileStatus()) {
            case BLANK:
                status = R.drawable.blank;
                break;
            case ZERO:
                status = R.drawable.zero;
                break;
            case CROSS:
                status = R.drawable.cross;
                break;
        }
        tileHolder.ivTile.setImageResource(status);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(tile);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }

    static class TileViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tvTile)
        AppCompatImageView ivTile;

        public TileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setItemClickListener(ItemClickListener<Tile> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
