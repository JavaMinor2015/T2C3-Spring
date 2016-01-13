package com.infosupport.t2c3.domain.customers;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.infosupport.t2c3.domain.abs.AbsEntity;
import com.infosupport.t2c3.domain.orders.Order;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Windows 7 on 11-1-2016.
 */
@Entity
@Getter
@Setter
public class Customer extends AbsEntity {

    private static final long serialVersionUID = -6108420221541643144L;

    private String firstName;
    private String lastName;
    private String emailAddress;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Address address;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Credentials credentials;
    @OneToMany(mappedBy = "customer", cascade = CascadeType.MERGE)
    @JsonManagedReference
    private List<Order> orders;

    /**
     * Add an order to the customer.
     *
     * @param order the order
     */
    public void addOrder(Order order) {
        orders.add(order);
    }
}