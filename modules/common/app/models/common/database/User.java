/*
 *  ---------------------------------
 *  Copyright (c) 2017
 *  RESOLVE Software Research Group
 *  School of Computing
 *  Clemson University
 *  All rights reserved.
 *  ---------------------------------
 *  This file is subject to the terms and conditions defined in
 *  file 'LICENSE.txt', which is part of this source code package.
 */

package models.common.database;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import deadbolt2.common.models.UserPermission;
import deadbolt2.common.models.UserRole;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import models.common.ModelUtilities;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

/**
 * <p>This class is the relational mapping of a user in the database and provides
 * methods to change the user in the database.</p>
 *
 * @author Chuck Cook
 * @author Yu-Shan Sun
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User implements Subject {

    // ===========================================================
    // Global Variables
    // ===========================================================

    /** <p>Unique ID for each user.</p> */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** <p>User's Email and Login User Name</p> */
    @Constraints.Required
    @Constraints.Email
    public String email;

    /** <p>User's Password (Hashed)</p> */
    @Constraints.Required
    public String password;

    /** <p>User's First Name</p> */
    @Constraints.Required
    public String firstName;

    /** <p>User's Last Name</p> */
    @Constraints.Required
    public String lastName;

    /** <p>User Type</p> */
    @Constraints.Required
    public int userType;

    /** <p>Last Login Date</p> */
    @Column(name = "lastLogin", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    public Date lastLogin;

    /** <p>Account Creation Date</p> */
    @Column(name = "createdOn", columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    @Constraints.Required
    public Date createdOn;

    /** <p>Boolean flag to check if User
     * is authenticated or not.</p> */
    public boolean authenticated;

    /** <p>Temporary code generated by the system
     * for authentication purposes.</p> */
    public String confirmationCode;

    /** <p>User's Default Project</p> */
    @Constraints.Required
    public String currentProject;

    /** <p>Timeout flag</p> */
    @Constraints.Required
    public int timeout;

    /** <p>Number of tries flag</p> */
    @Constraints.Required
    public int numTries;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>Default constructor. JPA needs this on some occasions.</p>
     */
    private User() {}

    /**
     * <p>Creates a new user object.</p>
     *
     * @param userEmail User's email.
     * @param userPassword User's password.
     * @param userFirstName User's first name.
     * @param userLastName User's last name.
     */
    private User(String userEmail, String userPassword, String userFirstName,
            String userLastName) {
        // User information
        email = userEmail;
        firstName = userFirstName;
        lastName = userLastName;
        password = ModelUtilities.encryptPassword(userPassword);
        userType = 0;
        lastLogin = null;
        createdOn = new Date();
        currentProject = Project.getDefault().name;
        timeout = 5;
        numTries = 3;
        authenticated = false;

        // Generate confirmation code
        confirmationCode =
                ModelUtilities.generateConfirmationCode(password, email,
                        firstName, lastName);
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>Adds a new user to the database.</p>
     *
     * @param email Email entered by the user.
     * @param password Password entered by the user.
     * @param userFirstName First name entered by the user.
     * @param userLastName Last name entered by the user.
     *
     * @return The newly created user object.
     */
    @Transactional
    public static User addUser(String email, String password,
            String userFirstName, String userLastName) {
        User u = new User(email, password, userFirstName, userLastName);
        u.save();

        return u;
    }

    /**
     * <p>Set the specified user to be authenticated.</p>
     *
     * @param email Email entered by the user.
     */
    @Transactional
    public static User authenticate(String email) {
        User u = findByEmail(email);
        u.authenticated = true;
        u.confirmationCode = "";
        u.save();

        return u;
    }

    /**
     * <p>Attempts to return a user if the email exists in
     * our database and the specified password matches as well.</p>
     *
     * @param email Email entered by the user.
     * @param password Password entered by the user.
     *
     * @return User if all the information matches, null otherwise.
     */
    @Transactional(readOnly = true)
    public static User connect(String email, String password) {
        User u = findByEmail(email);

        // Check user password if email is found.
        if (u != null) {
            if (!u.password.equals(ModelUtilities.encryptPassword(password))) {
                u = null;
            }
        }

        return u;
    }

    /**
     * <p>Edits the user specified by the {@code currentUserEmail}.</p>
     *
     * @param currentUserEmail Current user email.
     * @param firstName Updated user first name.
     * @param lastName Updated user last name.
     * @param email Updated user email.
     * @param timeout Updated timeout flag.
     * @param numTries Updated number of tries flag.
     */
    @Transactional
    public static void editUserProfile(String currentUserEmail,
            String firstName, String lastName, String email, int timeout,
            int numTries) {
        Query query =
                JPA.em()
                        .createQuery(
                                "update User u set u.email = :email, u.firstName = :firstName, "
                                        + "u.lastName = :lastName, u.timeout = :timeout, u.numTries = :numTries "
                                        + "where u.email = :currentUserEmail");
        query.setParameter("email", email);
        query.setParameter("firstName", firstName);
        query.setParameter("lastName", lastName);
        query.setParameter("timeout", timeout);
        query.setParameter("numTries", numTries);
        query.setParameter("currentUserEmail", currentUserEmail);
        query.executeUpdate();
    }

    /**
     * <p>Find a user by email.</p>
     *
     * @param email User email.
     *
     * @return The user corresponding to the query email.
     */
    @Transactional(readOnly = true)
    public static User findByEmail(String email) {
        Query query =
                JPA.em().createQuery(
                        "select u from User u where u.email = :email",
                        User.class);
        query.setParameter("email", email);

        List result = query.getResultList();
        User user = null;
        if (!result.isEmpty()) {
            user = (User) result.get(0);
        }

        return user;
    }

    /**
     * <p>Gets a unique identifier for the subject, such as a user name.
     * This is never used by {@code Deadbolt} itself, and is present to
     * provide an easy way of getting a useful piece of user information in,
     * for example, dynamic checks without the need to cast the {@code Subject}.</p>
     *
     * @return The user ID.
     */
    @Override
    public final String getIdentifier() {
        return String.valueOf(id);
    }

    /**
     * <p>Get all {@link Permission Permissions} held by this subject. Ordering is not important.</p>
     *
     * @return A non-null list of permissions.
     */
    @Override
    public final List<? extends Permission> getPermissions() {
        // Add the different permissions for this user.
        List<Permission> permissions = new ArrayList<>();
        if (authenticated) {
            permissions.add(UserPermission.ACTIVEUSER);
        }

        return permissions;
    }

    /**
     * <p>Get all {@link Role Roles} held by this subject. Ordering is not important.</p>
     *
     * @return A non-null list of roles.
     */
    @Override
    public final List<? extends Role> getRoles() {
        // Add the role based on the userType.
        List<Role> roles = new ArrayList<>();
        switch (userType) {
        case 1:
            roles.add(UserRole.SUPERUSER);
            break;
        case 2:
            roles.add(UserRole.ADMIN);
            break;
        default:
            roles.add(UserRole.USER);
            break;
        }

        return roles;
    }

    /**
     * <p>Checks to see if the user has been authenticated
     * or not.</p>
     *
     * @param email Email entered by the user.
     *
     * @return True if user is authenticated, false otherwise.
     */
    @Transactional(readOnly = true)
    public static Boolean hasAuthenticated(String email) {
        User u = findByEmail(email);
        return u.authenticated;
    }

    /**
     * <p>Update the last login date.</p>
     *
     * @param email Email entered by the user.
     */
    @Transactional
    public static User lastLogin(String email) {
        User u = findByEmail(email);
        u.lastLogin = new Date();
        u.save();

        return u;
    }

    /**
     * <p>Generate a new confirmation code and set the user
     * to not authenticated.</p>
     *
     * @param email Email entered by the user.
     */
    @Transactional
    public static User setNotAuthenticated(String email) {
        User u = findByEmail(email);
        u.confirmationCode =
                ModelUtilities.generateConfirmationCode(u.password, u.email,
                        u.firstName, u.lastName);
        u.authenticated = false;
        u.save();

        return u;
    }

    /**
     * <p>Update the password for the specified email.</p>
     *
     * @param email Email entered by the user.
     * @param password Password entered by the user.
     */
    @Transactional
    public static User updatePassword(String email, String password) {
        User u = findByEmail(email);
        u.password = ModelUtilities.encryptPassword(password);
        u.save();

        return u;
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    /**
     * <p>Store this user information.</p>
     */
    @Transactional
    private void save() {
        JPA.em().persist(this);
    }

}