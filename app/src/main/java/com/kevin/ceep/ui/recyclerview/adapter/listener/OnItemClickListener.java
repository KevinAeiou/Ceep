package com.kevin.ceep.ui.recyclerview.adapter.listener;

import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.Raridade;
import com.kevin.ceep.model.Trabalho;

public interface OnItemClickListener {

    void onItemClick(Profissao profissao, int posicao);
    void onItemClick(Raridade raridade, int posicao);
    void onItemClick(Trabalho trabalho, int adapterPosition);
}
