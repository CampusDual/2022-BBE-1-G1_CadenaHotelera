package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.ontimize.jee.common.services.user.UserInformation;

public class UserControl {

	public boolean controlAccess(int id) throws NotAuthorizedException {
		UserInformation user = ((UserInformation) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal());
		List<GrantedAuthority> userRole = (List<GrantedAuthority>) SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities();

		for (GrantedAuthority x : userRole) {
			if (x.getAuthority().compareTo("admin") != 0) {
				int identifier = (int) user.getOtherData().get("IDENTIFIER");
				if (identifier != id) {
					throw new NotAuthorizedException("NOT_AUTHORIZED");
				}
			}					
		}
		return true;
	}

	public boolean controlAccessClient(int id) throws NotAuthorizedException {
		UserInformation user = ((UserInformation) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal());
		List<GrantedAuthority> userRole = (List<GrantedAuthority>) SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities();
		boolean flag=false;
		for (GrantedAuthority x : userRole) {
			if (x.getAuthority().compareTo("client") == 0) {
				int identifier = (int) user.getOtherData().get("IDENTIFIER");
				if (identifier != id) {
					throw new NotAuthorizedException("NOT_AUTHORIZED");
				}else flag=true;
			}					
		}
		return flag;
	}
	
	
	
}
