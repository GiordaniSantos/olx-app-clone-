package giordanisilveirasantos.exercise.olx.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.widget.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import giordanisilveirasantos.exercise.olx.olxapp.R;
import giordanisilveirasantos.exercise.olx.olxapp.helper.ConfiguracaoFirebase;
import giordanisilveirasantos.exercise.olx.olxapp.helper.Permissoes;
import giordanisilveirasantos.exercise.olx.olxapp.model.Anuncio;

public class CadastrarAnuncioActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText campoTitulo, campoDescricao;
    private ImageView imagem1, imagem2, imagem3;
    private Spinner campoEstado, campoCategoria;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private Anuncio anuncio;
    private StorageReference storage;
    private android.app.AlertDialog dialog;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaUrlFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        //configura toolbar
        getSupportActionBar().setTitle("Cadastrar anúncio");

        //cofigurações iniciais
        storage = ConfiguracaoFirebase.getFireBaseStorage();

        //validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializaComponentes();
        carregarDadosSpinner();
    }


    public void salvarAnuncio(){

        dialog = new SpotsDialog.Builder().setContext(this).setMessage("Salvando anúncio").setCancelable(false).build();
        dialog.show();


        //Salvar imagem no Storage
        for(int i = 0; i<listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }


    }

    private void salvarFotoStorage(String url, int totalFotos, int contador){

        //Criar nó no storage
        final StorageReference imagemAnuncio = storage.child("imagens").child("anuncios").child(anuncio.getIdAnuncio()).child("imagem"+contador);

        //fazer upload do arquivo
        final UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(url));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String urlConvertida = uri.toString(); //esta url funciona
                        listaUrlFotos.add(urlConvertida);
                        if(totalFotos == listaUrlFotos.size()){
                            anuncio.setFotos(listaUrlFotos);
                            anuncio.salvar();

                            dialog.dismiss();
                            finish();
                        }

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload!");
                Log.i("INFO", "Falha ao fazer uploads: "+e.getMessage());
            }
        });

    }

    private Anuncio configurarAnuncio(){
        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        String descricao = campoDescricao.getText().toString();

        anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    public void validarDadosAnuncio(View view){
        anuncio = configurarAnuncio();
        String valor = String.valueOf(campoValor.getRawValue());

        if(listaFotosRecuperadas.size() != 0){
            if(!anuncio.getEstado().isEmpty()){
                if(!anuncio.getCategoria().isEmpty()){
                    if(!anuncio.getTitulo().isEmpty()){
                        if(!valor.isEmpty() && !valor.equals("0")){
                            if(!anuncio.getTelefone().isEmpty() && anuncio.getTelefone().length() >= 10){
                                if(!anuncio.getDescricao().isEmpty()){
                                    salvarAnuncio();
                                }else{
                                    exibirMensagemErro("Preencha o campo descrição!");
                                }
                            }else{
                                exibirMensagemErro("Preencha o campo telefone!");
                            }
                        }else{
                            exibirMensagemErro("Preencha o campo valor!");
                        }
                    }else{
                        exibirMensagemErro("Preencha o campo titulo!");
                    }
                }else{
                    exibirMensagemErro("Preencha o campo categoria!");
                }
            }else{
                exibirMensagemErro("Preencha o campo estado!");
            }

        }else{
            exibirMensagemErro("Selecione ao menos uma foto!");
        }
    }

    private void exibirMensagemErro(String mensagem){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }


    private void carregarDadosSpinner(){
        /*String[] estados = new String[]{
        "SP", "RS"
        }*/
        //configura spinner estado
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapter);

        //configura spinner categoria
        String[] categorias = getResources().getStringArray(R.array.categoria);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);

    }



    private void inicializaComponentes(){
        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);

        //configura localidade para pt-BR
        Locale locale = new Locale("pt","BR");
        campoValor.setLocale(locale);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidaPermissoes();
            }
        }

    }

    private void alertaValidaPermissoes(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;

        }
    }

    public void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            //Recuperar Imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImage = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if(requestCode == 1){
                imagem1.setImageURI(imagemSelecionada);
            }else if(requestCode == 2){
                imagem2.setImageURI(imagemSelecionada);
            }else if(requestCode == 3){
                imagem3.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImage);
        }
    }
}