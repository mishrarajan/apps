package com.auth.service;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.auth.exception.OAuth2AuthenticationProcessingException;
import com.auth.model.AuthUser;
import com.auth.provider.OAuth2UserInfo;
import com.auth.provider.OAuth2UserInfoFactory;
import com.auth.provider.UserPrincipal;
import com.auth.util.AuthProvider;

@Component
public class CustomOidcUserService extends OidcUserService {
	protected static final Log logger = LogFactory.getLog(CustomOidcUserService.class);
	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser user = super.loadUser(userRequest);
		logger.debug("OidcUser: " + user);
		try {
			return processOidcUser(userRequest, user);
		} catch (AuthenticationException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
		}
	}
	private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser)
			throws javax.security.sasl.AuthenticationException {
		logger.debug("Processing OidcUser.");
		OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
				userRequest.getClientRegistration().getRegistrationId(), oidcUser.getAttributes());

		if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
			throw new OAuth2AuthenticationProcessingException("Email not found from Oidc provider");
		}
		Optional<AuthUser> userOptional = Optional.empty(); //userRepository.findByEmail(oAuth2UserInfo.getEmail());
		AuthUser user;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!user.getProvider().equals(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId()))) {
				throw new OAuth2AuthenticationProcessingException(
						"Looks like you're signed up with " + user.getProvider() + " account. Please use your "
								+ user.getProvider() + " account to login.");
			}
			user = updateExistingUser(user, oAuth2UserInfo);
		} else {
			user = registerNewUser(userRequest, oAuth2UserInfo);
		}
		return UserPrincipal.create(user, oidcUser.getAttributes());
	}

	
	private AuthUser registerNewUser(OidcUserRequest oidcUserRequest, OAuth2UserInfo oAuth2UserInfo) {
		logger.debug("Registring OidcUser.");
		AuthUser user = new AuthUser();
		user.setProvider(AuthProvider.valueOf(oidcUserRequest.getClientRegistration().getRegistrationId()));
		user.setProviderId(oAuth2UserInfo.getId());
		user.setName(oAuth2UserInfo.getName());
		user.setEmail(oAuth2UserInfo.getEmail());
		user.setImageUrl(oAuth2UserInfo.getImageUrl());
		//return userRepository.save(user);
		return user;
	}

	private AuthUser updateExistingUser(AuthUser existingUser, OAuth2UserInfo oAuth2UserInfo) {
		logger.debug("Updating OidcUser.");
		existingUser.setName(oAuth2UserInfo.getName());
		existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
		//return userRepository.save(existingUser);
		return existingUser;
	}
}
