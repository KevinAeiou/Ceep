package com.kevin.ceep.ui.fragment;

import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_DESEJO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_NOME_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_LISTA_PERSONAGEM;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_TRABALHO;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CHAVE_USUARIOS;
import static com.kevin.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_ALTERA_TRABALHO;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevin.ceep.R;
import com.kevin.ceep.model.Personagem;
import com.kevin.ceep.model.Profissao;
import com.kevin.ceep.model.Raridade;
import com.kevin.ceep.model.Trabalho;
import com.kevin.ceep.model.TrabalhoProducao;
import com.kevin.ceep.ui.activity.ListaRaridadeActivity;
import com.kevin.ceep.ui.activity.TrabalhoEspecificoActivity;
import com.kevin.ceep.ui.recyclerview.adapter.ListaTrabalhoProducaoAdapter;
import com.kevin.ceep.ui.recyclerview.adapter.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class ListaTrabalhosFragment extends Fragment {
    private static final String TAG="MainActivity";
    ActivityResultLauncher<Intent> activityLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG,"onActivityResult");
                if (result.getResultCode()==1){
                    Intent intent=result.getData();
                    if (intent!=null){

                    }
                }
            }
    );
    private DatabaseReference databaseReference;
    private ListaTrabalhoProducaoAdapter trabalhoAdapter;
    private RecyclerView recyclerView;
    private List<TrabalhoProducao> trabalhos;
    private String usuarioId, personagemId;
    private List<Personagem> personagens;
    private Menu itemMenuPersonagem;
    private SwipeRefreshLayout swipeRefreshLayout;
    public ListaTrabalhosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_trabalhos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inicializaComponentes(view);
        List<Personagem> todosPersonagens = pegaTodosPersonagens();
        if (todosPersonagens.size() > 0){
            atualizaListaTrabalho();
        }else{
            Log.d("listaPersonagem", "Lista todosPersonagens está vazia.");
        }
        configuraSwipeRefreshLayout(view);
        configuraBotaoInsereTrabalho(view);
        configuraDeslizeItem();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_personagem, menu);
        itemMenuPersonagem = menu;
        MenuItem itemBusca = menu.findItem(R.id.itemMenuBusca);
        SearchView busca = (SearchView) itemBusca.getActionView();
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void filtroLista(String newText) {
        // creating a new array list to filter our data.
        List<TrabalhoProducao> listaFiltrada = new ArrayList<>();

        // running a for loop to compare elements.
        for (TrabalhoProducao item : trabalhos) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getNome().toLowerCase().contains(newText.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                listaFiltrada.add(item);
            }
        }
        if (listaFiltrada.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Snackbar.make(getView(),"Nem um resultado encontrado!", Snackbar.LENGTH_LONG).show();
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            trabalhoAdapter.setListaFiltrada(listaFiltrada);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("itemMenuSelecionado", String.valueOf(item));
        Log.d("listaPersonagens", "Personagens na lista do menu:");
        for (Personagem personagem: personagens){
            Log.d("listaPersonagens", personagem.getNome());
            if (personagem.getNome().equals(item.getTitle().toString())){
                personagemId = personagem.getId();
                atualizaListaTrabalho();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void configuraDeslizeItem() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int posicaoDeslize = viewHolder.getAdapterPosition();
                ListaTrabalhoProducaoAdapter trabalhoAdapter = (ListaTrabalhoProducaoAdapter) recyclerView.getAdapter();
                removeTrabalhoLista(posicaoDeslize);
                if (trabalhoAdapter != null) {
                    trabalhoAdapter.remove(posicaoDeslize);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void removeTrabalhoLista(int swipePosicao) {
        String idTrabalho = trabalhos.get(swipePosicao).getId();
        databaseReference.child(usuarioId).child(CHAVE_LISTA_PERSONAGEM).
                child(personagemId).child(CHAVE_LISTA_DESEJO).
                child(idTrabalho).removeValue();
    }
    private void configuraSwipeRefreshLayout(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutTrabalhos);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (personagemId != null){
                atualizaListaTrabalho();
            }
        });
    }
    private void configuraBotaoInsereTrabalho(View view) {
        FloatingActionButton botaoInsereTrabaho = view.findViewById(R.id.floatingActionButton);
        botaoInsereTrabaho.setOnClickListener(v -> vaiParaRaridadeActivity());
    }
    private void vaiParaRaridadeActivity() {
        Intent iniciaListaRaridade =
                new Intent(getContext(),
                        ListaRaridadeActivity.class);
        iniciaListaRaridade.putExtra(CHAVE_PERSONAGEM, personagemId);
        startActivity(iniciaListaRaridade,
                ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        //startActivityForResult(iniciaFormularioNota, CODIGO_REQUISICAO_INSERE_NOTA);
    }

    private void inicializaComponentes(View view) {
        usuarioId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        recyclerView = view.findViewById(R.id.listaTrabalhoRecyclerView);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(CHAVE_USUARIOS);
    }
    private void atualizaListaTrabalho() {
        List<TrabalhoProducao> todosTrabalhos = pegaTodosTrabalhos();
        configuraRecyclerView(todosTrabalhos);
    }
    private void configuraRecyclerView(List<TrabalhoProducao> todosTrabalhos) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        configuraAdapter(todosTrabalhos, recyclerView);
    }
    private void configuraAdapter(List<TrabalhoProducao> todosTrabalhos, RecyclerView listaTrabalhos) {
        trabalhoAdapter = new ListaTrabalhoProducaoAdapter(getContext(),todosTrabalhos);
        listaTrabalhos.setAdapter(trabalhoAdapter);
        trabalhoAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Profissao profissao, int posicao) {

            }

            @Override
            public void onItemClick(Personagem personagem, int posicao) {

            }

            @Override
            public void onItemClick(Trabalho trabalho, int adapterPosition) {
                vaiParaTrabalhoEspecificoActivity(trabalho);
            }

            @Override
            public void onItemClick(Raridade raridade, int adapterPosition) {

            }
        });
    }
    private void vaiParaTrabalhoEspecificoActivity(Trabalho trabalho) {
        Intent iniciaTrabalhoEspecificoActivity=
                new Intent(getActivity(), TrabalhoEspecificoActivity.class);
        iniciaTrabalhoEspecificoActivity.putExtra(CHAVE_TRABALHO, CODIGO_REQUISICAO_ALTERA_TRABALHO);
        iniciaTrabalhoEspecificoActivity.putExtra(CHAVE_NOME_TRABALHO, trabalho);
        iniciaTrabalhoEspecificoActivity.putExtra(CHAVE_PERSONAGEM, personagemId);
        activityLauncher.launch(iniciaTrabalhoEspecificoActivity);
    }
    private List<Personagem> pegaTodosPersonagens() {
        Log.d("listaPersonagens", "Entrou na funçao pegaTodosPersonagens");
        personagens = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference(CHAVE_USUARIOS);
        databaseReference.child(usuarioId).child(CHAVE_LISTA_PERSONAGEM).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        personagens.clear();
                        Log.d("listaPersonagens", "Limpou a lista de personagens");
                        for (DataSnapshot dn:dataSnapshot.getChildren()){
                            Personagem personagem = dn.getValue(Personagem.class);
                            personagens.add(personagem);
                            Log.d("listaPersonagens", "Personagem adicionado: " + personagem.getNome());
                            itemMenuPersonagem.add(personagem.getNome());
                        }
                        personagemId = personagens.get(0).getId();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("listaPersonagens", "Erro ao definir lista de personagens.");
                    }

                });
        Log.d("listaPersonagens", "Saiu da funçao pegaTodosPersonagens");
        return personagens;
    }
    private List<TrabalhoProducao> pegaTodosTrabalhos() {
        trabalhos = new ArrayList<>();
        Log.d("USUARIO", usuarioId);
        Log.d("USUARIO", personagemId);
        databaseReference.child(usuarioId).child(CHAVE_LISTA_PERSONAGEM).
                child(personagemId).child(CHAVE_LISTA_DESEJO).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        trabalhos.clear();
                        for (DataSnapshot dn:dataSnapshot.getChildren()){
                            TrabalhoProducao trabalho = dn.getValue(TrabalhoProducao.class);
                            trabalhos.add(trabalho);
                        }
                        trabalhoAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        return trabalhos;
    }
}