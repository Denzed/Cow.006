package com.cow006.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.cow006.gui.tableactivities.LeaderboardActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

public class MainMenuActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 9001;
    String userID;
    String username = "YOU";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        GoogleSignInOptions googleSignInOptions = buildGoogleSignInRequest();
        mGoogleApiClient = buildGoogleApiClient(googleSignInOptions);
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            handleSignInResult(pendingResult.get());
        } else {
            pendingResult.setResultCallback(this::handleSignInResult);
        }
        findViewById(R.id.sign_in_button).setOnClickListener(this::onSignIn);
    }

    @NonNull
    private GoogleApiClient buildGoogleApiClient(GoogleSignInOptions gso) {
        return new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @NonNull
    private GoogleSignInOptions buildGoogleSignInRequest() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestId()
                .build();
    }

    public void goToRules(View view) {
        Intent intent = new Intent(this, RulesActivity.class);
        startActivity(intent);
    }

    public void goToMultiSetup(View view) {
        Intent intent = new Intent(this, SetupMultiActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void goToSoloSetup(View view) {
        Intent intent = new Intent(this, SetupSoloGameActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void goToLeaderboard(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void handleSignInResult(@NonNull GoogleSignInResult result) {
        if (result.isSuccess() && result.getSignInAccount() != null) {
            GoogleSignInAccount acct = result.getSignInAccount();
            userID = acct.getId();
            username = acct.getDisplayName();
            updateUI(true);
        } else {
            updateUI(false);
        }
    }

    private void updateUI(boolean loggedIn) {
        String text;
        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.google_sign_buttons_flipper);
        int childIndex;
        if (loggedIn) {
            childIndex = flipper.indexOfChild(findViewById(R.id.sign_out_button));
            text = getString(R.string.logged_in_prefix) + username;
            findViewById(R.id.multi_button).setClickable(true);
        } else {
            childIndex = flipper.indexOfChild(findViewById(R.id.sign_in_button));
            text = getString(R.string.not_logged_in_text);
            findViewById(R.id.multi_button).setClickable(false);
        }
        flipper.setDisplayedChild(childIndex);
        ((TextView) findViewById(R.id.sign_in_information_text_view)).setText(text);
    }

    public void onSignIn(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    public void onSignOut(View view) {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        updateUI(false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println(connectionResult.getErrorMessage());
    }
}
