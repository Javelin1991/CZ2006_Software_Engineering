package seaplus.seefood.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.CallbackManager;

import seaplus.seefood.R;

public class AccountFragment extends Fragment {


    private Button fbtn;
    private Button loginBtn;
    private Button newUserBtn;
    private CallbackManager callbackManager;
    private static Activity parent;
    public static final String MyPREFERENCES = "MyPrefs" ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    public void backGroundThread(Activity parent) {
        this.parent = parent;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Account");
        setHasOptionsMenu(true);


        attachedOnclickListener();
        newUserOnclickListener();
        loginFacebookOnclickListener();
    }

    private void loginFacebookOnclickListener(){
        fbtn = (Button) getView().findViewById(R.id.login_button);
        fbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newFBUser = new Intent(view.getContext(), FacebookLoginActivity.class);
                newFBUser.putExtra("CLocationObj", getActivity().getIntent().getParcelableExtra("CLocationObj"));
                startActivity(newFBUser);
            }
        });
    }

    private void newUserOnclickListener(){
        newUserBtn = (Button) getView().findViewById(R.id.newuser);
        newUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newUser = new Intent(view.getContext(), NewUserSignUpActivity.class);
                newUser.putExtra("CLocationObj", getActivity().getIntent().getParcelableExtra("CLocationObj"));
                startActivity(newUser);
            }
        });
    }


    private void attachedOnclickListener(){
        loginBtn= (Button) getView().findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent normalLogin = new Intent(view.getContext(), NormalLoginActivity.class);
                normalLogin.putExtra("CLocationObj", getActivity().getIntent().getParcelableExtra("CLocationObj"));
                startActivity(normalLogin);
            }
        });
    }

    //disable action button (search and filter) for account
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.search_btn).setVisible(false);
        menu.findItem(R.id.filter_btn).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}