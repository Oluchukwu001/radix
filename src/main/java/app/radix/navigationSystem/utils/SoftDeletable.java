package app.radix.navigationSystem.utils;

public interface SoftDeletable {
    void setDeleted(boolean deleted);
    boolean isDeleted();
}