package nazarko.inveritasoft.com.my_client_2.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by nazarko on 12.12.17.
 */
@Entity(tableName = "users")
public class User {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
