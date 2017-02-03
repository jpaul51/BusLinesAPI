package app.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name="google_user")
public class User implements UserDetails {

	@Id
	private String id;
	private boolean likesCats;
	
	
	
	
	
	
	public User() {
		super();
	}




	public User(String id, boolean likesCats) {
		super();
		this.id = id;
		this.likesCats = likesCats;
	}




	public String getId() {
		return id;
	}




	public void setId(String id) {
		this.id = id;
	}




	public boolean isLikesCats() {
		return likesCats;
	}

	public void setLikesCats(boolean likesCats) {
		this.likesCats = likesCats;
	}


	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (likesCats ? 1231 : 1237);
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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (likesCats != other.likesCats)
			return false;
		return true;
	}




	@Override
	public String toString() {
		return "User [id=" + id + ", likesCats=" + likesCats + "]";
	}




	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	
	
	
	
}
