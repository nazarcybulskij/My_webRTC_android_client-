package nazarko.inveritasoft.com.my_client_2;

import android.app.Activity;
import android.app.DialogFragment;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import nazarko.inveritasoft.com.my_client_2.persistence.AppDataBase;
import nazarko.inveritasoft.com.my_client_2.persistence.User;

import static org.webrtc.ContextUtils.getApplicationContext;

/**
 * Created by nazarko on 12.12.17.
 */

public class EnterEmailNewUserFragment extends DialogFragment {

    EditText emailEdt;
    Button sentEmailBtn;
    Button closeBtn;

    AppDataBase dataBase;



    public static EnterEmailNewUserFragment newInstance(){
        EnterEmailNewUserFragment enterNewEmailDialog = new EnterEmailNewUserFragment();
        return enterNewEmailDialog;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_enter_user_mail, container, false);
        initDate();
        initView(v);
        return v;
    }

    private void initView(View v) {
        emailEdt = (EditText) v.findViewById(R.id.edt_email);
        sentEmailBtn = (Button) v.findViewById(R.id.btn_sent);
        closeBtn  = (Button) v.findViewById(R.id.close);
        closeBtn.setOnClickListener(view -> {
            dismiss();
        });
        sentEmailBtn.setOnClickListener(view -> {
            if (!emailEdt.getText().toString().trim().equals(""))
                saveEmail(emailEdt.getText().toString().trim());
            else
                Toast.makeText(getActivity(), "пустий емайл", Toast.LENGTH_SHORT).show();
        });

    }

    private void saveEmail(String email) {
        if (dataBase!=null){
            User user = new User();
            user.setName(email);
             new AsyncTask<User, Void, Void>() {
                 @Override
                 protected Void doInBackground(User... users) {
                     dataBase.getUserDao().insert(users[0]);
                     return  null;
                 }
                 @Override
                 protected void onPostExecute(Void aVoid) {
                     dismiss();
                 }
             }.execute(user);

        }
    }

    private void initDate() {
        dataBase = Room.databaseBuilder(getActivity(), AppDataBase.class, AppDataBase.DB_NAME).build();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if(activity instanceof DialogInterface.OnDismissListener){
            ((DialogInterface.OnDismissListener)activity).onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }
}
