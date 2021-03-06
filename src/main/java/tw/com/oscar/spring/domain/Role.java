/**
 * Role.java
 * Title: Oscar Wei Web Project
 * Copyright: Copyright(c)2015, oscarwei168
 *
 * @author Oscar Wei
 * @since 2015/8/7
 * <p>
 * H i s t o r y
 * 2015/8/7 Oscar Wei v1
 * + File created
 */
package tw.com.oscar.spring.domain;

import org.hibernate.annotations.Cascade;
import tw.com.oscar.spring.domain.commons.BaseEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Title: Role.java<br>
 * </p>
 * <strong>Description:</strong> A Role entity <br>
 * This function include: - <br>
 * <p>
 * Copyright: Copyright (c) 2015<br>
 * </p>
 * <p>
 * Company: oscarwei168 Inc.
 * </p>
 *
 * @author Oscar Wei
 * @version v1, 2015/8/7
 * @since 2015/8/7
 */
@Entity
@Table(name = "ROLE", uniqueConstraints = @UniqueConstraint(name = "UK_ROLENAME", columnNames = {"NAME"}))
public class Role extends BaseEntity {

    private String name;
    private String description;

    private Set<Account> accounts;
    private Set<Permission> permissions = new HashSet<>(0);

    public Role() {
    }

    public Role(String name) {
        super();
        this.name = name;
    }

    @Column(name = "NAME", nullable = false, length = 60)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIPTION", nullable = false, length = 100)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToMany
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinTable(name = "ACCOUNT_ROLE", joinColumns = {@JoinColumn(name = "ID_ROLE")}, inverseJoinColumns = {
            @JoinColumn(name = "ID_ACCOUNT")})
    @org.hibernate.annotations.ForeignKey(name = "FK_ROLE_ACCOUNT")
    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @org.hibernate.annotations.ForeignKey(name = "FK_ROLE_PERMISSION")
    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (getName() != null ? !getName().equals(role.getName()) : role.getName() != null) return false;
        return !(getDescription() != null ? !getDescription().equals(role.getDescription()) : role.getDescription() != null);

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        return result;
    }
}
