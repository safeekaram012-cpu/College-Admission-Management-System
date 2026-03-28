package com.cams.model;

/**
 * Model class representing a row in the Admins table.
 */
public class Admin {

    private int    adminId;
    private String username;
    private String password;  // SHA-256 hex

    public Admin() {}

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int    getAdminId()          { return adminId; }
    public void   setAdminId(int v)     { this.adminId = v; }

    public String getUsername()         { return username; }
    public void   setUsername(String v) { this.username = v; }

    public String getPassword()         { return password; }
    public void   setPassword(String v) { this.password = v; }

    @Override
    public String toString() {
        return "Admin[id=" + adminId + ", username=" + username + "]";
    }
}
