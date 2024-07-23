package com.toptech.hubresource.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String username;
    private String token;
}
