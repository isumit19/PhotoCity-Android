package isumit19.photocity.com;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText RegEmail, RegPassword, RegCPassword;
    Button RegBtn, register_loginBtn;

    FirebaseAuth mAuth;

    ProgressBar RegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        RegEmail = findViewById(R.id.email);
        RegPassword = findViewById(R.id.password);
        RegCPassword = findViewById(R.id.confirm_password);
        RegBtn = findViewById(R.id.register);
        RegProgress = findViewById(R.id.progressBar);
        register_loginBtn = findViewById(R.id.login);

        mAuth = FirebaseAuth.getInstance();

        register_loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(RegEmail.getText().toString().trim().length()>0 && RegPassword.getText().toString().trim().length()>0 && RegCPassword.getText().toString().trim().length()>0)
                {
                    RegBtn.setEnabled(true);
                    RegBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                else{
                    RegBtn.setEnabled(false);
                    RegBtn.setBackgroundColor(getResources().getColor(R.color.light_accent));
                }

            }
        };

        RegEmail.addTextChangedListener(watcher);
        RegPassword.addTextChangedListener(watcher);
        RegCPassword.addTextChangedListener(watcher);



        RegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = RegEmail.getText().toString();
                String password = RegPassword.getText().toString();
                String cpassword = RegCPassword.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(cpassword)){

                    if(cpassword.equals(password)){

                        RegProgress.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    RegProgress.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(intent);
                                    finish();

                                }
                                else{
                                    String error = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,error,Toast.LENGTH_LONG).show();
                                    RegProgress.setVisibility(View.INVISIBLE);

                                }
                            }
                        });

                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Confirm Password and Password does not match",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            sendToMain();
        }
    }

    private void sendToMain() {

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
