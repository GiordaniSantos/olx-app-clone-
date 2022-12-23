package giordanisilveirasantos.exercise.olx.olxapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import giordanisilveirasantos.exercise.olx.olxapp.R;
import giordanisilveirasantos.exercise.olx.olxapp.helper.ConfiguracaoFirebase;

public class CadastroActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso;

    private FirebaseAuth autenticacao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        //configura toolbar
        getSupportActionBar().setTitle("Logar/Cadastrar");

        inicializaComponentes();
        autenticacao = ConfiguracaoFirebase.getFireBaseAutenticacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if(!email.isEmpty()){
                    if(!senha.isEmpty()){
                        //verificar o estado do switch
                        if(tipoAcesso.isChecked()){ //cadastro
                            autenticacao.createUserWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){

                                        //direcionar para a tela principal

                                        Toast.makeText(CadastroActivity.this,"Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                    }else{
                                        String erroExcecao = "";

                                        try{
                                            throw task.getException();
                                        }catch (FirebaseAuthWeakPasswordException e){
                                            erroExcecao = "Digite uma senha mais forte!";
                                        }catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "Por favor, digite um email válido!";
                                        }catch (FirebaseAuthUserCollisionException e){
                                            erroExcecao = "Esta conta já foi criada!";
                                        }catch (Exception e){
                                            erroExcecao = "Ao cadastrar usuario: "+e.getMessage();
                                            e.printStackTrace();
                                        }

                                        Toast.makeText(CadastroActivity.this,"Erro: "+erroExcecao, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{ //login
                            autenticacao.signInWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(CadastroActivity.this,"Logado com sucesso!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
                                        }else{
                                            Toast.makeText(CadastroActivity.this,"Erro ao fazer o login: "+task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                }
                            });
                        }
                    }else{
                        Toast.makeText(CadastroActivity.this,"Preencha a senha!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CadastroActivity.this,"Preencha o E-Mail!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void inicializaComponentes(){
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);
    }
}