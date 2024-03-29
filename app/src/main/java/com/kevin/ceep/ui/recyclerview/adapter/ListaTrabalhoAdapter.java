package com.kevin.ceep.ui.recyclerview.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.ceep.R;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;

import java.util.List;

public class ListaTrabalhoAdapter extends RecyclerView.Adapter<ListaTrabalhoAdapter.TrabalhoViewHolder> {

    private List<Trabalho> trabalhos;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ListaTrabalhoAdapter(Context context,List<Trabalho> trabalho) {
        this.trabalhos = trabalho;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setListaFiltrada(List<Trabalho> listaFiltrada){
        this.trabalhos=listaFiltrada;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrabalhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_trabalho, parent, false);
        return new TrabalhoViewHolder(viewCriada);
    }

    @Override
    public void onBindViewHolder(@NonNull TrabalhoViewHolder holder, int position) {
        Trabalho trabalho = trabalhos.get(position);
        holder.vincula(trabalho);
    }

    @Override
    public int getItemCount() {
        return trabalhos.size();
    }

    public void altera(int posicao, Trabalho nota) {
        trabalhos.set(posicao, nota);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position < 0 || position >= trabalhos.size()) {
            return;
        }
        trabalhos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,trabalhos.size());
        notifyDataSetChanged();
    }

    public class TrabalhoViewHolder extends RecyclerView.ViewHolder{

        private final CardView cardview_trabalho;
        private final TextView nome_trabalho;
        private final TextView profissao_trabalho;
        private final TextView nivel_trabalho;
        private Trabalho trabalho;

        public TrabalhoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardview_trabalho = itemView.findViewById(R.id.itemCardViewTrabalho);
            nome_trabalho = itemView.findViewById(R.id.itemNomeTrabalho);
            profissao_trabalho = itemView.findViewById(R.id.itemProfissaoTrabalho);
            nivel_trabalho = itemView.findViewById(R.id.itemNivelTrabalho);
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trabalho, getAdapterPosition()));
        }
        public void vincula(Trabalho trabalho) {
            this.trabalho = trabalho;
            preencheCampo(trabalho);
        }
        private void preencheCampo(Trabalho trabalho) {
            nome_trabalho.setText(trabalho.getNome());
            configuraCorNomeTrabalho(trabalho);
            profissao_trabalho.setText(trabalho.getProfissao());
            profissao_trabalho.setTextColor(Color.WHITE);
            nivel_trabalho.setText(String.valueOf(trabalho.getNivel()));
            nivel_trabalho.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_nivel));
        }

        private void configuraCorNomeTrabalho(Trabalho trabalho) {
            String raridade = trabalho.getRaridade();
            if (raridade.equals("Comum")){
                nome_trabalho.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_raridade_comum));
            }else if (raridade.equals("Raro")||raridade.equals("Melhorado")){
                nome_trabalho.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_raridade_raro));
            }else if (raridade.equals("Especial")){
                nome_trabalho.setTextColor(ContextCompat.getColor(context,R.color.cor_texto_raridade_especial));
            }
        }
    }
    public void adiciona(Trabalho trabalho){
        trabalhos.add(trabalho);
        notifyDataSetChanged();
    }
}
