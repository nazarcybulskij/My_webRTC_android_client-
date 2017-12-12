package nazarko.inveritasoft.com.my_client_2.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by nazarko on 12.12.17.
 */
@Dao
public interface UsersDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM users")
    List<User> getAll();

    @Delete
    void deleteAll(User... categories);


}
