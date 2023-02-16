package com.github.vitaliibaranetskyi.library.entity.impl;

import com.github.vitaliibaranetskyi.library.entity.ModifiableEntity;

import java.util.Calendar;

public class User extends ModifiableEntity {
    private static final long serialVersionUID = 1L;

    private String email;
    private String password;
    private String salt;
    private Role role;
    private State state;
    private double fine;
    private String name;
    private Lang preferredLang;
    private Calendar fineLastChecked;

    /**
     * User roles, used in authorization
     */
    public enum Role {
        UNKNOWN, USER, LIBRARIAN, ADMIN
    }

    /**
     * User states
     */
    public enum State {
        UNKNOWN, VALID, BLOCKED, DELETED
    }

    /**
     * Builder pattern. This class also sets defaults for id, role and state. If they are not defined, default values
     * will be applied.
     */
    public static class Builder {
        private long id = -1;
        private Calendar modified;
        private String email;
        private String password;
        private String salt;
        private Role role;
        private State state;
        private double fine;
        private String name;
        private Lang preferredLang;
        private Calendar fineLastChecked;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setModified(Calendar modified) {
            this.modified = modified;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setSalt(String salt) {
            this.salt = salt;
            return this;
        }

        public Builder setRole(Role role) {
            this.role = role;
            return this;
        }

        public Builder setRole(String role) {
            if (role != null) {
                this.role = Role.valueOf(role);
            }
            return this;
        }

        public Builder setState(State state) {
            this.state = state;
            return this;
        }

        public Builder setState(String state) {
            if (state !=null) {
                this.state = State.valueOf(state);
            }
            return this;
        }

        public Builder setFine(double fine) {
            this.fine = fine;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPreferredLang(Lang preferredLang) {
            this.preferredLang = preferredLang;
            return this;
        }

        public Builder setFineLastChecked(Calendar fineLastChecked) {
            this.fineLastChecked = fineLastChecked;
            return this;
        }

        public User build() {
            // set default values
            if (role == null) {
                role = Role.USER;
            }

            if (state == null) {
                state = State.VALID;
            }
            return new User(id, modified, email, password, salt, role, state, fine, name, preferredLang,
                    fineLastChecked);
        }
    }

    /**
     * Made private intentionally, use Builder class to instantiate
     */
    private User(long id, Calendar modified, String email,
                 String password, String salt, Role role, State state, double fine,
                 String name, Lang preferredLang, Calendar fineLastChecked) {
        super(id, modified);
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.role = role;
        this.state = state;
        this.fine = fine;
        this.name = name;
        this.preferredLang = preferredLang;
        this.fineLastChecked = fineLastChecked;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Lang getPreferredLang() {
        return preferredLang;
    }

    public void setPreferredLang(Lang preferredLang) {
        this.preferredLang = preferredLang;
    }

    public Calendar getFineLastChecked() {
        return fineLastChecked;
    }

    public void setFineLastChecked(Calendar fineLastChecked) {
        this.fineLastChecked = fineLastChecked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", modified=" + ModifiableEntity.format(modified) +
                ", email='" + email + '\'' +
                ", password=<hidden>" +
                ", salt=<hidden>" +
                ", role=" + role +
                ", state=" + state +
                ", fine=" + fine +
                ", name='" + name + '\'' +
                ", preferredLang=" + preferredLang +
                ", fineLastChecked=" + ModifiableEntity.format(fineLastChecked) +
                '}';
    }
}