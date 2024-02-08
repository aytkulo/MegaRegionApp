package com.kg.megaregionapp.expense;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Aytkul Omurzakov on 7/9/2017.
 */

public class Account implements Serializable {

    public String expenseId;

    public long amount;

    public long expense;

    public String postman;

    public String enteredUser;

    public String updatedUser;

    public String expenseDate;

    public Date createdDate;

    public Date updatedDate;

}
