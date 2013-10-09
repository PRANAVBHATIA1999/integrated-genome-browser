package com.affymetrix.genometry.genopub;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Session;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometry.AnnotSecurity;
import java.util.logging.Level;


public class GenoPubSecurity implements AnnotSecurity, Serializable {
	
	public static final String    SESSION_KEY = "GenoPubSecurity";
	public static final String    ADMIN_ROLE  = "admin";
	public static final String    USER_ROLE   = "user";
	public static final String    GUEST_ROLE  = "guest";

	public static final String    USER_SCOPE_LEVEL  = "USER";
	public static final String    GROUP_SCOPE_LEVEL = "GROUP";
	public static final String    ALL_SCOPE_LEVEL   = "ALL";
	
	private  boolean                scrutinizeAccess = false;
	
	
	private User                    user;
	private boolean                isAdminRole = false;
	private boolean                isGuestRole = true;
	
	private boolean                isFDTSupported = false;
	
	
	private final HashMap<Integer, UserGroup>   groupsMemCollabVisibility = new HashMap<Integer, UserGroup>();
	private final HashMap<Integer, UserGroup>   groupsMemVisibility = new HashMap<Integer, UserGroup>();
  private final HashMap<Integer, Institute>   institutesVisibility = new HashMap<Integer, Institute>();
	
	
	private final HashMap<String, HashMap<Integer, QualifiedAnnotation>> versionToAuthorizedAnnotationMap = new HashMap<String, HashMap<Integer, QualifiedAnnotation>>();
	private  Map<String, GenomeVersion> versionNameToVersionMap = new HashMap<String, GenomeVersion>();
	
	private String                  baseURL;
	
	
	public void setBaseURL(String fullURL, String servletPath, String pathInfo) {
		baseURL = "";
		
        String extraPath = servletPath + pathInfo;
        int pos = fullURL.lastIndexOf(extraPath);
        if (pos > 0) {
        	baseURL = fullURL.substring(0, pos);
        }
	}
	
	public String getBaseURL() {
		return baseURL;
	}
	
	@SuppressWarnings("unchecked")
	public GenoPubSecurity(Session sess, String userName, boolean scrutinizeAccess, boolean isAdminRole, boolean isGuestRole, boolean isFDTSupported) throws Exception {
    
    this.isFDTSupported = isFDTSupported;

    // Are the annotations loaded from the db?  If so, security
		// logic is driven from info in db, otherwise, access to all resources
		// is granted.
		this.scrutinizeAccess = scrutinizeAccess;
		
		if (this.scrutinizeAccess) {
			// Lookup user
			List<User> users = (List<User>)sess.createQuery("SELECT u from User as u where u.userName = '" + userName + "'").list();
			if (users == null || users.isEmpty()) {
				throw new Exception("Cannot find user " + userName);
			}
			user = users.get(0);	
			this.isAdminRole = isAdminRole;
			this.isGuestRole = isGuestRole;
			
			// Hash groups that user is member of
			for (UserGroup sc : (Set<UserGroup>)user.getMemberUserGroups()) {
				groupsMemCollabVisibility.put(sc.getIdUserGroup(), sc);
				groupsMemVisibility.put(sc.getIdUserGroup(), sc);
				// Hash institutes of group that user is member of
				for(Institute i : (Set<Institute>)sc.getInstitutes()) {
				  institutesVisibility.put(i.getIdInstitute(), i);
				}
			}
			// Hash groups that user is manager of
			for (UserGroup sc : (Set<UserGroup>)user.getManagingUserGroups()) {
				groupsMemCollabVisibility.put(sc.getIdUserGroup(), sc);
				groupsMemVisibility.put(sc.getIdUserGroup(), sc);
				// Hash institutes of group that user is manager of
        for(Institute i : (Set<Institute>)sc.getInstitutes()) {
          institutesVisibility.put(i.getIdInstitute(), i);
        }
			}
			// Hash groups that user is collaborator of
			for (UserGroup sc : (Set<UserGroup>)user.getCollaboratingUserGroups()) {
				groupsMemCollabVisibility.put(sc.getIdUserGroup(), sc);
			}
			this.loadAuthorizedResources(sess);	
		}
		
		
		
	}
	
