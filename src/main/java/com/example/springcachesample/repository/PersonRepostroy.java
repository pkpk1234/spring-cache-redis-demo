package com.example.springcachesample.repository;

import com.example.springcachesample.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by pkpk1234 on 2017/5/20.
 */
@Repository
public interface PersonRepostroy extends JpaRepository<Person, Long> {
}
