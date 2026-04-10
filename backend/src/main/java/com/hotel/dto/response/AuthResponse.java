package com.hotel.dto.response;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public AuthResponse() {}

    private AuthResponse(Builder b) {
        this.token = b.token;
        this.userId = b.userId;
        this.email = b.email;
        this.firstName = b.firstName;
        this.lastName = b.lastName;
        this.role = b.role;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token, email, firstName, lastName, role;
        private Long userId;
        public Builder token(String v) { this.token = v; return this; }
        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder firstName(String v) { this.firstName = v; return this; }
        public Builder lastName(String v) { this.lastName = v; return this; }
        public Builder role(String v) { this.role = v; return this; }
        public AuthResponse build() { return new AuthResponse(this); }
    }

    public String getToken() { return token; }
    public String getType() { return type; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
}
