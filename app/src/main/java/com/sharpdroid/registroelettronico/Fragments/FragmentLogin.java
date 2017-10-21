package com.sharpdroid.registroelettronico.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.orm.SugarRecord;
import com.sharpdroid.registroelettronico.API.V2.APIClient;
import com.sharpdroid.registroelettronico.Adapters.LoginAdapter;
import com.sharpdroid.registroelettronico.Databases.Entities.LoginRequest;
import com.sharpdroid.registroelettronico.Databases.Entities.Profile;
import com.sharpdroid.registroelettronico.R;
import com.sharpdroid.registroelettronico.Utils.Account;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import kotlin.Unit;
import retrofit2.HttpException;

import static com.sharpdroid.registroelettronico.Utils.Metodi.fetchDataOfUser;
import static com.sharpdroid.registroelettronico.Utils.Metodi.loginFeedback;

public class FragmentLogin extends SlideFragment {

    @BindView(R.id.mail)
    TextInputEditText mEditTextMail;
    @BindView(R.id.password)
    TextInputEditText mEditTextPassword;
    @BindView(R.id.login_btn)
    Button mButtonLogin;

    private boolean loggedIn = false;
    private Context mContext;

    public FragmentLogin() {
        // Required empty public constructor
    }

    public static FragmentLogin newInstance() {
        return new FragmentLogin();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mContext = getContext();
        return inflater.inflate(R.layout.fragment_login_light, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEditTextMail.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(mContext, R.drawable.ic_person), null, null, null);
        mEditTextPassword.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(mContext, R.drawable.ic_password), null, null, null);

        mEditTextMail.setEnabled(!loggedIn);
        mEditTextPassword.setEnabled(!loggedIn);
        mButtonLogin.setEnabled(!loggedIn);

        mButtonLogin.setOnClickListener(v -> login());
        mEditTextPassword.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean canGoForward() {
        return loggedIn;
    }

    private void login() {
        String mEmail = mEditTextMail.getText().toString();
        String mPassword = mEditTextPassword.getText().toString();

        mEditTextMail.setEnabled(false);
        mEditTextPassword.setEnabled(false);
        mButtonLogin.setEnabled(false);
        mButtonLogin.setText(R.string.caricamento);

        APIClient.Companion.with(mContext, null).postLogin(new LoginRequest(mPassword, mEmail, ""))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(login -> {
                    final List<String> checkedIdents = new ArrayList<>();
                    if (login.getChoices() != null) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext()).title("Account multiplo").content("Seleziona gli account che vuoi importare").positiveText("OK").neutralText("Annulla")
                                .alwaysCallMultiChoiceCallback()
                                .dividerColor(Color.TRANSPARENT)
                                .adapter(new LoginAdapter(mPassword, mEmail, login.getChoices(), getContext(), (checked) -> {
                                    checkedIdents.clear();
                                    checkedIdents.addAll(checked);
                                    System.out.println(checked.toString());
                                    return Unit.INSTANCE;
                                }), new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false))
                                .onPositive((materialDialog, dialogAction) -> {
                                    for (String ident : checkedIdents) {
                                        loginWithIdent(mEmail, mPassword, ident);
                                    }
                                    postLogin();
                                })
                                .onNeutral((dialog, which) -> {
                                    mButtonLogin.setText(R.string.login);
                                    mEditTextMail.setEnabled(true);
                                    mEditTextPassword.setEnabled(true);
                                    mButtonLogin.setEnabled(true);
                                });
                        builder.show();
                    } else {
                        SugarRecord.save(new Profile(mEmail, login.getFirstName() + " " + login.getLastName(), mPassword, "", Long.valueOf(login.getIdent().substring(1, 8)), login.getToken(), login.getExpire().getTime()));

                        Account.Companion.with(getActivity()).setUser(Long.valueOf(login.getIdent().substring(1, 8)));
                        fetchDataOfUser(getActivity());
                        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                .putBoolean("first_run", false)
                                .apply();

                        mButtonLogin.setText(R.string.login_riuscito);
                        postLogin();
                    }
                }, error -> {
                    if (error instanceof HttpException) {
                        Log.e("FragmentLogin", ((HttpException) error).response().errorBody().string());
                    }

                    loginFeedback(error, getContext());

                    mButtonLogin.setText(R.string.login);
                    mEditTextMail.setEnabled(true);
                    mEditTextPassword.setEnabled(true);
                    mButtonLogin.setEnabled(true);
                });
    }

    private void postLogin() {
        loggedIn = true;
        updateNavigation();
        nextSlide();
    }

    private void loginWithIdent(String email, String password, String ident) {
        Context c = getContext();
        APIClient.Companion.with(c, null).postLogin(new LoginRequest(password, email, ident))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(login_nested -> {
                    SugarRecord.save(new Profile(email, login_nested.getFirstName() + " " + login_nested.getLastName(), password, "", Long.valueOf(login_nested.getIdent().substring(1, 8)), login_nested.getToken(), login_nested.getExpire().getTime()));
                    Account.Companion.with(c).setUser(Long.valueOf(login_nested.getIdent().substring(1, 8)));
                    fetchDataOfUser(c);

                    PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean("first_run", false).apply();
                    mButtonLogin.setText(R.string.login_riuscito);
                }, error -> {
                    loginFeedback(error, c);

                    mButtonLogin.setText(R.string.login);
                    mEditTextMail.setEnabled(true);
                    mEditTextPassword.setEnabled(true);
                    mButtonLogin.setEnabled(true);
                });
    }
}