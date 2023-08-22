package com.ai180183.johortravelrouteplanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.view.View.GONE;

public class AccountActivity extends AppCompatActivity {
    private Users user;
    com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation;

    ImageView profileImage;
    TextView aFullName, aEmail, aPhone, resendCode;
    EditText currentPass, newPass, confirmPass, nameET, emailET, OTP, phoneET;
    ImageButton editNameBtn, editEmailBtn, editPhoneBtn;
    TextInputLayout layoutCurrentPass, layoutNewPass, layoutConfirmPass, layoutOTP, layoutPhoneNum;
    Button resetPass, newAdmin, logoutBtn, verifyBtn, confirmBtn, cancelBtn;
    CountryCodePicker CCP;
    ProgressBar progressBar;
    Boolean onVerification = false;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user1;
    String userID, aVerificationId;
    StorageReference storageReference;
    PhoneAuthProvider.ForceResendingToken aResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks aCallbacks;

    private static final String TAG = "AccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        BottomNavigationView bottomNavigationView;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_account);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_explore:
                        Intent intent1 = new Intent(AccountActivity.this, ExploreActivity.class);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.navigation_plan:
                        Intent intent2 = new Intent(AccountActivity.this, PlanActivity.class);
                        startActivity(intent2);
                        finish();
                        return true;
                    case R.id.navigation_log:
                        Intent intent3 = new Intent(AccountActivity.this, TravelLogActivity.class);
                        startActivity(intent3);
                        finish();
                        return true;
                    case R.id.navigation_account:
                        Intent intent4 = new Intent(AccountActivity.this, AccountActivity.class);
                        startActivity(intent4);
                        finish();
                        return true;
                }
                return false;
            }
        });

        aPhone = findViewById(R.id.phoneNum);
        aFullName = findViewById(R.id.fullName);
        aEmail = findViewById(R.id.eMail);
        resetPass = findViewById(R.id.resetPassword);
        profileImage = findViewById(R.id.profileImage);
        layoutCurrentPass = findViewById(R.id.layoutCpass);
        layoutNewPass = findViewById(R.id.layoutNpass);
        layoutConfirmPass = findViewById(R.id.layoutConfirmPassword);
        currentPass = findViewById(R.id.Cpassword);
        newPass = findViewById(R.id.Npassword);
        confirmPass = findViewById(R.id.ConfirmPassword);
        progressBar = findViewById(R.id.loadingA);
        newAdmin = findViewById(R.id.addNewAdmin);
        editNameBtn = findViewById(R.id.editName);
        editEmailBtn = findViewById(R.id.editEmail);
        editPhoneBtn = findViewById(R.id.editPhone);
        logoutBtn = findViewById(R.id.logoutBtn);
        nameET = findViewById(R.id.nameET);
        emailET = findViewById(R.id.emailET);
        resendCode = findViewById(R.id.resendCode);
        OTP = findViewById(R.id.OTP);
        layoutOTP = findViewById(R.id.layoutOTP);
        verifyBtn = findViewById(R.id.verifyBtn);
        layoutPhoneNum = findViewById(R.id.layoutPhoneNum);
        phoneET = findViewById(R.id.phoneET);
        CCP = findViewById(R.id.CCP);
        confirmBtn = findViewById(R.id.confirmBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        user = new Users();
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user1 = fAuth.getCurrentUser();

        showUserProfile();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        aCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG,"onVerificationCompleted: " + phoneAuthCredential);
                UpdatePhoneNumAuth(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG,"onVerificationFailed: ", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    phoneET.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                aVerificationId = verificationId;
                aResendToken = token;
            }
        };

        //   Reset Password   //
        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPass.setVisibility(GONE);
                logoutBtn.setVisibility(GONE);
                layoutCurrentPass.setVisibility(View.VISIBLE);
                user.setPassword(currentPass.getText().toString());
                confirmBtn.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.VISIBLE);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unableButton();
                    }
                });
                confirmBtn.setText("Check");
                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(currentPass.getText().toString())) {
                            currentPass.setError("Please enter the current password first.");
                            return;
                        }
                        if (currentPass.getText().toString().length() < 6) {
                            currentPass.setError("Password must be >= 6 characters.");
                            return;
                        }
                        if (currentPass.getText().toString().length() > 10) {
                            currentPass.setError("Password must be <= 10 characters.");
                            return;
                        }
                        layoutCurrentPass.setVisibility(GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        DocumentReference docRef = fStore.collection("users").document(userID);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc != null && doc.exists()) {
                                        String password = doc.getString("password");
                                        if (password.equals(currentPass.getText().toString())) {
                                            progressBar.setVisibility(GONE);
                                            layoutNewPass.setVisibility(View.VISIBLE);
                                            layoutConfirmPass.setVisibility(View.VISIBLE);
                                            confirmBtn.setText("Confirm");
                                            if (TextUtils.isEmpty(newPass.getText().toString())) {
                                                newPass.setError("New Password is required.");
                                                return;
                                            }
                                            if (newPass.getText().toString().length() < 6) {
                                                newPass.setError("Password must be >= 6 characters.");
                                                return;
                                            }
                                            if (newPass.getText().toString().equals(currentPass.getText().toString())) {
                                                newPass.setError("New password CANNOT same as current password!");
                                                return;
                                            }
                                            if (newPass.getText().toString().length() > 10) {
                                                newPass.setError("Password must be <= 10 characters.");
                                                return;
                                            }
                                            if (!isValidPassword(newPass.getText().toString())) {
                                                newPass.setError("Password must be combination of alphabets and numbers.");
                                                return;
                                            }
                                            if (TextUtils.isEmpty(confirmPass.getText().toString())) {
                                                confirmPass.setError("Confirm password is required.");
                                                return;
                                            }
                                            if (!newPass.getText().toString().equals(confirmPass.getText().toString())) {
                                                confirmPass.setError("Passwords are not matched!");
                                                return;
                                            }
                                            if(!newPass.getText().toString().isEmpty() && newPass.getText().toString().equals(confirmPass.getText().toString())) {
                                                progressBar.setVisibility(View.VISIBLE);
                                                showUserProfile();
                                                user.setPassword(currentPass.getText().toString());
                                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), user.getPassword());
                                                //Prompt the user to re-provide their sign-in credentials
                                                user1.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "User is re-authenticated.");
                                                            user.setPassword(confirmPass.getText().toString());
                                                            user1.updatePassword(user.getPassword()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "onSuccess: Password is updated.");
                                                                    docRef.update("password", user.getPassword()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            progressBar.setVisibility(GONE);
                                                                            layoutCurrentPass.setVisibility(GONE);
                                                                            layoutNewPass.setVisibility(GONE);
                                                                            layoutConfirmPass.setVisibility(GONE);
                                                                            Toast.makeText(AccountActivity.this, "Password is reset.", Toast.LENGTH_SHORT).show();
                                                                            unableButton();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            layoutCurrentPass.setVisibility(GONE);
                                                                            layoutNewPass.setVisibility(GONE);
                                                                            layoutConfirmPass.setVisibility(GONE);
                                                                            Toast.makeText(AccountActivity.this, "Failed to reset password.", Toast.LENGTH_SHORT).show();
                                                                            Log.e(TAG, "onFailure: Failed to reset password, " + e.toString());
                                                                        }
                                                                    });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e(TAG, "onFailure: Failed to update password, " + e.toString());
                                                                }
                                                            });
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        unableButton();
                                                        Toast.makeText(AccountActivity.this, "Password is INVALID!", Toast.LENGTH_LONG).show();
                                                        Log.e(TAG, "onFailure: Failed to re-authenticate user, " + e.toString());
                                                    }
                                                });
                                            }
                                        } else {
                                            unableButton();
                                            Toast.makeText(AccountActivity.this, "Password is WRONG!", Toast.LENGTH_LONG).show();
                                            Log.e(TAG,"onFailure: Password is not existed." + Exception.class.toString());
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        //   Update Name   //
        editNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unableButton();
                emailET.setVisibility(View.INVISIBLE);
                aEmail.setVisibility(View.VISIBLE);
                CCP.setVisibility(GONE);
                layoutPhoneNum.setVisibility(GONE);
                aPhone.setVisibility(View.VISIBLE);
                aFullName.setVisibility(View.INVISIBLE);
                nameET.setVisibility(View.VISIBLE);
                nameET.setText(user.getFullName());
                resetPass.setVisibility(GONE);
                verifyBtn.setVisibility(View.VISIBLE);

                logoutBtn.setText("Cancel");
                logoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unableButton();
                    }
                });

                verifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(nameET.getText().toString().isEmpty()){
                            nameET.setError("Name is Required");
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        user.setFullName(nameET.getText().toString());
                        Log.d(TAG, "onEvent: Changing profile name...");
                        DocumentReference docRef = fStore.collection("users").document(userID);
                        docRef.update("name", user.getFullName()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBar.setVisibility(GONE);
                                Toast.makeText(AccountActivity.this, "Name is edited.", Toast.LENGTH_SHORT).show();
                                unableButton();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AccountActivity.this, "Failed to edit name.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onFailure: Failed to edit name, " + e.toString());
                            }
                        });
                    }
                });
            }
        });

        //   Update Email   //
        editEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unableButton();
                nameET.setVisibility(View.INVISIBLE);
                aFullName.setVisibility(View.VISIBLE);
                CCP.setVisibility(GONE);
                layoutPhoneNum.setVisibility(GONE);
                aPhone.setVisibility(View.VISIBLE);
                aEmail.setVisibility(View.INVISIBLE);
                emailET.setVisibility(View.VISIBLE);
                emailET.setText(user.getEmail());
                resetPass.setVisibility(GONE);
                verifyBtn.setVisibility(View.VISIBLE);

                logoutBtn.setText("Cancel");
                logoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unableButton();
                    }
                });

                verifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(emailET.getText().toString().isEmpty()){
                            emailET.setError("Valid Email is Required");
                            return;
                        }
                        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
                        if(emailET.getText().toString().matches(regex)) {
                            progressBar.setVisibility(View.VISIBLE);
                            showUserProfile();
                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(),user.getPassword());

                            //Prompt the user to re-provide their sign-in credentials
                            user1.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "User is re-authenticated.");
                                    if(task.isSuccessful()){
                                        user.setEmail(emailET.getText().toString());
                                        user1.updateEmail(user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User email address is updated.");
                                                    DocumentReference docRef = fStore.collection("users").document(userID);
                                                    docRef.update("email", user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressBar.setVisibility(GONE);
                                                            Toast.makeText(AccountActivity.this,
                                                                    "Email is edited to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(AccountActivity.this,
                                                                    "Failed to edit email.", Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "onFailure: Failed to edit email, " + e.toString());
                                                        }
                                                    });
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "onFailure: Failed to update email, " + e.toString());
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, "onFailure: Failed to re-authenticate user, " + task.getException().toString());
                                    }
                                }
                            });
                        } else {
                            emailET.setError("Valid Email is Required");
                            return;
                        }
                        unableButton();
                    }
                });
            }
        });

        //   Update Phone Number   //
        editPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unableButton();
                nameET.setVisibility(View.INVISIBLE);
                aFullName.setVisibility(View.VISIBLE);
                emailET.setVisibility(View.INVISIBLE);
                aEmail.setVisibility(View.VISIBLE);
                aPhone.setVisibility(View.INVISIBLE);
                CCP.setVisibility(View.VISIBLE);
                layoutPhoneNum.setVisibility(View.VISIBLE);
                resetPass.setVisibility(GONE);
                verifyBtn.setVisibility(View.VISIBLE);

                logoutBtn.setText("Cancel");
                logoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        unableButton();
                    }
                });

                verifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String number = phoneET.getText().toString();
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
                                        phoneET.setError("Please enter a new phone number.");
                                        Log.d("TAG", "onFailed: Phone exists.");
                                        return;
                                    }
                                }
                                if (!isExisting) {
                                    if(phoneET.getText().toString().isEmpty()){
                                        phoneET.setError("Phone number is required.");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(CCP.getSelectedCountryCode())){
                                        phoneET.setError("Country code is required.");
                                        return;
                                    }
                                    if(!phoneET.getText().toString().isEmpty() && phoneET.getText().toString().length() >= 9) {
                                        if (!onVerification) {
                                            progressBar.setVisibility(View.VISIBLE);
                                            user.setPhone("+" + CCP.getSelectedCountryCode() + phoneET.getText().toString());
                                            Log.d(TAG, "onEvent: Changing phone number...");
                                            user1.unlink(PhoneAuthProvider.PROVIDER_ID).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    Log.d(TAG,"Unlink "+user.getPhone());
                                                    sendVerificationCode(user.getPhone());
                                                    resetPass.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            progressBar.setVisibility(View.VISIBLE);
                                                            Log.d(TAG, "onSuccess: Phone is updated to " + user.getPhone());
                                                            String code = OTP.getText().toString();
                                                            PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(aVerificationId,code);
                                                            UpdatePhoneNumAuth(phoneAuthCredential);
                                                            unableButton();
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG,"Failed, "+e.toString());
                                                }
                                            });
                                        } else {
                                            progressBar.setVisibility(GONE);
                                            String code = OTP.getText().toString();
                                            if(code.isEmpty()){
                                                OTP.setError("Required");
                                                return;
                                            }
                                            verifyPhoneNumberWithCode(aVerificationId,code);
                                            unableButton();
                                        }
                                    } else {
                                        unableButton();
                                        Log.d(TAG, "Input error!");
                                        Toast.makeText(AccountActivity.this, "Please try again.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
                Intent intent = new Intent(AccountActivity.this,ExploreActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void UpdatePhoneNumAuth(PhoneAuthCredential phoneAuthCredential) {
        user1.linkWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                user1.updatePhoneNumber(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DocumentReference docRef = fStore.collection("users").document(userID);
                        docRef.update("phone", user.getPhone()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AccountActivity.this, "Phone is edited.", Toast.LENGTH_SHORT).show();
                                unableButton();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AccountActivity.this, "Failed to edit phone.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onFailure: Failed to edit phone, " + e.toString());
                                unableButton();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Unable to update phone number, " + e.toString());
                    }
                });
                progressBar.setVisibility(GONE);
                resendCode.setVisibility(View.VISIBLE);
                resendCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resendVerificationCode(user.getPhone(), aResendToken);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"Unable to re-authenticate this phone number," + e.toString());
            }
        });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number,60L, TimeUnit.SECONDS,this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    @Override
                    public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        aVerificationId = s;
                        aResendToken = forceResendingToken;
                        onVerification = true;
                        progressBar.setVisibility(GONE);
                        verifyBtn.setVisibility(GONE);
                        layoutOTP.setVisibility(View.VISIBLE);
                        resetPass.setVisibility(View.VISIBLE);
                        resetPass.setText("Verify");
                    }
                    @Override
                    public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
                        // called if otp is automatically detected by the app
                        UpdatePhoneNumAuth(phoneAuthCredential);
                        onVerification = false;
                    }
                    @Override
                    public void onVerificationFailed(@NotNull FirebaseException e) {
                        Toast.makeText(AccountActivity.this, "Failed to verify the phone number", Toast.LENGTH_LONG).show();
                        Log.d("TAG", "onFailed: Failed to verify user with the first OTP, "+e.toString());
                    }
                });
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        UpdatePhoneNumAuth(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                AccountActivity.this,        // Activity (for callback binding)
                aCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void unableButton() {
        nameET.setVisibility(GONE);
        emailET.setVisibility(GONE);
        CCP.setVisibility(GONE);
        layoutPhoneNum.setVisibility(GONE);
        layoutOTP.setVisibility(GONE);
        resendCode.setVisibility(GONE);
        layoutConfirmPass.setVisibility(GONE);
        layoutCurrentPass.setVisibility(GONE);
        layoutNewPass.setVisibility(GONE);
        aFullName.setVisibility(View.VISIBLE);
        aEmail.setVisibility(View.VISIBLE);
        aPhone.setVisibility(View.VISIBLE);
        verifyBtn.setVisibility(GONE);
        resetPass.setVisibility(View.VISIBLE);
        resetPass.setText("Reset Password");
        logoutBtn.setVisibility(View.VISIBLE);
        logoutBtn.setText("Logout");
        progressBar.setVisibility(GONE);
        layoutCurrentPass.setVisibility(GONE);
        layoutNewPass.setVisibility(GONE);
        layoutConfirmPass.setVisibility(GONE);
        confirmBtn.setVisibility(GONE);
        cancelBtn.setVisibility(GONE);
    }

    private void showUserProfile() {
        userID = user1.getUid();
        DocumentReference docRef = fStore.collection("users").document(userID);
        docRef.addSnapshotListener(AccountActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    user.setFullName(documentSnapshot.getString("name"));
                    user.setEmail(documentSnapshot.getString("email"));
                    user.setPassword(documentSnapshot.getString("password"));
                    user.setPhone(documentSnapshot.getString("phone"));
                    user.setUser(documentSnapshot.getBoolean("isUser"));
                    user.setImgUrl(documentSnapshot.getString("profileImage"));
                    user.setUserId(userID);
                    Picasso.get()
                            .load(user.getImgUrl())
                            .transform(new CropCircleTransformation())
                            .into(profileImage);
                    aFullName.setText(user.getFullName());
                    aEmail.setText(user.getEmail());
                    aPhone.setText(user.getPhone());
                    if(!user.getUser()){
                        newAdmin.setVisibility(View.VISIBLE);
                        newAdmin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                progressBar.setVisibility(View.VISIBLE);
                                startActivity(new Intent(getApplicationContext(),AddNewAdmin.class));
                                finish();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "onFailed: Documents do not exists, " + e.toString());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(AccountActivity.this, SettingsActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK && data != null){
                Uri imageUri = data.getData();
                Picasso.get()
                        .load(imageUri)
                        .transform(new CropCircleTransformation())
                        .into(profileImage);
                uploadImageToFirebase(imageUri);
            }
        } else {
            Toast.makeText(this, "Error, Please try again", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"onFailed：" + Exception.class.toString());
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to firebase storage
        final StorageReference image = storageReference.child("users/"+userID+"/profile.jpg");
        image.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
                        Picasso.get().load(uri).into(profileImage);
                        DocumentReference docRef = fStore.collection("users").document(userID);
                        docRef.update("profileImage", uri.toString());
                    }
                });

                Toast.makeText(AccountActivity.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "OnFailure: Upload failed, "+e.toString());
                Toast.makeText(AccountActivity.this, "Upload Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"on Start");
        if (fAuth.getCurrentUser() == null) {
            startActivity(new Intent(AccountActivity.this, RegisterActivity.class));
            finish();
        }
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