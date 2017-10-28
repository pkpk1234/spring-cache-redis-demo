package com.example.springcachesample.controller;

import com.example.springcachesample.model.Person;
import com.example.springcachesample.service.VerySlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Created by pkpk1234 on 2017/5/20.
 */
@RestController
@Slf4j
public class PersonController {
    @Autowired
    private VerySlowService verySlowService;

    @GetMapping("/findall")
    public ResponseEntity<Collection<Person>> findAll() {
        return ResponseEntity.ok(this.verySlowService.getAllPerson());
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<Person> findById(@PathVariable("id") String id) {
        Long personId = Long.parseLong(id);
        return ResponseEntity.ok(this.verySlowService.getPersonById(personId));
    }

    @PutMapping("/person")
    public ResponseEntity<String> updatePerson(@RequestBody Person newPerson) {
        if (newPerson.getPersonId() != null) {
            log.info("update person");
            this.verySlowService.updatePerson(newPerson.getPersonId(), newPerson);
            return ResponseEntity.ok("success");
        } else {
            log.info("can't update person");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can't not update this Person.");
        }
    }

    @PostMapping("/load")
    public ResponseEntity<String> load() {
        this.verySlowService.loadAllPersons();
        return ResponseEntity.ok("load ok");
    }
}
