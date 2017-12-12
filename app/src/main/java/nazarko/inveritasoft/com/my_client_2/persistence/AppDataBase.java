package nazarko.inveritasoft.com.my_client_2.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by nazarko on 12.12.17.
 */
@Database(entities = {User.class},version = 1)
public abstract class AppDataBase extends RoomDatabase {

    public static final String DB_NAME = "app_db";

    public abstract UsersDao getUserDao();


}
