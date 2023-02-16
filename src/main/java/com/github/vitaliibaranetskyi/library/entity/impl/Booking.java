package com.github.vitaliibaranetskyi.library.entity.impl;

import com.github.vitaliibaranetskyi.library.entity.ModifiableEntity;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class Booking extends ModifiableEntity {
    private static final long serialVersionUID = 1L;

    private User user;
    private State state;
    private Place located;
    private List<Book> books;

    /**
     * Made private intentionally, use Builder class to instantiate
     */
    private Booking(long id, Calendar modified, User user, State state, Place located,
                    List<Book> books) {
        super(id, modified);
        this.user = user;
        this.state = state;
        this.located = located;
        this.books = books;
    }

    /**
     * Builder pattern
     */
    public static class Builder {
        private long id = -1;
        private Calendar modified;
        private User user;
        private State state;
        private Place located;
        private List<Book> books = new ArrayList<>();

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setModified(Calendar modified) {
            this.modified = modified;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setState(State state) {
            this.state = state;
            return this;
        }

        public Builder setLocated(Place located) {
            this.located = located;
            return this;
        }

        public Builder setBooks(List<Book> books) {
            this.books = books;
            return this;
        }
        
        public Builder addBook(Book book) {
            books.add(book);
            return this;
        }
        
        public Booking build() {
            if (state == null) {
                state = State.NEW;
            }
            if (located == null) {
                located = Place.LIBRARY;
            }
            
            return new Booking(id, modified, user, state, located, books);
        }
    }
    public enum State {
        UNKNOWN, NEW, BOOKED, DELIVERED, DONE, CANCELED
    }

    public enum Place {
        UNKNOWN, LIBRARY, USER
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Place getLocated() {
        return located;
    }

    public void setLocated(Place located) {
        this.located = located;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public void addBook(Book book) throws ServiceException {
        BookStat bookStat = book.getBookStat();

        if ((bookStat.getInStock() - bookStat.getReserved()) <= 0) {
            throw new ServiceException("error.no.free.books.to.reserve");
        }

        bookStat.setReserved(bookStat.getReserved() + 1);
        bookStat.setTimesWasBooked(bookStat.getTimesWasBooked() + 1);
        books.add(book);
    }

    public void removeBook(Book book) {
        BookStat bookStat = book.getBookStat();

        bookStat.setReserved(bookStat.getReserved() - 1);
        books.remove(book);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return user.equals(booking.user) && books.equals(booking.books);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, books);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "created=" + ModifiableEntity.format(modified) +
                ", id=" + id +
                ", user=" + user +
                ", state=" + state +
                ", located=" + located +
                ", books=" + books +
                '}';
    }
}
