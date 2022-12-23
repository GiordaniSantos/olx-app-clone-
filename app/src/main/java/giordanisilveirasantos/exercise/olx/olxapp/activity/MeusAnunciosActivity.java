package giordanisilveirasantos.exercise.olx.olxapp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;
import giordanisilveirasantos.exercise.olx.olxapp.R;
import giordanisilveirasantos.exercise.olx.olxapp.adapter.AdapterAnuncios;
import giordanisilveirasantos.exercise.olx.olxapp.databinding.ActivityMeusAnunciosBinding;
import giordanisilveirasantos.exercise.olx.olxapp.helper.ConfiguracaoFirebase;
import giordanisilveirasantos.exercise.olx.olxapp.helper.RecyclerItemClickListener;
import giordanisilveirasantos.exercise.olx.olxapp.model.Anuncio;

public class MeusAnunciosActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMeusAnunciosBinding binding;
    private RecyclerView recyclerAnuncios;
    private List<Anuncio> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMeusAnunciosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //configura toolbar


        //configurações iniciais
        anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase().child("meus_anuncios").child(ConfiguracaoFirebase.getIdUsuario());

        inicializaComponentes();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Meus Anúncios");

        //configurar recyclerView
        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuncios.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncios, this);
        recyclerAnuncios.setAdapter(adapterAnuncios);

        //recupera anuncios para o usuario
        recuperarAnuncios();

        //Adiciona o evento de clique no recyclerView
        recyclerAnuncios.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnuncios,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                Anuncio anuncioSelecionado = anuncios.get(position);
                                anuncioSelecionado.remover();

                                adapterAnuncios.notifyDataSetChanged();
                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );

        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_meus_anuncios);
        //appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CadastrarAnuncioActivity.class));
            }
        });
    }

    private void recuperarAnuncios(){
        dialog = new SpotsDialog.Builder().setContext(this).setMessage("Buscando anúncios").setCancelable(false).build();
        dialog.show();
        anuncioUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                anuncios.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    anuncios.add(ds.getValue(Anuncio.class));
                }
                Collections.reverse(anuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void inicializaComponentes(){
        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);
    }

}