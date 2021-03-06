package com.infosupport.t2c3.security;

import com.infosupport.t2c3.domain.accounts.Credentials;
import com.infosupport.t2c3.domain.accounts.Customer;
import com.infosupport.t2c3.exceptions.NonUniqueValueException;
import com.infosupport.t2c3.repositories.CredentialsRepository;
import com.infosupport.t2c3.repositories.CustomerRepository;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Windows 7 on 11-1-2016.
 */
@Component
public class SecurityService {

    private static final int RANDOM_MIN = 12345;
    private static final Logger LOGGER = Logger.getLogger(SecurityService.class.getName());
    public static final BigDecimal DEFAULT_CREDIT_LIMIT = BigDecimal.valueOf(100);


    @Setter
    @Autowired
    private CredentialsRepository credentialsRepo;

    @Setter
    @Autowired
    private CustomerRepository customerRepo;

    /**
     * Create credentials.
     * Calling constructor won't hash the password.
     *
     * @param userName the username
     * @param password the password
     * @return created credentials with hashed password
     */
    public Credentials createCredentials(String userName, String password) {
        String hashedPassword = hash(password);
        return new Credentials(userName, hashedPassword, "");
    }

    /**
     * Verifies the given username and password for login try.
     *
     * @param userName given username
     * @param password given password
     * @return login token
     */
    public String verify(String userName, String password) {
        String encodedPassword = hash(password);

        Credentials c = credentialsRepo.findByUserName(userName);
        if (c != null && encodedPassword.equals(c.getPassword())) {
            return createToken(c);
        }
        return "";
    }

    /**
     * Creates a token from firstname, lastname and random number.
     *
     * @param c credentials used for logging in
     * @return generated token
     */
    private String createToken(Credentials c) {
        Customer customer = customerRepo.findByCredentialsUserName(c.getUserName());
        String token = hash(customer.getFirstName() + customer.getLastName() + (new SecureRandom().nextInt() + RANDOM_MIN));
        customer.getCredentials().setToken(token);
        credentialsRepo.save(customer.getCredentials());
        return token;
    }

    /**
     * SHA-512 hash a String.
     *
     * @param toHash String to be hashed
     * @return hashed String
     */
    private String hash(String toHash) {
        MessageDigest mda = null;
        try {
            mda = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e);
        }
        String hashedString = "";
        if (mda != null) {
            hashedString = Hex.encodeHexString(mda.digest(toHash.getBytes(Charset.forName("UTF-8"))));
        }
        return hashedString;
    }

    /**
     * Log a user out, clear token in database.
     *
     * @param token token of the user to log out
     */
    public void logout(String token) {
        Credentials credentials = credentialsRepo.findByToken(token);
        credentials.setToken("");
        credentialsRepo.save(credentials);
    }

    /**
     * Register a customer.
     *
     * @param customer customer with data to write to db.
     */
    public void register(Customer customer) throws NonUniqueValueException {
        Credentials oldCredentials = customer.getCredentials();

        Customer cust = customerRepo.findByCredentialsUserName(oldCredentials.getUserName());
        if (cust != null) {
            throw new NonUniqueValueException("Already a customer with this username");
        }

        Credentials newCredentials = createCredentials(oldCredentials.getUserName(), oldCredentials.getPassword());

        customer.setCredentials(newCredentials);
        customer.setCreditLimit(DEFAULT_CREDIT_LIMIT);

        customerRepo.save(customer);
    }

    /**
     * Find a customer by their token.
     *
     * @param tokenValue the token
     * @return the customer or null
     */
    public Customer getCustomerByToken(String tokenValue) {
        return customerRepo.findByCredentialsToken(tokenValue);
    }
}
