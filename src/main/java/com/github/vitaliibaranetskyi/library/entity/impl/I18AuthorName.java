package com.github.vitaliibaranetskyi.library.entity.impl;

import com.github.vitaliibaranetskyi.library.entity.Entity;

import java.util.Objects;

public class I18AuthorName extends Entity {
    private static final long serialVersionUID = 1L;

    private Lang lang;
    private String name;

    /**
     * Made private intentionally, use Builder class to instantiate
     */
    private I18AuthorName(long id, Lang lang, String name) {
        super(id);
        this.lang = lang;
        this.name = name;
    }

    /**
     * Builder pattern
     */
    public static class Builder {
        private long id = -1;
        private Lang lang;
        private String name;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setLang(Lang lang) {
            this.lang = lang;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public I18AuthorName build() {
            return new I18AuthorName(id, lang, name);
        }
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        I18AuthorName that = (I18AuthorName) o;
        return lang == that.lang && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lang, name);
    }

    @Override
    public String toString() {
        return "I18AuthorName{" +
                ", langId=" + lang +
                ", name='" + name + '\'' +
                '}';
    }
}
