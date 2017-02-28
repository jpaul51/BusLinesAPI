package app.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name="google_user")
public class CustomUser     {

	@Id
	String userID;
	
	boolean likeCats;

	
	public CustomUser()
	{
		
	}
	
	public CustomUser(String userId)
	{
		this.userID=userId;
	}
	
	public CustomUser(String userId, boolean likeCats)
	{
		this.userID=userId;
		this.likeCats = likeCats;
	}


	public String getUserID() {
		return userID;
	}


	public void setUserID(String userID) {
		this.userID = userID;
	}

	
	public boolean isLikeCats() {
		return likeCats;
	}

	public void setLikeCats(boolean likeCats) {
		this.likeCats = likeCats;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (likeCats ? 1231 : 1237);
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomUser other = (CustomUser) obj;
		if (likeCats != other.likeCats)
			return false;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CustomUser [userID=" + userID + ", likeCats=" + likeCats + "]";
	}











	
	
	
	
	
	
}
