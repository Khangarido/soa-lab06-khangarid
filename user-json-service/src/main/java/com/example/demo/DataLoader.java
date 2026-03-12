package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.entity.UserProfile;
import com.example.demo.repository.UserProfileRepository;

// Ankhny medeelel oruulah (Seed data on startup)
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserProfileRepository profileRepository;

    public DataLoader(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public void run(String... args) {
        if (profileRepository.count() == 0) {
            UserProfile khangarid = new UserProfile(
                    "22B1NUM4730",
                    "Khangarid",
                    "khangarid@num.edu.mn",
                    "Software Engineering student at NUM. SOA Lab 06.",
                    "+976-9911-2233"
            );
            profileRepository.save(khangarid);
            log.info("Dummy profile uusgelee: Khangarid (22B1NUM4730)");
        }
    }
}
