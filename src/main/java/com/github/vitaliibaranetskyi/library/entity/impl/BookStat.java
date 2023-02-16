package com.github.vitaliibaranetskyi.library.entity.impl;

import com.github.vitaliibaranetskyi.library.entity.Entity;

import java.util.Objects;

public class BookStat extends Entity {
    private static final long serialVersionUID = 1L;

    private long total;
    private long inStock;
    private long reserved;
    private long timesWasBooked;

    /**
     * Made private intentionally, use Builder class to instantiate
     */
    private BookStat(long id, long total, long inStock, long reserved, long timesWasBooked) {
        super(id);
        this.total = total;
        this.inStock = inStock;
        this.reserved = reserved;
        this.timesWasBooked = timesWasBooked;
    }

    /**
     * Builder pattern
     */
    public static class Builder {
        private long id = -1;
        private long total;
        private long inStock = -1;
        private long reserved;
        private long timesWasBooked;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setTotal(long total) {
            this.total = total;
            return this;
        }

        public Builder setInStock(long inStock) {
            this.inStock = inStock;
            return this;
        }

        public Builder setReserved(long reserved) {
            this.reserved = reserved;
            return this;
        }

        public Builder setTimesWasBooked(long timesWasBooked) {
            this.timesWasBooked = timesWasBooked;
            return this;
        }

        public BookStat build() {
            if (inStock == -1) {
                inStock = total;
            }
            return new BookStat(id, total, inStock, reserved, timesWasBooked);
        }
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getInStock() {
        return inStock;
    }

    public void setInStock(long inStock) {
        this.inStock = inStock;
    }

    public long getReserved() {
        return reserved;
    }

    public void setReserved(long reserved) {
        this.reserved = reserved;
    }

    public long getTimesWasBooked() {
        return timesWasBooked;
    }

    public void setTimesWasBooked(long timesWasBooked) {
        this.timesWasBooked = timesWasBooked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookStat bookStat = (BookStat) o;
        return total == bookStat.total && inStock == bookStat.inStock
                && reserved == bookStat.reserved && timesWasBooked == bookStat.timesWasBooked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, inStock, reserved, timesWasBooked);
    }

    @Override
    public String toString() {
        return "BookStat{" +
                "id=" + id +
                ", total=" + total +
                ", inStock=" + inStock +
                ", reserved=" + reserved +
                ", timesWasBooked=" + timesWasBooked +
                '}';
    }
}
