package it.unipi.iet.onspot.utilities;

import com.google.firebase.database.IgnoreExtraProperties;



/* Java Object represent the user in the db */
@IgnoreExtraProperties
public class User {

    public String firstName;
    public String lastName;
    public String birthday;
    public String gender;
    public String photoURL;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String firstName, String lastName, String photoURL, String birthday, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
        this.photoURL = photoURL;
    }

}