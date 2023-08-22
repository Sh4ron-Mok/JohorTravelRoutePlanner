package com.ai180183.johortravelrouteplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class LoginActivity extends AppCompatActivity {
    private Users user;
    private static final String TAG = "Login";
    EditText nEmail, nPass, nPhone, nOTP;
    Button nLoginBtn, nLoginVPhoneBtn;
    TextView nCreateBtn, nForgotPass, nResendOTP;
    TextInputLayout layoutEmail, layoutPassword, layoutPhone, layoutOTP;
    CountryCodePicker countryCodePicker;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseUser user2;
    FirebaseFirestore fStore;
    String verificationId, phoneNum;
    PhoneAuthCredential phoneAuthCredential;
    PhoneAuthProvider.ForceResendingToken token;
    Boolean verification = false;
    String code, userID;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = new Users();
        nEmail = findViewById(R.id.eMail);
        nPass = findViewById(R.id.passWord);
        nLoginBtn = findViewById(R.id.loginBtn);
        nPhone = findViewById(R.id.pHone);
        nOTP = findViewById(R.id.oTp);
        nLoginVPhoneBtn = findViewById(R.id.loginWithPhone);
        nCreateBtn = findViewById(R.id.createBtn);
        nForgotPass = findViewById(R.id.forgotPass);
        nResendOTP = findViewById(R.id.resendOTP);
        countryCodePicker = findViewById(R.id.cCode);
        layoutEmail = findViewById(R.id.layoutemail);
        layoutPassword = findViewById(R.id.layoutpassword);
        layoutPhone = findViewById(R.id.layoutpHone);
        layoutOTP = findViewById(R.id.layoutoTP);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.loadingL);
        storageReference = FirebaseStorage.getInstance().getReference();

        nLoginVPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutOTP.setVisibility(View.GONE);
                layoutPhone.setVisibility(View.VISIBLE);
                countryCodePicker.setVisibility(View.VISIBLE);
                layoutEmail.setVisibility(View.GONE);
                layoutPassword.setVisibility(View.GONE);
                nForgotPass.setVisibility(View.GONE);
                if(nPhone.getText().toString().isEmpty()){
                    nPhone.setError("Valid Phone is Required");
                    return;
                }
                if(!nPhone.getText().toString().isEmpty() && nPhone.getText().toString().length() >= 9) {
                    if(!verification){
                        progressBar.setVisibility(View.VISIBLE);
                        nLoginVPhoneBtn.setEnabled(false);
                        phoneNum = "+"+countryCodePicker.getSelectedCountryCode()+nPhone.getText().toString();
                        Log.d(TAG, "Phone No.: " + phoneNum);
                        sendVerificationCode(phoneNum);
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        code = nOTP.getText().toString();
                        if(code.isEmpty()){
                            nOTP.setError("Required");
                            return;
                        }

                        phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId,code);
                        verifyCode(phoneAuthCredential);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        nLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nLoginVPhoneBtn.setEnabled(true);
                nLoginVPhoneBtn.setText("Login with Phone");
                nResendOTP.setVisibility(View.GONE);
                layoutOTP.setVisibility(View.GONE);
                layoutPhone.setVisibility(View.GONE);
                countryCodePicker.setVisibility(View.GONE);
                layoutEmail.setVisibility(View.VISIBLE);
                layoutPassword.setVisibility(View.VISIBLE);
                nForgotPass.setVisibility(View.VISIBLE);
                user.setEmail(nEmail.getText().toString());
                user.setPassword(nPass.getText().toString());

                if (TextUtils.isEmpty(nEmail.getText().toString())) {
                    nEmail.setError("Email is required.");
                    return;
                }
                if (TextUtils.isEmpty(nPass.getText().toString())) {
                    nPass.setError("Password is required.");
                    return;
                }
                if (nPass.getText().toString().length() < 6) {
                    nPass.setError("Password must be >= 6 characters.");
                    return;
                }
                if (nPass.getText().toString().length() > 10) {
                    nPass.setError("Password must be <= 10 characters.");
                    return;
                }
                if (!isValidPassword(nPass.getText().toString())) {
                    nPass.setError("Password must be combination of alphabets and numbers.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                fAuth.signInWithEmailAndPassword(user.getEmail(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"Logged in successfully.",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(),ExploreActivity.class));
                        }else {
                            Toast.makeText(LoginActivity.this, "Email/ password is not CORRECT." , Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onFailure: Failed to sign in, "+task.getException().toString());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        nCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });

        nForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText resetMail = new EditText(view.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter your email here.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset link is sent to your email.", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error! Reset link cannot be sent." + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "onFailure: Failed to send reset link, "+e.toString());
                            }
                        });
                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //close the dialog
                    }
                });
                passwordResetDialog.create().show();
            }
        });

        nResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nPhone.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Invalid Number!", Toast.LENGTH_SHORT).show();
                    return;
                }
                PhoneAuthProvider.getInstance().verifyPhoneNumber(nPhone.getText().toString(), 60L, TimeUnit.SECONDS, LoginActivity.this, mCallBack);
            }
        });
    }

    private boolean isValidPassword(String password) {
        Pattern PASSWORD_PATTERN
                = Pattern.compile(
                "^(?=.*[A-Za-z])(?=.*[0-9]).{6,10}$");
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }

    private void verifyCode(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user.setUserId(Objects.requireNonNull(fAuth.getCurrentUser()).getUid());
                            Toast.makeText(LoginActivity.this, "Phone is verified.", Toast.LENGTH_SHORT).show();
                            checkUserProfile();
                            startActivity(new Intent(getApplicationContext(), ExploreActivity.class));
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "CANNOT Verify & Access Account.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                nLoginVPhoneBtn.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Account doesn't EXISTS! You may register a new account first.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: Account doesn't exists! "+e.toString());
            }
        });
    }

    private void checkUserProfile() {
        user2 = fAuth.getCurrentUser();
        assert user2 != null;
        userID = user2.getUid();

        DocumentReference docRef = fStore.collection("users").document(userID);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                if(documentSnapshot.exists()){
                    user.setFullName(documentSnapshot.getString("name"));
                    user.setEmail(documentSnapshot.getString("email"));
                    user.setPhone(documentSnapshot.getString("phone"));
                    user.setPassword(documentSnapshot.getString("password"));
                    user.setUserId(userID);
                    user.setUser(documentSnapshot.getBoolean("isUser"));
                    Log.d(TAG, "Success to retrieve data from "+userID);
                }else {
                    Log.d(TAG, "onEvent: Document do not exists");
                }
            }
        });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number,60L, TimeUnit.SECONDS,this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NotNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Toast.makeText(LoginActivity.this,
                                "OTP Timeout, Please Re-generate the OTP Again.", Toast.LENGTH_SHORT).show();
                        nResendOTP.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        token = forceResendingToken;
                        verification = true;
                        progressBar.setVisibility(View.GONE);
                        layoutOTP.setVisibility(View.VISIBLE);
                        nLoginVPhoneBtn.setText("Verify");
                        nLoginVPhoneBtn.setEnabled(true);
                        if(nOTP.getText().toString().isEmpty()){
                            nOTP.setError("Required");
                            return;
                        }
                    }
                    @Override
                    public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
                        // called if otp is automatically detected by the app
                        verifyCode(phoneAuthCredential);
                    }
                    @Override
                    public void onVerificationFailed(@NotNull FirebaseException e) {
                        Toast.makeText(LoginActivity.this,"Please register first!", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "onFailure: Failed to verify user, "+e.toString());
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                });
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeAutoRetrievalTimeOut(@NotNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            Toast.makeText(LoginActivity.this, "OTP Timeout, Please Re-generate the OTP Again.", Toast.LENGTH_SHORT).show();
            nResendOTP.setVisibility(View.VISIBLE);
        }
        @Override
        public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            progressBar.setVisibility(View.GONE);
            nLoginVPhoneBtn.setText("Verify");
            nLoginVPhoneBtn.setEnabled(true);
        }
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                nOTP.setText(code);
                verifyCode(phoneAuthCredential);
            }
        }
        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(LoginActivity.this, "Cannot resend the OTP.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onFailure: Failed to resend the OTP, "+e.toString());
            progressBar.setVisibility(View.GONE);
            nLoginVPhoneBtn.setEnabled(true);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if(user.getFullName() != null){
            startActivity(new Intent(getApplicationContext(),ExploreActivity.class));
            finish();
        }
        Log.d(TAG,"on Start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"on Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"on Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"on Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"on Destroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"on Restart");
    }
}