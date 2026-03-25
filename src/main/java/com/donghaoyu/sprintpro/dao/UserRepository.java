package com.donghaoyu.sprintpro.dao;

import com.donghaoyu.sprintpro.entity.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<TbUser, Integer> {

}