	public Document getXML() {
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("GenoPubSecurity");
		root.addAttribute("userName",        user != null ? user.getUserName() : "");
		root.addAttribute("userDisplayName", user != null ? user.getUserDisplayName() : "");
		root.addAttribute("name",            user != null ? user.getName() : "");
		root.addAttribute("isAdmin",         isAdminRole ? "Y" : "N");
		root.addAttribute("isGuest",         isGuestRole ? "Y" : "N");
    root.addAttribute("isFDTSupported",  isFDTSupported ? "Y" : "N");
		root.addAttribute("canManageUsers",  isAdminRole || (user != null && user.getManagingUserGroups().size() > 0) ? "Y" : "N");
		
		
		return doc;		
	}
	
	public boolean belongsToGroup(Integer idUserGroup) {
		return isMember(idUserGroup) || isCollaborator(idUserGroup) || isManager(idUserGroup);
	}
	
	public boolean belongsToGroup(UserGroup group) {
		return isMember(group) || isCollaborator(group) || isManager(group);
	}
	
	public boolean isMember(UserGroup group) {
		return isMember(group.getIdUserGroup());
	}
	
	public boolean belongsToInstitute(Integer idInstitute) {
    boolean isMyInstitute = false;
  
    if (idInstitute != null) {
      isMyInstitute = this.institutesVisibility.containsKey(idInstitute);
    }

    return isMyInstitute;
  }
	
	@SuppressWarnings("unchecked")
	public boolean isMember(Integer idUserGroup) {
		if (!scrutinizeAccess) {
			return false;
		}
		
		boolean isMember = false;
		for(UserGroup g : (Set<UserGroup>)user.getMemberUserGroups()) {
			if (g.getIdUserGroup().equals(idUserGroup)) {
				isMember = true;
				break;
			}
		}
		return isMember;
	}
	
	public boolean isCollaborator(UserGroup group) {	
		return isCollaborator(group.getIdUserGroup());		
	}
	
	@SuppressWarnings("unchecked")
	public boolean isCollaborator(Integer idUserGroup) {
		if (!scrutinizeAccess) {
			return false;
		}
		boolean isCollaborator = false;
		for(UserGroup g : (Set<UserGroup>)user.getCollaboratingUserGroups()) {
			if (g.getIdUserGroup().equals(idUserGroup)) {
				isCollaborator = true;
				break;
			}
		}
		return isCollaborator;
	}
	
	public boolean isManager(UserGroup group) {
		return isManager(group.getIdUserGroup());
	}
	
	@SuppressWarnings("unchecked")
	public boolean isManager(Integer idUserGroup) {
		if (!scrutinizeAccess) {
			return false;
		}
		
		boolean isManager = false;
		if (this.isAdminRole) {
			isManager = true;
		} else {
			for(UserGroup g : (Set<UserGroup>)user.getManagingUserGroups()) {
				if (g.getIdUserGroup().equals(idUserGroup)) {
					isManager = true;
					break;
				}
			}			
		}
		return isManager;
	}
	
	@SuppressWarnings("unchecked")
	public UserGroup getDefaultUserGroup() {
		if (!scrutinizeAccess) {
			return null;
		}
		UserGroup defaultUserGroup = null;
		if (user.getManagingUserGroups() != null && user.getManagingUserGroups().size() > 0) {
			defaultUserGroup = UserGroup.class.cast(user.getManagingUserGroups().iterator().next());			
		} else if (user.getMemberUserGroups() != null && user.getMemberUserGroups().size() > 0) {
			defaultUserGroup = UserGroup.class.cast(user.getMemberUserGroups().iterator().next());			
		} else if (user.getCollaboratingUserGroups() != null && user.getCollaboratingUserGroups().size() > 0) {
			defaultUserGroup = UserGroup.class.cast(user.getCollaboratingUserGroups().iterator().next());			
		} 
		return defaultUserGroup;
	}
	
