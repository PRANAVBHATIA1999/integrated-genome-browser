package com.affymetrix.genometry.servlets.das2manager;

public interface Owned {
	public boolean isOwner(Integer idUser);
	public boolean isUserGroup(Integer idUserGroup);

}
