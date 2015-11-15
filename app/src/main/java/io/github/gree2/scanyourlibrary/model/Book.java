package io.github.gree2.scanyourlibrary.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hqlgree2 on 11/15/15.
 */
public class Book extends RealmObject {

    @PrimaryKey
    private String isbn;
    private String titl;
    private String auth;
    private String publ;
    private String subt;
    private String tran;
    private String year;
    private String page;
    private String desc;
    private String pric;
    private String layo;
    private String fmtn;
    private Boolean sync;

    public String getTitl() {
        return titl;
    }

    public void setTitl(String titl) {
        this.titl = titl;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getPubl() {
        return publ;
    }

    public void setPubl(String publ) {
        this.publ = publ;
    }

    public String getSubt() {
        return subt;
    }

    public void setSubt(String subt) {
        this.subt = subt;
    }

    public String getTran() {
        return tran;
    }

    public void setTran(String tran) {
        this.tran = tran;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPric() {
        return pric;
    }

    public void setPric(String pric) {
        this.pric = pric;
    }

    public String getLayo() {
        return layo;
    }

    public void setLayo(String layo) {
        this.layo = layo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getFmtn() {
        return fmtn;
    }

    public void setFmtn(String fmtn) {
        this.fmtn = fmtn;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }
}