package com.example.baza;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.util.Log;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class SignUpActivityTest {

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private Task<AuthResult> mockAuthTask;

    @Mock
    private Task<Void> mockFirestoreTask;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private CollectionReference mockCollectionReference;

    @Mock
    private DocumentReference mockDocumentReference;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);

        when(mockFirestore.collection("users")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference);
        when(mockDocumentReference.set(any())).thenReturn(mockFirestoreTask);
    }

    @Test
    public void testSignUp_Success() {
        try (ActivityScenario<SignUpActivity> scenario = ActivityScenario.launch(SignUpActivity.class)) {
            scenario.onActivity(activity -> {
                activity.setAuth(mockAuth);
                activity.setDb(mockFirestore);
                Log.d("Test", "Firestore instance set: " + mockFirestore);

                EditText etEmail = activity.findViewById(R.id.et_email);
                EditText etPassword = activity.findViewById(R.id.et_password);
                EditText etUsername = activity.findViewById(R.id.et_username);
                Button btnSignUp = activity.findViewById(R.id.btn_register);

                etEmail.setText("test@example.com");
                etPassword.setText("password123");
                etUsername.setText("TestUser");

                when(mockAuth.createUserWithEmailAndPassword("test@example.com", "password123"))
                        .thenReturn(mockAuthTask);
                when(mockAuthTask.isSuccessful()).thenReturn(true);
                when(mockAuth.getCurrentUser()).thenReturn(mockUser);
                when(mockUser.getUid()).thenReturn("testUid");
                when(mockFirestoreTask.isSuccessful()).thenReturn(true);

                doAnswer(invocation -> {
                    OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
                    listener.onComplete(mockAuthTask);
                    return null;
                }).when(mockAuthTask).addOnCompleteListener(any());

                doAnswer(invocation -> {
                    OnCompleteListener<Void> listener = invocation.getArgument(0);
                    listener.onComplete(mockFirestoreTask);
                    return null;
                }).when(mockFirestoreTask).addOnCompleteListener(any());

                btnSignUp.performClick();

                verify(mockFirestore).collection("users");
                verify(mockCollectionReference).document("testUid");

                ArgumentCaptor<Map<String, Object>> userMapCaptor = ArgumentCaptor.forClass(Map.class);
                verify(mockDocumentReference).set(userMapCaptor.capture());

                Map<String, Object> capturedUserMap = userMapCaptor.getValue();
                assertEquals("test@example.com", capturedUserMap.get("email"));
                assertEquals("TestUser", capturedUserMap.get("username"));
                assertEquals("testUid", capturedUserMap.get("uid"));
                assertEquals("user", capturedUserMap.get("role"));
            });
        }
    }

}
