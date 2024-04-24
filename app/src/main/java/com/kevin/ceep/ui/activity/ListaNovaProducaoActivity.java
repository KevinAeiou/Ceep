package com.kevin.ceep.ui.activity;

import static com.kevin.ceep.ui.Utilitario.comparaString;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.R;
import com.kevin.ceep.databinding.ActivityListaNovaProducaoBinding;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoEstoque;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoEspecificoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListaNovaProducaoActivity extends AppCompatActivity {
    private ActivityListaNovaProducaoBinding binding;
    private ProgressBar indicadorProgresso;
    private RecyclerView meuRecycler;
    private DatabaseReference minhaReferencia;
    private List<Trabalho> listaTrabalhosFiltrada;
    private ListaTrabalhoEspecificoAdapter listaTrabalhoEspecificoAdapter;
    private String personagemId;
    private HorizontalScrollView linearLayoutGruposChips;
    private ChipGroup grupoChipsProfissoes;
    private List<String> listaProfissoes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicializaComponentes();
        recebeDadosIntent();
        atualizaListaTodosTrabalhos();
    }

    private void configuraGrupoChipsProfissoes(List<Trabalho> todosTrabalhos) {
        Log.d("configuraGrupo","Passou aqui");
        listaProfissoes.clear();
        for (Trabalho trabalho : todosTrabalhos) {
            if (listaProfissoes.isEmpty()) {
                listaProfissoes.add(trabalho.getProfissao());
            } else {
                if (profissaoNaoExiste(trabalho)) {
                    listaProfissoes.add(trabalho.getProfissao());
                }
            }
        }
        if (!listaProfissoes.isEmpty()) {
            int idProfissao = 0;
            for (String profissao : listaProfissoes) {
                Chip chipProfissao = (Chip) LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_chip, null);
                chipProfissao.setText(profissao);
                chipProfissao.setId(idProfissao);
                grupoChipsProfissoes.addView(chipProfissao);
                idProfissao += 1;
            }
            List<String> profissoesSelecionadas = new ArrayList<>();
            grupoChipsProfissoes.setOnCheckedStateChangeListener((grupo, listaIds) -> {
                profissoesSelecionadas.clear();
                for (int id : listaIds) {
                    profissoesSelecionadas.add(listaProfissoes.get(id));
                }
                if (!profissoesSelecionadas.isEmpty()) {
                    listaTrabalhosFiltrada = new ArrayList<>();
                    for (String profissao : profissoesSelecionadas) {
                        Log.d("configuraGrupo", "Selecionada: "+profissao);
                    }
                    for (Trabalho trabalho: todosTrabalhos) {
                        if (profissaoNaoExiste(trabalho)) {
                            listaTrabalhosFiltrada.add(trabalho);
                        }
                    }/*
                    if (listaTrabalhosFiltrada.isEmpty()) {
                        listaTrabalhoEspecificoAdapter.limpaLista();
                        Snackbar.make(binding.constrintLayoutListaNovaProducao, "Nem um resultado encontrado!", Snackbar.LENGTH_LONG).show();
                    }*/
                } else {
                    Log.d("configuraGrupo", "Nenhuma selecionada!");
                    listaTrabalhosFiltrada = todosTrabalhos;
                }
                listaTrabalhoEspecificoAdapter.setListaFiltrada(listaTrabalhosFiltrada);
            });
        }
    }

    private boolean profissaoNaoExiste(Trabalho trabalho) {
        for (String profissao : listaProfissoes) {
            if (comparaString(profissao, trabalho.getProfissao())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_personagem, menu);
        MenuItem itemBusca = menu.findItem(R.id.itemMenuBusca);
        itemBusca.setOnMenuItemClickListener(item -> {
            linearLayoutGruposChips.setVisibility(View.VISIBLE);
            return true;
        });

        configuraCampoDeVBusca(itemBusca);
        return super.onCreateOptionsMenu(menu);
    }

    private void configuraCampoDeVBusca(MenuItem itemBusca) {
        androidx.appcompat.widget.SearchView busca = (androidx.appcompat.widget.SearchView) itemBusca.getActionView();
        busca.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtroLista(newText);
                return false;
            }
        });
    }

    private void filtroLista(String newText) {
        List<Trabalho> listaFiltrada = new ArrayList<>();
        for (Trabalho trabalho : listaTrabalhosFiltrada) {
            if (comparaString(trabalho.getNome(), newText)) {
                listaFiltrada.add(trabalho);
            }
        }
        if (listaFiltrada.isEmpty()) {
            listaTrabalhoEspecificoAdapter.limpaLista();
            Snackbar.make(binding.getRoot(),"Nem um resultado encontrado!", Snackbar.LENGTH_LONG).show();
        } else {
            listaTrabalhoEspecificoAdapter.setListaFiltrada(listaFiltrada);
        }
    }

    private void recebeDadosIntent() {
        Intent dadosRecebidos = getIntent();
        if (dadosRecebidos.hasExtra(CHAVE_PERSONAGEM)) {
            personagemId = (String) dadosRecebidos.getSerializableExtra(CHAVE_PERSONAGEM);
        }
    }

    private void atualizaListaTodosTrabalhos() {
        List<Trabalho> todosTrabalhos = pegaTodosTrabalhos();
        configuraMeuRecycler(todosTrabalhos);
    }

    private void configuraMeuRecycler(List<Trabalho> todosTrabalhos) {
        meuRecycler.setHasFixedSize(true);
        meuRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        configuraAdapter(todosTrabalhos, meuRecycler);
    }

    private void configuraAdapter(List<Trabalho> todosTrabalhos, RecyclerView meuRecycler) {
        listaTrabalhoEspecificoAdapter = new ListaTrabalhoEspecificoAdapter(getApplicationContext(), todosTrabalhos, null);
        meuRecycler.setAdapter(listaTrabalhoEspecificoAdapter);
        listaTrabalhoEspecificoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Profissao profissao, int posicao) {

            }

            @Override
            public void onItemClick(Trabalho trabalho, int adapterPosition) {
                vaiParaConfirmaTrabalhoActivity(trabalho);
            }

            @Override
            public void onItemClick(TrabalhoEstoque trabalhoEstoque, int adapterPosition, int botaoId) {

            }
        });
    }

    private void vaiParaConfirmaTrabalhoActivity(Trabalho trabalho) {
        Intent iniciaVaiParaConfirmaTrabalhoActivity = new Intent(getApplicationContext(), ConfirmaTrabalhoActivity.class);
        iniciaVaiParaConfirmaTrabalhoActivity.putExtra(CHAVE_TRABALHO, trabalho);
        iniciaVaiParaConfirmaTrabalhoActivity.putExtra(CHAVE_PERSONAGEM, personagemId);
        startActivity(iniciaVaiParaConfirmaTrabalhoActivity);
        finish();
    }

    private List<Trabalho> pegaTodosTrabalhos() {
        List<Trabalho> todosTrabalhos = new ArrayList<>();
        listaTrabalhosFiltrada = new ArrayList<>();
        minhaReferencia.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                todosTrabalhos.clear();
                for (DataSnapshot dn : dataSnapshot.getChildren()) {
                    Trabalho trabalho = dn.getValue(Trabalho.class);
                    if (trabalho != null) {
                        todosTrabalhos.add(trabalho);
                    }
                }
                todosTrabalhos.sort(Comparator.comparing(Trabalho::getProfissao).thenComparing(Trabalho::getRaridade).thenComparing(Trabalho::getNivel).thenComparing(Trabalho::getNome));
                listaTrabalhosFiltrada = todosTrabalhos;
                configuraGrupoChipsProfissoes(todosTrabalhos);
                indicadorProgresso.setVisibility(View.GONE);
                listaTrabalhoEspecificoAdapter.setListaFiltrada(listaTrabalhosFiltrada);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(binding.constrintLayoutListaNovaProducao, "Erro ao carregar dados: "+ databaseError, Snackbar.LENGTH_LONG).show();
            }
        });
        return todosTrabalhos;
    }

    private void inicializaComponentes() {
        binding = ActivityListaNovaProducaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        indicadorProgresso = binding.indicadorProgressoListaNovaProducao;
        meuRecycler = binding.recyclerViewListaNovaProducao;
        FirebaseDatabase meuBanco = FirebaseDatabase.getInstance();
        minhaReferencia = meuBanco.getReference(CHAVE_LISTA_TRABALHO);
        linearLayoutGruposChips = binding.linearLayoutGrupoChipsListaNovaProducao;
        grupoChipsProfissoes = binding.grupoProfissoesChipListaNovaProducao;
        listaProfissoes = new ArrayList<>();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding = null;
    }
}