package com.gobi.android.model;

import java.util.Objects;

public class Firma {
    private volatile static Firma instance;
    private String firmaAdi;
    private Double firmaLat;
    private Double firmaLon;
    private Integer satinAlMiktari;

    private Firma() {

    }

    public static Firma getInstance() {
        if (instance == null) {
            synchronized (User.class) {
                if (instance == null) {
                    instance = new Firma();
                }
            }
        }
        return instance;
    }

    public String getFirmaAdi() {
        return firmaAdi;
    }

    public void setFirmaAdi(String firmaAdi) {
        this.firmaAdi = firmaAdi;
    }

    public Double getFirmaLat() {
        return firmaLat;
    }

    public void setFirmaLat(Double firmaLat) {
        this.firmaLat = firmaLat;
    }

    public Double getFirmaLon() {
        return firmaLon;
    }

    public void setFirmaLon(Double firmaLon) {
        this.firmaLon = firmaLon;
    }

    public Integer getSatinAlMiktari() {
        return satinAlMiktari;
    }

    public void setSatinAlMiktari(Integer satinAlMiktari) {
        this.satinAlMiktari = satinAlMiktari;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Firma)) return false;
        Firma firma = (Firma) o;
        return Objects.equals(getFirmaAdi(), firma.getFirmaAdi()) && Objects.equals(getFirmaLat(), firma.getFirmaLat()) && Objects.equals(getFirmaLon(), firma.getFirmaLon()) && Objects.equals(getSatinAlMiktari(), firma.getSatinAlMiktari());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirmaAdi(), getFirmaLat(), getFirmaLon(), getSatinAlMiktari());
    }
}
