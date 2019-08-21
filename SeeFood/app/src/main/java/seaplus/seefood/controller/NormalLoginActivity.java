package seaplus.seefood.controller;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

import seaplus.seefood.R;
import seaplus.seefood.model.User;

import static seaplus.seefood.R.id.forgotPw;
import static seaplus.seefood.R.id.login;
import static seaplus.seefood.controller.NewUserSignUpActivity.md5;

public class NormalLoginActivity extends AppCompatActivity implements View.OnClickListener {


    Button loginBtn;
    Button forgotPwBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_login);

        // my_child_toolbar is defined in the layout file
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Login");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //underline the forgotPw Btn text
        forgotPwBtn = (Button) findViewById(R.id.forgotPw);
        forgotPwBtn.setPaintFlags(forgotPwBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgotPwBtn.setOnClickListener(this);

        //Login Button
        loginBtn= (Button) this.findViewById(login);
        loginBtn.setOnClickListener(this);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }


    @Override
    public void onClick(View view) {

        if(view.getId() == login) {
            normalUserLogIn(view);
        }else if (view.getId() == forgotPw){
            forgotPW(view);
        }

    }

    public void forgotPW(View view){
        Intent tmpPage = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        tmpPage.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
        startActivity(tmpPage);
    }


    public void normalUserLogIn(View view) {

        final EditText usernameEditText = (EditText) findViewById(R.id.username);

        final EditText passwordEditText = (EditText) findViewById(R.id.password);

        if (usernameEditText.getText().toString().matches("") || passwordEditText.getText().toString().matches("")) {

            Toast.makeText(this, "A username and password are required.", Toast.LENGTH_SHORT).show();

        } else {

            ParseUser.logInInBackground(usernameEditText.getText().toString(), md5(passwordEditText.getText().toString()), new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {

                    if (user != null) {


                        passwordEditText.setText("");
                        usernameEditText.setText("");

                        String username = user.get("name").toString();
                        displayToast("Hello " + username + ", welcome back!");
                        User tmpuser = new User(username,"");
                        Intent mainPage = new Intent(getApplicationContext(), MainNavigationActivity.class);
                        mainPage.putExtra("UserObj", tmpuser);
                        mainPage.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
                        startActivity(mainPage);

                    } else {

                        displayToast(e.getMessage());

                    }

                }
            });

        }
    }

    public void displayToast(String s){
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

}


