package com.auth.service;

import com.auth.model.dto.UserDto;

public interface AuthUserDetailsService {

	public void removeCachedUser(String username);
	public UserDto getUserDetails(String username);
	
}
