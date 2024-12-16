package com.example.baza;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(packageName = "com.example.baza", sdk = 29)
public class SignInActivityTest {

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private Task<AuthResult> mockAuthTask;

    @Mock
    private FirebaseUser mockUser;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
    }


    @Test
    public void testSignIn_Success() {
        try (ActivityScenario<SignInActivity> scenario = ActivityScenario.launch(SignInActivity.class)) {
            scenario.onActivity(activity -> {
                activity.setAuth(mockAuth);

                EditText etEmail = activity.findViewById(R.id.et_email);
                EditText etPassword = activity.findViewById(R.id.et_password);
                Button btnSignIn = activity.findViewById(R.id.btn_login);

                etEmail.setText("user@example.com");
                etPassword.setText("password123");

                when(mockAuth.signInWithEmailAndPassword("user@example.com", "password123"))
                        .thenReturn(mockAuthTask);

                btnSignIn.performClick();
                verify(mockAuth).signInWithEmailAndPassword("user@example.com", "password123");
            });
        }
    }
}
