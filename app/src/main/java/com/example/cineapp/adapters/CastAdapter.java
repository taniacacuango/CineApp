package com.example.cineapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cineapp.R;
import com.example.cineapp.models.Cast;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private List<Cast> castList;

    public CastAdapter(List<Cast> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.tvName.setText(cast.getName());

        // Cargamos la foto del actor. Si no tiene foto, Glide manejará el error.
        Glide.with(holder.itemView.getContext())
                .load(cast.getFullProfilePath())
                .placeholder(R.drawable.ic_launcher_background) // Imagen temporal mientras carga
                .circleCrop() // Esto hace que la foto sea circular y se vea más profesional
                .into(holder.ivProfile);
    }

    @Override
    public int getItemCount() {
        // Mostramos solo los primeros 10 actores para no saturar la pantalla
        return Math.min(castList.size(), 10);
    }

    public static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivCastProfile);
            tvName = itemView.findViewById(R.id.tvCastName);
        }
    }
}