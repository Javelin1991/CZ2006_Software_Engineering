package seaplus.seefood.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import seaplus.seefood.R;

import static seaplus.seefood.controller.NewUserSignUpActivity.md5;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private Button requestCodeBtn;
    private TextView forgotPwTextView;
    private EditText newPasswordText;
    private boolean resetPwFlag = false;
    private boolean newPwFlag = false;
    private int randomGeneratedCode;
    private EditText emailEditText;
    private ParseUser currentUser;
    private String email;
    private int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // my_child_toolbar is defined in the layout file
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Forgot Password");
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        newPasswordText = (EditText) findViewById(R.id.newPassword);
        newPasswordText.setVisibility(View.INVISIBLE);

        forgotPwTextView = (TextView) findViewById(R.id.forgotPasswordTextView);
        forgotPwTextView.setText("Enter your email and a code will be sent to your registered email.");

        requestCodeBtn = (Button) findViewById(R.id.requestBtn);
        requestCodeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(!resetPwFlag && !newPwFlag) {

            emailEditText = (EditText) findViewById(R.id.email);
            email = emailEditText.getText().toString();

            ParseQuery<ParseUser> emailUserQuery = ParseUser.getQuery();
            emailUserQuery.whereEqualTo("email", email);
            emailUserQuery.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {

                        if (objects.size() == 0) {
                            displayToast("Invalid email address. Please try again");
                        } else {
                            emailEditText.setText("");
                            textTransform("Enter the reset code below", "Submit");
                            emailEditText.setHint("Code");

                            sendEmail(email);
                            resetPwFlag = true;
                            displayToast("A password reset code has been sent to your email.");
                            for(ParseUser user: objects){
                                currentUser = user;
                            }
                        }

                    } else {
                        displayToast(e.getMessage());
                    }
                }
            });


        }else if (!newPwFlag){

            if(pwCodeMatcher(randomGeneratedCode)){
                newPwFlag = true;
                emailEditText.setText("");
                emailEditText.setHint("Confirm Password");
                textTransform("Enter a new password", "Save");
                newPasswordText.setVisibility(View.VISIBLE);
                newPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                emailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            }else if (counter == 4) {
                displayToast("You have already attempted 5 times.  A new password reset code has been sent to your email.");
                sendEmail(email);
                counter = 0;
            }else{
                displayToast("Invalid reset code. Please try again");
                counter++;
            }

        }else{

            if(newPwMatcher()){
                createNewPassword();
            }else{
                displayToast("Password mismatch. Please enter again");
            }
        }
    }

    public void sendEmail(String email){
        String fromEmail = "cz2002.fsp2.group5@gmail.com";
        String fromPassword = "cz2006@ssp1";
        String toEmail = email;
        String emailSubject = "Forgot Password";
        int randomOTP = ThreadLocalRandom.current().nextInt(1000, 9999);
        String emailBody = "Your OTP is: " + randomOTP;
        new SendMailTask(ForgotPasswordActivity.this).execute(fromEmail,
                fromPassword, toEmail, emailSubject, emailBody);

        randomGeneratedCode = randomOTP;
    }

    public void textTransform(String s1, String s2){

        forgotPwTextView.setText(s1);
        requestCodeBtn.setText(s2);
    }

    public boolean newPwMatcher(){


        final EditText oldpwEditText = (EditText) findViewById(R.id.email);
        final String oldpwCodeText = oldpwEditText.getText().toString();

        final EditText newpwEditText = (EditText) findViewById(R.id.newPassword);
        final String newpwCodeText = newpwEditText.getText().toString();

        return(oldpwCodeText.matches(newpwCodeText));

        //return true;
    }

    public boolean pwCodeMatcher(int code) {

        final EditText userCodeEditText = (EditText) findViewById(R.id.email);
        final String userCodeText = userCodeEditText.getText().toString();
        return (Integer.toString(code).matches(userCodeText));
        //return true;
    }

    public void createNewPassword(){
        final EditText newpwEditText = (EditText) findViewById(R.id.newPassword);
        final String newpwCodeText = newpwEditText.getText().toString();
        final String tmpUserName = currentUser.getUsername();

        String adminPw = currentUser.get("adminPw").toString();
        if (newpwCodeText.isEmpty()) {
            displayToast("The new password field is empty. Please enter and try again");
        }
        else{
            handlePwReset(tmpUserName, adminPw, newpwCodeText);
        }
    }

    //show required display message
    public void displayToast(String s){
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    public void handlePwReset(final String tmpUserName,final String adminPw, final String newPassword){
        ParseUser.logInInBackground(tmpUserName, adminPw, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {

                if (user != null) {
                    user.setPassword(md5(newPassword));
                    user.put("adminPw", md5(newPassword));
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                displayToast("Your password has been reset successfully.");
                                cleanUp();
                                navigateBackToMainPage();
                            }else{
                                displayToast(e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    public void cleanUp(){
        ParseUser tmpUser = ParseUser.getCurrentUser();
        ParseUser.logOut();
        tmpUser.logOut();
        tmpUser = ParseUser.getCurrentUser();
    }

    public void navigateBackToMainPage(){
        //to further ensure the cleanup is done
        cleanUp();

        //navigate back to main page
        Intent mainPage = new Intent(getApplicationContext(), MainNavigationActivity.class);
        mainPage.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
        startActivity(mainPage);
    }
}

