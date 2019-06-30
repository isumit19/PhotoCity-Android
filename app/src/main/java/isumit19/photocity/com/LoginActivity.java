package isumit19.photocity.com;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginBtn, login_registerBtn;

    FirebaseAuth mAuth;

    ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        login_registerBtn = findViewById(R.id.reg);
        loginProgress = findViewById(R.id.progressBar);




        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(loginEmail.getText().toString().trim().length()>0 && loginPassword.getText().toString().trim().length()>0 )
                {
                    loginBtn.setEnabled(true);
                    loginBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                else{
                    loginBtn.setEnabled(false);
                    loginBtn.setBackgroundColor(getResources().getColor(R.color.light_accent));
                }

            }
        };
        loginEmail.addTextChangedListener(watcher);
        loginPassword.addTextChangedListener(watcher);




        login_registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginemail = loginEmail.getText().toString();
                String loginpass = loginPassword.getText().toString();

                if(!TextUtils.isEmpty(loginemail) && !TextUtils.isEmpty(loginpass)){

                    loginProgress.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginemail,loginpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            loginProgress.setVisibility(View.INVISIBLE);
                            if(task.isSuccessful()){
                                sendToMain();
                            }
                            else{
                                String error = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error "+error,Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }


            }
        });








    }

    private void sendToMain(){

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){

            sendToMain();

        }

    }
}
