package com.example.springcachesample;

import com.example.springcachesample.model.Person;
import com.example.springcachesample.repository.PersonRepostroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * @author 李佳明
 * @date 2017-10-28
 *
 * 请依次执行如下命令
 * curl http://localhost:8080/findall    :load from db
 * curl http://localhost:8080/findall    :load from cache
 * curl http://localhost:8080/person/1   :load from db
 * curl http://localhost:8080/person/1   :load from cache
 * curl -X 'PUT'
    -d '{"personId":1,"firstName":"new f1","lastName":"new l1","age":40}'
    -H 'content-type:application/json' http://localhost:8080/person :update data,and update cache
 * curl http://localhost:8080/person/1   :load from cache,with new value
 * curl -d{} http://localhost:8080/load  :update all data,and evict all cache
 * curl http://localhost:8080/findall    :load from db
 * curl http://localhost:8080/findall    :load from cache
 */

@SpringBootApplication
@EnableCaching
public class SpringCacheSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheSampleApplication.class, args);
    }
}

@Component
class InitRunner implements CommandLineRunner {
    @Autowired
    private PersonRepostroy personRepostroy;

    @Transactional
    @Override
    public void run(String... strings) throws Exception {
        Person p1 = new Person("f1", "l1", 20);
        Person p2 = new Person("f2", "l2", 20);
        Person p3 = new Person("f3", "l3", 20);
        Person p4 = new Person("f4", "l4", 20);
        Person p5 = new Person("f5", "l5", 20);
        this.personRepostroy.save(Arrays.asList(p1, p2, p3, p4, p5));
    }
}
