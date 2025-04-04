package com.EmailVerfication.MailOtp.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users_app")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

   
    @Column(name = "user_name", nullable = false) 
    private String userName;

    
    @Column(unique = true, nullable = true)
    private String githubId;

    @Column(nullable = false) 
    private String email;

  
    @Column(nullable = true) 
    private String password;

    @Column(nullable = false)
    private boolean enabled = false;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    
    public User() {
    }

  
    public User(String githubId, String email, String fullName, String userName) {
        this.githubId = githubId;
        this.email = email;
        this.fullName = fullName;
        this.userName = userName;
        this.enabled = true; 
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
 

    public String getUserName() {
		return userName;
	}

    @Override
    public String getUsername() {
        return email; // Typically, email is used as username
    }

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
	public String getPassword() {
        return password;
    }

    public void setPassword(String password1) {
        this.password = password1;
    }

    @Override
	public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Return empty authorities if roles are not used
    }

 

    @Override
    public boolean isAccountNonExpired() {
        return true; // Accounts do not expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Accounts are not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials do not expire
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", fullName='" + fullName + '\'' +
               ", userName='" + userName + '\'' +
               ", githubId='" + githubId + '\'' +
               ", email='" + email + '\'' +
               ", enabled=" + enabled +
               '}';
    }
}
