package models.common.form;

import java.util.ArrayList;
import java.util.List;
import models.common.database.User;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

/**
 * <p>This class serves as a model class for registration form and allows
 * the fields to be populated by Play Framework's {@link play.data.Form} class.</p>
 *
 * @author Yu-Shan Sun
 * @version 1.0
 */
public class RegistrationForm {

    // ===========================================================
    // Global Variables
    // ===========================================================

    /** <p>New user's first name</p> */
    @Constraints.Required
    private String myFirstName;

    /** <p>New user's last name</p> */
    @Constraints.Required
    private String myLastName;

    /** <p>New user's email</p> */
    @Constraints.Required
    @Constraints.Email
    private String myEmail;

    /** <p>New user's password</p> */
    @Constraints.Required
    private String myPassword;

    /** <p>New user's retype of password</p> */
    @Constraints.Required
    private String myRetypePassword;

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>First name field in the registration form.</p>
     *
     * @return A string.
     */
    public final String getFirstName() {
        return myFirstName;
    }

    /**
     * <p>Stores the first name field.</p>
     *
     * @param firstName First name.
     */
    public final void setFirstName(String firstName) {
        myFirstName = firstName;
    }

    /**
     * <p>Last name field in the registration form.</p>
     *
     * @return A string.
     */
    public final String getLastName() {
        return myLastName;
    }

    /**
     * <p>Stores the last name field.</p>
     *
     * @param lastName Last name.
     */
    public final void setLastName(String lastName) {
        myLastName = lastName;
    }

    /**
     * <p>Email field in the registration form.</p>
     *
     * @return A string.
     */
    public final String getEmail() {
        return myEmail;
    }

    /**
     * <p>Stores the email field.</p>
     *
     * @param email Email
     */
    public final void setEmail(String email) {
        myEmail = email;
    }

    /**
     * <p>Password field in the registration form./p>
     *
     * @return A string.
     */
    public final String getPassword() {
        return myPassword;
    }

    /**
     * <p>Stores the password field.</p>
     *
     * @param password Password
     */
    public void setPassword(String password) {
        myPassword = password;
    }

    /**
     * <p>Retype password field in the registration form.</p>
     *
     * @return A string.
     */
    public final String getRetypePassword() {
        return myRetypePassword;
    }

    /**
     * <p>Stores the retype password field.</p>
     *
     * @param retypePassword Retyped password
     */
    public final void setRetypePassword(String retypePassword) {
        myRetypePassword = retypePassword;
    }

    /**
     * <p>Validation method for the form.</p>
     *
     * @return A list of {@link ValidationError} if there are errors
     * in the registration form, {@code null} otherwise.
     */
    public final List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        // Check for a registered user with the same email.
        if (User.findByEmail(myEmail) != null) {
            errors.add(new ValidationError("email", "This e-mail is already registered."));
        }

        // Check that the password has a minimum length of 6
        if (myPassword.length() < 6) {
            errors.add(new ValidationError("passwordLength", "The password needs to be at least 6 characters long."));
        } else {
            if (!myPassword.equals(myRetypePassword)) {
                errors.add(new ValidationError("notSamePassword", "The two password fields do not match."));
            }
        }

        return errors.isEmpty() ? null : errors;
    }

}