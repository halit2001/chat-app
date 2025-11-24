package com.chat_app.common_library.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberDetailsResponse {
    private String id;
    private String username;
    private String email;
}