	@SuppressWarnings("unchecked")
	public boolean canRead(Object object) {
		if (!scrutinizeAccess) {
			return true;
		}
		
		boolean canRead = false;
		if (isAdminRole) {
			// Admins can read any annotation
			canRead = true;
		} else if (object instanceof Annotation) {
			Annotation a = Annotation.class.cast(object);
			
      // First, check to see if the user is a specified as a collaborator
      // on this request.
      for (Iterator i = a.getCollaborators().iterator(); i.hasNext();) {
        User collaborator = (User)i.next();
        if (user.getIdUser().equals(collaborator.getIdUser())) {
          canRead = true;
          break;
        }
      }     
      
      if (!canRead) {
        if (a.getCodeVisibility().equals(Visibility.PUBLIC)) {
          // Public annotations can be read by anyone
          canRead = true;
          
        } else if (a.getCodeVisibility().equals(Visibility.MEMBERS)) {
          // Annotations with Members visibility can be read by members
          // or managers of the annotation's security group.        
          if (this.isMember(a.getIdUserGroup()) || this.isManager(a.getIdUserGroup())) {
            canRead = true;
          }
        } else if (a.getCodeVisibility().equals(Visibility.MEMBERS_AND_COLLABORATORS)) {
          // Annotations with Members & Collaborators visibility can be read by 
          // members, collaborators, or managers of the annotation's security group.        
          if (this.belongsToGroup(a.getIdUserGroup())) {
            canRead = true;
          }
        } else if (a.getCodeVisibility().equals(Visibility.INSTITUTE)) {
          // Annotations with Institution visibility can be read by members
          // or managers of the annotation's institution.        
          if (this.belongsToInstitute(a.getIdInstitute())) {
            canRead = true;
          }
        } else if (a.getIdUser() != null && a.getIdUser().equals(user.getIdUser())) {
          // Regardless of visibility, owner can read annotation
          canRead = true;
        } else if (this.isManager(a.getIdUserGroup())) {
          // Regardless of visibility, group manager can read annotation
          canRead = true;
        }
        
      }
		} else if (object instanceof AnnotationGrouping) {
			AnnotationGrouping ag = AnnotationGrouping.class.cast(object);
			
			
			// First see if the user is collaborator on 
			// any annotations of the annotation grouping
			for(Object a : ag.getAnnotationGroupings()) {
			  for (Iterator i = ((Annotation)a).getCollaborators().iterator(); i.hasNext();) {
	        User collaborator = (User)i.next();
	        if (user.getIdUser().equals(collaborator.getIdUser())) {
	          canRead = true;
	          break;
	        }
	      }  
			}
			
			if (!canRead) {
        if (ag.hasVisibility(Visibility.PUBLIC)) {
          // Annotation groups for public annotations can be read by anyone
          canRead = true;
          
        } else if (ag.hasVisibility(Visibility.MEMBERS)) {
          // Annotation groups for Annotations with Members visibility can be 
          // read by members or managers of the annotation's security group.  
          for(Object a : ag.getAnnotationGroupings()) {
            if (this.isMember(((Annotation)a).getIdUserGroup()) || this.isManager(((Annotation)a).getIdUserGroup())) {
              canRead = true;
              break;
            }         
          }
        } else if (ag.hasVisibility(Visibility.MEMBERS_AND_COLLABORATORS)) {
          // Annotation groups for Annotations with Members & Collaborators 
          // visibility can be read by members, collaborators, or managers 
          // of the annotation's security group.        
          for(Object a : ag.getAnnotationGroupings()) {
            if (this.belongsToGroup(((Annotation)a).getIdUserGroup())) {
              canRead = true;
              break;
            }
          }
        }  else if (ag.hasVisibility(Visibility.INSTITUTE)) {
          // Annotation groups for Annotations with Institution
          // visibility can be read by users belonging to
          // the annotation's institute        
          for(Object a : ag.getAnnotationGroupings()) {
            if (this.belongsToInstitute(((Annotation)a).getIdInstitute())) {
              canRead = true;
              break;
            }
          }
        } else if (this.belongsToGroup(ag.getIdUserGroup())) {
          // If user is part of group that owns annotation grouping, user
          // can read it.
          canRead = true;
        }
        
      }
			
		} else {
			canRead = true;
		}
		return canRead;
	}
	
