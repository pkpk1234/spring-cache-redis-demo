package com.example.springcachesample.service;

import com.example.springcachesample.model.Person;
import com.example.springcachesample.repository.PersonRepostroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by pkpk1234 on 2017/5/20.
 */
@Service
@Slf4j
public class VerySlowService {
    @Autowired
    private PersonRepostroy personRepostroy;
    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @Cacheable("personCache")
    public Collection<Person> getAllPerson() {
        log.info("Cache not hit,load from db.");
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(threadLocalRandom.nextLong(5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Person> allPerson = this.personRepostroy.findAll();
        long end = System.currentTimeMillis();
        log.info("VerySlowService getAllPerson user {} sec.", (end - start) / 1000.0);
        return allPerson;
    }

    @Cacheable(cacheNames = "personCache", key = "#id")
    public Person getPersonById(Long id) {
        log.info("Cache not hit,load from db.");
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(threadLocalRandom.nextLong(5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        log.info("VerySlowService getAllPerson user {} sec.", (end - start) / 1000.0);
        return this.personRepostroy.findOne(id);
    }

    /**
     * must return new Object,@CachePut replace cache with the new Object
     *
     * @param id
     * @param newPerson
     * @return
     */
    @CachePut(cacheNames = {"personCache"}, key = "#id")
    public Person updatePerson(Long id, Person newPerson) {
        return this.personRepostroy.save(newPerson);
    }

    @CacheEvict(cacheNames = {"personCache"}, allEntries = true)
    public void loadAllPersons() {
        Person p1 = new Person(1L, "new f1", "new l1", 20);
        Person p2 = new Person(2L, "new f2", "new l2", 20);
        Person p3 = new Person(3L, "new f3", "new l3", 20);
        Person p4 = new Person(4L, "new f4", "new l4", 20);
        Person p5 = new Person(5L, "new f5", "new l5", 20);
        this.personRepostroy.save(Arrays.asList(p1, p2, p3, p4, p5));
    }
}
