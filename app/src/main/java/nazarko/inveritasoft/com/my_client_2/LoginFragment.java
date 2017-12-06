package nazarko.inveritasoft.com.my_client_2;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import nazarko.inveritasoft.com.my_client_2.model.Response;
import nazarko.inveritasoft.com.my_client_2.network.NetworkUtil;
import nazarko.inveritasoft.com.my_client_2.util.Validation;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static nazarko.inveritasoft.com.my_client_2.util.Validation.validateEmail;
import static nazarko.inveritasoft.com.my_client_2.util.Validation.validateFields;

/**
 * Created by nazarko on 06.12.17.
 */

public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private EditText mEtEmail;
    private EditText mEtPassword;
    private Button mBtLogin;
    private TextView mTvRegister;
    private TextInputLayout mTiEmail;
    private TextInputLayout mTiPassword;
    private ProgressBar mProgressBar;

    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login,container,false);
        initData();
        initViews(view);
        return  view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    private void initData() {
        mSubscriptions = new CompositeSubscription();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }


    private void initViews(View v) {
            mEtEmail = (EditText) v.findViewById(R.id.et_email);
            mEtPassword = (EditText) v.findViewById(R.id.et_password);
            mBtLogin = (Button) v.findViewById(R.id.btn_login);
            mTiEmail = (TextInputLayout) v.findViewById(R.id.ti_email);
            mTiPassword = (TextInputLayout) v.findViewById(R.id.ti_password);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progress);
            mTvRegister = (TextView) v.findViewById(R.id.tv_register);

            mBtLogin.setOnClickListener(view -> login());
            mTvRegister.setOnClickListener(view -> goToRegister());
        }

    private void goToRegister() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        RegisterFragment fragment = new RegisterFragment();
        ft.replace(R.id.fragmentFrame,fragment,RegisterFragment.TAG);
        ft.commit();

    }

    private void login() {
        setError();
        String email = mEtEmail.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();
        int err = 0;
        if (!validateEmail(email)){
            err++;
            mTiEmail.setError("Email should be valid !");
        }

        if (!validateFields(password)) {
            err++;
            mTiPassword.setError("Password should not be empty !");
        }

        if (err == 0){
            loginProcess(email,password);
            mProgressBar.setVisibility(View.VISIBLE);
        }else{
           showToast("Enter Valid Details !");
        }
    }

    private void loginProcess(String email, String password) {
        mSubscriptions.add(NetworkUtil.getRetrofit(email, password).login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));

    }

    private void handleError(Throwable error) {
        mProgressBar.setVisibility(View.GONE);
        if (error instanceof HttpException) {
            Gson gson = new GsonBuilder().create();
            try {
                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showToast(response.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showToast("Network Error !");
        }
    }

    private void showToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    private void handleResponse(Response response) {

        mProgressBar.setVisibility(View.GONE);
        mEtEmail.setText(null);
        mEtPassword.setText(null);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.TOKEN,response.getToken());
        editor.putString(Constants.EMAIL,response.getMessage());
        editor.apply();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);

    }

    private void setError() {
        mEtEmail.setError(null);
        mEtPassword.setError(null);
    }


}