	@SuppressWarnings("unchecked")
	public boolean canWrite(Object object) {
		if (!scrutinizeAccess) {
			return false;
		}
		
		boolean canWrite = false;
		
		// Admins can write any object
		if (this.isAdminRole) {
			canWrite = true;
		} else if (object instanceof Owned) {
			// If object is owned by the user, he can write it 
			Owned o = Owned.class.cast(object);
			if (o.isOwner(user.getIdUser())) {
				canWrite = true;
			}
			
			if (!canWrite) {
				if (object instanceof Annotation) {
					Annotation a = Annotation.class.cast(object);
					if (this.isManager(a.getIdUserGroup())) {
						canWrite = true;
					} 
				}
			}
			
		} else if (object instanceof AnnotationGrouping) {
			// User that is a member or manager of the group that "owns" the 
			// annotation grouping can write it.
			AnnotationGrouping ag = AnnotationGrouping.class.cast(object);
			if (this.isMember(ag.getIdUserGroup()) || this.isManager(ag.getIdUserGroup())) {
				canWrite = true;
			}
			
		} else if (object instanceof UserGroup) {
			UserGroup g = (UserGroup)object;
			if (this.isManager(g)) {
				canWrite = true;
			}
		} 
		
		return canWrite;
	}
	

	public boolean appendAnnotationHQLSecurity(String scopeLevel,
			                                     StringBuffer queryBuf, 
			                                     String annotationAlias, 
			                                     String annotationGroupingAlias, 
			                                     String collaboratorAlias,
			                                     boolean addWhere)
	  throws Exception {
		if (!scrutinizeAccess) {
			return addWhere;
		}
		
		if (isAdminRole) {
			
			// Admins don't have any restrictions
			return addWhere;
			
		} else if (isGuestRole) {
			
			// Only get public annotations for guests
			addWhere = AND(addWhere, queryBuf);
			queryBuf.append("(");
			queryBuf.append(annotationAlias).append(".codeVisibility = '" + Visibility.PUBLIC + "'");	
			queryBuf.append(")");
			
		} else if (scopeLevel.equals(GenoPubSecurity.USER_SCOPE_LEVEL)) {
			// Scope to annotations owned by this user
			addWhere = AND(addWhere, queryBuf);
			
			queryBuf.append("(");
			
			appendUserOwnedHQLSecurity(queryBuf, annotationAlias, annotationGroupingAlias, addWhere);
			
			// Also pick up empty folders belonging to group
			addWhere = OR(addWhere, queryBuf);
			appendEmptyAnnotationGroupingHQLSecurity(queryBuf, annotationAlias, annotationGroupingAlias, addWhere);
			
			queryBuf.append(")");
			
		} else if (scopeLevel.equals(GenoPubSecurity.GROUP_SCOPE_LEVEL) || scopeLevel.equals(GenoPubSecurity.ALL_SCOPE_LEVEL)) {
			addWhere = AND(addWhere, queryBuf);

			// If this user isn't part of any group or we aren't searching for public
			// annotations, add a security statement that will ensure no rows are returned.
			if (groupsMemCollabVisibility.isEmpty() && !scopeLevel.equals(GenoPubSecurity.ALL_SCOPE_LEVEL)) {
				appendUserOwnedHQLSecurity(queryBuf, annotationAlias, annotationGroupingAlias, addWhere);
			} else {
				boolean hasSecurityCriteria = false;
				queryBuf.append("(");
				
				if (!this.groupsMemVisibility.isEmpty()) {
					
					// For annotations with MEMBER visibility, limit to security group
					// in which user is member (or manager).  (Also, include
					// any public annotations belong one of the user's groups.)					
					queryBuf.append("(");
					queryBuf.append(annotationAlias).append(".codeVisibility in ('" + Visibility.MEMBERS + "', '" + Visibility.PUBLIC + "')");
					addWhere = AND(addWhere, queryBuf);
					queryBuf.append(annotationAlias).append(".idUserGroup ");
					appendIdInStatement(queryBuf, this.groupsMemVisibility);
					queryBuf.append(")");
					
					hasSecurityCriteria = true;
					
				}

				
				// For annotations with MEMBER & COLLABORATOR visibility, limit to security group
				// in which user is collaborator, member, (or manager)
				if (!this.groupsMemCollabVisibility.isEmpty()) {
					
				  if (hasSecurityCriteria) {
	          addWhere = OR(addWhere, queryBuf);				    
				  }
					
					queryBuf.append("(");
					queryBuf.append(annotationAlias).append(".codeVisibility = '" + Visibility.MEMBERS_AND_COLLABORATORS + "'");
					addWhere = AND(addWhere, queryBuf);
					queryBuf.append(annotationAlias).append(".idUserGroup ");
					appendIdInStatement(queryBuf, this.groupsMemCollabVisibility);
					queryBuf.append(")");

					hasSecurityCriteria = true;
				}
				
				if (!this.institutesVisibility.isEmpty()) {
				  
				  if (hasSecurityCriteria) {
	          addWhere = OR(addWhere, queryBuf);
				  }
          
          // For annotations with INSTITUTION visibility, limit to the 
				  // annotations with an institute that the user's group belongs to.  
          queryBuf.append("(");
          			queryBuf.append(annotationAlias).append(".codeVisibility in ('" + Visibility.INSTITUTE + "')");
          addWhere = AND(addWhere, queryBuf);
          			queryBuf.append(annotationAlias).append(".idInstitute ");
          appendIdInStatement(queryBuf, this.institutesVisibility);
          queryBuf.append(")");
          
          hasSecurityCriteria = true;
        }
				

				// Also pick up empty folders belonging to group
				if (annotationGroupingAlias != null && !this.groupsMemCollabVisibility.isEmpty()) {
				  if (hasSecurityCriteria) {
	          addWhere = OR(addWhere, queryBuf);				    
				  }
					appendEmptyAnnotationGroupingHQLSecurity(queryBuf, annotationAlias, annotationGroupingAlias, addWhere);
					hasSecurityCriteria = true;
				}
				

				// Get all user owned annotations
				if (hasSecurityCriteria) {
					addWhere = OR(addWhere, queryBuf);
				}
				appendUserOwnedHQLSecurity(queryBuf, annotationAlias, annotationGroupingAlias, addWhere);
				hasSecurityCriteria = true;
				
				
				// Get all annotations where user is a collaborator
				if (hasSecurityCriteria) {
          addWhere = OR(addWhere, queryBuf);
        }
		    queryBuf.append("(");
		    	queryBuf.append(collaboratorAlias).append(".idUser = ").append(user.getIdUser());
		    queryBuf.append(")");   
        hasSecurityCriteria = true;
				
				// Include all public annotations if scope = ALL	
				if (scopeLevel.equals(GenoPubSecurity.ALL_SCOPE_LEVEL)) {

					if (hasSecurityCriteria) {
						addWhere = OR(addWhere, queryBuf);						
					}
					
					queryBuf.append("(");
					queryBuf.append(annotationAlias).append(".codeVisibility = '" + Visibility.PUBLIC + "'");	
					queryBuf.append(")");					
				}
				queryBuf.append(")");					
				
			} 
		} else {
			throw new Exception("invalid scope level " + scopeLevel);
		}
		return addWhere;
	}




