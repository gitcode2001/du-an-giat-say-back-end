package com.example.duangiatsay.service.implement;

import com.example.duangiatsay.model.Role;
import com.example.duangiatsay.repository.RoleRepository;
import com.example.duangiatsay.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
@Service
public class RoleService implements IRoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }
}
