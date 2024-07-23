package com.toptech.hubresource.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String email;
    private boolean enabled;
    private List<Role> roles;

    public UserDetails getAuthUser() {
        AuthUser authUser = new AuthUser();
        authUser.setUsername(this.username);
        authUser.setAccountNonExpired(false);
        authUser.setAccountNonLocked(false);
        authUser.setCredentialsNonExpired(false);
        authUser.setEnabled(this.enabled);
        authUser.setPassword(this.password);
        authUser.setRoles(this.roles);
        return authUser;
    }

}