	@SuppressWarnings("unchecked")
	private void appendUserOwnedHQLSecurity(StringBuffer queryBuf, 
            								 String annotationAlias,
            								 String annotationGroupingAlias,
            								 boolean addWhere) throws Exception {
		queryBuf.append("(");
		
												 queryBuf.append(annotationAlias).append(".idUser = ").append(user.getIdUser());

		if (user.getManagingUserGroups().size() > 0) {
		  addWhere = OR(addWhere, queryBuf);  
		  
	    	queryBuf.append(annotationAlias).append(".idUserGroup in (");
	    boolean firstTime = true;
	    for (UserGroup group : (Set<UserGroup>)user.getManagingUserGroups()) {
	      if (!firstTime) {
	        queryBuf.append(",");
	      }
	      queryBuf.append(group.getIdUserGroup());
	      firstTime = false;
	    }
	    queryBuf.append(")");  
		}
    
		queryBuf.append(")");		
	}

 
	

	@SuppressWarnings("unchecked")
	private boolean appendEmptyAnnotationGroupingHQLSecurity(StringBuffer queryBuf, 
            								                String annotationAlias,
            								                String annotationGroupingAlias,
            								                boolean addWhere) throws Exception {
		queryBuf.append("(");

																queryBuf.append(annotationAlias).append(".idAnnotation is NULL");
		
		addWhere = AND(addWhere, queryBuf);

		queryBuf.append("(");					
																queryBuf.append(annotationGroupingAlias).append(".idUserGroup is NULL");
		if (this.groupsMemCollabVisibility.size() > 0) {
			addWhere = OR(addWhere, queryBuf);
			queryBuf.append(annotationGroupingAlias).append(".idUserGroup");
			appendIdInStatement(queryBuf, this.groupsMemCollabVisibility);			
		}
		queryBuf.append(")");

		queryBuf.append(")");
		return addWhere;
	}

	
	@SuppressWarnings("unchecked")
	private void appendIdInStatement(StringBuffer queryBuf, HashMap idMap) {
		queryBuf.append(" in (");
		for(Iterator i = idMap.keySet().iterator(); i.hasNext();) { 
			Integer id = (Integer)i.next();
			queryBuf.append(id);				
			if (i.hasNext()) {
				queryBuf.append(",");
			}
		}
		queryBuf.append(")");
	}
	
