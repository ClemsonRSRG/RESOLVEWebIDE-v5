package controllers.common.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.common.database.User;
import models.common.form.RegistrationForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.CSRF;
import play.filters.csrf.RequireCSRFCheck;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.common.authentication.registration;
import views.html.common.authentication.registrationSuccess;

/**
 * <p>This class serves as a controller class for registering new users.</p>
 *
 * @author Yu-Shan Sun
 * @version 1.0
 */
public class Registration extends Controller {

    // ===========================================================
    // Global Variables
    // ===========================================================

    /** <p>Play Framework's WS API client</p> */
    @Inject
    private WSClient myWSClient;

    /** <p>Form factory</p> */
    @Inject
    private FormFactory myFormFactory;

    /** <p>An executor to get the current HTTP context.</p> */
    @Inject
    private HttpExecutionContext myHttpExecutionContext;

    /** <p>JPA API</p> */
    @Inject
    private JPAApi myJpaApi;

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>This renders the registration page for the WebIDE.</p>
     *
     * @return The result of rendering the page.
     */
    @AddCSRFToken
    public Result index() {
        String token = CSRF.getToken(request()).map(t -> t.value()).orElse("no token");
        return ok(registration.render(myFormFactory.form(RegistrationForm.class), token));
    }

    /**
     * <p>This handles the registration form submission for the WebIDE.</p>
     *
     * @return The result of rendering the page.
     */
    @AddCSRFToken
    @RequireCSRFCheck
    @Transactional(readOnly = true)
    public CompletionStage<Result> handleSubmit() {
        Form<RegistrationForm> userForm = myFormFactory.form(RegistrationForm.class).bindFromRequest();

        // Perform the basic validation checks.
        if (userForm.hasErrors()) {
            String token = CSRF.getToken(request()).map(t -> t.value()).orElse("no token");
            return CompletableFuture.supplyAsync(() -> badRequest(registration.render(userForm, token)),
                    myHttpExecutionContext.current());
        } else {
            RegistrationForm form = userForm.get();

            // Perform our own validation checks. If we detect errors, then
            // we display the registration page with the errors highlighted.
            // If there are no errors, we display the success page.
            CompletionStage<List<ValidationError>> resultPromise = validate(form);
            return resultPromise.thenApplyAsync(result -> {
                    if (result != null) {
                        String token = CSRF.getToken(request()).map(t -> t.value()).orElse("no token");
                        for (ValidationError error : result) {
                            userForm.reject(error);
                        }
                        return badRequest(registration.render(userForm, token));
                    }
                    else {
                        return ok(registrationSuccess.render());
                    }
                }, myHttpExecutionContext.current());
        }
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    /**
     * <p>Our own custom validation method for the registration form.</p>
     *
     * @return A {@link CompletionStage} with a list of {@link ValidationError}
     * if there are errors in the registration form, {@code null} otherwise.
     */
    @Transactional(readOnly = true)
    private CompletionStage<List<ValidationError>> validate(RegistrationForm form) {
        // Check the reCaptcha server to see if this form contains a valid
        // user response token provided by reCaptcha.
        String postData = "secret=" + "6LfPmRcTAAAAAOkZxLlOOrL9sa1_z7d3CDenChdi" + "&response=" + form.getReCaptcha();
        CompletionStage<JsonNode> responsePromise = myWSClient.url("https://www.google.com/recaptcha/api/siteverify").
                setContentType("application/x-www-form-urlencoded").
                post(postData).thenApply(WSResponse::asJson);

        // Once we obtain a JsonNode response from the reCaptcha server, apply the following
        // code asynchronously. Notice that we have to pass a "HttpExecutionContext" to avoid
        // getting a "No HTTP Context" error.
        return responsePromise.thenApplyAsync(response -> {
            List<ValidationError> errors = new ArrayList<>();

            // Check to see if we got a success response from the reCaptcha server
            if (!response.get("success").asBoolean()) {
                errors.add(new ValidationError("reCaptchaFailure", "The reCaptcha did not succeed."));
            }

            // Check for a registered user with the same email.
            // Note that "findByEmail" expects a JPA entity manager,
            // which is not present if we don't wrap the call using
            // "withTransaction()".
            ValidationError emailError = myJpaApi.withTransaction(() -> {
                if (User.findByEmail(form.getEmail()) != null) {
                    return new ValidationError("registeredEmail", "This e-mail is already registered.");
                }

                return null;
            });

            // If the result from the "withTransaction" call contains a
            // "ValidationError", then we add it to our list.
            if (emailError != null) {
                errors.add(emailError);
            }

            // Check that the password has a minimum length of 6 and a maximum of 20
            if (form.getPassword().length() < 6 || form.getPassword().length() > 20) {
                errors.add(new ValidationError("passwordLength", "The password must be 6-20 characters long."));
            } else {
                if (!form.getPassword().equals(form.getConfirmPassword())) {
                    errors.add(new ValidationError("notSamePassword", "The two password fields do not match."));
                }
            }

            return errors.isEmpty() ? null : errors;
        }, myHttpExecutionContext.current());
    }

}