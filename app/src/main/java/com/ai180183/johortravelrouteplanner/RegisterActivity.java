package com.ai180183.johortravelrouteplanner;

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
import androidx.appcompat.app.AppCompatActivity;

import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private Users user;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    EditText mFullName, mEmail, mPass, mPhone, otpCode;
    Button mRegisterBtn;
    TextView mLoginBtn, resendOTP;
    TextInputLayout mLayoutPhone, mLayoutOtp;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    Boolean verificationOnProgress = false;
    PhoneAuthCredential phoneAuthCredential;
    CountryCodePicker ccp;
    PhoneAuthProvider.ForceResendingToken token;
    String verificationId, otp, userID, phoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user = new Users();
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPass = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn = findViewById(R.id.register);
        mLoginBtn = findViewById(R.id.login);
        mLayoutOtp = findViewById(R.id.layoutotp);
        mLayoutPhone = findViewById(R.id.layoutphone);
        otpCode = findViewById(R.id.otp);
        resendOTP = findViewById(R.id.resendOtp);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.loadingR);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhone.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Invalid Number!", Toast.LENGTH_SHORT).show();
                    return;
                }
                PhoneAuthProvider.getInstance().verifyPhoneNumber(mPhone.getText().toString(), 60L, TimeUnit.SECONDS, RegisterActivity.this, callbacks);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = fAuth.getCurrentUser().getUid();
                user.setEmail(mEmail.getText().toString());
                user.setPassword(mPass.getText().toString());
                user.setFullName(mFullName.getText().toString());
                user.setPhone("+" + ccp.getSelectedCountryCode() + mPhone.getText().toString());
                user.setUser(true);
                user.setUserId(userID);

                if (TextUtils.isEmpty(mFullName.getText().toString())) {
                    mFullName.setError("Name is required.");
                    return;
                }
                if (mFullName.getText().toString().length() > 25) {
                    mFullName.setError("Name must be <= 25 characters.");
                    return;
                }
                if (TextUtils.isEmpty(mEmail.getText().toString())) {
                    mEmail.setError("Email is required.");
                    return;
                }
                if (mEmail.getText().toString().length() > 25) {
                    mEmail.setError("Email must be <= 25 characters.");
                    return;
                }
                if (TextUtils.isEmpty(mPass.getText().toString())) {
                    mPass.setError("Password is required.");
                    return;
                }
                if (mPass.getText().toString().length() < 6) {
                    mPass.setError("Password must be >= 6 characters.");
                    return;
                }
                if (mPass.getText().toString().length() > 10) {
                    mPass.setError("Password must be <= 10 characters.");
                    return;
                }
                if (!isValidPassword(mPass.getText().toString())) {
                    mPass.setError("Password must be combination of alphabets and numbers.");
                    return;
                }
                if (TextUtils.isEmpty(ccp.getSelectedCountryCode())){
                    mPhone.setError("Country Code is required.");
                    return;
                }
                if (TextUtils.isEmpty(mPhone.getText().toString())) {
                    mPhone.setError("Phone is required.");
                    return;
                }
                if (mPhone.getText().toString().length() < 10
                        && mPhone.getText().toString().length() > 11) {
                    mPhone.setError("Please enter a valid phone number.");
                    return;
                }
                if(!mPhone.getText().toString().isEmpty() && mPhone.getText().toString().length() >= 9) {
                    if(!verificationOnProgress){
                        AuthCredential emailCredential = EmailAuthProvider
                                .getCredential(mEmail.getText().toString(),mPass.getText().toString());
                        Objects.requireNonNull(fAuth.getCurrentUser())
                                .linkWithCredential(emailCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "linkWithCredential:success");
                                    progressBar.setVisibility(View.VISIBLE);
                                    mRegisterBtn.setEnabled(false);
                                    phoneNum = "+" + ccp.getSelectedCountryCode() + mPhone.getText().toString();
                                    Log.d("phone", "Phone No.: " + phoneNum);
                                    checkForPhoneNumber(phoneNum);
                                } else {
                                    Toast.makeText(RegisterActivity.this,
                                            "Error! This account has been registered.", Toast.LENGTH_SHORT).show();
                                    Log.d("TAG", "onFailed: Failed to authenticate user for "
                                            + userID + ", " + Objects.requireNonNull(task.getException()).toString());
                                }
                            }
                        });

                    }else {
                        mLayoutOtp.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        otp = otpCode.getText().toString();
                        if(otp.isEmpty()){
                            otpCode.setError("Required");
                            return;
                        }
                        phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId,otp);
                        verifyAuth(phoneAuthCredential);
                        progressBar.setVisibility(View.GONE);
                    }
                }else {
                    mPhone.setError("Valid Phone Required");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void checkForPhoneNumber(String number){
        CollectionReference cRef = fStore.collection("users");
        Query q1 = cRef.whereEqualTo("phone",number);
        q1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                boolean isExisting = false;
                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    String handPhone;
                    handPhone = ds.getString("phone");
                    if (handPhone.equals(number)) {
                        isExisting = true;
                        progressBar.setVisibility(View.GONE);
                        mRegisterBtn.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,"This number has been registered.",Toast.LENGTH_SHORT).show();
                        Log.d("TAG","onFailed: Phone exists.");
                        cancelEmailVerification();
                    }
                }
                if (!isExisting) {
                    Log.d("TAG","onEvent: Phone isn't exist.");
                    requestPhoneAuth(number);
                }
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

    private void requestPhoneAuth(String phone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone,60L, TimeUnit.SECONDS,this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NotNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Toast.makeText(RegisterActivity.this, "OTP Timeout, Please Re-generate the OTP Again.", Toast.LENGTH_SHORT).show();
                        resendOTP.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        token = forceResendingToken;
                        verificationOnProgress = true;
                        progressBar.setVisibility(View.GONE);
                        mRegisterBtn.setText("Verify");
                        mRegisterBtn.setEnabled(true);
                        mLayoutOtp.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
                        // called if otp is automatically detected by the app
                        verifyAuth(phoneAuthCredential);
                        progressBar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onVerificationFailed(@NotNull FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        mRegisterBtn.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Failed to verify user. ", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onFailed: Failed to verify user, "+e.toString());
                    }
                });
    }

    private void cancelEmailVerification() {
        FirebaseUser user1 = fAuth.getCurrentUser();
        if (user1 != null) {
            for (UserInfo profile : user1.getProviderData()) {
                // Id of the provider
                String providerId = profile.getProviderId();
                if (providerId.equals("password")) {
                    fAuth.getCurrentUser().unlink(providerId)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Auth provider unlinked from account
                                        Toast.makeText(RegisterActivity.this, "Please enter another phone number.", Toast.LENGTH_LONG).show();
                                        Log.d("TAG", "onEvent: Unlink account, " + task.toString());
                                        progressBar.setVisibility(View.GONE);
                                        mRegisterBtn.setEnabled(true);
                                    } else {
                                        Log.d("TAG", "onFailed: Failed to unlink account, " + task.getException().toString());
                                    }
                                }
                            });
                }
            }
        }
    }

    public void verifyAuth(PhoneAuthCredential credential){
        Objects.requireNonNull(fAuth.getCurrentUser()).linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                user.setUserId(fAuth.getCurrentUser().getUid());
                Toast.makeText(RegisterActivity.this, "Account is Created and Verified.", Toast.LENGTH_SHORT).show();
                checkUserProfile();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Register","on Start");
        if(user.getFullName() != null){
            progressBar.setVisibility(View.VISIBLE);
            startActivity(new Intent(getApplicationContext(),ExploreActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Register","on Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Register","on Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Register","on Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Register","on Destroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Register","on Restart");
    }

    private void checkUserProfile() {
        userID = fAuth.getCurrentUser().getUid();
        DocumentReference dRef = fStore.collection("users").document(userID);
        Map<String, Object> user1 = new HashMap<>();
        user1.put("userID",user.getUserId());
        user1.put("name",user.getFullName());
        user1.put("email",user.getEmail());
        user1.put("phone",user.getPhone());
        user1.put("password",user.getPassword());
        user1.put("isUser",user.getUser());

        dRef.set(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "onSuccess: User profile is created." + userID);
                startActivity(new Intent(getApplicationContext(),ExploreActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: Failed to create user, " + e.toString());
            }
        });
    }
}