	protected boolean AND(boolean addWhere, StringBuffer queryBuf) {
		if (addWhere) {
			queryBuf.append(" WHERE ");
			addWhere = false;
		} else {
			queryBuf.append(" AND ");
		}
		return addWhere;
	}
	
	protected boolean OR(boolean addWhere, StringBuffer queryBuf) {
		if (addWhere) {
			queryBuf.append(" WHERE ");
			addWhere = false;
		} else {
			queryBuf.append(" OR ");
		}
		return addWhere;
	}

	public boolean isAdminRole() {
    	return isAdminRole;
    }

	public void setAdminRole(boolean isAdminRole) {
    	this.isAdminRole = isAdminRole;
    }	
	
	public boolean isGuestRole() {
		return isGuestRole;
	}
	
	public String getUserName() {
		if (user != null ) {
			return user.getUserName();
		} else {
			return "";
		}
	}
	
	public Integer getIdUser() {
		if (user != null ) {
			return user.getIdUser();
		} else {
			return null;
		}
	}
	

	
	public void loadAuthorizedResources(Session sess) throws Exception {
		if (!scrutinizeAccess) {
			return;
		}
		
		// Start over if we have already loaded the resources
		if (!versionToAuthorizedAnnotationMap.isEmpty()) {
			versionToAuthorizedAnnotationMap.clear();
		}
		
		// Cache the authorized annotation ids of each genome version for this user
		AnnotationQuery annotationQuery = new AnnotationQuery();
		annotationQuery.runAnnotationQuery(sess, this, false);
		
		// Cache the genome versions
		this.versionNameToVersionMap = annotationQuery.getGenomeVersionNameMap();
		
		// Cache the authorized annotations
		for (Organism organism : annotationQuery.getOrganisms()) {
			for (String genomeVersionName : annotationQuery.getVersionNames(organism)) {

				HashMap<Integer, QualifiedAnnotation> annotationMap = this.versionToAuthorizedAnnotationMap.get(genomeVersionName);
				if (annotationMap == null) {
					annotationMap = new HashMap<Integer, QualifiedAnnotation>();
					this.versionToAuthorizedAnnotationMap.put(genomeVersionName, annotationMap);
				}
				for (QualifiedAnnotation qa : annotationQuery.getQualifiedAnnotations(organism, genomeVersionName)) {
					annotationMap.put(qa.getAnnotation().getIdAnnotation(), qa);
				}
			}
		}

	}
	
	
	
	//
	//
	// AnnotSecurity methods
	//
	//

	public boolean isAuthorized(String genomeVersionName, String annotationName, Object annotationId) {
		// When annotation is loaded directly from file system, all annotations 
		// are shown.
		if (!scrutinizeAccess) { 
			return true;
		}

		// If the annotation id is not provided, block access
		if (annotationId == null) {
			Logger.getLogger(GenoPubSecurity.class.getName()).log(Level.WARNING, "Unable to find annotation id for {0}.  Blocking access.", annotationName);
		}

		// Get the hash map of annotation ids this user is authorized to view
		Map annotationMap = (Map)versionToAuthorizedAnnotationMap.get(genomeVersionName);

		// Returns true if annotation id is in hash map; otherwise, returns false
		return annotationMap.containsKey(annotationId);

	}
	
