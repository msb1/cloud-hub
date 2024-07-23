package com.toptech.hubresource.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class  AuthRequest {

    private String username;
    private String password;

}
