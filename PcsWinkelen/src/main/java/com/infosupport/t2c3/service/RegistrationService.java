package com.infosupport.t2c3.service;

import com.infosupport.t2c3.domain.accounts.Customer;
import com.infosupport.t2c3.esb.DataVaultService;
import com.infosupport.t2c3.security.SecurityService;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Windows 7 on 12-1-2016.
 */
@RestController
@RequestMapping(produces = "application/json")
@CrossOrigin
@Setter
public class RegistrationService {

    private Logger logger = LogManager.getLogger(RegistrationService.class.getName());
    public static final String CUSTOMER_REGISTER = "CUSTOMER_REGISTER";

    @Autowired
    private SecurityService securityService;
    @Autowired
    private DataVaultService dataVaultService;

    /**
     * Register a customer.
     *
     * @param customer customer with data to write to db.
     * @return token, so user is immediately logged in
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> register(@RequestBody Customer customer) {
        securityService.register(customer);
        dataVaultService.store(CUSTOMER_REGISTER, customer);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
