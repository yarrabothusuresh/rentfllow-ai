package com.rentflow.portal.dto;

public class CustomerLoginRequestDTO {
    private String email;
    private String password;

    public CustomerLoginRequestDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
