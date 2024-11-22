package sap.ass02.vertxrideservice.domain.entities;

import java.util.Objects;

/**
 * The implementation of the MutableUser interface.
 */
public class UserImpl implements User {

    private int id;
    private String userName;
    private int credit;
    private boolean admin;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setName(String username) {
        this.userName = username;
    }

    @Override
    public void setCredit(int credit) {
        this.credit = credit;
    }

    @Override
    public void setIsAdmin(boolean isAdmin) {
        this.admin = isAdmin;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public String userName() {
        return this.userName;
    }

    @Override
    public int credit() {
        return this.credit;
    }

    @Override
    public boolean admin() {
        return this.admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserImpl that = (UserImpl) o;
        return id == that.id && credit == that.credit && admin == that.admin && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, credit, admin);
    }
}