	public Map<String, Object> getProperties(String genomeVersionName, String annotationName, Object annotationId) {
		// When annotation is loaded directly from file system, annotations
		// don't have any additional properties
		if (!scrutinizeAccess) { 
			return null;
		}

		// If the annotation access is blocked, don't show
		// properties.
		if (!isAuthorized(genomeVersionName, annotationName, annotationId)) {
			return null;
		}

		// Get the hash map of annotation ids this user is authorized to view
		Map<Integer, QualifiedAnnotation> annotationMap = versionToAuthorizedAnnotationMap.get(genomeVersionName);
		QualifiedAnnotation qa = annotationMap.get(annotationId);
		Map<String, Object> props =  qa.getAnnotation().getProperties();
		
		// Add the 'info' URL as a property.  
		if (!props.containsKey(Annotation.PROP_URL)) {
			props.put(Annotation.PROP_URL, this.baseURL + "/" + GenoPubServlet.GENOPUB_WEBAPP_NAME + "?idAnnotation=" + annotationId);
		}
		return props;
		
	}
	
	
	
	public boolean isBarGraphData(String data_root, String genomeVersionName, String annotationName, Object annotationId) {
		// When annotation is loaded directly from file system, just return true
		if (!scrutinizeAccess) { 
			return true;
		}

		// If the annotation access is blocked, return false
		if (!isAuthorized(genomeVersionName, annotationName, annotationId)) {
			return false;
		}

		// Get the hash map of annotation ids this user is authorized to view
		Map<Integer, QualifiedAnnotation> annotationMap = versionToAuthorizedAnnotationMap.get(genomeVersionName);
		QualifiedAnnotation qa = annotationMap.get(annotationId);
		try {
			return qa.getAnnotation().isBarGraphData(data_root);
		} catch (Exception e) {
			return false;
		}
		
		
	}
	
	public boolean isUseqGraphData(String data_root, String genomeVersionName, String annotationName, Object annotationId) {
		// When annotation is loaded directly from file system, just return true
		if (!scrutinizeAccess) { 
			return true;
		}

		// If the annotation access is blocked, return false
		if (!isAuthorized(genomeVersionName, annotationName, annotationId)) {
			return false;
		}

		// Get the hash map of annotation ids this user is authorized to view
		Map<Integer, QualifiedAnnotation> annotationMap = versionToAuthorizedAnnotationMap.get(genomeVersionName);
		QualifiedAnnotation qa = annotationMap.get(annotationId);
		try {
			return qa.getAnnotation().isUseqGraphData(data_root);
		} catch (Exception e) {
			return false;
		}
		
		
	}
	
	public boolean isBamData(String data_root, String genomeVersionName, String annotationName, Object annotationId) {
		// When annotation is loaded directly from file system, just return true
		if (!scrutinizeAccess) { 
			return true;
		}

		// If the annotation access is blocked, return false
		if (!isAuthorized(genomeVersionName, annotationName, annotationId)) {
			return false;
		}

		// Get the hash map of annotation ids this user is authorized to view
		Map<Integer, QualifiedAnnotation> annotationMap = versionToAuthorizedAnnotationMap.get(genomeVersionName);
		QualifiedAnnotation qa = annotationMap.get(annotationId);
		try {
			return qa.getAnnotation().isBamData(data_root);
		} catch (Exception e) {
			return false;
		}
		
		
	}
	

	
	public String getSequenceDirectory(String data_root, AnnotatedSeqGroup genome) throws Exception {
		if (scrutinizeAccess) {
			GenomeVersion genomeVersion = versionNameToVersionMap.get(genome.getID());
			if (genomeVersion == null) {
				throw new Exception("Cannot find genome version " + genome.getID() + " in genome version map");
			}
			return data_root + Constants.SEQUENCE_DIR_PREFIX + genomeVersion.getIdGenomeVersion().toString() + "/";
		} else {
			return data_root + genome.getOrganism() + "/" + genome.getID() + "/dna/";
		}
	}

	


}
 