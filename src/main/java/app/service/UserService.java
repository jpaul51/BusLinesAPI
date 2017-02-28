package app.service;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.dao.UserRepository;
import app.model.CustomUser;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	public UserService( )
	{
				
	}
	
	
	
	public CustomUser findCustomUser(String id)
	{
		
		return userRepository.findOne(id);
		
	}
	
	public boolean deleteUser(String userId)
	{
		if(userExists(userId))
		{
			userRepository.delete(userId);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean userExists(String userId)
	{
		return userRepository.findOne(userId)!=null;
	}
	
	public void registerUser(String id, boolean likeCats)
	{
		CustomUser customUser = new CustomUser(id,likeCats);
		userRepository.save(customUser);
	}
	
	
	
}
