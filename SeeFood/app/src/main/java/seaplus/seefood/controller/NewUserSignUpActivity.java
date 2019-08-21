package seaplus.seefood.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import seaplus.seefood.R;

import static seaplus.seefood.R.id.createAccount;

public class NewUserSignUpActivity extends AppCompatActivity implements View.OnClickListener  {

    Button createAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_sign_up);

        // my_child_toolbar is defined in the layout file
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Sign Up");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //Login Button
        createAccountBtn = (Button) this.findViewById(createAccount);
        createAccountBtn.setOnClickListener(this);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    @Override
    public void onClick(View view) {

        createNewAccount(view);

    }

    public void createNewAccount(View view) {

        final EditText newUserNameEditText = (EditText) findViewById(R.id.newusername);

        final EditText firstNameEditText = (EditText) findViewById(R.id.firstName);

        final EditText lastNameEditText = (EditText) findViewById(R.id.lastName);

        final EditText passwordEditText = (EditText) findViewById(R.id.password);

        final EditText confirmPwEditText = (EditText) findViewById(R.id.confirmPW);

        final EditText emailEditText = (EditText) findViewById(R.id.email);

        String username = newUserNameEditText.getText().toString();
        String displayedName = firstNameEditText.getText().toString() + " " + lastNameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String confirmPW = confirmPwEditText.getText().toString();

        if (firstNameEditText.getText().toString().matches("") || passwordEditText.getText().toString().matches("")) {

            displayToast("A username and password are required.");

        } else if (!password.matches(confirmPW)){

            displayToast("Password does not match with the confirmed password.");

        } else {

            ParseUser user = new ParseUser();
            user.setUsername(username);
            String hashedPw = md5(password);
            user.setPassword(hashedPw);
            user.setEmail(email);
            user.put("name", displayedName);
            user.put("adminPw", hashedPw);

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {

                    if (e == null){

                        String tmpName = firstNameEditText.getText() + " " + lastNameEditText.getText();
                        displayToast("Hello " + tmpName + ", welcome to SeeFOOD!");

                        newUserNameEditText.setText("");
                        passwordEditText.setText("");
                        firstNameEditText.setText("");
                        lastNameEditText.setText("");
                        emailEditText.setText("");
                        confirmPwEditText.setText("");


                        Intent mainPage = new Intent(getApplicationContext(), MainNavigationActivity.class);
                        mainPage.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
                        startActivity(mainPage);
                    }else{
                        displayToast(e.getMessage());
                    }
                }
            });

        }
    }

    public static String md5(String s)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")),0,s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public void displayToast(String s){
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

}


