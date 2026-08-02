package com.norte.security;

import com.norte.entity.User;

public class CurrentUserHolder {

    private static final ThreadLocal<User> CURRENT = new ThreadLocal<>();

    public static void set(User user) { CURRENT.set(user); }
    public static User get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
}
