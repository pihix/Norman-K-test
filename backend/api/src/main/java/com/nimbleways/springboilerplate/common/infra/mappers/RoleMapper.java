package com.nimbleways.springboilerplate.common.infra.mappers;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;

public class RoleMapper extends AbstractDefaultEnumMapper<Role> {

    public static final RoleMapper INSTANCE = new RoleMapper();

    public RoleMapper() {
        super(Role.class);
    }
}
