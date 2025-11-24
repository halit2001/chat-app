package com.chat_app.common_library.response;

import lombok.*;

@Getter
@Setter
@Builder
public class UserServerPermissions {
    private boolean isOwner;
    private boolean isMember;
}
