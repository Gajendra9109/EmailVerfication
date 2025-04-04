package com.EmailVerfication.MailOtp.Dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true) 
public class UserDto {
	private Long id;
	private String fullName;
	private String email;
	private String password;
	private boolean enabled;
	private Date createdAt;
	private Date updatedAt;
	private String username;
	private String authorities;
	private boolean accountNonLocked;
	private boolean accountNonExpired;
	private boolean credentialsNonExpired;

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAuthorities() {
		return authorities;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	@Override
	public String toString() {
		return "UserDto [id=" + id + ", fullName=" + fullName + ", email=" + email + ", password=" + password
				+ ", enabled=" + enabled + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", username="
				+ username + ", authorities=" + authorities + ", accountNonLocked=" + accountNonLocked
				+ ", accountNonExpired=" + accountNonExpired + ", credentialsNonExpired=" + credentialsNonExpired
				+ ", getId()=" + getId() + ", getFullName()=" + getFullName() + ", getEmail()=" + getEmail()
				+ ", getPassword()=" + getPassword() + ", isEnabled()=" + isEnabled() + ", getCreatedAt()="
				+ getCreatedAt() + ", getUpdatedAt()=" + getUpdatedAt() + ", getUsername()=" + getUsername()
				+ ", getAuthorities()=" + getAuthorities() + ", isAccountNonLocked()=" + isAccountNonLocked()
				+ ", isAccountNonExpired()=" + isAccountNonExpired() + ", isCredentialsNonExpired()="
				+ isCredentialsNonExpired() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

	public UserDto(Long id, String fullName, String email, String password, boolean enabled, Date createdAt,
			Date updatedAt, String username, String authorities, boolean accountNonLocked, boolean accountNonExpired,
			boolean credentialsNonExpired) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.password = password;
		this.enabled = enabled;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.username = username;
		this.authorities = authorities;
		this.accountNonLocked = accountNonLocked;
		this.accountNonExpired = accountNonExpired;
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public UserDto() {
	}

}